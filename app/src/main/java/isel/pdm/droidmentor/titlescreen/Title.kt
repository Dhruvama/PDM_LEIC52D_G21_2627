package isel.pdm.droidmentor.titlescreen

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
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
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Test tag of the "Start a chat" button. */
const val START_CHAT_BUTTON_TAG = "start_chat_button"

/** Test tag of the About button. */
const val ABOUT_BUTTON_TAG = "about_button"

/** Time the dots take to (almost fully) form the swirl before navigating to the chat. */
private const val SCATTER_DELAY_MS = 700L

// ---- The chat's empty screen, so the bust lines up with the chat's logo (portrait) ----

/** Chat top bar: 44 dp buttons with 8 dp above and below (below the status bar). */
private val CHAT_TOP_BAR_HEIGHT = 60.dp

/** Size of the chat's logo in portrait. */
private val CHAT_LOGO_SIZE = 240.dp

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
 * @param onAbout called when the user presses the About button.
 * @param background brush painted behind the whole screen. Defaults to the theme background.
 */
@Composable
fun TitleScreen(
    onStartChat: () -> Unit,
    onAbout: () -> Unit,
    background: Brush = SolidColor(MaterialTheme.colorScheme.background),
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
        onAbout = onAbout,
        background = background,
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
    onAbout: () -> Unit,
    background: Brush,
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        TitleLandscape(gathered, onStartChatClick, onAbout, background)
    } else {
        TitlePortrait(gathered, onStartChatClick, onAbout, background)
    }
}

@Composable
private fun TitlePortrait(
    gathered: Boolean,
    onStartChatClick: () -> Unit,
    onAbout: () -> Unit,
    background: Brush,
) {
    val composerHeight = with(LocalDensity.current) {
        MaterialTheme.typography.bodyLarge.lineHeight.toDp()
    } * CHAT_COMPOSER_LINES + CHAT_COMPOSER_FIXED_HEIGHT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
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
            TitleTopRow(onAbout = onAbout)
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
            ParticleBust(gathered, Modifier.requiredSize(300.dp, 330.dp))
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
    onAbout: () -> Unit,
    background: Brush,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .systemBarsPadding(),
    ) {
        TitleTopRow(onAbout = onAbout)

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
                ParticleBust(gathered, Modifier.size(240.dp, 264.dp))
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

/** App name on the left, About button on the right. */
@Composable
private fun TitleTopRow(onAbout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppName()
        Spacer(Modifier.weight(1f))
        AboutButton(onClick = onAbout)
    }
}

/** "DroidMentor" where "Mentor" is highlighted with the primary color. */
@Composable
private fun AppName() {
    // stringResource is composable, so the strings are read before building the text.
    val first = stringResource(R.string.title_app_name_first)
    val second = stringResource(R.string.title_app_name_second)
    val highlight = MaterialTheme.colorScheme.primary
    val name = buildAnnotatedString {
        append(first)
        withStyle(SpanStyle(color = highlight)) { append(second) }
    }
    Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

/** 44 dp circular button with an info icon. */
@Composable
private fun AboutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .testTag(ABOUT_BUTTON_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = stringResource(R.string.title_about_description),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TitleHeadline() {
    Text(
        text = stringResource(R.string.title_headline),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun TitleSubtitle() {
    Text(
        text = stringResource(R.string.title_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = 280.dp),
    )
}

/** Pill-shaped gradient button. The caller decides its width. */
@Composable
private fun StartChatButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val brush = Brush.linearGradient(listOf(colors.tertiary, colors.primary))
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(CircleShape)
            .background(brush)
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
                color = colors.onPrimary,
            )
            // Decorative: the button text already says what it does.
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colors.onPrimary,
            )
        }
    }
}

@Composable
private fun TitleFootnote() {
    Text(
        text = stringResource(R.string.title_footnote),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
private fun ChatAlignedBustPreview() {
    DroidMentorTheme(dynamicColor = false) {
        Box(contentAlignment = Alignment.Center) {
            ChatAlignedBust(gathered = true)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatWelcomeSpacePreview() {
    DroidMentorTheme(dynamicColor = false) {
        // Draws nothing on purpose: the border shows the space it takes.
        Box(Modifier.border(1.dp, MaterialTheme.colorScheme.outline)) {
            ChatWelcomeSpace()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun TitleScreenPortraitPreview() {
    DroidMentorTheme(dynamicColor = false) {
        TitleScreen(onStartChat = {}, onAbout = {})
    }
}

@Preview(showBackground = true, widthDp = 740, heightDp = 360)
@Composable
private fun TitleScreenLandscapePreview() {
    DroidMentorTheme(dynamicColor = false) {
        TitleScreen(onStartChat = {}, onAbout = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun TitleScreenGradientPreview() {
    DroidMentorTheme(dynamicColor = false) {
        TitleScreen(
            onStartChat = {},
            onAbout = {},
            background = Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
            ),
        )
    }
}