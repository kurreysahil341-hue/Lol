package com.example.data.model

data class AssistantAction(
    val type: ActionType,
    val targetApp: String? = null,
    val targetName: String? = null,
    val phoneNumber: String? = null,
    val messageText: String? = null,
    val searchQuery: String? = null,
    val locationQuery: String? = null,
    val numericValue: Int? = null, // e.g., brightness %, volume level
    val rawCommand: String = "",
    val feedbackMessage: String = "",
    val requiresConfirmation: Boolean = false,
    val payloadDetails: String? = null
)

data class ParseResult(
    val action: AssistantAction,
    val confidence: Float = 1.0f,
    val sourceEngine: String = "RuleEngine" // "RuleEngine" or "GeminiAI"
)
