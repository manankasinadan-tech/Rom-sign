package com.example.romresigner.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.romresigner.model.KeyDetails
import com.example.romresigner.ui.RomResignerViewModel
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
fun KeyManagerScreen(viewModel: RomResignerViewModel) {
    val keyBundle by viewModel.keyBundle.collectAsState()
    val context = LocalContext.current

    var showGenerateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("key_manager_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // En-tête Trousseau
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Trousseau de Clés AOSP",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "7 clés de production générées & actives",
                                    fontSize = 12.sp,
                                    color = CyberEmerald
                                )
                            }
                        }

                        Button(
                            onClick = { showGenerateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_open_keygen")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Régénérer", color = Color(0xFF00363D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Toutes les clés sont stockées dans les formats officiels PKCS#8 (.pk8) et certificats X.509 (.x509.pem). Les empreintes SHA-256 sont synchronisées avec SELinux.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Cartes de Clés
        items(keyBundle, key = { it.alias }) { key ->
            KeyCard(
                keyDetails = key,
                onCopy = { hash ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Key Fingerprint", hash))
                    Toast.makeText(context, "Empreinte copiée dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showGenerateDialog) {
        KeyGenerationDialog(
            onDismiss = { showGenerateDialog = false },
            onConfirm = { size, org ->
                viewModel.regenerateKeys(size, org)
                showGenerateDialog = false
            }
        )
    }
}

@Composable
fun KeyCard(keyDetails: KeyDetails, onCopy: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
            .testTag("key_card_${keyDetails.alias}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = keyDetails.alias.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = keyDetails.type.description,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RSA ${keyDetails.keySize}-bit",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Cible principale : ${keyDetails.type.defaultAppTarget}",
                fontSize = 12.sp,
                color = CyberEmerald,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // SHA-256 Fingerprint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "SHA-256 (SELinux Signer Fingerprint)", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = keyDetails.sha256Fingerprint,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = { onCopy(keyDetails.sha256Fingerprint) },
                    modifier = Modifier.size(28.dp).testTag("btn_copy_${keyDetails.alias}")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = CyberCyan, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun KeyGenerationDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var orgName by remember { mutableStateOf("LineageOS-Official-Release") }
    var keySize by remember { mutableStateOf(2048) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Générer un Trousseau AOSP", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = orgName,
                    onValueChange = { orgName = it },
                    label = { Text("Organisme / Développeur (O=)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_org_name")
                )

                Text("Taille des clés cryptographiques :", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { keySize = 2048 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (keySize == 2048) CyberEmerald else CyberSurfaceVariant
                        )
                    ) {
                        Text("2048 bits (Rapide)", color = if (keySize == 2048) Color(0xFF003918) else TextPrimary)
                    }

                    Button(
                        onClick = { keySize = 4096 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (keySize == 4096) CyberEmerald else CyberSurfaceVariant
                        )
                    ) {
                        Text("4096 bits (Max)", color = if (keySize == 4096) Color(0xFF003918) else TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(keySize, orgName) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                modifier = Modifier.testTag("btn_confirm_keygen")
            ) {
                Text("Générer les 7 clés", color = Color(0xFF003918), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextSecondary) }
        },
        containerColor = CyberSurface
    )
}
