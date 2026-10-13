package isel.pdm.droidmentor.about

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glassCard
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

private data class Member(val number: String, val name: String)

private val team = listOf(
    Member("A51517", "Dhruva Adão"),
    Member("A51548", "João Sousa"),
    Member("A51691", "Paulo Vaz"),
)

private val professors = listOf("Prof. Paulo Pereira", "Prof. Diogo Cardoso")

/**
 * About screen: the app, the team, the course and a link to the repository.
 * The particle mascot at the top ([ParticleDroid]) scatters when tapped and gathers back
 * with a new tip.
 *
 * @param onNavigateToGitHub Called when the user taps the repository button.
 */
@Composable
fun AboutScreen(
    onNavigateToGitHub: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val mentorTips = listOf(
        stringResource(R.string.about_tip_1),
        stringResource(R.string.about_tip_2),
        stringResource(R.string.about_tip_3),
        stringResource(R.string.about_tip_4),
        stringResource(R.string.about_tip_5),
        stringResource(R.string.about_tip_6),
        stringResource(R.string.about_tip_7),
    )

    val c = LocalAppColors.current
    var tipIndex by rememberSaveable { mutableIntStateOf(0) }

    CompositionLocalProvider(LocalContentColor provides c.textPrimary) {
        Column(
            modifier
                .fillMaxSize()
                .glowBackground(c)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                // Same 44.dp round glass button as the chat's top bar
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .glass(c, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.about_back_content_description),
                        tint = c.textPrimary
                    )
                }
            }

            // Keeps the cards readable on tablets and in landscape
            val content = Modifier.widthIn(max = 560.dp).fillMaxWidth()

            ParticleDroid(onClick = { tipIndex = (tipIndex % (mentorTips.size - 1)) + 1 })
            MentorTip(mentorTips[tipIndex], content)
            Header()

            Box(modifier = Modifier.glassCard(c).padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.about_app_description),
                        color = c.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Feature(stringResource(R.string.about_feature_byok_title), stringResource(R.string.about_feature_byok_desc))
                    Feature(stringResource(R.string.about_feature_compose_title), stringResource(R.string.about_feature_compose_desc))
                    Feature(stringResource(R.string.about_feature_mentor_title), stringResource(R.string.about_feature_mentor_desc))
                }
            }

            InfoCard(stringResource(R.string.about_developers), content) {
                team.forEach { MemberRow(it) }
            }

            InfoCard(stringResource(R.string.about_course), content) {
                Text(stringResource(R.string.about_course_name), fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.about_course_details), color = c.textSecondary)
                Spacer(Modifier.size(4.dp))
                Text(stringResource(R.string.about_professors), style = MaterialTheme.typography.labelLarge, color = c.accent)
                professors.forEach { Text(it, color = c.textSecondary) }
            }

            GitHubButton(onNavigateToGitHub)

            Text(
                stringResource(R.string.about_footer),
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary
            )
        }
    }
}

/** App name in the accent gradient, tagline and version. */
@Composable
private fun Header() {
    val c = LocalAppColors.current
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "1.0"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "DroidMentor",
            style = MaterialTheme.typography.displaySmall.merge(TextStyle(brush = c.crossGradient)),
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.about_header_subtitle),
            color = c.textSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            "v$version",
            style = MaterialTheme.typography.labelMedium,
            color = c.accent,
            modifier = Modifier
                .glass(c, CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/** Speech bubble with the mascot's current tip. */
@Composable
private fun MentorTip(tip: String, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    AnimatedContent(
        targetState = tip,
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 3 }) togetherWith
                    (fadeOut() + slideOutVertically { -it / 3 })
        },
        modifier = modifier,
        label = "tip"
    ) { text ->
        Text(
            text,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .glass(c, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalAppColors.current
    Column(
        modifier
            .glassCard(c)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

/** A feature with its label in a small accent pill. */
@Composable
private fun Feature(label: String, description: String) {
    val c = LocalAppColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = c.onAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 72.dp)
                .clip(CircleShape)
                .background(c.accent)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        Text(description, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
    }
}

/** Avatar with the initials over the accent gradient, name and student number. */
@Composable
private fun MemberRow(member: Member) {
    val c = LocalAppColors.current
    val initials = member.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(c.crossGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = c.onCrossGradient, fontWeight = FontWeight.Bold)
        }
        Column {
            Text(member.name, fontWeight = FontWeight.SemiBold)
            Text(member.number, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
    }
}

@Composable
private fun GitHubButton(onClick: () -> Unit) {
    val c = LocalAppColors.current
    Text(
        stringResource(R.string.about_github_button),
        color = c.accent,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .glass(c, CircleShape, halo = true)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp)
    )
}

@Composable
private fun AboutPreviewContent(colors: AppColors) {
    DroidMentorTheme(colors = colors) {
        AboutScreen(onNavigateToGitHub = {})
    }
}

@Preview(heightDp = 1400)
@Composable
fun AboutScreenDarkPreview() = AboutPreviewContent(DefaultDarkColors)

@Preview(heightDp = 1400)
@Composable
fun AboutScreenLightPreview() = AboutPreviewContent(DefaultLightColors)

@Preview(heightDp = 1400)
@Composable
fun AboutScreenVioletPreview() = AboutPreviewContent(PresetThemes[1])