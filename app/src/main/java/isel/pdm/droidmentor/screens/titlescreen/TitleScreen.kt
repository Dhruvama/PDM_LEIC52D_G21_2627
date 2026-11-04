package isel.pdm.droidmentor.screens.titlescreen

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import isel.pdm.droidmentor.screens.titlescreen.components.ChatAlignedBust
import isel.pdm.droidmentor.screens.titlescreen.components.StartChatButton
import isel.pdm.droidmentor.screens.titlescreen.components.TitleFootnote
import isel.pdm.droidmentor.screens.titlescreen.components.TitleHeadline
import isel.pdm.droidmentor.screens.titlescreen.components.TitleSubtitle
import isel.pdm.droidmentor.screens.titlescreen.components.TitleTopBar
import isel.pdm.droidmentor.ui.components.animations.particledroid.ParticleDroid
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Time the dots take to (almost fully) form the swirl before navigating to the chat. */
private const val SCATTER_DELAY_MS = 700L

// ---- The chat's empty screen, so the droid lines up with the chat's logo (portrait) ----

/** Chat top bar: 44 dp buttons with 8 dp above and below (below the status bar). */
private val CHAT_TOP_BAR_HEIGHT = 60.dp

/** Lines of the chat composer's empty text field. */
private const val CHAT_COMPOSER_LINES = 2

/** Chat composer without its text: 14 dp padding × 2, 14 dp gap, 52 dp button row, 12 dp margin. */
private val CHAT_COMPOSER_FIXED_HEIGHT = 106.dp

/**
 * Title screen of DroidMentor: the particle droid, the app pitch and the
 * "Start a chat" button. Its only state is the visual transition (dots to swirl).
 *
 * @param onStartChat called when the user starts a chat, after the dots have scattered
 * (immediately if the system has animations turned off).
 * @param onHistory called when the user presses the History button.
 * @param onSettings called when the user presses the Settings button.
 * @param onAbout called when the user presses the About button.
 */
@Composable
fun TitleScreen(
    onStartChat: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    var gathered by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val reducedMotion = remember(context) { isReducedMotion(context) }

    // The droid stays in the swirl while this screen fades out, and re-forms only when
    // the user comes back to it (each time the screen starts again).
    LifecycleStartEffect(Unit) {
        gathered = true
        onStopOrDispose { }
    }

    TitleScreenContent(
        gathered = gathered,
        onStartChatClick = {
            if (reducedMotion) {
                onStartChat()
            } else if (gathered) { // ignore taps while the transition is running
                scope.launch {
                    // Purely visual transition: the dots form the swirl, then we navigate.
                    gathered = false
                    delay(SCATTER_DELAY_MS)
                    onStartChat()
                }
            }
        },
        onHistory = onHistory,
        onSettings = onSettings,
        onAbout = onAbout,
    )
}

private fun isReducedMotion(context: Context): Boolean =
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    ) == 0f

/** Stateless title screen: picks portrait or landscape from the current orientation. */
@Composable
private fun TitleScreenContent(
    gathered: Boolean,
    onStartChatClick: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        TitleLandscape(gathered, onStartChatClick, onHistory, onSettings, onAbout)
    } else {
        TitlePortrait(gathered, onStartChatClick, onHistory, onSettings, onAbout)
    }
}

@Composable
private fun TitlePortrait(
    gathered: Boolean,
    onStartChatClick: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    val composerHeight = with(LocalDensity.current) {
        MaterialTheme.typography.bodyLarge.lineHeight.toDp()
    } * CHAT_COMPOSER_LINES + CHAT_COMPOSER_FIXED_HEIGHT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .glowBackground(LocalAppColors.current),
    ) {
        // The space the chat leaves between its top bar and its composer, where it centers its logo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(top = CHAT_TOP_BAR_HEIGHT, bottom = composerHeight),
            contentAlignment = Alignment.Center,
        ) {
            ChatAlignedBust(gathered)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            TitleTopBar(onHistory = onHistory, onSettings = onSettings, onAbout = onAbout)
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StartChatButton(onClick = onStartChatClick, modifier = Modifier.fillMaxWidth())
                TitleFootnote()
            }
        }
    }
}

@Composable
private fun TitleLandscape(
    gathered: Boolean,
    onStartChatClick: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .glowBackground(LocalAppColors.current)
            .systemBarsPadding(),
    ) {
        TitleTopBar(onHistory = onHistory, onSettings = onSettings, onAbout = onAbout)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                ParticleDroid(onClick = null, gathered = gathered, size = 240.dp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            ) {
                TitleHeadline()
                TitleSubtitle()
                StartChatButton(onClick = onStartChatClick, modifier = Modifier.width(280.dp))
                TitleFootnote()
            }
        }
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun TitleScreenPortraitDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        TitleScreen(onStartChat = {}, onHistory = {}, onSettings = {}, onAbout = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun TitleScreenPortraitLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        TitleScreen(onStartChat = {}, onHistory = {}, onSettings = {}, onAbout = {})
    }
}

@Preview(showBackground = true, widthDp = 740, heightDp = 360)
@Composable
private fun TitleScreenLandscapeDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        TitleScreen(onStartChat = {}, onHistory = {}, onSettings = {}, onAbout = {})
    }
}

@Preview(showBackground = true, widthDp = 740, heightDp = 360)
@Composable
private fun TitleScreenLandscapeLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        TitleScreen(onStartChat = {}, onHistory = {}, onSettings = {}, onAbout = {})
    }
}