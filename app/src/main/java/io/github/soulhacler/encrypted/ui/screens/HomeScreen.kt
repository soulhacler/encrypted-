package io.github.soulhacler.encrypted.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.components.EncryptedLogo
import io.github.soulhacler.encrypted.ui.components.NeonButton
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

fun GameMode.accent(): Color = when (this) {
    GameMode.CLASSIC -> Neon.Cyan
    GameMode.TEAM_BATTLE -> Neon.Purple
    GameMode.PASS_AND_PLAY -> Neon.Red
    GameMode.PARTY -> Neon.Gold
}

fun GameMode.symbol(): String = when (this) {
    GameMode.CLASSIC -> "⏱"
    GameMode.TEAM_BATTLE -> "⚔"
    GameMode.PASS_AND_PLAY -> "⇄"
    GameMode.PARTY -> "✦"
}

@Composable
fun HomeScreen(vm: GameViewModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val logoSize = (maxWidth.value / 9f).coerceIn(28f, 52f).sp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            EncryptedLogo(fontSize = logoSize, decryptMillis = 600)
            Spacer(Modifier.height(8.dp))
            Text(
                "The phonetic code-breaking party game",
                color = Neon.Muted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            vm.library?.let { lib ->
                Text(
                    "${lib.puzzles.size} PUZZLES · ${lib.categories.size} CATEGORIES",
                    style = EncryptedType.Label.copy(fontSize = 11.sp),
                    color = Neon.Cyan.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Spacer(Modifier.height(28.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
            ) {
                GameMode.entries.forEachIndexed { index, mode ->
                    ModeCard(index, mode) { vm.openSetup(mode) }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
            ) {
                NeonButton(
                    "How to play", vm::openHowToPlay, Modifier.weight(1f),
                    colour = Neon.White, filled = false, height = 56.dp, fontSize = 14.sp, icon = Icons.Filled.Info,
                )
                NeonButton(
                    "Settings", vm::openSettings, Modifier.weight(1f),
                    colour = Neon.White, filled = false, height = 56.dp, fontSize = 14.sp, icon = Icons.Filled.Settings,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModeCard(index: Int, mode: GameMode, onClick: () -> Unit) {
    val accent = mode.accent()
    val shape = RoundedCornerShape(22.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.16f), Neon.Surface.copy(alpha = 0.9f))))
            .border(1.5.dp, accent.copy(alpha = 0.7f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(18.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(accent.copy(alpha = 0.15f))
                .border(1.dp, accent, RoundedCornerShape(16.dp)),
        ) {
            Text(mode.symbol(), color = accent, fontSize = 28.sp)
        }
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(
                "0${index + 1} // ${mode.title.uppercase()}",
                color = accent,
                fontFamily = EncryptedType.Mono,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(mode.tagline, color = Neon.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 19.sp)
        }
        Text("›", color = accent, fontSize = 34.sp, modifier = Modifier.padding(start = 8.dp))
    }
}
