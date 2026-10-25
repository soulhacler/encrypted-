package io.github.soulhacler.encrypted.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.BuildConfig
import io.github.soulhacler.encrypted.core.engine.GameConfig
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.components.NeonButton
import io.github.soulhacler.encrypted.ui.components.NeonChip
import io.github.soulhacler.encrypted.ui.components.ScreenHeader
import io.github.soulhacler.encrypted.ui.components.SectionLabel
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

@Composable
fun SettingsScreen(vm: GameViewModel) {
    BackHandler { vm.openHome() }
    val settings = vm.settings
    var showCustom by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Settings", onBack = vm::openHome, modifier = Modifier.padding(horizontal = 4.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                SectionLabel("Default round length")
                val isCustom = settings.roundSeconds !in GameConfig.PRESET_ROUND_SECONDS
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    GameConfig.PRESET_ROUND_SECONDS.forEach { s ->
                        NeonChip("${s}s", settings.roundSeconds == s, { vm.setDefaultRoundSeconds(s) }, Modifier.weight(1f))
                    }
                    NeonChip(if (isCustom) "${settings.roundSeconds}s" else "Custom", isCustom, { showCustom = true }, Modifier.weight(1.2f))
                }

                SectionLabel("Sound & feel")
                ToggleRow("Sound effects", "Reveals, right/wrong and the buzzer", settings.soundEnabled, vm::setSoundEnabled)
                ToggleRow(
                    "Countdown beeps", "Ticks during the last five seconds",
                    settings.countdownSounds && settings.soundEnabled, vm::setCountdownSounds, enabled = settings.soundEnabled,
                )
                ToggleRow("Haptic feedback", "Light vibrations on taps and answers", settings.hapticsEnabled, vm::setHapticsEnabled)

                SectionLabel("Classic best scores")
                if (settings.classicBest.isEmpty()) {
                    Text("No scores yet. Play a Classic game to set one.", color = Neon.Muted)
                } else {
                    settings.classicBest.toSortedMap().forEach { (seconds, best) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("${seconds}s rounds", color = Neon.White, modifier = Modifier.weight(1f))
                            Text("$best", color = Neon.Cyan, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    NeonButton("Reset best scores", vm::resetBestScores, Modifier.fillMaxWidth(), colour = Neon.Red, filled = false, height = 52.dp, fontSize = 14.sp)
                }

                SectionLabel("About")
                val lib = vm.library
                Text("ENCRYPTED ${BuildConfig.VERSION_NAME}", color = Neon.White, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Bold)
                if (lib != null) {
                    Text("${lib.puzzles.size} puzzles in ${lib.unlockedPacks.size} pack(s)", color = Neon.Muted)
                }
                Text(
                    "Works fully offline. No accounts, no adverts, no tracking.",
                    color = Neon.Muted, modifier = Modifier.padding(top = 4.dp),
                )
                Text("© 2026. All rights reserved.", color = Neon.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showCustom) {
        CustomTimerDialog(settings.roundSeconds, Neon.Cyan, onDismiss = { showCustom = false }) {
            vm.setDefaultRoundSeconds(it)
            showCustom = false
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(shape)
            .background(Neon.Surface)
            .border(1.dp, Neon.Outline, shape)
            .clickable(enabled = enabled, role = Role.Switch) { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = if (enabled) Neon.White else Neon.Muted, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Neon.Muted, fontSize = 13.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Neon.Background,
                checkedTrackColor = Neon.Cyan,
                uncheckedThumbColor = Neon.Muted,
                uncheckedTrackColor = Neon.SurfaceHigh,
                uncheckedBorderColor = Neon.Outline,
            ),
        )
    }
}
