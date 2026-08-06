package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ActionType
import com.example.data.model.AssistantAction
import com.example.ui.MainViewModel
import com.example.ui.components.SystemAppShortcutCard
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.VioletSecondary

data class SystemAppItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val voicePrompt: String,
    val action: AssistantAction
)

@Composable
fun SystemAppsScreen(viewModel: MainViewModel) {

    val appShortcuts = listOf(
        SystemAppItem(
            title = "YouTube",
            subtitle = "Search & play songs/videos",
            icon = Icons.Default.PlayArrow,
            color = Color(0xFFFF0000),
            voicePrompt = "AI Assistant YouTube kholo",
            action = AssistantAction(ActionType.OPEN_APP, targetApp = "YouTube", rawCommand = "YouTube kholo", feedbackMessage = "YouTube khol raha hu.")
        ),
        SystemAppItem(
            title = "Google Maps",
            subtitle = "Search locations & navigation",
            icon = Icons.Default.Map,
            color = Color(0xFF34A853),
            voicePrompt = "AI Assistant Google Maps me Bilaspur search karo",
            action = AssistantAction(ActionType.MAPS_SEARCH, targetApp = "Google Maps", locationQuery = "Bilaspur", rawCommand = "Google Maps me Bilaspur search karo", feedbackMessage = "Maps me Bilaspur search kar raha hu.")
        ),
        SystemAppItem(
            title = "Navigation",
            subtitle = "Start turn-by-turn navigation",
            icon = Icons.Default.Navigation,
            color = NeonCyanPrimary,
            voicePrompt = "AI Assistant Navigation start karo",
            action = AssistantAction(ActionType.MAPS_NAVIGATE, locationQuery = "Bilaspur", rawCommand = "Navigation start karo", feedbackMessage = "Navigation start kar raha hu.")
        ),
        SystemAppItem(
            title = "Phone Dialer",
            subtitle = "Call contacts & redial",
            icon = Icons.Default.Call,
            color = Color(0xFF4CAF50),
            voicePrompt = "AI Assistant Papa ko call karo",
            action = AssistantAction(ActionType.CALL_CONTACT, targetName = "Papa", phoneNumber = "+919876543210", rawCommand = "Papa ko call karo", feedbackMessage = "Papa ko call kar raha hu.")
        ),
        SystemAppItem(
            title = "WhatsApp",
            subtitle = "Send messages & voice calls",
            icon = Icons.Default.QuestionAnswer,
            color = Color(0xFF25D366),
            voicePrompt = "AI Assistant WhatsApp me Rahul ko message bhejo",
            action = AssistantAction(ActionType.WHATSAPP_MESSAGE, targetName = "Rahul", phoneNumber = "+919876543211", messageText = "Hello from AI Assistant 2.0", rawCommand = "WhatsApp me Rahul ko message bhejo", feedbackMessage = "WhatsApp message bhej raha hu.")
        ),
        SystemAppItem(
            title = "Camera",
            subtitle = "Open camera & photo capture",
            icon = Icons.Default.CameraAlt,
            color = VioletSecondary,
            voicePrompt = "AI Assistant Camera kholo",
            action = AssistantAction(ActionType.OPEN_CAMERA, targetApp = "Camera", rawCommand = "Camera kholo", feedbackMessage = "Camera open kar raha hu.")
        ),
        SystemAppItem(
            title = "Video Recorder",
            subtitle = "Record high-res video",
            icon = Icons.Default.Videocam,
            color = Color(0xFFE11D48),
            voicePrompt = "AI Assistant Video record karo",
            action = AssistantAction(ActionType.RECORD_VIDEO, targetApp = "Camera", rawCommand = "Video record karo", feedbackMessage = "Video recorder open kar raha hu.")
        ),
        SystemAppItem(
            title = "Gallery / Photos",
            subtitle = "Browse images & media",
            icon = Icons.Default.PhotoLibrary,
            color = AmberTertiary,
            voicePrompt = "AI Assistant Gallery kholo",
            action = AssistantAction(ActionType.OPEN_GALLERY, targetApp = "Photos", rawCommand = "Gallery kholo", feedbackMessage = "Gallery open kar raha hu.")
        ),
        SystemAppItem(
            title = "System Settings",
            subtitle = "WiFi, Bluetooth & Display",
            icon = Icons.Default.Settings,
            color = Color(0xFF64748B),
            voicePrompt = "AI Assistant Settings kholo",
            action = AssistantAction(ActionType.SYSTEM_SETTING, targetApp = "settings", rawCommand = "Settings kholo", feedbackMessage = "Settings open kar raha hu.")
        ),
        SystemAppItem(
            title = "WiFi Settings",
            subtitle = "Configure network & WiFi",
            icon = Icons.Default.Wifi,
            color = NeonCyanPrimary,
            voicePrompt = "AI Assistant WiFi on karo",
            action = AssistantAction(ActionType.WIFI_CONTROL, targetApp = "wifi", rawCommand = "WiFi on karo", feedbackMessage = "WiFi Settings khol raha hu.")
        ),
        SystemAppItem(
            title = "Bluetooth",
            subtitle = "Manage Bluetooth devices",
            icon = Icons.Default.Bluetooth,
            color = Color(0xFF3B82F6),
            voicePrompt = "AI Assistant Bluetooth off karo",
            action = AssistantAction(ActionType.BLUETOOTH_CONTROL, targetApp = "bluetooth", rawCommand = "Bluetooth off karo", feedbackMessage = "Bluetooth Settings khol raha hu.")
        ),
        SystemAppItem(
            title = "Display Brightness",
            subtitle = "Adjust screen brightness",
            icon = Icons.Default.LightMode,
            color = AmberTertiary,
            voicePrompt = "AI Assistant Brightness 40 percent karo",
            action = AssistantAction(ActionType.BRIGHTNESS_CONTROL, numericValue = 40, rawCommand = "Brightness 40 percent karo", feedbackMessage = "Brightness settings adjust kar raha hu (40%).")
        ),
        SystemAppItem(
            title = "Google Drive",
            subtitle = "Cloud files & documents",
            icon = Icons.Default.Cloud,
            color = Color(0xFF4285F4),
            voicePrompt = "AI Assistant Drive kholo",
            action = AssistantAction(ActionType.DRIVE_SEARCH, targetApp = "Drive", rawCommand = "Drive kholo", feedbackMessage = "Google Drive open kar raha hu.")
        ),
        SystemAppItem(
            title = "Files & Storage",
            subtitle = "Manage local files",
            icon = Icons.Default.Folder,
            color = Color(0xFFF59E0B),
            voicePrompt = "AI Assistant Files kholo",
            action = AssistantAction(ActionType.FILES_OPEN, targetApp = "Files", rawCommand = "Files kholo", feedbackMessage = "Files manager open kar raha hu.")
        ),
        SystemAppItem(
            title = "Clock & Alarms",
            subtitle = "Alarms, timers & stopwatch",
            icon = Icons.Default.Schedule,
            color = VioletSecondary,
            voicePrompt = "AI Assistant Alarm kholo",
            action = AssistantAction(ActionType.OPEN_APP, targetApp = "Clock", rawCommand = "Alarm kholo", feedbackMessage = "Clock open kar raha hu.")
        ),
        SystemAppItem(
            title = "Calculator",
            subtitle = "Quick math & calculations",
            icon = Icons.Default.Calculate,
            color = Color(0xFF10B981),
            voicePrompt = "AI Assistant Calculator kholo",
            action = AssistantAction(ActionType.OPEN_APP, targetApp = "Calculator", rawCommand = "Calculator kholo", feedbackMessage = "Calculator open kar raha hu.")
        ),
        SystemAppItem(
            title = "Gmail",
            subtitle = "Check & draft emails",
            icon = Icons.Default.Email,
            color = Color(0xFFEA4335),
            voicePrompt = "AI Assistant Gmail kholo",
            action = AssistantAction(ActionType.OPEN_APP, targetApp = "Gmail", rawCommand = "Gmail kholo", feedbackMessage = "Gmail open kar raha hu.")
        ),
        SystemAppItem(
            title = "Play Store",
            subtitle = "Search & install apps",
            icon = Icons.Default.ShoppingBag,
            color = Color(0xFF0F9D58),
            voicePrompt = "AI Assistant Play Store kholo",
            action = AssistantAction(ActionType.OPEN_APP, targetApp = "Play Store", rawCommand = "Play Store kholo", feedbackMessage = "Play Store open kar raha hu.")
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "SYSTEM APPS & SHORTCUTS",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = "Tap to trigger direct voice command & launch app",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(appShortcuts) { item ->
                SystemAppShortcutCard(
                    title = item.title,
                    subtitle = "${item.subtitle} • Voice: \"${item.voicePrompt}\"",
                    icon = item.icon,
                    accentColor = item.color,
                    onClick = {
                        viewModel.executeDirectAction(item.action)
                    }
                )
            }
        }
    }
}
