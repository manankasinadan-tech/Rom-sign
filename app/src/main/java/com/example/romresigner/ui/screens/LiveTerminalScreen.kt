package com.example.romresigner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.romresigner.model.LogEntry
import com.example.romresigner.model.LogLevel
import com.example.romresigner.ui.RomResignerViewModel
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveTerminalScreen(viewModel: RomResignerViewModel) {
    val logs by viewModel.logs.collectAsState()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf<LogLevel?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(logs, selectedFilter, searchQuery) {
        logs.filter { entry ->
            val matchFilter = selectedFilter == null || entry.level == selectedFilter
            val matchSearch = searchQuery.isBlank() ||
                    entry.message.contains(searchQuery, ignoreCase = true) ||
                    entry.tag.contains(searchQuery, ignoreCase = true)
            matchFilter && matchSearch
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("live_terminal_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // En-tête de la console
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
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
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Console de Télémétrie en Direct",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Journal exporté dans /sdcard/ROM_Resigner/logs/", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.testTag("btn_export_logs")
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Exporter", tint = CyberCyan)
                        }

                        IconButton(
                            onClick = { viewModel.clearLogs() },
                            modifier = Modifier.testTag("btn_clear_logs")
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Effacer", tint = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Barre de recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filtrer par mot-clé (ex: SELinux, APK, hash...)", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("input_search_logs"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberEmerald,
                        unfocusedBorderColor = CyberBorder,
                        focusedContainerColor = CyberSurfaceVariant,
                        unfocusedContainerColor = CyberSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pilules de filtrage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterPill(
                        label = "TOUT (${logs.size})",
                        isSelected = selectedFilter == null,
                        color = CyberCyan,
                        onClick = { selectedFilter = null }
                    )
                    FilterPill(
                        label = "SUCCESS",
                        isSelected = selectedFilter == LogLevel.SUCCESS,
                        color = CyberEmerald,
                        onClick = { selectedFilter = LogLevel.SUCCESS }
                    )
                    FilterPill(
                        label = "AUDIT",
                        isSelected = selectedFilter == LogLevel.AUDIT,
                        color = Color(0xFF00E5FF),
                        onClick = { selectedFilter = LogLevel.AUDIT }
                    )
                    FilterPill(
                        label = "WARN",
                        isSelected = selectedFilter == LogLevel.WARN,
                        color = Color(0xFFFFB300),
                        onClick = { selectedFilter = LogLevel.WARN }
                    )
                    FilterPill(
                        label = "ERR",
                        isSelected = selectedFilter == LogLevel.ERROR,
                        color = Color(0xFFFF5252),
                        onClick = { selectedFilter = LogLevel.ERROR }
                    )
                }
            }
        }

        // Fenêtre Terminal Noire
        Card(
            colors = CardDefaults.cardColors(containerColor = TerminalBg),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
                .testTag("terminal_container")
        ) {
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun log correspondant au filtre.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .testTag("terminal_logs_list"),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        TerminalLine(log)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) color else TextSecondary
        )
    }
}

@Composable
fun TerminalLine(entry: LogEntry) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = entry.timestamp,
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "[${entry.level.name}]",
            color = when (entry.level) {
                LogLevel.SUCCESS -> CyberEmerald
                LogLevel.WARN -> Color(0xFFFFB300)
                LogLevel.ERROR -> Color(0xFFFF5252)
                LogLevel.AUDIT -> CyberCyan
                LogLevel.INFO -> Color(0xFF64B5F6)
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "<${entry.tag}>",
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = entry.message,
            color = when (entry.level) {
                LogLevel.SUCCESS -> CyberEmerald
                LogLevel.WARN -> Color(0xFFFFD54F)
                LogLevel.ERROR -> Color(0xFFFF8A80)
                else -> TextPrimary
            },
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
