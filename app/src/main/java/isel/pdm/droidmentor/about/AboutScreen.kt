package isel.pdm.droidmentor.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.about.components.AboutHeader
import isel.pdm.droidmentor.about.components.AboutTopBar
import isel.pdm.droidmentor.about.components.FeatureRow
import isel.pdm.droidmentor.about.components.GitHubButton
import isel.pdm.droidmentor.about.components.InfoCard
import isel.pdm.droidmentor.about.components.MemberRow
import isel.pdm.droidmentor.about.components.MentorTip
import isel.pdm.droidmentor.ui.components.animations.particledroid.ParticleDroid
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

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
 * @param onBack Called when the user taps the back button.
 */
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onNavigateToGitHub: () -> Unit,
    onBack: () -> Unit = {},
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
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "1.0"
    }
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
            AboutTopBar(onBackClick = onBack)

            // Keeps the cards readable on tablets and in landscape
            val content = Modifier.widthIn(max = 560.dp).fillMaxWidth()

            ParticleDroid(onClick = { tipIndex = (tipIndex % (mentorTips.size - 1)) + 1 })
            MentorTip(mentorTips[tipIndex], content)
            AboutHeader(version)

            Box(modifier = Modifier.frostedGlass(c).padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.about_app_description),
                        color = c.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    FeatureRow(stringResource(R.string.about_feature_byok_title), stringResource(R.string.about_feature_byok_desc))
                    FeatureRow(stringResource(R.string.about_feature_compose_title), stringResource(R.string.about_feature_compose_desc))
                    FeatureRow(stringResource(R.string.about_feature_mentor_title), stringResource(R.string.about_feature_mentor_desc))
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