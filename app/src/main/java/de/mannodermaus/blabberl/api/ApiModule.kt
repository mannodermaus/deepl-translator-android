package de.mannodermaus.blabberl.api

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class ApiModule {
    @Provides
    fun moshi(): Moshi = Moshi.Builder().build()

    @Provides
    fun translationService(moshi: Moshi): TranslationService = DeepLTranslationService(moshi)
}
