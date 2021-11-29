package de.mannodermaus.blabberl.api

import de.mannodermaus.blabberl.models.Language
import de.mannodermaus.blabberl.models.LocalizedText

interface TranslationService {
    /**
     *
     */
    suspend fun translate(sourceText: LocalizedText, targetLanguage: Language): LocalizedText?
}
