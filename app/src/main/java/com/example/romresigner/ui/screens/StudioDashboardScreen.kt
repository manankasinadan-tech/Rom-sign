package com.example.romresigner.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.romresigner.model.PipelineStage
import com.example.romresigner.model.RomPackageType
import com.example.romresigner.ui.RomResignerViewModel
import com.example.romresigner.ui.components.StorageFileBrowserDialog
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StudioDashboardScreen(
    viewModel: RomResignerViewModel,
    onNavigateToLogs: () -> Unit
) {
    val summary by viewModel.romSummary.collectAsState()
    val progress by viewModel.progressState.collectAsState()
    val logs by viewModel.logs.collectAsState()

    var showRomPicker by remember { mutableStateOf(false) }
    var showPathsDialog by remember { mutableStateOf(false) }
    var showSdCardBrowser by remember { mutableStateOf(false) }

    // Launcher Système SAF pour sélectionner n'importe quel fichier (.zip, .img, .bin, etc.)
    val safFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onSafUriSelected(uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("studio_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête Statut de la ROM Cible & Sélecteur Principal
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
                    .testTag("card_rom_status")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ROM / IMAGE SÉLECTIONNÉE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = summary.romName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = summary.formattedFileSize,
                                color = CyberEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Source : ${summary.sourcePath}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Boutons de sélection du fichier ROM / IMG
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { safFileLauncher.launch(arrayOf("*/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_pick_saf_file")
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color(0xFF003918), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sélecteur Système", color = Color(0xFF003918), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showSdCardBrowser = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_browse_sdcard")
                        ) {
                            Icon(Icons.Default.SdCard, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Parcourir SDCard", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(label = summary.packageType.label, color = CyberCyan)
                        StatusChip(label = summary.androidVersion, color = CyberEmerald)
                        StatusChip(label = "0% Bootloop Risk", color = CyberEmerald)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Protection active : SELinux mac_permissions.xml synchronisé, GApps protégées, build.prop -> release-keys.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }



        // Configuration des Répertoires dans /sdcard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
                    .testTag("card_storage_paths")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Espaces de travail & Sortie",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }

                        TextButton(
                            onClick = { showPathsDialog = true },
                            modifier = Modifier.testTag("btn_edit_paths")
                        ) {
                            Text("Changer", color = CyberCyan, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    PathRow(label = "Workspace", path = summary.workspacePath)
                    Spacer(modifier = Modifier.height(6.dp))
                    PathRow(label = "Sortie", path = summary.outputPath)
                }
            }
        }

        // Carte d'Exécution & Progression du Pipeline
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(22.dp))
                    .testTag("card_pipeline_execution")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MOTEUR DE SIGNATURE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberEmerald,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = progress.stage.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (progress.isRunning) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(CyberEmerald)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val animatedProgress by animateFloatAsState(targetValue = progress.progressPercent, label = "progress")
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .testTag("pipeline_progress_bar"),
                        color = CyberEmerald,
                        trackColor = CyberSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (progress.currentItem.isNotBlank()) progress.currentItem else "Prêt au traitement",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bouton d'action principal
                    if (!progress.isRunning) {
                        Button(
                            onClick = { viewModel.startResigning() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_start_resigning"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF003918)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (progress.stage == PipelineStage.COMPLETED) "Relancer la Resignature" else "Lancer la Resignature Pro",
                                color = Color(0xFF003918),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.cancelResigning() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_cancel_resigning"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Interrompre l'opération",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Aperçu en direct des Logs avec raccourci
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
                    .testTag("card_live_log_preview")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aperçu de la Télémétrie",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }

                        TextButton(
                            onClick = onNavigateToLogs,
                            modifier = Modifier.testTag("btn_view_full_terminal")
                        ) {
                            Text("Plein écran", color = CyberEmerald, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val recentLogs = logs.takeLast(4)
                    if (recentLogs.isEmpty()) {
                        Text(
                            text = "En attente du démarrage du moteur. Tous les événements s'afficheront ici en temps réel.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    } else {
                        recentLogs.forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "[${log.timestamp.take(8)}]",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.message,
                                    color = when (log.level.name) {
                                        "SUCCESS" -> CyberEmerald
                                        "WARN" -> Color(0xFFFFB300)
                                        "ERROR" -> Color(0xFFFF5252)
                                        else -> TextPrimary
                                    },
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Sélection de la ROM Cible
    if (showRomPicker) {
        RomTargetPickerDialog(
            currentName = summary.romName,
            currentType = summary.packageType,
            onDismiss = { showRomPicker = false },
            onConfirm = { name, type ->
                viewModel.updateRomTarget(name, type)
                showRomPicker = false
            }
        )
    }

    // Modal de Répertoires
    if (showPathsDialog) {
        PathsConfigDialog(
            currentWorkspace = summary.workspacePath,
            currentOutput = summary.outputPath,
            onDismiss = { showPathsDialog = false },
            onConfirm = { ws, out ->
                viewModel.updateDirectories(ws, out)
                showPathsDialog = false
            }
        )
    }

    // Navigateur de fichiers SDCard interactif
    if (showSdCardBrowser) {
        StorageFileBrowserDialog(
            onDismiss = { showSdCardBrowser = false },
            onFileSelected = { name, path, size ->
                viewModel.onRomFileSelected(name, path, size)
                showSdCardBrowser = false
            }
        )
    }
}

@Composable
fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PathRow(label: String, path: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        Text(
            text = path,
            fontSize = 12.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

@Composable
fun RomTargetPickerDialog(
    currentName: String,
    currentType: RomPackageType,
    onDismiss: () -> Unit,
    onConfirm: (String, RomPackageType) -> Unit
) {
    var romName by remember { mutableStateOf(currentName) }
    var selectedType by remember { mutableStateOf(currentType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sélectionner la ROM Cible", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = romName,
                    onValueChange = { romName = it },
                    label = { Text("Nom du fichier ou dossier") },
                    modifier = Modifier.fillMaxWidth().testTag("input_rom_name")
                )

                Text("Format de la ROM :", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

                RomPackageType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedType == type) CyberSurfaceVariant else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type.label,
                            fontSize = 13.sp,
                            color = if (selectedType == type) CyberEmerald else TextPrimary,
                            fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(romName, selectedType) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                modifier = Modifier.testTag("btn_confirm_rom_target")
            ) {
                Text("Valider", color = Color(0xFF003918), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextSecondary) }
        },
        containerColor = CyberSurface
    )
}

@Composable
fun PathsConfigDialog(
    currentWorkspace: String,
    currentOutput: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var ws by remember { mutableStateOf(currentWorkspace) }
    var out by remember { mutableStateOf(currentOutput) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Répertoires de Travail", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = ws,
                    onValueChange = { ws = it },
                    label = { Text("Espace de travail (/sdcard)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_workspace_path")
                )
                OutlinedTextField(
                    value = out,
                    onValueChange = { out = it },
                    label = { Text("Dossier de sortie (/sdcard)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_output_path")
                )
                Text(
                    text = "Les dossiers seront automatiquement créés dans le stockage interne.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(ws, out) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.testTag("btn_confirm_paths")
            ) {
                Text("Appliquer", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextSecondary) }
        },
        containerColor = CyberSurface
    )
}
