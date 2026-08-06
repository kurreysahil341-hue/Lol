package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.CommandHistoryEntity
import com.example.data.db.ContactAliasEntity
import com.example.data.model.AssistantAction
import com.example.data.repository.AssistantRepository
import com.example.engine.AssistantSpeechEngine
import com.example.engine.SpeechState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class NavTab {
    DASHBOARD, SYSTEM_APPS, HISTORY, CONTACTS, SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AssistantRepository(application)
    private var speechEngine: AssistantSpeechEngine? = null

    val history: StateFlow<List<CommandHistoryEntity>> = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteHistory: StateFlow<List<CommandHistoryEntity>> = repository.favoriteHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val contacts: StateFlow<List<ContactAliasEntity>> = repository.contacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedTab = MutableStateFlow(NavTab.DASHBOARD)
    val selectedTab: StateFlow<NavTab> = _selectedTab.asStateFlow()

    private val _userTranscript = MutableStateFlow("")
    val userTranscript: StateFlow<String> = _userTranscript.asStateFlow()

    private val _aiResponse = MutableStateFlow("Namaste! Main AI Assistant 2.0 hu. Aap mujhse bol kar koi bhi task karwa sakte hain.")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _lastAction = MutableStateFlow<AssistantAction?>(null)
    val lastAction: StateFlow<AssistantAction?> = _lastAction.asStateFlow()

    private val _pendingConfirmationAction = MutableStateFlow<AssistantAction?>(null)
    val pendingConfirmationAction: StateFlow<AssistantAction?> = _pendingConfirmationAction.asStateFlow()

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _volumeLevel = MutableStateFlow(0f)
    val volumeLevel: StateFlow<Float> = _volumeLevel.asStateFlow()

    private val _speechLanguage = MutableStateFlow("hi-IN")
    val speechLanguage: StateFlow<String> = _speechLanguage.asStateFlow()

    private val _isWakeWordActive = MutableStateFlow(true)
    val isWakeWordActive: StateFlow<Boolean> = _isWakeWordActive.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialAliasesIfEmpty()
        }
        initSpeechEngine()
    }

    private fun initSpeechEngine() {
        speechEngine = AssistantSpeechEngine(
            context = getApplication(),
            onResultRecognized = { recognizedText ->
                processCommandText(recognizedText)
            }
        )

        viewModelScope.launch {
            speechEngine?.speechState?.collect { state ->
                _speechState.value = state
            }
        }

        viewModelScope.launch {
            speechEngine?.volumeLevel?.collect { vol ->
                _volumeLevel.value = vol
            }
        }
    }

    fun selectTab(tab: NavTab) {
        _selectedTab.value = tab
    }

    fun startListening() {
        speechEngine?.languageLocale = if (_speechLanguage.value == "hi-IN") Locale("hi", "IN") else Locale("en", "IN")
        speechEngine?.startListening()
    }

    fun stopListening() {
        speechEngine?.stopListening()
    }

    fun processCommandText(rawText: String) {
        _userTranscript.value = rawText
        viewModelScope.launch {
            val parseResult = repository.processVoiceCommand(rawText)
            val action = parseResult.action
            _lastAction.value = action
            _aiResponse.value = action.feedbackMessage

            // Speak response
            speechEngine?.speak(action.feedbackMessage)

            // Check security
            if (repository.isDangerous(action)) {
                _pendingConfirmationAction.value = action
            } else {
                repository.executeSystemAction(action)
            }
        }
    }

    fun getSecurityWarning(action: AssistantAction): String {
        return repository.getSecurityWarning(action)
    }

    fun confirmPendingAction() {
        _pendingConfirmationAction.value?.let { action ->
            repository.executeSystemAction(action)
            _aiResponse.value = "Confirmed and executed: ${action.feedbackMessage}"
            speechEngine?.speak("Action executed successfully.")
        }
        _pendingConfirmationAction.value = null
    }

    fun dismissPendingAction() {
        _pendingConfirmationAction.value = null
        _aiResponse.value = "Action cancelled for security."
        speechEngine?.speak("Cancelled action.")
    }

    fun toggleFavorite(item: CommandHistoryEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun addContactAlias(alias: String, name: String, phone: String) {
        viewModelScope.launch {
            repository.addContactAlias(alias, name, phone)
        }
    }

    fun deleteContactAlias(id: Long) {
        viewModelScope.launch {
            repository.deleteContactAlias(id)
        }
    }

    fun setSpeechLanguage(langCode: String) {
        _speechLanguage.value = langCode
        speechEngine?.languageLocale = if (langCode == "hi-IN") Locale("hi", "IN") else Locale("en", "IN")
    }

    fun toggleWakeWord(active: Boolean) {
        _isWakeWordActive.value = active
    }

    fun executeDirectAction(action: AssistantAction) {
        _userTranscript.value = action.rawCommand
        _aiResponse.value = action.feedbackMessage
        speechEngine?.speak(action.feedbackMessage)

        if (repository.isDangerous(action)) {
            _pendingConfirmationAction.value = action
        } else {
            repository.executeSystemAction(action)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechEngine?.destroy()
    }
}
