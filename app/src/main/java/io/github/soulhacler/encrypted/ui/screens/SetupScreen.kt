package io.github.soulhacler.encrypted.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.core.engine.GameConfig
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.components.ColourDot
import io.github.soulhacler.encrypted.ui.components.NeonButton
import io.github.soulhacler.encrypted.ui.components.NeonChip
import io.github.soulhacler.encrypted.ui.components.ScreenHeader
import io.github.soulhacler.encrypted.ui.components.SectionLabel
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

@Composable
fun SetupScreen(vm: GameViewModel) {
    BackHandler { vm.openHome() }
    val setup = vm.setup
    val mode = setup.mode
    val accent = mode.accent()
    val library = vm.library ?: return

    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader(mode.title, onBack = vm::openHome, modifier = Modifier.padding(horizontal = 4.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                Text(mode.tagline, color = Neon.Muted, fontSize = 15.sp)

                SectionLabel("Category", colour = accent)
                library.selectableCategories.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        row.forEach { category ->
                            NeonChip(
                                text = category.name,
                                selected = setup.categoryId == category.id,
                                onClick = { vm.selectCategory(category.id) },
                                colour = accent,
                                leading = category.icon,
                                subtitle = "${library.count(category.id, setup.difficulty)}",
                                modifier = Modifier.weight(1f).height(104.dp),
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                SectionLabel("Difficulty", colour = accent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    (listOf<Difficulty?>(null) + Difficulty.entries).forEach { difficulty ->
                        NeonChip(
                            text = difficulty?.label ?: "Mixed",
                            selected = setup.difficulty == difficulty,
                            onClick = { vm.selectDifficulty(difficulty) },
                            colour = accent,
                            subtitle = "${library.count(setup.categoryId, difficulty)}",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (mode.timed) {
                    SectionLabel("Round length", colour = accent)
                    RoundLengthPicker(setup.roundSeconds, accent, vm::selectRoundSeconds)
                }

                if (mode.maxParticipants > 1) {
                    val noun = mode.participantNoun
                    SectionLabel(if (mode == GameMode.TEAM_BATTLE) "Teams" else "Players", colour = accent)
                    setup.names.forEachIndexed { index, name ->
                        ParticipantRow(
                            index = index,
                            name = name,
                            canRemove = setup.names.size > mode.minParticipants,
                            onRename = { vm.renameParticipant(index, it) },
                            onRemove = { vm.removeParticipant(index) },
                        )
                    }
                    if (setup.names.size < mode.maxParticipants) {
                        NeonButton(
                            "Add $noun", vm::addParticipant, Modifier.fillMaxWidth().padding(top = 4.dp),
                            colour = accent, filled = false, height = 52.dp, fontSize = 15.sp, icon = Icons.Filled.Add,
                        )
                    }

                    SectionLabel("Rounds per $noun", colour = accent)
                    Stepper(setup.rounds, 1, mode.maxRounds, accent, vm::selectRounds)
                    if (mode == GameMode.TEAM_BATTLE) {
                        Text(
                            "Each round, one team member reads aloud while the rest of the team decodes.",
                            color = Neon.Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        val problem = vm.setupProblem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            if (problem != null) {
                Text(problem, color = Neon.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            NeonButton(
                "Start game", vm::startGame, Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                colour = accent, enabled = problem == null, icon = Icons.Filled.PlayArrow,
            )
        }
    }
}

@Composable
private fun RoundLengthPicker(seconds: Int, accent: Color, onSelect: (Int) -> Unit) {
    var showCustom by remember { mutableStateOf(false) }
    val isCustom = seconds !in GameConfig.PRESET_ROUND_SECONDS
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        GameConfig.PRESET_ROUND_SECONDS.forEach { preset ->
            NeonChip("${preset}s", seconds == preset, { onSelect(preset) }, Modifier.weight(1f), colour = accent)
        }
        NeonChip(
            if (isCustom) "${seconds}s" else "Custom", isCustom, { showCustom = true }, Modifier.weight(1.2f), colour = accent,
        )
    }
    if (showCustom) {
        CustomTimerDialog(seconds, accent, onDismiss = { showCustom = false }) {
            onSelect(it)
            showCustom = false
        }
    }
}

@Composable
fun CustomTimerDialog(initial: Int, accent: Color, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var value by remember { mutableFloatStateOf(initial.coerceIn(CUSTOM_MIN, CUSTOM_MAX).toFloat()) }
    val seconds = (value / 5).toInt() * 5
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Neon.SurfaceHigh,
        title = { Text("CUSTOM TIMER", fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "%d:%02d".format(seconds / 60, seconds % 60),
                    color = accent, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 44.sp,
                )
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = CUSTOM_MIN.toFloat()..CUSTOM_MAX.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
                )
                Text("${CUSTOM_MIN}s to ${CUSTOM_MAX / 60} minutes", color = Neon.Muted, fontSize = 13.sp)
            }
        },
        confirmButton = { TextButton({ onConfirm(seconds) }) { Text("SET", color = accent, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onDismiss) { Text("CANCEL", color = Neon.Muted) } },
    )
}

private const val CUSTOM_MIN = 10
private const val CUSTOM_MAX = 300

@Composable
private fun ParticipantRow(
    index: Int,
    name: String,
    canRemove: Boolean,
    onRename: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val colour = Neon.teamColour(index)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        ColourDot(colour, size = 16.dp)
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onRename,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colour,
                unfocusedBorderColor = Neon.Outline,
                cursorColor = colour,
                focusedTextColor = Neon.White,
                unfocusedTextColor = Neon.White,
            ),
            modifier = Modifier.weight(1f),
        )
        if (canRemove) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove $name", tint = Neon.Muted)
            }
        }
    }
}

@Composable
private fun Stepper(value: Int, min: Int, max: Int, accent: Color, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NeonButton("−", { onChange(value - 1) }, Modifier.width(64.dp), colour = accent, filled = false, enabled = value > min, height = 52.dp)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.width(80.dp)) {
            Text(
                "$value", color = Neon.White, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black,
                fontSize = 30.sp, textAlign = TextAlign.Center,
            )
        }
        NeonButton("+", { onChange(value + 1) }, Modifier.width(64.dp), colour = accent, filled = false, enabled = value < max, height = 52.dp)
    }
}
