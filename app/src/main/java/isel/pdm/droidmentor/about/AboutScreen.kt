package isel.pdm.droidmentor.about

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glassCard
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private data class Member(val number: String, val name: String)

private val team = listOf(
    Member("A51517", "Dhruva Adão"),
    Member("A51548", "João Sousa"),
    Member("A51691", "Paulo Vaz"),
)

private val professors = listOf("Prof. Paulo Pereira", "Prof. Diogo Cardoso")

/** What the mascot says each time it is tapped. */
private val mentorTips = listOf(
    "Psttt, carrega aqui pá ",
    "Trabalho pesado não é na main thread: usa coroutines e Dispatchers.IO.",
    "State hoisting: quem tem o estado decide, quem desenha só mostra.",
    "Um composable deve ser idempotente e sem efeitos colaterais.",
    "A tua API key é tua: nunca a ponhas no código nem no Git.",
    "Põe Log.v nos callbacks do ciclo de vida para veres quando a Activity é recriada.",
    "IMPORTANTE: Não te esqueças de jogar elden ring",
)

/**
 * About screen: the app, the team, the course and a link to the repository.
 * The mascot at the top floats, blinks and gives a new tip every time it is tapped.
 *
 * @param onNavigateToGitHub Called when the user taps the repository button.
 */
@Composable
fun AboutScreen(
    onNavigateToGitHub: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
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
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = c.textSecondary
                    )
                }
            }

            // Keeps the cards readable on tablets and in landscape
            val content = Modifier.widthIn(max = 560.dp).fillMaxWidth()

            DroidMascot(onClick = { tipIndex = (tipIndex % (mentorTips.size - 1)) + 1 })
            MentorTip(mentorTips[tipIndex], content)
            Header()

            Box(modifier = Modifier.glassCard(c).padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Um cliente LLM para Android que te ajuda a pensar como " +
                                "dev sénior Android, utilizando a API REST do Gemini.",
                        color = c.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Feature("BYOK", "Utiliza a tua chave do Gemini.")
                    Feature("Compose", "Interface 100% Jetpack Compose, com temas à tua medida.")
                    Feature("Mentor", "Respostas pensadas para ajudar no desenvolvimento Android.")
                }
            }

            InfoCard("Desenvolvedores", content) {
                team.forEach { MemberRow(it) }
            }

            InfoCard("Unidade curricular", content) {
                Text("Programação em Dispositivos Móveis", fontWeight = FontWeight.SemiBold)
                Text("LEIC · ISEL · Semestre de inverno 2026/2027", color = c.textSecondary)
                Spacer(Modifier.size(4.dp))
                Text("Docentes", style = MaterialTheme.typography.labelLarge, color = c.accent)
                professors.forEach { Text(it, color = c.textSecondary) }
            }

            GitHubButton(onNavigateToGitHub)

            Text(
                "Feito com ☕ e Kotlin no ISEL",
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
            "O teu mentor sénior de Android, no bolso.",
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

/**
 * Android robot head drawn by hand with the accent gradient.
 * It floats over a pulsing glow, blinks every few seconds and bounces when tapped.
 */
@Composable
private fun DroidMascot(onClick: () -> Unit, size: Dp = 132.dp) {
    val c = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val bounce = remember { Animatable(1f) }

    val transition = rememberInfiniteTransition(label = "mascot")
    val float by transition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "float"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "pulse"
    )
    // Eyes stay open most of the time and close quickly once per cycle
    val eyeOpen by transition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 3600
                1f at 3300 using LinearEasing
                0.1f at 3420 using LinearEasing
                1f at 3540
            }
        ),
        label = "blink"
    )

    Box(
        Modifier
            .size(size)
            .semantics { contentDescription = "Mascote do DroidMentor. Toca para uma dica." }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button
            ) {
                onClick()
                scope.launch {
                    bounce.snapTo(0.85f)
                    bounce.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Glow behind the head
            drawCircle(
                Brush.radialGradient(
                    listOf(c.accent.copy(alpha = 0.35f * pulse), Color.Transparent),
                    center = center,
                    radius = this.size.minDimension / 2
                )
            )
            // Shadow on the "floor", smaller when the head is higher
            val shadowWidth = this.size.width * (0.42f + float / 100f)
            drawOval(
                c.textPrimary.copy(alpha = 0.12f),
                topLeft = Offset(center.x - shadowWidth / 2, this.size.height * 0.84f),
                size = Size(shadowWidth, this.size.height * 0.05f)
            )
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = float.dp.toPx()
                    scaleX = bounce.value
                    scaleY = bounce.value
                }
        ) {
            val radius = this.size.width * 0.34f
            val headCenter = Offset(center.x, this.size.height * 0.72f)

            // Antennae, one in each accent
            listOf(-120f to c.bottomAccent, -60f to c.topAccent).forEach { (deg, color) ->
                val a = Math.toRadians(deg.toDouble())
                fun at(r: Float) = headCenter + Offset(r * cos(a).toFloat(), r * sin(a).toFloat())
                drawLine(
                    color, at(radius * 0.85f), at(radius * 1.3f),
                    strokeWidth = radius * 0.09f, cap = StrokeCap.Round
                )
            }

            // Head: top half of a circle
            drawArc(
                brush = c.crossGradient,
                startAngle = 180f, sweepAngle = 180f, useCenter = true,
                topLeft = headCenter - Offset(radius, radius),
                size = Size(radius * 2, radius * 2)
            )

            // Eyes
            val eye = Size(radius * 0.18f, radius * 0.18f * eyeOpen)
            listOf(-1f, 1f).forEach { side ->
                val eyeCenter = headCenter + Offset(side * radius * 0.42f, -radius * 0.45f)
                drawOval(
                    c.onCrossGradient,
                    topLeft = eyeCenter - Offset(eye.width / 2, eye.height / 2),
                    size = eye
                )
            }
        }
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
        "Ver código no GitHub  ↗",
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