package de.mannodermaus.blabberl.ui.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.mannodermaus.blabberl.api.TranslationService
import de.mannodermaus.blabberl.models.Language
import de.mannodermaus.blabberl.models.LocalizedText
import de.mannodermaus.blabberl.speech.SpeechEvent
import de.mannodermaus.blabberl.speech.SpeechHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val translationService: TranslationService,
    private val speechHandler: SpeechHandler,
) : ViewModel() {

    data class State(
        val sourceText: String,
        val sourceLanguage: Language,
        val targetText: String,
        val targetLanguage: Language,
        val isSpeaking: Boolean
    )

    private val _state = MutableStateFlow(
        State(
            sourceText = "",
            sourceLanguage = Language.Japanese,
            targetText = "",
            targetLanguage = Language.English,
            isSpeaking = false
        )
    )

    private var translationJob: Job? = null

    val state: StateFlow<State> = _state

    override fun onCleared() {
        super.onCleared()
        translationJob?.cancel()
    }

    fun updateSourceText(text: String) {
        _state.update {
            it.copy(
                sourceText = text
            )
        }

        scheduleTranslation()
    }

    fun swapLanguages() {
        _state.update {
            it.copy(
                sourceLanguage = it.targetLanguage,
                targetLanguage = it.sourceLanguage
            )
        }

        scheduleTranslation()
    }

    fun startSpeechRecognition() {
        speechHandler.recognizeSpeech(_state.value.sourceLanguage)
            .onEach { event ->
                when (event) {
                    is SpeechEvent.SpeechStarted -> {
                        _state.update { it.copy(isSpeaking = true) }
                    }

                    is SpeechEvent.SpeechEnded -> {
                        _state.update { it.copy(isSpeaking = false) }
                    }

                    is SpeechEvent.Results -> {
                        _state.update { it.copy(sourceText = event.text) }
                        scheduleTranslation()
                    }

                    is SpeechEvent.Error -> {
                        Timber.e("Speech recognition error ${event.code}")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /* Private */

    private fun scheduleTranslation() {
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            delay(500L)
            translate()
        }
    }

    private fun translate() {
        val state = _state.value
        val text = state.sourceText
        val from = state.sourceLanguage
        val to = state.targetLanguage

        if (text.isEmpty()) {
            // Nothing to translate, reset all text
            _state.update {
                it.copy(
                    sourceText = "",
                    targetText = ""
                )
            }
            return
        }

        viewModelScope.launch {
            val translation = translationService.translate(
                sourceText = LocalizedText(text, from),
                targetLanguage = to
            )

            _state.update {
                it.copy(
                    sourceText = text,
                    sourceLanguage = from,
                    targetText = translation?.text ?: "",
                    targetLanguage = translation?.language ?: to
                )
            }
        }
    }
}
