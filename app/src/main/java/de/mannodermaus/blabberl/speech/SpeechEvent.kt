package de.mannodermaus.blabberl.speech

sealed class SpeechEvent {
    object SpeechStarted : SpeechEvent()
    object SpeechEnded : SpeechEvent()
    data class Results(val text: String, val final: Boolean) : SpeechEvent()
    data class Error(val code: Int) : SpeechEvent()
}
