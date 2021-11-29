package de.mannodermaus.blabberl.speech

import de.mannodermaus.blabberl.models.Language
import kotlinx.coroutines.flow.Flow

interface SpeechHandler {
    fun recognizeSpeech(language: Language): Flow<SpeechEvent>
}
