package de.mannodermaus.blabberl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import de.mannodermaus.blabberl.ui.theme.BlabberlTheme
import de.mannodermaus.blabberl.ui.translate.TranslateScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlabberlTheme {
                TranslateScreen()
            }
        }
    }
}
