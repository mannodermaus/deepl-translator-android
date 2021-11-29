package de.mannodermaus.blabberl.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import de.mannodermaus.blabberl.models.Language
import io.michaelrocks.bimap.HashBiMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class AndroidSpeechHandler(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : SpeechHandler {

    private val recognizer = createSpeechRecognizer(context)

    private val languageCodes = HashBiMap.create(Language.values().associateWith {
        when (it) {
            Language.Japanese -> "ja-JP"
            Language.English -> "en-US"
            Language.German -> "de-DE"
        }
    })

    override fun recognizeSpeech(language: Language): Flow<SpeechEvent> =
        callbackFlow {
            val listener = object : RecognitionListenerAdapter() {
                override fun onBeginningOfSpeech() {
                    trySend(SpeechEvent.SpeechStarted)
                }

                override fun onEndOfSpeech() {
                    trySend(SpeechEvent.SpeechEnded)
                }

                override fun onPartialResults(partialResults: Bundle) {
                    // Send down the first result
                    partialResults
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.run { trySend(SpeechEvent.Results(this, final = false)) }
                }

                override fun onResults(results: Bundle) {
                    results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.run { trySend(SpeechEvent.Results(this, final = true)) }
                    close()
                }

                override fun onError(error: Int) {
                    trySend(SpeechEvent.Error(error))
                    close()
                }
            }

            recognizer.setRecognitionListener(listener)
            recognizer.startListening(Intent().apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            })
            awaitClose { recognizer.stopListening() }

        }.flowOn(dispatcher).distinctUntilChanged()

    /* Private */

    private val Language.code
        get() = languageCodes.getValue(this)

    private fun createSpeechRecognizer(context: Context): SpeechRecognizer =
        if (Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
            Timber.d("Create On-Device Speech Recognizer")
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            Timber.d("Create Normal Speech Recognizer")
            SpeechRecognizer.createSpeechRecognizer(context)
        }

    private abstract class RecognitionListenerAdapter : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            /* No-op */
        }

        override fun onBeginningOfSpeech() {
            /* No-op */
        }

        override fun onRmsChanged(rmsdB: Float) {
            /* No-op */
        }

        override fun onBufferReceived(buffer: ByteArray) {
            /* No-op */
        }

        override fun onEndOfSpeech() {
            /* No-op */
        }

        override fun onError(error: Int) {
            /* No-op */
        }

        override fun onResults(results: Bundle) {
            /* No-op */
        }

        override fun onPartialResults(partialResults: Bundle) {
            /* No-op */
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            /* No-op */
        }
    }
}
