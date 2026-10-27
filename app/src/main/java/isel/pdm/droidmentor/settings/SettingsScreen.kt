package isel.pdm.droidmentor.settings

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.components.HsvColorPicker
import isel.pdm.droidmentor.ui.components.SendButton
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.BackgroundLimits
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.GlowSpec
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import isel.pdm.droidmentor.ui.theme.systemAppColors
import isel.pdm.droidmentor.ui.theme.toBackground
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

/** Gemini models the user can pick. Id sent to the API and the name shown. */
// TODO confirmar os modelos disponíveis na API do Gemini quando se fizer o cliente REST
private val geminiModels = listOf(
    "gemini-2.5-flash" to "Flash",
    "gemini-2.5-pro" to "Pro",
)

/** How deep the mentor goes in its answers. */
enum class MentorLevel(val label: String) {
    BEGINNER("Iniciante"),
    INTERMEDIATE("Intermédio"),
    SENIOR("Avançado"),
}

/**
 * Root of the Settings screen: connects [viewModel] to the stateless screen content.
 *
 * @param viewModel Holds the colors and handles the user's actions.
 * @param onGetApiKey Called when the user wants to create a key (opens Google AI Studio).
 * @param onBack Called when the user taps the back button.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onGetApiKey: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = remember(viewModel, onGetApiKey, onBack) {
        object : SettingsActions {
            override fun onColorsChange(colors: AppColors) = viewModel.changeColors(colors)
            override fun onColorsFinished() = viewModel.saveColors()
            override fun onTopLinkedChange(linked: Boolean) = viewModel.changeTopLinked(linked)
            override fun onBottomLinkedChange(linked: Boolean) = viewModel.changeBottomLinked(linked)
            override fun onPresetSelect(preset: AppColors) = viewModel.applyPreset(preset)
            override fun onUseSystemColors() = viewModel.useSystemColors()
            // `.invoke()` calls the screen's parameters, not this object's own functions.
            override fun onGetApiKey() = onGetApiKey.invoke()
            override fun onBack() = onBack.invoke()
        }
    }
    SettingsScreenContent(state = viewModel.state, actions = actions, modifier = modifier)
}

/**
 * Settings screen: Gemini API key and model, how the mentor answers and the app colors.
 * The whole screen uses the colors being edited, so the theme changes show live.
 * Custom colors are edited in a separate full-screen editor ([ColorEditor]),
 * so the preview stays visible while the colors change.
 *
 * Stateless: every user action goes to [actions].
 *
 * @param state What the screen shows.
 * @param actions What happens when the user interacts with the screen.
 * @param modifier Modifier for the screen's root.
 */
@Composable
private fun SettingsScreenContent(
    state: SettingsScreenState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    val system = systemAppColors()
    val draft = state.colors ?: system

    // TODO carregar a API key guardada (de forma segura, p.ex. cifrada)
    var apiKey by rememberSaveable { mutableStateOf("") }
    // TODO carregar o modelo, o nível e as preferências do mentor guardados
    var model by rememberSaveable { mutableStateOf(geminiModels.first().first) }
    var level by rememberSaveable { mutableStateOf(MentorLevel.INTERMEDIATE) }
    var codeExamples by rememberSaveable { mutableStateOf(true) }
    var shortAnswers by rememberSaveable { mutableStateOf(false) }

    var editingColors by rememberSaveable { mutableStateOf(false) }
    // Hoisted so the settings keep their scroll position after closing the editor
    val settingsScroll = rememberScrollState()

    DroidMentorTheme(colors = draft) {
        val c = LocalAppColors.current
        CompositionLocalProvider(LocalContentColor provides c.textPrimary) {
            AnimatedContent(
                targetState = editingColors,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = modifier,
                label = "colorEditor"
            ) { editing ->
                if (editing) {
                    ColorEditor(
                        draft = draft,
                        topLinked = state.topLinked,
                        bottomLinked = state.bottomLinked,
                        onDraftChange = actions::onColorsChange,
                        onTopLinkedChange = actions::onTopLinkedChange,
                        onBottomLinkedChange = actions::onBottomLinkedChange,
                        onFinished = actions::onColorsFinished,
                        onClose = { editingColors = false }
                    )
                } else {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .glowBackground(c)
                            .safeDrawingPadding()
                            .verticalScroll(settingsScroll)
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Keeps the cards readable on tablets and in landscape
                        val content = Modifier.widthIn(max = 560.dp).fillMaxWidth()

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            IconButton(onClick = actions::onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = c.textSecondary
                                )
                            }
                        }

                        Column(content, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Definições",
                                style = MaterialTheme.typography.headlineMedium.merge(
                                    TextStyle(brush = c.crossGradient)
                                ),
                                fontWeight = FontWeight.Bold
                            )
                            Text("Ajusta o mentor à tua maneira.", color = c.textSecondary)
                        }

                        SectionCard("Gemini", content) {
                            ApiKeyField(
                                value = apiKey,
                                // TODO guardar a API key quando o utilizador acabar de a escrever
                                onValueChange = { apiKey = it.trim() }
                            )
                            Text(
                                "A chave fica só neste dispositivo e é enviada apenas ao Gemini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary
                            )
                            TextButton(onClick = actions::onGetApiKey, modifier = Modifier.align(Alignment.End)) {
                                Text("Obter uma chave  ↗")
                            }

                            Label("Modelo")
                            SegmentedPill(
                                options = geminiModels,
                                selected = model,
                                // TODO guardar o modelo escolhido
                                onSelect = { model = it }
                            )
                        }

                        SectionCard("Mentor", content) {
                            Label("Nível das respostas")
                            SegmentedPill(
                                options = MentorLevel.entries.map { it to it.label },
                                selected = level,
                                // TODO guardar o nível escolhido
                                onSelect = { level = it }
                            )
                            // TODO guardar as duas opções abaixo
                            SwitchRow(
                                "Incluir exemplos de código",
                                "Kotlin e Compose sempre que fizer sentido.",
                                codeExamples
                            ) { codeExamples = it }
                            SwitchRow(
                                "Respostas curtas",
                                "Vai direto ao assunto, sem rodeios.",
                                shortAnswers
                            ) { shortAnswers = it }
                        }

                        SectionCard("Aparência", content) {
                            ThemePreview(Modifier.height(220.dp))

                            Label("Temas prontos")
                            PresetRow(
                                selected = draft,
                                onSelect = actions::onPresetSelect
                            )

                            Label("Modo")
                            SegmentedPill(
                                options = listOf(false to "Escuro", true to "Claro"),
                                selected = c.isLight,
                                onSelect = { light ->
                                    actions.onColorsChange(
                                        draft.copy(background = draft.background.toBackground(light))
                                    )
                                    actions.onColorsFinished()
                                }
                            )
                        }

                        // Opens the full-screen editor
                        Row(
                            content
                                .glass(c)
                                .clickable(role = Role.Button) { editingColors = true }
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Cores personalizadas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Fundo, brilhos e acentos à tua escolha.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textSecondary
                                )
                            }
                            Text("›", style = MaterialTheme.typography.titleLarge, color = c.accent)
                        }

                        TextButton(onClick = actions::onUseSystemColors) {
                            Text(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Usar cores do sistema"
                                else "Repor cores"
                            )
                        }
                    }
                }
            }
        }
    }
}
/**
 * Settings screen: Gemini API key and model, how the mentor answers and the app colors.
 * The whole screen uses the colors being edited, so the theme changes show live.
 * Custom colors are edited in a separate full-screen editor ([ColorEditor]),
 * so the preview stays visible while the colors change.
 *
 * For now everything lives only in memory. The places that will read or
 * write persistent data are marked with TODO.
 *
 * @param onGetApiKey Called when the user wants to create a key (opens Google AI Studio).
 */
//@Composable
//fun SettingsScreen(
//    state: SettingsScreenState,
//    onColorsChange: (AppColors) -> Unit,
//    onColorsFinished: () -> Unit,
//    onTopLinkedChange: (Boolean) -> Unit,
//    onBottomLinkedChange: (Boolean) -> Unit,
//    onPresetSelect: (AppColors) -> Unit,
//    onUseSystemColors: () -> Unit,
//    onGetApiKey: () -> Unit,
//    onBack: () -> Unit = {},
//    modifier: Modifier = Modifier,
//) {
//
//    val system = systemAppColors()
//    val draft = state.colors ?: system
//    // TODO carregar a API key guardada (de forma segura, p.ex. cifrada)
//    var apiKey by rememberSaveable { mutableStateOf("") }
//    // TODO carregar o modelo, o nível e as preferências do mentor guardados
//    var model by rememberSaveable { mutableStateOf(geminiModels.first().first) }
//    var level by rememberSaveable { mutableStateOf(MentorLevel.INTERMEDIATE) }
//    var codeExamples by rememberSaveable { mutableStateOf(true) }
//    var shortAnswers by rememberSaveable { mutableStateOf(false) }
//
//    var editingColors by rememberSaveable { mutableStateOf(false) }
//    // Hoisted so the settings keep their scroll position after closing the editor
//    val settingsScroll = rememberScrollState()
//
//    // TODO guardar as cores (e se os acentos estão ligados) e aplicá-las a toda a app
//
//    DroidMentorTheme(colors = draft) {
//        val c = LocalAppColors.current
//        CompositionLocalProvider(LocalContentColor provides c.textPrimary) {
//            AnimatedContent(
//                targetState = editingColors,
//                transitionSpec = { fadeIn() togetherWith fadeOut() },
//                modifier = modifier,
//                label = "colorEditor"
//            ) { editing ->
//                if (editing) {
//                    ColorEditor(
//                        draft = draft,
//                        topLinked = state.topLinked,
//                        bottomLinked = state.bottomLinked,
//                        onDraftChange = onColorsChange,
//                        onTopLinkedChange = onTopLinkedChange,
//                        onBottomLinkedChange = onBottomLinkedChange,
//                        onFinished = onColorsFinished,
//                        onClose = { editingColors = false }
//                    )
//                } else {
//                    Column(
//                        Modifier
//                            .fillMaxSize()
//                            .glowBackground(c)
//                            .safeDrawingPadding()
//                            .verticalScroll(settingsScroll)
//                            .padding(horizontal = 20.dp, vertical = 24.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        // Keeps the cards readable on tablets and in landscape
//                        val content = Modifier.widthIn(max = 560.dp).fillMaxWidth()
//
//                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
//                            IconButton(onClick = onBack) {
//                                Icon(
//                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                                    contentDescription = "Voltar",
//                                    tint = c.textSecondary
//                                )
//                            }
//                        }
//
//                        Column(content, verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                            Text(
//                                "Definições",
//                                style = MaterialTheme.typography.headlineMedium.merge(
//                                    TextStyle(brush = c.crossGradient)
//                                ),
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text("Ajusta o mentor à tua maneira.", color = c.textSecondary)
//                        }
//
//                        SectionCard("Gemini", content) {
//                            ApiKeyField(
//                                value = apiKey,
//                                // TODO guardar a API key quando o utilizador acabar de a escrever
//                                onValueChange = { apiKey = it.trim() }
//                            )
//                            Text(
//                                "A chave fica só neste dispositivo e é enviada apenas ao Gemini.",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = c.textSecondary
//                            )
//                            TextButton(onClick = onGetApiKey, modifier = Modifier.align(Alignment.End)) {
//                                Text("Obter uma chave  ↗")
//                            }
//
//                            Label("Modelo")
//                            SegmentedPill(
//                                options = listOf(false to "Escuro", true to "Claro"),
//                                selected = c.isLight,
//                                onSelect = { light ->
//                                    onColorsChange(draft.copy(background = draft.background.toBackground(light)))
//                                    onColorsFinished()
//                                    onSelect = { model = it }
//                                }
//                            )
//                        }
//
//                        SectionCard("Mentor", content) {
//                            Label("Nível das respostas")
//                            SegmentedPill(
//                                options = listOf(false to "Escuro", true to "Claro"),
//                                selected = c.isLight,
//                                onSelect = { light ->
//                                    onColorsChange(draft.copy(background = draft.background.toBackground(light)))
//                                    onColorsFinished()
//                                }
//                            )
//                            // TODO guardar as duas opções abaixo
//                            SwitchRow(
//                                "Incluir exemplos de código",
//                                "Kotlin e Compose sempre que fizer sentido.",
//                                codeExamples
//                            ) { codeExamples = it }
//                            SwitchRow(
//                                "Respostas curtas",
//                                "Vai direto ao assunto, sem rodeios.",
//                                shortAnswers
//                            ) { shortAnswers = it }
//                        }
//
//                        SectionCard("Aparência", content) {
//                            ThemePreview(Modifier.height(220.dp))
//
//                            Label("Temas prontos")
//                            PresetRow(
//                                selected = draft,
//                                onSelect = onPresetSelect
//                            )
//
//                            Label("Modo")
//                            SegmentedPill(
//                                options = listOf(false to "Escuro", true to "Claro"),
//                                selected = c.isLight,
//                                onSelect = { light ->
//                                    onColorsChange(draft.copy(background = draft.background.toBackground(light)))
//                                    onColorsFinished()
//                                }
//                            )
//                        }
//
//                        // Opens the full-screen editor
//                        Row(
//                            content
//                                .glass(c)
//                                .clickable(role = Role.Button) { editingColors = true }
//                                .padding(20.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Column(Modifier.weight(1f)) {
//                                Text(
//                                    "Cores personalizadas",
//                                    style = MaterialTheme.typography.titleMedium,
//                                    fontWeight = FontWeight.SemiBold
//                                )
//                                Text(
//                                    "Fundo, brilhos e acentos à tua escolha.",
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = c.textSecondary
//                                )
//                            }
//                            Text("›", style = MaterialTheme.typography.titleLarge, color = c.accent)
//                        }
//
//                        TextButton(onClick = onUseSystemColors) {
//                            Text(
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Usar cores do sistema"
//                                else "Repor cores"
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

/** Glass text field for the key, hidden by default, with a button to show it. */
@Composable
private fun ApiKeyField(value: String, onValueChange: (String) -> Unit) {
    val c = LocalAppColors.current
    var visible by rememberSaveable { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .glass(c, RoundedCornerShape(16.dp))
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = c.textPrimary, fontFamily = FontFamily.Monospace),
            cursorBrush = SolidColor(c.accent),
            visualTransformation =
                if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 14.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) Text("Cola aqui a tua API key", color = c.textSecondary)
                inner()
            }
        )
        TextButton(onClick = { visible = !visible }) {
            Text(if (visible) "Ocultar" else "Mostrar")
        }
    }
}

/** Which color the single picker in [ColorEditor] is editing. */
private enum class ColorTarget(val label: String) {
    BACKGROUND("Fundo"),
    TOP("Brilho de cima"),
    TOP_ACCENT("Acento de cima"),
    BOTTOM("Brilho de baixo"),
    BOTTOM_ACCENT("Acento de baixo"),
}

private fun AppColors.colorOf(target: ColorTarget): Color = when (target) {
    ColorTarget.BACKGROUND -> background
    ColorTarget.TOP -> top.glow
    ColorTarget.TOP_ACCENT -> top.rawAccent
    ColorTarget.BOTTOM -> bottom.glow
    ColorTarget.BOTTOM_ACCENT -> bottom.rawAccent
}

/** Returns a copy with [target] set to [color]. A linked accent follows its glow. */
private fun AppColors.withColor(
    target: ColorTarget,
    color: Color,
    topLinked: Boolean,
    bottomLinked: Boolean,
): AppColors = when (target) {
    ColorTarget.BACKGROUND -> copy(background = color)
    ColorTarget.TOP -> copy(
        top = top.copy(glow = color, rawAccent = if (topLinked) color else top.rawAccent)
    )
    ColorTarget.TOP_ACCENT -> copy(top = top.copy(rawAccent = color))
    ColorTarget.BOTTOM -> copy(
        bottom = bottom.copy(glow = color, rawAccent = if (bottomLinked) color else bottom.rawAccent)
    )
    ColorTarget.BOTTOM_ACCENT -> copy(bottom = bottom.copy(rawAccent = color))
}

/**
 * Full-screen color editor. The preview is always visible (pinned at the top in
 * portrait, on the left in landscape) and a single picker edits the color chosen
 * in the row of chips above it.
 */
@Composable
private fun ColorEditor(
    draft: AppColors,
    topLinked: Boolean,
    bottomLinked: Boolean,
    onDraftChange: (AppColors) -> Unit,
    onTopLinkedChange: (Boolean) -> Unit,
    onBottomLinkedChange: (Boolean) -> Unit,
    onFinished: () -> Unit,
    onClose: () -> Unit,
) {
    val c = LocalAppColors.current
    BackHandler(onBack = onClose)

    var selected by rememberSaveable { mutableStateOf(ColorTarget.BACKGROUND) }
    // The accent chips only show when that accent is separate from its glow
    val targets = ColorTarget.entries.filter {
        when (it) {
            ColorTarget.TOP_ACCENT -> !topLinked
            ColorTarget.BOTTOM_ACCENT -> !bottomLinked
            else -> true
        }
    }
    // If the selected accent gets linked again, fall back to its glow
    val target = when {
        selected in targets -> selected
        selected == ColorTarget.TOP_ACCENT -> ColorTarget.TOP
        else -> ColorTarget.BOTTOM
    }

    val header: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Cores personalizadas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onClose) { Text("Concluído") }
        }
    }

    val controls: @Composable ColumnScope.() -> Unit = {
        TargetChips(
            targets = targets,
            selected = target,
            colorOf = draft::colorOf,
            onSelect = { selected = it }
        )

        // One picker instance per target, so nothing carries over between colors
        key(target) {
            HsvColorPicker(
                color = draft.colorOf(target),
                onColorChange = {
                    onDraftChange(draft.withColor(target, it, topLinked, bottomLinked))
                },
                onColorChangeFinished = onFinished,
                saturationRange =
                    if (target == ColorTarget.BACKGROUND) BackgroundLimits.saturation(c.isLight)
                    else 0f..1f,
                valueRange = when (target) {
                    ColorTarget.BACKGROUND -> BackgroundLimits.value(c.isLight)
                    ColorTarget.TOP, ColorTarget.BOTTOM -> 0.5f..1f
                    ColorTarget.TOP_ACCENT, ColorTarget.BOTTOM_ACCENT -> 0.3f..1f
                }
            )
        }

        if (target == ColorTarget.TOP || target == ColorTarget.BOTTOM) {
            val isTop = target == ColorTarget.TOP
            SwitchRow(
                "Acento igual ao brilho",
                "Desliga para escolher outra cor para botões e texto em destaque.",
                checked = if (isTop) topLinked else bottomLinked,
                onCheckedChange = { linked ->
                    if (isTop) {
                        onTopLinkedChange(linked)
                        if (linked) onDraftChange(draft.copy(top = GlowSpec(draft.top.glow)))
                    } else {
                        onBottomLinkedChange(linked)
                        if (linked) onDraftChange(draft.copy(bottom = GlowSpec(draft.bottom.glow)))
                    }
                    onFinished()
                }
            )
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .glowBackground(c)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (maxWidth > maxHeight) {
            // Landscape / tablet: preview on the left, controls on the right
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ThemePreview(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    header()
                    controls()
                }
            }
        } else {
            // Portrait: preview pinned at the top, only the controls scroll
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                header()
                ThemePreview(Modifier.height(200.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = controls
                )
            }
        }
    }
}

/** Row of chips, one per editable color, each with a swatch of its current value. */
@Composable
private fun TargetChips(
    targets: List<ColorTarget>,
    selected: ColorTarget,
    colorOf: (ColorTarget) -> Color,
    onSelect: (ColorTarget) -> Unit,
) {
    val c = LocalAppColors.current
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        targets.forEach { t ->
            val isSelected = t == selected
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) c.accent else Color.Transparent)
                    .border(1.dp, c.borderSubtle, CircleShape)
                    .clickable(role = Role.RadioButton) { onSelect(t) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(colorOf(t))
                        .border(1.dp, c.borderSubtle, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    t.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) c.onAccent else c.textSecondary
                )
            }
        }
    }
}

/** Small mock of the chat screen, so the colors can be seen in context. */
@Composable
private fun ThemePreview(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .glowBackground(c)
            .border(1.dp, c.borderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChatBubble("Porque é que o meu estado desaparece ao rodar o ecrã?", fromUser = true)
        ChatBubble("Usa rememberSaveable em vez de remember.", fromUser = false)

        Spacer(Modifier.weight(1f))

        Row(
            Modifier
                .fillMaxWidth()
                .glass(c, CircleShape)
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Escreve uma mensagem…",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
                modifier = Modifier.weight(1f)
            )
            SendButton(onClick = {}, diameter = 36.dp)
        }
    }
}

@Composable
private fun ColumnScope.ChatBubble(text: String, fromUser: Boolean) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(16.dp)
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (fromUser) c.onCrossGradient else c.textPrimary,
        modifier = Modifier
            .align(if (fromUser) Alignment.End else Alignment.Start)
            .widthIn(max = 220.dp)
            .then(
                if (fromUser) Modifier.clip(shape).background(c.crossGradient)
                else Modifier.glass(c, shape)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun PresetRow(selected: AppColors, onSelect: (AppColors) -> Unit) {
    val c = LocalAppColors.current
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PresetThemes.forEach { preset ->
            val isSelected = preset == selected
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(preset.background)
                    .background(
                        // The glows over the background, so it shows if the theme is light or dark
                        Brush.linearGradient(
                            listOf(
                                preset.top.glow.copy(alpha = 0.9f),
                                Color.Transparent,
                                preset.bottom.glow.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) c.textPrimary else c.borderSubtle,
                        shape = CircleShape
                    )
                    .clickable(role = Role.RadioButton) { onSelect(preset) }
            )
        }
    }
}

/** Pill with one option per segment; the selected one is filled with the accent. */
@Composable
private fun <T> SegmentedPill(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    val c = LocalAppColors.current
    val pill = RoundedCornerShape(50)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(pill)
            .border(1.dp, c.borderSubtle, pill)
            .padding(4.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(pill)
                    .background(if (isSelected) c.accent else Color.Transparent)
                    .clickable(role = Role.RadioButton) { if (!isSelected) onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) c.onAccent else c.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val c = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalAppColors.current
    Column(
        modifier
            .glass(c)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

/** Small accent label, optionally with a swatch of the color it refers to. */
@Composable
private fun Label(text: String, swatch: Color? = null) {
    val c = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (swatch != null) {
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(swatch)
                    .border(1.dp, c.borderSubtle, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = c.accent)
    }
}
