package isel.pdm.droidmentor.settings.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.components.HsvColorPicker
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.BackgroundLimits
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.GlowSpec
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val CUSTOM_COLORS_ENTRY_TAG = "custom_colors_entry"
const val USE_SYSTEM_COLORS_TAG = "use_system_colors"
const val COLOR_EDITOR_TAG = "color_editor"

/** Row that opens the full-screen color editor. */
@Composable
fun CustomColorsEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Row(
        modifier
            .glass(c)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(20.dp)
            .testTag(CUSTOM_COLORS_ENTRY_TAG),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.settings_custom_colors_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.settings_custom_colors_desc),
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary
            )
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = c.accent)
    }
}

/** Button that goes back to the system colors (or the default ones before Android 12). */
@Composable
fun UseSystemColorsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.testTag(USE_SYSTEM_COLORS_TAG)) {
        Text(
            stringResource(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) R.string.settings_use_system_colors
                else R.string.settings_reset_colors
            )
        )
    }
}

/** Which color the single picker in [ColorEditor] is editing. */
private enum class ColorTarget(@StringRes val labelRes: Int) {
    BACKGROUND(R.string.settings_color_background),
    TOP(R.string.settings_color_top_glow),
    TOP_ACCENT(R.string.settings_color_top_accent),
    BOTTOM(R.string.settings_color_bottom_glow),
    BOTTOM_ACCENT(R.string.settings_color_bottom_accent),
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
 *
 * @param draft Colors being edited.
 * @param topLinked Whether the top accent follows the top glow.
 * @param bottomLinked Whether the bottom accent follows the bottom glow.
 * @param onDraftChange Called with the new colors while the user edits.
 * @param onTopLinkedChange Called when the top accent is linked or unlinked.
 * @param onBottomLinkedChange Called when the bottom accent is linked or unlinked.
 * @param onFinished Called when the user finishes a change, to save it.
 * @param onClose Called when the user closes the editor.
 */
@Composable
fun ColorEditor(
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
                stringResource(R.string.settings_custom_colors_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.settings_custom_colors_done))
            }
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
                title = stringResource(R.string.settings_accent_linked),
                description = stringResource(R.string.settings_accent_linked_desc),
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
            .testTag(COLOR_EDITOR_TAG)
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
                    stringResource(t.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) c.onAccent else c.textSecondary
                )
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun CustomColorsEntryDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        CustomColorsEntry(
            onClick = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun UseSystemColorsButtonPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        UseSystemColorsButton(onClick = { })
    }
}

// Colors and the linked switches change: check it in Interactive Mode.
@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ColorEditorPreview() {
    var draft by remember { mutableStateOf(DefaultDarkColors) }
    var topLinked by remember { mutableStateOf(true) }
    var bottomLinked by remember { mutableStateOf(true) }
    DroidMentorTheme(colors = draft) {
        ColorEditor(
            draft = draft,
            topLinked = topLinked,
            bottomLinked = bottomLinked,
            onDraftChange = { draft = it },
            onTopLinkedChange = { topLinked = it },
            onBottomLinkedChange = { bottomLinked = it },
            onFinished = { },
            onClose = { },
        )
    }
}