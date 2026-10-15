package isel.pdm.droidmentor.about.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.about.Member
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/**
 * A team member: avatar with the initials over the accent gradient, name and student number.
 *
 * @param member The member to show.
 */
@Composable
fun MemberRow(member: Member, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val initials = member.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
    Row(
        modifier = modifier,
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
            Text(member.name, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
            Text(member.number, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun MemberRowDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MemberRow(
            member = previewMember,
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun MemberRowLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        MemberRow(
            member = previewMember,
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

private val previewMember = Member("A51517", "Dhruva Adão")