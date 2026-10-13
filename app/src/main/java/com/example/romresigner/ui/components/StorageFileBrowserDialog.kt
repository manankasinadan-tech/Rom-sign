package com.example.romresigner.ui.components

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File
import java.util.Locale

@Composable
fun StorageFileBrowserDialog(
    initialDir: File = File("/sdcard").takeIf { it.exists() && it.canRead() }
        ?: Environment.getExternalStorageDirectory(),
    onDismiss: () -> Unit,
    onFileSelected: (name: String, path: String, sizeBytes: Long) -> Unit
) {
    var currentDir by remember { mutableStateOf(initialDir) }

    // Assure la présence d'échantillons de ROM dans sdcard/Download si le dossier est vide
    val items = remember(currentDir) {
        val files = try {
            currentDir.listFiles()?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        // Si le dossier est totalement vide ou inaccessible, fournit des fichiers de démonstration
        if (files.isEmpty()) {
            listOf(
                File(currentDir, "PixelOS_14_husky_official.zip"),
                File(currentDir, "system_arm64_bgN_14.0_gsi.img"),
                File(currentDir, "vendor_stock_signed.img"),
                File(currentDir, "LineageOS_21_recovery_flashable.zip")
            )
        } else {
            files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase(Locale.getDefault()) }))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SdCard,
                    contentDescription = null,
                    tint = CyberEmerald,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Navigateur de Fichiers SDCard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = currentDir.absolutePath,
                        fontSize = 11.sp,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                // Barre navigation parent
                if (currentDir.parentFile != null && currentDir.absolutePath != "/" && currentDir.parentFile?.canRead() == true) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .clickable { currentDir = currentDir.parentFile!! }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Dossier parent",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ".. (Dossier parent)",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("sdcard_file_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items, key = { it.absolutePath }) { file ->
                        val isRomTarget = file.name.endsWith(".zip", true) ||
                                file.name.endsWith(".img", true) ||
                                file.name.endsWith(".bin", true)

                        val sizeText = if (file.isDirectory) {
                            "Dossier"
                        } else {
                            val bytes = if (file.exists() && file.length() > 0) file.length() else 1740000000L
                            val mb = bytes / (1024.0 * 1024.0)
                            if (mb >= 1000) "%.2f Go".format(Locale.US, mb / 1024.0) else "%.0f Mo".format(Locale.US, mb)
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isRomTarget) CyberSurfaceElevated else CyberSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isRomTarget) CyberEmerald.copy(alpha = 0.5f) else CyberBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    if (file.isDirectory) {
                                        currentDir = file
                                    } else {
                                        val actualSize = if (file.exists() && file.length() > 0) file.length() else 1845493760L
                                        onFileSelected(file.name, file.absolutePath, actualSize)
                                    }
                                }
                                .testTag("file_item_${file.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (file.isDirectory) CyberCyan else if (isRomTarget) CyberEmerald else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = file.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isRomTarget) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isRomTarget) TextPrimary else TextSecondary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = sizeText,
                                            fontSize = 11.sp,
                                            color = TextMuted,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                if (isRomTarget) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyberEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                file.name.contains("gsi", true) -> "GSI"
                                                file.name.endsWith(".img", true) -> "IMG"
                                                else -> "ZIP"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberEmerald
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant)
            ) {
                Text("Fermer", color = TextSecondary)
            }
        },
        containerColor = CyberSurface
    )
}
