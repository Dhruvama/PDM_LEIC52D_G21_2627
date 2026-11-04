package isel.pdm.droidmentor.screens.titlescreen.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.components.animations.particledroid.ParticleDroid
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/** Size of the chat's logo in portrait. */
private val CHAT_LOGO_SIZE = 240.dp

/**
 * Size of the droid in portrait. Its swirl's radius (half of it) must match the chat logo's
 * entry swirl (TITLE_SWIRL_RADIUS_DP in ParticleLogoComponent.kt).
 */
private val TITLE_DROID_SIZE = 300.dp

/**
 * The droid, headline and subtitle, laid out like the chat's empty screen lays out its logo,
 * greeting and suggestions (activechat/components/MessageList.kt). So the droid's swirl is
 * centered exactly where the chat's logo gathers from it, and nothing moves in the cross-fade.
 *
 * @param gathered `true` = the dots form the droid; `false` = they are in the swirl.
 */
@Composable
fun ChatAlignedBust(gathered: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        // The droid is bigger than the chat's logo: it overflows this box, around its center.
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