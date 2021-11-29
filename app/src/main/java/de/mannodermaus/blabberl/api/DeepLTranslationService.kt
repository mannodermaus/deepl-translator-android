package de.mannodermaus.blabberl.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import de.mannodermaus.blabberl.BuildConfig
import de.mannodermaus.blabberl.models.Language
import de.mannodermaus.blabberl.models.LocalizedText
import io.michaelrocks.bimap.HashBiMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import timber.log.Timber
import javax.inject.Inject

class DeepLTranslationService(
    moshi: Moshi,
    private val authKey: String = BuildConfig.DEEPL_AUTH_KEY,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TranslationService {

    private val retrofitService = Retrofit.Builder()
        .baseUrl("https://api-free.deepl.com/v2/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create<DeepLRetrofitService>()

    private val languageCodes = HashBiMap.create(Language.values().associateWith {
        when (it) {
            Language.Japanese -> "JA"
            Language.English -> "EN"
            Language.German -> "DE"
        }
    })

    override suspend fun translate(
        sourceText: LocalizedText,
        targetLanguage: Language
    ): LocalizedText? = withContext(dispatcher) {
        val sourceCode = sourceText.language.code
        val targetCode = targetLanguage.code

        val response = runCatching {
            retrofitService.translate(
                authKey = authKey,
                sourceLanguage = sourceCode,
                targetLanguage = targetCode,
                text = sourceText.text
            )
        }

        response.exceptionOrNull()?.let {
            Timber.e(it, "Translation error")
        }

        response.getOrNull()
            ?.translations
            ?.firstOrNull()
            ?.text
            ?.run { LocalizedText(this, targetLanguage) }
    }

    /* Private */

    private val Language.code
        get() = languageCodes.getValue(this)

    /* Inner classes */

    interface DeepLRetrofitService {
        @FormUrlEncoded
        @POST("translate")
        suspend fun translate(
            @Field("auth_key") authKey: String,
            @Field("source_lang") sourceLanguage: String,
            @Field("target_lang") targetLanguage: String,
            @Field("text") text: String
        ): DeepLTranslationResponse
    }

    @JsonClass(generateAdapter = true)
    data class DeepLTranslationResponse(
        @Json(name = "translations")
        val translations: List<DeepLTranslation>
    )

    @JsonClass(generateAdapter = true)
    data class DeepLTranslation(
        @Json(name = "text")
        val text: String
    )
}
