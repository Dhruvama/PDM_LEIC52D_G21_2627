package isel.pdm.droidmentor.titlescreen

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.components.animations.particledroid.ParticleDroid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Settings

/** Test tag of the "Start a chat" button. */
const val START_CHAT_BUTTON_TAG = "start_chat_button"

/** Test tag of the About button. */
const val ABOUT_BUTTON_TAG = "about_button"

/** Test tag of the History button. */
const val TITLE_HISTORY_BUTTON_TAG = "title_history_button"

/** Test tag of the Settings button. */
const val SETTINGS_BUTTON_TAG = "settings_button"

/** Time the dots take to (almost fully) form the swirl before navigating to the chat. */
private const val SCATTER_DELAY_MS = 700L

// ---- The chat's empty screen, so the bust lines up with the chat's logo (portrait) ----

/** Chat top bar: 44 dp buttons with 8 dp above and below (below the status bar). */
private val CHAT_TOP_BAR_HEIGHT = 60.dp

/** Size of the chat's logo in portrait. */
private val CHAT_LOGO_SIZE = 240.dp

/**
 * Size of the droid in portrait. Its swirl's radius (half of it) must match the chat logo's
 * entry swirl (TITLE_SWIRL_RADIUS_DP in ParticleLogoComponent.kt).
 */
private val TITLE_DROID_SIZE = 300.dp

/** Lines of the chat composer's empty text field. */
private const val CHAT_COMPOSER_LINES = 2

/** Chat composer without its text: 14 dp padding × 2, 14 dp gap, 52 dp button row, 12 dp margin. */
private val CHAT_COMPOSER_FIXED_HEIGHT = 106.dp

// ---------------------------------------------------------------------------
// Public root: owns the one piece of local UI state
// ---------------------------------------------------------------------------

/**
 * Title screen of DroidMentor: the particle bust, the app pitch and the
 * "Start a chat" button.
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

// ---------------------------------------------------------------------------
// Private stateless content
// ---------------------------------------------------------------------------

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
            TitleTopRow(onHistory = onHistory, onSettings = onSettings, onAbout = onAbout)
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

/**
 * The bust, headline and subtitle, laid out like the chat's empty screen lays out its logo,
 * greeting and suggestions (activechat/components/MessageList.kt). So the bust's swirl is
 * centered exactly where the chat's logo gathers from it, and nothing moves in the cross-fade.
 */
@Composable
private fun ChatAlignedBust(gathered: Boolean) {
    Column(
        modifier = Modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        // The bust is bigger than the chat's logo: it overflows this box, around its center.
        Box(Modifier.size(CHAT_LOGO_SIZE), contentAlignment = Alignment.Center) {
            ParticleDroid(
                onClick = null,
                gathered = gathered,
                modifier = Modifier.requiredSize(TITLE_DROID_SIZE),
                size = TITLE_DROID_SIZE,
            )
        }
        Box(contentAlignment = Alignment.TopCenter) {
            ChatWelcomeSpace()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TitleHeadline()
                TitleSubtitle()
            }
        }
    }
}

/**
 * Takes the same space as the chat's greeting and suggestion chips, but draws nothing and is
 * hidden from accessibility services.
 */
@Composable
private fun ChatWelcomeSpace() {
    // Every greeting fits in one line, so the longest one gives the same height.
    val greeting = stringResource(R.string.welcome_good_afternoon) + "\n" +
            stringResource(R.string.welcome_question)
    val suggestions = listOf(
        stringResource(R.string.suggestion_review_viewmodel),
        stringResource(R.string.suggestion_explain_recomposition),
        stringResource(R.string.suggestion_room_or_datastore),
    )
    Column(
        modifier = Modifier
            .drawWithContent { }
            .clearAndSetSemantics { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = suggestion, style = MaterialTheme.typography.labelLarge)
                }
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
        TitleTopRow(onHistory = onHistory, onSettings = onSettings, onAbout = onAbout)

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

// ---------------------------------------------------------------------------
// Small named pieces
// ---------------------------------------------------------------------------

/** App name on the left, History, Settings and About buttons on the right. */
@Composable
private fun TitleTopRow(onHistory: () -> Unit, onSettings: () -> Unit, onAbout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppName()
        Spacer(Modifier.weight(1f))
        TitleIconButton(
            icon = Icons.AutoMirrored.Filled.List,
            contentDescription = stringResource(R.string.chat_history_title),
            testTag = TITLE_HISTORY_BUTTON_TAG,
            onClick = onHistory,
        )
        TitleIconButton(
            icon = Icons.Default.Settings,
            contentDescription = stringResource(R.string.title_activity_settings),
            testTag = SETTINGS_BUTTON_TAG,
            onClick = onSettings,
        )
        TitleIconButton(
            icon = Icons.Default.Info,
            contentDescription = stringResource(R.string.title_about_description),
            testTag = ABOUT_BUTTON_TAG,
            onClick = onAbout,
        )
    }
}

/**
 * 44 dp circular frosted glass button with [icon], used in the top row.
 *
 * @param icon The icon shown in the middle.
 * @param contentDescription Read by accessibility services, since the button has no text.
 * @param testTag Tag used to find the button in tests.
 * @param onClick Called when the user taps the button.
 */
@Composable
private fun TitleIconButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .size(44.dp)
            .frostedGlass(c, CircleShape)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = c.textPrimary,
        )
    }
}

/** "DroidMentor" where "Mentor" is highlighted with the accent gradient. */
@Composable
private fun AppName() {
    val c = LocalAppColors.current
    // stringResource is composable, so the strings are read before building the text.
    val first = stringResource(R.string.title_app_name_first)
    val second = stringResource(R.string.title_app_name_second)
    val name = buildAnnotatedString {
        append(first)
        withStyle(SpanStyle(brush = c.crossGradient)) { append(second) }
    }
    Text(
        text = name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = c.textPrimary,
    )
}


@Composable
private fun TitleHeadline() {
    Text(
        text = stringResource(R.string.title_headline),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.textPrimary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun TitleSubtitle() {
    Text(
        text = stringResource(R.string.title_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = LocalAppColors.current.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = 280.dp),
    )
}

/** Pill-shaped glass button with an accent halo. The caller decides its width. */
@Composable
private fun StartChatButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .height(58.dp)
            .frostedGlass(c, CircleShape)
            .glass(c, CircleShape)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .testTag(START_CHAT_BUTTON_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.title_start_chat),
                style = MaterialTheme.typography.titleMedium,
                color = c.accent,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = c.accent,
            )
        }
    }
}

@Composable
private fun TitleFootnote() {
    Text(
        text = stringResource(R.string.title_footnote),
        style = MaterialTheme.typography.labelSmall,
        color = LocalAppColors.current.textSecondary,
        textAlign = TextAlign.Center,
    )
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
private fun ChatAlignedBustDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current), contentAlignment = Alignment.Center) {
            ChatAlignedBust(gathered = true)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
private fun ChatAlignedBustLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current), contentAlignment = Alignment.Center) {
            ChatAlignedBust(gathered = true)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatWelcomeSpacePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        // Draws nothing on purpose: the border shows the space it takes.
        Box(Modifier.border(1.dp, LocalAppColors.current.border)) {
            ChatWelcomeSpace()
        }
    }
}

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