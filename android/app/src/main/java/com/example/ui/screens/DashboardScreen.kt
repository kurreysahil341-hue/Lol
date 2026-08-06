package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.SpeechState
import com.example.ui.MainViewModel
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.VioletSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val speechState by viewModel.speechState.collectAsState()
    val volumeLevel by viewModel.volumeLevel.collectAsState()
    val transcript by viewModel.userTranscript.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val speechLang by viewModel.speechLanguage.collectAsState()

    var manualText by remember { mutableStateOf("") }

    val samplePrompts = remember {
        listOf(
            "AI Assistant YouTube kholo",
            "Arijit Singh songs search karo",
            "Google Maps me Bilaspur search karo",
            "Phone kaise chalaye sikhao (Voice Teacher)",
            "WhatsApp kaise chalaye sikhao",
            "Navigation start karo",
            "Papa ko call karo",
            "Camera kholo",
            "Photo click karo",
            "WhatsApp me Rahul ko message bhejo",
            "Mera favourite song Arijit Singh hai yaad rakho",
            "Mujhe kya yaad hai mera preference batao",
            "Kal subah 7 baje ka alarm lagao",
            "Battery jaldi khatam ho rahi hai problem solution",
            "Storage full ho gaya space problem",
            "Wi-Fi net nahi chal raha problem",
            "Settings kholo",
            "WiFi settings kholo",
            "Bluetooth settings kholo"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Visual Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.assistant_hero_1784786439086),
                    contentDescription = "Assistant Hero Visual",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "AI ASSISTANT 2.0",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyanPrimary
                            )
                        )
                        Text(
                            text = "Voice Command Execution Engine ($speechLang)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.LightGray
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Futuristic Waveform Visualizer & Mic Trigger
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            VoiceWaveformVisualizer(
                speechState = speechState,
                volumeLevel = volumeLevel,
                size = 170.dp
            )

            IconButton(
                onClick = {
                    if (speechState is SpeechState.Listening) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (speechState is SpeechState.Listening) MaterialTheme.colorScheme.error else NeonCyanPrimary
                    )
                    .testTag("mic_toggle_button")
            ) {
                Icon(
                    imageVector = if (speechState is SpeechState.Listening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Input Mic",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Text(
            text = when (speechState) {
                is SpeechState.Listening -> "Listening... Speak your command now"
                is SpeechState.Processing -> "Processing voice intent..."
                is SpeechState.Speaking -> "Assistant speaking response..."
                is SpeechState.Error -> (speechState as SpeechState.Error).message
                else -> "Tap Microphone or Say 'AI Assistant'"
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (speechState is SpeechState.Listening) NeonCyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // AI Conversation Transcript Cards
        if (transcript.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "You Said:",
                        style = MaterialTheme.typography.labelSmall.copy(color = VioletSecondary)
                    )
                    Text(
                        text = transcript,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(1.dp, NeonCyanPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "AI Assistant 2.0 Response:",
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonCyanPrimary)
                    )
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speech Audio Output",
                        tint = NeonCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = aiResponse,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text Fallback Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualText,
                onValueChange = { manualText = it },
                placeholder = { Text("Type voice command (Hindi/English)...", color = Color.Gray, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("manual_command_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface,
                    focusedBorderColor = NeonCyanPrimary,
                    unfocusedBorderColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (manualText.isNotBlank()) {
                        viewModel.processCommandText(manualText)
                        manualText = ""
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyanPrimary)
                    .testTag("btn_send_manual_command")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Command",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Natural Voice Command Chips (Hindi + English)
        Text(
            text = "TRY VOICE COMMANDS (Hindi / English)",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            samplePrompts.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SlateSurface)
                        .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(20.dp))
                        .clickable { viewModel.processCommandText(prompt) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("chip_prompt_$prompt")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = NeonCyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
