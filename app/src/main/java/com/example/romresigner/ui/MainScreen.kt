package com.example.romresigner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.romresigner.ui.screens.AiAuditScreen
import com.example.romresigner.ui.screens.KeyManagerScreen
import com.example.romresigner.ui.screens.LiveTerminalScreen
import com.example.romresigner.ui.screens.StudioDashboardScreen
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class ResignerTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    STUDIO("Studio", Icons.Filled.Build, Icons.Outlined.Build, "nav_studio"),
    KEYS("Clés AOSP", Icons.Filled.VpnKey, Icons.Outlined.VpnKey, "nav_keys"),
    LOGS("Terminal", Icons.Filled.Terminal, Icons.Outlined.Terminal, "nav_logs"),
    AI("Audit IA", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_ai")
}

@Composable
fun MainScreen(viewModel: RomResignerViewModel) {
    var selectedTab by remember { mutableStateOf(ResignerTab.STUDIO) }

    // BackHandler pour retourner à l'écran Studio depuis les onglets secondaires
    BackHandler(enabled = selectedTab != ResignerTab.STUDIO) {
        selectedTab = ResignerTab.STUDIO
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = CyberBg,
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                ResignerTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberEmerald,
                            selectedTextColor = CyberEmerald,
                            indicatorColor = CyberEmerald.copy(alpha = 0.2f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberBg)
        ) {
            when (selectedTab) {
                ResignerTab.STUDIO -> StudioDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToLogs = { selectedTab = ResignerTab.LOGS }
                )
                ResignerTab.KEYS -> KeyManagerScreen(viewModel = viewModel)
                ResignerTab.LOGS -> LiveTerminalScreen(viewModel = viewModel)
                ResignerTab.AI -> AiAuditScreen(viewModel = viewModel)
            }
        }
    }
}
