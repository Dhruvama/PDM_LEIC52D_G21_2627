package isel.pdm.droidmentor.screens.titlescreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/** Test tag of the About button. */
const val ABOUT_BUTTON_TAG = "about_button"

/** Test tag of the History button. */
const val TITLE_HISTORY_BUTTON_TAG = "title_history_button"

/** Test tag of the Settings button. */
const val SETTINGS_BUTTON_TAG = "settings_button"

/**
 * App name on the left, History, Settings and About buttons on the right.
 *
 * @param onHistory Called when the user taps the History button.
 * @param onSettings Called when the user taps the Settings button.
 * @param onAbout Called when the user taps the About button.
 */
@Composable
fun TitleTopBar(
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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

/**
 * 44 dp circular frosted glass button with [icon], used in the top bar.
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
        Icon(imageVector = icon, contentDescription = contentDescription, tint = c.textPrimary)
    }
}

@Preview(widthDp = 400)
@Composable
private fun TitleTopBarDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        TitleTopBar(
            onHistory = { }, onSettings = { }, onAbout = { },
            modifier = Modifier.glowBackground(LocalAppColors.current),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun TitleTopBarLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        TitleTopBar(
            onHistory = { }, onSettings = { }, onAbout = { },
            modifier = Modifier.glowBackground(LocalAppColors.current),
        )
    }
}