package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.NavTab
import com.example.ui.components.ConfirmationDialog
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SystemAppsScreen
import com.example.ui.theme.AiAssistantTheme
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.SlateSurface

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            AiAssistantTheme {
                MainAppStructure(viewModel = viewModel)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS
        )

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}

@Composable
fun MainAppStructure(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val pendingAction by viewModel.pendingConfirmationAction.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = selectedTab == NavTab.DASHBOARD,
                    onClick = { viewModel.selectTab(NavTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Dashboard") },
                    label = { Text("Assistant", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyanPrimary,
                        selectedTextColor = NeonCyanPrimary,
                        indicatorColor = NeonCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_dashboard")
                )

                NavigationBarItem(
                    selected = selectedTab == NavTab.SYSTEM_APPS,
                    onClick = { viewModel.selectTab(NavTab.SYSTEM_APPS) },
                    icon = { Icon(Icons.Default.Apps, contentDescription = "System Apps") },
                    label = { Text("Apps", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyanPrimary,
                        selectedTextColor = NeonCyanPrimary,
                        indicatorColor = NeonCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_apps")
                )

                NavigationBarItem(
                    selected = selectedTab == NavTab.HISTORY,
                    onClick = { viewModel.selectTab(NavTab.HISTORY) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyanPrimary,
                        selectedTextColor = NeonCyanPrimary,
                        indicatorColor = NeonCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_history")
                )

                NavigationBarItem(
                    selected = selectedTab == NavTab.CONTACTS,
                    onClick = { viewModel.selectTab(NavTab.CONTACTS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Contacts") },
                    label = { Text("Contacts", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyanPrimary,
                        selectedTextColor = NeonCyanPrimary,
                        indicatorColor = NeonCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_contacts")
                )

                NavigationBarItem(
                    selected = selectedTab == NavTab.SETTINGS,
                    onClick = { viewModel.selectTab(NavTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyanPrimary,
                        selectedTextColor = NeonCyanPrimary,
                        indicatorColor = NeonCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SlateDarkBg)
        ) {
            when (selectedTab) {
                NavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                NavTab.SYSTEM_APPS -> SystemAppsScreen(viewModel = viewModel)
                NavTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                NavTab.CONTACTS -> ContactsScreen(viewModel = viewModel)
                NavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }

            // Security Confirmation Modal for dangerous actions
            pendingAction?.let { action ->
                ConfirmationDialog(
                    action = action,
                    warningMessage = viewModel.getSecurityWarning(action),
                    onConfirm = { viewModel.confirmPendingAction() },
                    onDismiss = { viewModel.dismissPendingAction() }
                )
            }
        }
    }
}
