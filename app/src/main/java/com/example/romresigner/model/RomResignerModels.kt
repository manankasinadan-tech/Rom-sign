package com.example.romresigner.model

enum class RomPackageType(val label: String, val iconDescription: String) {
    ZIP_ARCHIVE("Archive ZIP Flashable", "Archive recovery flashable standard"),
    EXTRACTED_FOLDER("Dossier ROM Extrait", "Arborescence système extraite"),
    SYSTEM_IMG("Image Partition (system.img)", "Image brute ou sparse ext4/erofs"),
    GSI_IMAGE("Generic System Image (GSI)", "Image système générique pour Treble")
}

enum class AospKeyType(val keyName: String, val description: String, val defaultAppTarget: String) {
    PLATFORM("platform", "Clé centrale du système Android", "framework-res, SystemUI, SettingsProvider"),
    RELEASEKEY("releasekey", "Clé des applications utilisateur & OTA", "Apps tierces, Mises à jour du système"),
    SHARED("shared", "Clé des processus partagés (Home, Contacts)", "Contacts, Launcher, Dialers"),
    MEDIA("media", "Clé du fournisseur de médias", "MediaProvider, Galerie, Téléchargements"),
    NETWORKSTACK("networkstack", "Clé du module réseau Android", "NetworkStack, CaptivePortalLogin"),
    BLUETOOTH("bluetooth", "Clé du sous-système Bluetooth", "Bluetooth.apk, BluetoothServices"),
    VERITY("verity", "Clé dm-verity et intégrité de démarrage", "Vérification des partitions au boot")
}

data class KeyDetails(
    val type: AospKeyType,
    val alias: String,
    val subjectDn: String,
    val sha1Fingerprint: String,
    val sha256Fingerprint: String,
    val keySize: Int = 2048,
    val isGenerated: Boolean = true,
    val privateKeyFile: String = "",
    val certFile: String = ""
)

enum class LogLevel {
    INFO,
    SUCCESS,
    WARN,
    ERROR,
    AUDIT
}

data class LogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String
)

enum class BootloopRiskSeverity {
    CRITICAL,
    WARNING,
    INFO
}

data class BootloopRisk(
    val title: String,
    val severity: BootloopRiskSeverity,
    val description: String,
    val recommendation: String,
    val autoFixed: Boolean = false
)

enum class PipelineStage(val title: String) {
    IDLE("En attente"),
    ANALYZING("Analyse approfondie de la ROM"),
    KEY_MAPPING("Attribution des clés AOSP"),
    SIGNING_APKS("Signature cryptographique des APKs & APEX"),
    PATCHING_SELINUX("Mise à jour des signatures SELinux (mac_permissions)"),
    PATCHING_OTA_PROPS("Correction build.prop (release-keys) & otacerts"),
    VERIFYING("Vérification d'intégrité anti-bootloop"),
    PACKAGING("Reconditionnement du livrable final"),
    COMPLETED("ROM signée avec succès (Prête au flash)"),
    FAILED("Échec du processus")
}

data class ResigningProgressState(
    val stage: PipelineStage = PipelineStage.IDLE,
    val currentItem: String = "",
    val progressPercent: Float = 0f,
    val currentStep: Int = 0,
    val totalSteps: Int = 7,
    val isRunning: Boolean = false,
    val errorDetails: String? = null
)

data class RomSummary(
    val romName: String = "LineageOS_Custom_A14.zip",
    val packageType: RomPackageType = RomPackageType.ZIP_ARCHIVE,
    val androidVersion: String = "Android 14 (API 34)",
    val originalBuildTags: String = "test-keys",
    val apkCount: Int = 68,
    val apexCount: Int = 14,
    val gappsDetected: Boolean = true,
    val gappsAction: String = "Préservation des certs officiels Google",
    val selinuxMode: String = "Enforcing (mac_permissions.xml détecté)",
    val workspacePath: String = "/sdcard/ROM_Resigner/workspace",
    val outputPath: String = "/sdcard/ROM_Resigner/output",
    val sourcePath: String = "/sdcard/LineageOS_Custom_A14.zip",
    val fileSizeBytes: Long = 1845493760L,
    val formattedFileSize: String = "1.72 Go"
)
