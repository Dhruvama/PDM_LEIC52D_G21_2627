package isel.pdm.droidmentor.activechat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass

const val BACK_BUTTON_TAG = "back_button"
const val HISTORY_BUTTON_TAG = "history_button"
const val OPTIONS_BUTTON_TAG = "options_button"

/**
 * Top bar of the Active Chat screen: a round back button, a pill with the mentor's
 * avatar and the chat title, a round history button and a round options button.
 *
 * The bar itself has no background; each of its parts has its own surface,
 * so it can sit on top of any screen background.
 *
 * @param title Title of the chat. Cut with an ellipsis if it does not fit in one line.
 * @param onBackClick Called when the user taps the back button.
 * @param onHistoryClick Called when the user taps the history button.
 * @param onSettingsClick Called when the user taps the options button.
 * @param modifier Modifier applied to the bar.
 */
@Composable
fun ChatTopBarComponent(
    title: String,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ComponentRoundIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.chat_top_bar_back),
            onClick = onBackClick,
            testTag = BACK_BUTTON_TAG,
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            ComponentTitlePill(title = title)
        }
        ComponentRoundIconButton(
            icon = Icons.AutoMirrored.Filled.List,
            contentDescription = stringResource(R.string.chat_top_bar_history),
            onClick = onHistoryClick,
            testTag = HISTORY_BUTTON_TAG,
        )
        ComponentRoundIconButton(
            icon = Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.chat_top_bar_options),
            onClick = onSettingsClick,
            testTag = OPTIONS_BUTTON_TAG,
        )
    }
}

/**
 * A 44.dp round glass button.
 */
@Composable
private fun ComponentRoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String,
) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .size(44.dp)
            .frostedGlass(colors, CircleShape),
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .testTag(testTag),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = colors.textPrimary,
            )
        }
    }
}

/**
 * Glass pill with the mentor's avatar and the chat title in one line. Not clickable.
 */
@Composable
private fun ComponentTitlePill(title: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .height(44.dp)
            .frostedGlass(colors, CircleShape)
            .padding(start = 6.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MentorAvatarComponent()
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun ChatTopBarComponentShortTitlePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatTopBarComponent(
            title = previewShortTitle,
            onBackClick = { },
            onHistoryClick = { },
            onSettingsClick = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatTopBarComponentLongTitlePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatTopBarComponent(
            title = previewLongTitle,
            onBackClick = { },
            onHistoryClick = { },
            onSettingsClick = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentRoundIconButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentRoundIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.chat_top_bar_back),
            onClick = { },
            testTag = BACK_BUTTON_TAG,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentTitlePillPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentTitlePill(title = previewShortTitle)
    }
}

private const val previewShortTitle = "Compose state"

private const val previewLongTitle =
    "Why does my LazyColumn recompose every item when I add a single message at the bottom?"