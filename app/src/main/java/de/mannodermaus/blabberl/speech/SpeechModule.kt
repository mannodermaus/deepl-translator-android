package de.mannodermaus.blabberl.speech

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
class SpeechModule {

    @Provides
    fun speechHandler(@ApplicationContext context: Context): SpeechHandler =
        AndroidSpeechHandler(context)
}
