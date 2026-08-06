package com.example.data.repository

import android.content.Context
import com.example.data.db.AssistantDatabase
import com.example.data.db.CommandHistoryEntity
import com.example.data.db.ContactAliasEntity
import com.example.data.model.AssistantAction
import com.example.data.model.ParseResult
import com.example.engine.AssistantNlpEngine
import com.example.engine.SecurityManager
import com.example.engine.SystemActionDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AssistantRepository(private val context: Context) {

    private val db = AssistantDatabase.getDatabase(context)
    private val dao = db.assistantDao()
    private val nlpEngine = AssistantNlpEngine()
    private val dispatcher = SystemActionDispatcher(context)
    private val securityManager = SecurityManager()

    val history: Flow<List<CommandHistoryEntity>> = dao.getAllHistory()
    val favoriteHistory: Flow<List<CommandHistoryEntity>> = dao.getFavoriteHistory()
    val contacts: Flow<List<ContactAliasEntity>> = dao.getAllAliases()

    suspend fun processVoiceCommand(rawInput: String): ParseResult {
        val currentContacts = try { contacts.first() } catch (e: Exception) { emptyList() }
        val parseResult = nlpEngine.parseCommand(rawInput, currentContacts)

        // Save command into Room DB history
        val isSuccess = !securityManager.isActionDangerous(parseResult.action)
        dao.insertHistory(
            CommandHistoryEntity(
                rawCommand = rawInput,
                actionType = parseResult.action.type.name,
                feedbackText = parseResult.action.feedbackMessage,
                isSuccess = isSuccess
            )
        )

        return parseResult
    }

    fun isDangerous(action: AssistantAction): Boolean {
        return securityManager.isActionDangerous(action)
    }

    fun getSecurityWarning(action: AssistantAction): String {
        return securityManager.getConfirmationWarningMessage(action)
    }

    fun executeSystemAction(action: AssistantAction): Boolean {
        return dispatcher.executeAction(action)
    }

    suspend fun toggleFavorite(item: CommandHistoryEntity) {
        dao.updateHistory(item.copy(isFavorite = !item.isFavorite))
    }

    suspend fun deleteHistory(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }

    suspend fun addContactAlias(alias: String, name: String, phone: String) {
        dao.insertAlias(
            ContactAliasEntity(
                aliasName = alias.lowercase().trim(),
                actualName = name.trim(),
                phoneNumber = phone.trim()
            )
        )
    }

    suspend fun deleteContactAlias(id: Long) {
        dao.deleteAliasById(id)
    }

    suspend fun seedInitialAliasesIfEmpty() {
        val current = try { contacts.first() } catch (e: Exception) { emptyList() }
        if (current.isEmpty()) {
            addContactAlias("papa", "Dad", "+919876543210")
            addContactAlias("rahul", "Rahul Sharma", "+919876543211")
            addContactAlias("mummy", "Mom", "+919876543212")
            addContactAlias("doctor", "Dr. Verma (Hospital)", "+919876543213")
        }
    }
}
