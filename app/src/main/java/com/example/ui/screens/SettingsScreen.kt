package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.ui.MainViewModel
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SuccessGreen

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val speechLang by viewModel.speechLanguage.collectAsState()
    val isWakeWordActive by viewModel.isWakeWordActive.collectAsState()

    val geminiKeyStatus = try {
        if (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") "Active & Connected (Gemini 3.5 Flash)"
        else "Offline Mode (Rule Engine Active)"
    } catch (e: Exception) {
        "Offline Rule Engine Active"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "ASSISTANT SETTINGS & PREFERENCES",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = "Configure voice recognition, wake word & security controls",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Speech Language Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Speech Recognition Language",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyanPrimary
                    )
                )
                Text(
                    text = "Natural Hindi + English (Hinglish) voice parsing supported",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().testTag("radio_lang_hindi")
                ) {
                    RadioButton(
                        selected = speechLang == "hi-IN",
                        onClick = { viewModel.setSpeechLanguage("hi-IN") },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonCyanPrimary)
                    )
                    Text("Hindi / Hinglish (hi-IN)", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().testTag("radio_lang_english")
                ) {
                    RadioButton(
                        selected = speechLang == "en-IN",
                        onClick = { viewModel.setSpeechLanguage("en-IN") },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonCyanPrimary)
                    )
                    Text("English (en-IN / en-US)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Wake Word Detection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Wake Word Detection (\"AI Assistant\")",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyanPrimary
                        )
                    )
                    Text(
                        text = "Enable continuous listening for 'AI Assistant' wake phrase",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Switch(
                    checked = isWakeWordActive,
                    onCheckedChange = { viewModel.toggleWakeWord(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyanPrimary),
                    modifier = Modifier.testTag("switch_wake_word")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security & Dangerous Actions Protocol
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Security Confirmation Protocol",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyanPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dangerous actions (Delete files, Factory reset, Format storage, Money transfer, Emergency calls) require explicit visual and voice user confirmation before execution.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Intelligence Engine Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AI Intent Engine Status",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyanPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = geminiKeyStatus,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SuccessGreen
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hybrid Dual Engine: Offline Regex NLP provides instant response (<20ms). Online Gemini AI resolves complex natural Hindi conversational prompts.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}
