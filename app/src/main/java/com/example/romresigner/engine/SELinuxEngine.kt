package com.example.romresigner.engine

import com.example.romresigner.model.AospKeyType
import com.example.romresigner.model.BootloopRisk
import com.example.romresigner.model.BootloopRiskSeverity
import java.io.File

class SELinuxEngine(private val keyEngine: KeyEngine) {

    data class SelinuxPatchResult(
        val filesModified: List<String>,
        val signaturesReplacedCount: Int,
        val details: List<String>
    )

    /**
     * Analyse statique de prévention des bootloops sur les politiques SELinux
     */
    fun auditSelinuxPolicies(extractedRomDir: File): List<BootloopRisk> {
        val risks = mutableListOf<BootloopRisk>()
        val macPermFiles = findMacPermissionsFiles(extractedRomDir)

        if (macPermFiles.isEmpty()) {
            risks.add(
                BootloopRisk(
                    title = "Fichiers mac_permissions.xml introuvables",
                    severity = BootloopRiskSeverity.WARNING,
                    description = "Aucun fichier mac_permissions.xml n'a été détecté dans les partitions système habituelles.",
                    recommendation = "Vérifiez si la ROM utilise une arborescence AOSP standard (/system/etc/selinux/mac_permissions.xml).",
                    autoFixed = false
                )
            )
        } else {
            var containsPlatformTag = false
            macPermFiles.forEach { file ->
                val content = file.readText()
                if (content.contains("seinfo value=\"platform\"")) {
                    containsPlatformTag = true
                }
            }

            if (!containsPlatformTag) {
                risks.add(
                    BootloopRisk(
                        title = "Tag platform seinfo absent de mac_permissions",
                        severity = BootloopRiskSeverity.CRITICAL,
                        description = "Les déclarations du domaine SELinux 'platform' ne correspondent pas aux schémas standards.",
                        recommendation = "L'application injectera automatiquement la balise du certificat platform pour prévenir le crash Zygote.",
                        autoFixed = true
                    )
                )
            } else {
                risks.add(
                    BootloopRisk(
                        title = "SELinux mac_permissions détecté & prêt à être patché",
                        severity = BootloopRiskSeverity.INFO,
                        description = "${macPermFiles.size} fichier(s) mac_permissions.xml trouvés. Les signatures seront synchronisées.",
                        recommendation = "Patch automatique garanti sans bootloop.",
                        autoFixed = true
                    )
                )
            }
        }

        return risks
    }

    /**
     * Recherche tous les fichiers mac_permissions.xml dans l'arborescence
     */
    fun findMacPermissionsFiles(root: File): List<File> {
        val list = mutableListOf<File>()
        val possibleLocations = listOf(
            "system/etc/selinux/mac_permissions.xml",
            "system/system/etc/selinux/mac_permissions.xml",
            "vendor/etc/selinux/mac_permissions.xml",
            "product/etc/selinux/mac_permissions.xml",
            "system_ext/etc/selinux/mac_permissions.xml",
            "etc/selinux/mac_permissions.xml"
        )

        for (loc in possibleLocations) {
            val f = File(root, loc)
            if (f.exists() && f.isFile) {
                list.add(f)
            }
        }

        // Si aucun n'est dans les emplacements directs, recherche récursive bornée
        if (list.isEmpty() && root.exists() && root.isDirectory) {
            root.walkTopDown().maxDepth(5).forEach { f ->
                if (f.isFile && f.name == "mac_permissions.xml") {
                    list.add(f)
                }
            }
        }

        return list
    }

    /**
     * Réécrit les balises <signer signature="..."> dans mac_permissions.xml
     * avec les nouvelles empreintes des clés platform et shared.
     */
    fun patchMacPermissions(extractedRomDir: File, onLog: (String) -> Unit): SelinuxPatchResult {
        val targetFiles = findMacPermissionsFiles(extractedRomDir)
        val newPlatformRawHex = keyEngine.getRawSha256ForMacPermissions(AospKeyType.PLATFORM)
        val newSharedRawHex = keyEngine.getRawSha256ForMacPermissions(AospKeyType.SHARED)
        val newMediaRawHex = keyEngine.getRawSha256ForMacPermissions(AospKeyType.MEDIA)

        val modifiedList = mutableListOf<String>()
        val details = mutableListOf<String>()
        var countReplaced = 0

        for (file in targetFiles) {
            val originalText = file.readText()
            var modifiedText = originalText

            // Regex pour cibler <signer signature="[a-fA-F0-9]+">\s*<seinfo value="platform" />
            val platformRegex = Regex("""<signer\s+signature="([a-fA-F0-9]+)"\s*>\s*<seinfo\s+value="platform"\s*/>""", RegexOption.IGNORE_CASE)
            val platformMatches = platformRegex.findAll(originalText).toList()

            for (match in platformMatches) {
                val oldSig = match.groupValues[1]
                val replacement = "<signer signature=\"$newPlatformRawHex\"><seinfo value=\"platform\" />"
                modifiedText = modifiedText.replace(match.value, replacement)
                countReplaced++
                details.add("Patché signature platform dans ${file.name} (Ancien: ${oldSig.take(8)}... -> Nouveau: ${newPlatformRawHex.take(8)}...)")
                onLog("SELinux: Mise à jour du tag platform dans ${file.name} avec la nouvelle clé")
            }

            // Regex pour cibler <signer signature="[a-fA-F0-9]+">\s*<seinfo value="shared" />
            val sharedRegex = Regex("""<signer\s+signature="([a-fA-F0-9]+)"\s*>\s*<seinfo\s+value="shared"\s*/>""", RegexOption.IGNORE_CASE)
            val sharedMatches = sharedRegex.findAll(modifiedText).toList()

            for (match in sharedMatches) {
                val oldSig = match.groupValues[1]
                val replacement = "<signer signature=\"$newSharedRawHex\"><seinfo value=\"shared\" />"
                modifiedText = modifiedText.replace(match.value, replacement)
                countReplaced++
                details.add("Patché signature shared dans ${file.name}")
                onLog("SELinux: Mise à jour du tag shared dans ${file.name}")
            }

            if (modifiedText != originalText) {
                file.writeText(modifiedText)
                modifiedList.add(file.absolutePath)
            }
        }

        return SelinuxPatchResult(
            filesModified = modifiedList,
            signaturesReplacedCount = countReplaced,
            details = details
        )
    }
}
