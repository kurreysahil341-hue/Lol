package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

sealed class SpeechState {
    object Idle : SpeechState()
    object Listening : SpeechState()
    data class Processing(val text: String) : SpeechState()
    data class Speaking(val text: String) : SpeechState()
    data class Error(val message: String) : SpeechState()
}

class AssistantSpeechEngine(
    private val context: Context,
    private val onResultRecognized: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState

    private val _volumeLevel = MutableStateFlow(0f)
    val volumeLevel: StateFlow<Float> = _volumeLevel

    var languageLocale: Locale = Locale.forLanguageTag("hi-IN")
    var speechRate: Float = 1.0f
    var speechPitch: Float = 1.0f

    init {
        initializeSpeech()
    }

    private fun initializeSpeech() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@AssistantSpeechEngine)
            }
        }
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val result = tts.setLanguage(languageLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.language = Locale.ENGLISH
                }
                tts.setSpeechRate(speechRate)
                tts.setPitch(speechPitch)
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _speechState.value = SpeechState.Speaking("AI Assistant Speaking...")
                    }

                    override fun onDone(utteranceId: String?) {
                        _speechState.value = SpeechState.Idle
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _speechState.value = SpeechState.Idle
                    }
                })
            }
            isTtsReady = true
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            initializeSpeech()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageLocale.toString())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageLocale.toString())
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
            _speechState.value = SpeechState.Listening
        } catch (e: Exception) {
            _speechState.value = SpeechState.Error("Speech initialization error: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _speechState.value = SpeechState.Idle
    }

    fun speak(text: String) {
        if (isTtsReady && text.isNotBlank()) {
            _speechState.value = SpeechState.Speaking(text)
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID_ASSISTANT")
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _speechState.value = SpeechState.Idle
    }

    // RecognitionListener Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _speechState.value = SpeechState.Listening
    }

    override fun onBeginningOfSpeech() {
        _speechState.value = SpeechState.Listening
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Map rmsdB (-2 to 10 approx) to normalized 0f..1f for voice waveform animation
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
        _volumeLevel.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _speechState.value = SpeechState.Processing("Analyzing voice command...")
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record audio permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
            SpeechRecognizer.ERROR_NO_MATCH -> "No voice match found. Try again."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Please speak again."
            else -> "Listening interrupted"
        }
        _speechState.value = SpeechState.Error(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val topMatch = matches?.firstOrNull() ?: ""
        if (topMatch.isNotBlank()) {
            _speechState.value = SpeechState.Processing(topMatch)
            onResultRecognized(topMatch)
        } else {
            _speechState.value = SpeechState.Idle
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull() ?: ""
        if (partial.isNotBlank()) {
            _speechState.value = SpeechState.Processing(partial)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }
}
