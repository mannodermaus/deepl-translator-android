package de.mannodermaus.blabberl.ui.translate

import android.Manifest
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import de.mannodermaus.blabberl.R
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun TranslateScreen(viewModel: TranslateViewModel = hiltViewModel()) {
    Scaffold(
        topBar = {
            TranslateTopBar()
        }) {
        TranslateContent(viewModel)
    }
}

@Composable
private fun TranslateTopBar() {
    TopAppBar(title = {
        Text(stringResource(R.string.app_name))
    })
}

@Composable
private fun TranslateContent(viewModel: TranslateViewModel) {
    val vmState by viewModel.state.collectAsState()

    val currentSourceText = vmState.sourceText
    val currentSourceLanguage = vmState.sourceLanguage
    val currentTargetLanguage = vmState.targetLanguage

    Column {
        LanguageTextField(
            modifier = Modifier.weight(1f),
            value = currentSourceText,
            onValueChange = { newText ->
                viewModel.updateSourceText(newText)
            },
            hint = stringResource(R.string.hint_enter_text),
            overlayContent = {
                if (vmState.isSpeaking) {
                    AnimatedIcon(
                        modifier = Modifier.align(Alignment.Center).size(128.dp),
                        tint = Color(0x40FFFFFF),
                        icons = listOf(
                            painterResource(R.drawable.ic_volume_low),
                            painterResource(R.drawable.ic_volume_medium),
                            painterResource(R.drawable.ic_volume_high),
                        ),
                        contentDescription = ""
                    )
                }
            }
        )

        LanguageTextField(
            modifier = Modifier.weight(1f),
            value = vmState.targetText,
            editable = false
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(modifier = Modifier.weight(1f), onClick = { }) {
                    Text(currentSourceLanguage.toString())
                }

                Button(onClick = { viewModel.swapLanguages() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_swap),
                        contentDescription = stringResource(R.string.cd_swap_languages)
                    )
                }

                Button(modifier = Modifier.weight(1f), onClick = { }) {
                    Text(currentTargetLanguage.toString())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MicrophoneButton(onClick = {
                viewModel.startSpeechRecognition()
            })
        }
    }
}

@Composable
private fun LanguageTextField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    editable: Boolean = true,
    hint: String = "",
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    val style = MaterialTheme.typography.h4

    Box(modifier = modifier) {
        TextField(
            modifier = Modifier.fillMaxSize(),
            textStyle = style,
            value = value,
            onValueChange = onValueChange,
            readOnly = !editable,
            placeholder = {
                Text(text = hint, style = style)
            }
        )

//        overlayContent()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MicrophoneButton(
    onClick: () -> Unit
) {
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    FloatingActionButton(
        backgroundColor = MaterialTheme.colors.primary,
        onClick = {
            if (audioPermissionState.hasPermission) {
                onClick()
            } else if (audioPermissionState.shouldShowRationale || !audioPermissionState.permissionRequested) {
                audioPermissionState.launchPermissionRequest()
            } else {
                Timber.w("Feature not available") // TODO Dialog box redirecting to settings
            }
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_microphone),
            contentDescription = stringResource(R.string.cd_voice_input)
        )
    }
}

@Composable
private fun AnimatedIcon(
    icons: List<Painter>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    frameDurationMs: Int = 500,
) {
    if (icons.isEmpty()) return

    val animatedIconFloat by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = icons.size - 1f,
        animationSpec = infiniteRepeatable(tween(frameDurationMs, easing = LinearEasing))
    )
    val icon = animatedIconFloat.roundToInt()

    Icon(
        modifier = modifier,
        tint = tint,
        painter = icons[icon],
        contentDescription = contentDescription,
    )
}
