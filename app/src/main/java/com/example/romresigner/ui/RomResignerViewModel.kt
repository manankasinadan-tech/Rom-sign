package com.example.romresigner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.romresigner.ai.GeminiResignerAi
import com.example.romresigner.engine.KeyEngine
import com.example.romresigner.engine.RomResignerPipeline
import com.example.romresigner.engine.SELinuxEngine
import com.example.romresigner.model.BootloopRisk
import com.example.romresigner.model.KeyDetails
import com.example.romresigner.model.LogEntry
import com.example.romresigner.model.LogLevel
import com.example.romresigner.model.PipelineStage
import com.example.romresigner.model.ResigningProgressState
import com.example.romresigner.model.RomPackageType
import com.example.romresigner.model.RomSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RomResignerViewModel(application: Application) : AndroidViewModel(application) {

    private val keyEngine = KeyEngine(application)
    private val seLinuxEngine = SELinuxEngine(keyEngine)
    private val pipeline = RomResignerPipeline(application, keyEngine, seLinuxEngine)
    private val geminiAi = GeminiResignerAi()

    private val _romSummary = MutableStateFlow(RomSummary())
    val romSummary: StateFlow<RomSummary> = _romSummary.asStateFlow()

    private val _progressState = MutableStateFlow(ResigningProgressState())
    val progressState: StateFlow<ResigningProgressState> = _progressState.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _keyBundle = MutableStateFlow<List<KeyDetails>>(emptyList())
    val keyBundle: StateFlow<List<KeyDetails>> = _keyBundle.asStateFlow()

    private val _detectedRisks = MutableStateFlow<List<BootloopRisk>>(emptyList())
    val detectedRisks: StateFlow<List<BootloopRisk>> = _detectedRisks.asStateFlow()

    private val _aiAuditReport = MutableStateFlow<String?>(null)
    val aiAuditReport: StateFlow<String?> = _aiAuditReport.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _crashLogAnalysis = MutableStateFlow<String?>(null)
    val crashLogAnalysis: StateFlow<String?> = _crashLogAnalysis.asStateFlow()

    private var activeJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    init {
        val (ws, out) = pipeline.setupWorkspaceDirectories()
        _romSummary.value = _romSummary.value.copy(
            workspacePath = ws.absolutePath,
            outputPath = out.absolutePath
        )
        loadKeys()
        runLocalAudit(ws)
    }

    fun loadKeys() {
        val keys = keyEngine.getOrCreateKeyBundle()
        _keyBundle.value = keys
    }

    fun regenerateKeys(keySize: Int = 2048, orgName: String = "CustomROM-Engineering") {
        viewModelScope.launch {
            val keys = keyEngine.regenerateAllKeys(keySize, orgName)
            _keyBundle.value = keys
            addLog(LogLevel.SUCCESS, "KEYSTORE", "Trousseau de clés régénéré avec succès (RSA $keySize-bit, Organisme : $orgName)")
        }
    }

    private fun runLocalAudit(wsDir: File) {
        val risks = seLinuxEngine.auditSelinuxPolicies(wsDir)
        _detectedRisks.value = risks
    }

    fun startResigning() {
        if (_progressState.value.isRunning) return

        activeJob = viewModelScope.launch {
            val ws = File(_romSummary.value.workspacePath)
            val out = File(_romSummary.value.outputPath)

            pipeline.executeResigningPipeline(
                romSummary = _romSummary.value,
                workspaceDir = ws,
                outputDir = out,
                onProgress = { state -> _progressState.value = state },
                onLog = { logEntry ->
                    _logs.value = _logs.value + logEntry
                }
            )
        }
    }

    fun cancelResigning() {
        activeJob?.cancel()
        _progressState.value = ResigningProgressState(
            stage = PipelineStage.FAILED,
            currentItem = "Opération interrompue par l'utilisateur",
            isRunning = false
        )
        addLog(LogLevel.WARN, "PIPELINE", "Processus de signature interrompu manuellement.")
    }

    fun requestAiAudit() {
        viewModelScope.launch {
            _isAiThinking.value = true
            addLog(LogLevel.AUDIT, "GEMINI", "Lancement de l'audit approfondi avec Gemini 3.1 Pro (Thinking Mode HIGH)...")
            val report = geminiAi.auditRomPreFlight(_romSummary.value, _detectedRisks.value)
            _aiAuditReport.value = report
            _isAiThinking.value = false
            addLog(LogLevel.SUCCESS, "GEMINI", "Rapport d'audit IA généré avec succès !")
        }
    }

    fun analyzeCrashLogs(logcatText: String) {
        if (logcatText.isBlank()) return
        viewModelScope.launch {
            _isAiThinking.value = true
            addLog(LogLevel.AUDIT, "DIAGNOSTIC", "Analyse du logcat de crash par Gemini...")
            val result = geminiAi.analyzeCrashLogs(logcatText)
            _crashLogAnalysis.value = result
            _isAiThinking.value = false
            addLog(LogLevel.SUCCESS, "DIAGNOSTIC", "Diagnostic du crash complété.")
        }
    }

    fun updateRomTarget(name: String, type: RomPackageType) {
        _romSummary.value = _romSummary.value.copy(
            romName = name,
            packageType = type
        )
        addLog(LogLevel.INFO, "CONFIG", "Cible mise à jour : $name (${type.label})")
    }

    fun updateDirectories(workspace: String, output: String) {
        _romSummary.value = _romSummary.value.copy(
            workspacePath = workspace,
            outputPath = output
        )
        addLog(LogLevel.INFO, "CONFIG", "Répertoires mis à jour. Workspace : $workspace | Sortie : $output")
    }

    fun onRomFileSelected(name: String, sourcePath: String, sizeBytes: Long) {
        val detectedType = when {
            name.endsWith(".img", ignoreCase = true) && (name.contains("gsi", ignoreCase = true) || name.contains("arm64", ignoreCase = true)) -> RomPackageType.GSI_IMAGE
            name.endsWith(".img", ignoreCase = true) -> RomPackageType.SYSTEM_IMG
            name.endsWith(".zip", ignoreCase = true) -> RomPackageType.ZIP_ARCHIVE
            File(sourcePath).isDirectory -> RomPackageType.EXTRACTED_FOLDER
            else -> RomPackageType.ZIP_ARCHIVE
        }

        val formattedSize = formatFileSize(sizeBytes)

        _romSummary.value = _romSummary.value.copy(
            romName = name,
            packageType = detectedType,
            sourcePath = sourcePath,
            fileSizeBytes = sizeBytes,
            formattedFileSize = formattedSize
        )

        addLog(LogLevel.INFO, "PICKER", "Fichier ROM/IMG sélectionné : $name ($formattedSize)")
        addLog(LogLevel.SUCCESS, "ANALYSIS", "Format identifié : ${detectedType.label} | Source : $sourcePath")
        addLog(LogLevel.INFO, "WORKSPACE", "Préparation de l'espace de travail dans ${_romSummary.value.workspacePath}")
    }

    fun onSafUriSelected(uri: android.net.Uri) {
        var name = uri.lastPathSegment ?: "ROM_Target.zip"
        var size: Long = 0

        try {
            val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (nameIndex != -1) name = it.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = it.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            // Fallback si requête restreinte
        }

        onRomFileSelected(name, uri.toString(), size)
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Taille inconnue"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> "%.2f Go".format(Locale.US, gb)
            mb >= 1.0 -> "%.1f Mo".format(Locale.US, mb)
            else -> "%.0f Ko".format(Locale.US, kb)
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun addLog(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = timeFormat.format(Date()),
            level = level,
            tag = tag,
            message = message
        )
        _logs.value = _logs.value + entry
    }
}
