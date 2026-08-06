package com.example.engine

import com.example.data.model.ActionType
import com.example.data.model.AssistantAction

class SecurityManager {

    fun isActionDangerous(action: AssistantAction): Boolean {
        return action.type.requiresConfirmation || when (action.type) {
            ActionType.DELETE_FILE,
            ActionType.FACTORY_RESET,
            ActionType.FORMAT_STORAGE,
            ActionType.SEND_MONEY,
            ActionType.EMERGENCY_CALL -> true
            else -> false
        }
    }

    fun getConfirmationWarningMessage(action: AssistantAction): String {
        return when (action.type) {
            ActionType.DELETE_FILE -> "Warning: This will permanently delete local files. Are you sure you want to proceed?"
            ActionType.FACTORY_RESET -> "CRITICAL WARNING: Factory reset will erase all device data. Confirm execution?"
            ActionType.FORMAT_STORAGE -> "CRITICAL WARNING: Formatting storage will wipe your SD card/storage completely. Confirm?"
            ActionType.SEND_MONEY -> "Financial Security Alert: Please confirm initiating payment transfer for ${action.targetName ?: "contact"}."
            ActionType.EMERGENCY_CALL -> "Emergency Call Alert: Calling ${action.phoneNumber ?: "112"}. Confirm emergency call dispatch?"
            else -> "This action requires explicit user security confirmation. Do you want to proceed?"
        }
    }
}
