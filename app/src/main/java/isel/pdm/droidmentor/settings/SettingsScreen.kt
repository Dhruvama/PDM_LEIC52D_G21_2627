package isel.pdm.droidmentor.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.domain.settings.MentorSettings // Confirme o package do MentorSettings
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.settings.components.AppearenceComponent
import isel.pdm.droidmentor.settings.components.ColorEditor
import isel.pdm.droidmentor.settings.components.CustomColorsEntry
import isel.pdm.droidmentor.settings.components.GeminiConfigComponent
import isel.pdm.droidmentor.settings.components.MentorComponent
import isel.pdm.droidmentor.settings.components.UseSystemColorsButton
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import isel.pdm.droidmentor.ui.theme.systemAppColors

// 1. A NOVA FUNÇÃO PÚBLICA QUE FAZ A PONTE COM O VIEWMODEL (Faltava isto)
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
            override fun onGetApiKey() = onGetApiKey.invoke()
            override fun onBack() = onBack.invoke()
            // Assume-se que o ViewModel tem esta função com este nome
            override fun onMentorSettingsChange(settings: MentorSettings) = viewModel.updateMentorSettings(settings)
        }
    }
    SettingsScreenContent(state = viewModel.state, actions = actions, modifier = modifier)
}

@Composable
private fun SettingsScreenContent(
    state: SettingsScreenState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    val system = systemAppColors()
    val draft = state.colors ?: system
    val mentor = state.mentor

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

                        GeminiConfigComponent(
                            mentor = mentor,
                            onMentorSettingsChange = actions::onMentorSettingsChange,
                            onGetApiKey = actions::onGetApiKey,
                            modifier = content,
                        )
                        MentorComponent(
                            mentor = mentor,
                            onMentorSettingsChange = actions::onMentorSettingsChange,
                            modifier = content,
                        )
                        AppearenceComponent(
                            draft = draft,
                            onPresetSelect = actions::onPresetSelect,
                            onColorsChange = actions::onColorsChange,
                            onColorsFinished = actions::onColorsFinished,
                            modifier = content,
                        )
                        CustomColorsEntry(onClick = { editingColors = true }, modifier = content)
                        UseSystemColorsButton(onClick = actions::onUseSystemColors)
                    }
                }
            }
        }
    }
}