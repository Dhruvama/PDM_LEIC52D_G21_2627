package isel.pdm.droidmentor.chathistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.activechat.components.particlelogo.ParticleLogoComponent
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val EMPTY_HISTORY_TAG = "empty_history"

/**
 * Shown when there are no saved chats yet: the particle logo, a short explanation
 * and a button to start the first chat. Scrolls if it doesn't fit (e.g. in landscape).
 *
 * @param onNewChatClick Called when the user taps "New chat".
 */
@Composable
fun EmptyHistory(onNewChatClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .testTag(EMPTY_HISTORY_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        ParticleLogoComponent(size = 180.dp, glow = !c.isLight)
        Text(
            stringResource(R.string.chat_history_empty_title),
            color = c.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.chat_history_empty_body),
            color = c.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        Spacer(Modifier.height(4.dp))
        NewChatButton(onClick = onNewChatClick)
    }
}

@Preview(widthDp = 400, heightDp = 700)
@Composable
private fun EmptyHistoryDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        EmptyHistory(
            onNewChatClick = { },
            modifier = Modifier.fillMaxSize().glowBackground(LocalAppColors.current),
        )
    }
}

@Preview(widthDp = 400, heightDp = 700)
@Composable
private fun EmptyHistoryLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        EmptyHistory(
            onNewChatClick = { },
            modifier = Modifier.fillMaxSize().glowBackground(LocalAppColors.current),
        )
    }
}