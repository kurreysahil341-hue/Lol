package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawCommand: String,
    val actionType: String,
    val feedbackText: String,
    val isSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "contact_aliases")
data class ContactAliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val aliasName: String, // e.g., "papa", "rahul", "mom"
    val actualName: String, // e.g., "Rajesh Sharma"
    val phoneNumber: String, // e.g., "+919876543210"
    val isEmergency: Boolean = false
)
