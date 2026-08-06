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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ActionType
import com.example.data.model.AssistantAction
import com.example.ui.MainViewModel
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.SlateSurface

@Composable
fun ContactsScreen(viewModel: MainViewModel) {
    val contactsList by viewModel.contacts.collectAsState()

    var aliasInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "VOICE CONTACT ALIASES",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = "Map custom voice names like 'Papa', 'Rahul' to phone numbers for hands-free voice calling & WhatsApp.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add Contact Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Add New Voice Contact Alias",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyanPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = aliasInput,
                    onValueChange = { aliasInput = it },
                    label = { Text("Voice Keyword / Alias (e.g. Papa, Rahul, Doctor)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_alias_keyword"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyanPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Contact Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyanPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone Number (+91...)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_phone"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyanPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (aliasInput.isNotBlank() && phoneInput.isNotBlank()) {
                            viewModel.addContactAlias(aliasInput, nameInput.ifBlank { aliasInput }, phoneInput)
                            aliasInput = ""
                            nameInput = ""
                            phoneInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_alias"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyanPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Voice Alias", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SAVED VOICE CONTACTS (${contactsList.size})",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyLazyContacts(contactsList = contactsList, viewModel = viewModel)
    }
}

@Composable
private fun LazyLazyContacts(contactsList: List<com.example.data.db.ContactAliasEntity>, viewModel: MainViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(contactsList, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("contact_item_${item.aliasName}"),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NeonCyanPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "\"${item.aliasName}\" → ${item.actualName}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = item.phoneNumber,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.executeDirectAction(
                                AssistantAction(
                                    type = ActionType.CALL_CONTACT,
                                    targetName = item.aliasName,
                                    phoneNumber = item.phoneNumber,
                                    rawCommand = "${item.aliasName} ko call karo",
                                    feedbackMessage = "${item.aliasName} ko call kar raha hu."
                                )
                            )
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF4CAF50))
                    }

                    IconButton(
                        onClick = {
                            viewModel.executeDirectAction(
                                AssistantAction(
                                    type = ActionType.WHATSAPP_MESSAGE,
                                    targetName = item.aliasName,
                                    phoneNumber = item.phoneNumber,
                                    rawCommand = "WhatsApp me ${item.aliasName} ko message bhejo",
                                    feedbackMessage = "WhatsApp me ${item.aliasName} ko message bhej raha hu."
                                )
                            )
                        }
                    ) {
                        Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                    }

                    IconButton(onClick = { viewModel.deleteContactAlias(item.id) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }
        }
    }
}
