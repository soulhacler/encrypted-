package io.github.soulhacler.encrypted.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.components.ColourDot
import io.github.soulhacler.encrypted.ui.components.NeonButton
import io.github.soulhacler.encrypted.ui.components.formatClock
import io.github.soulhacler.encrypted.ui.components.rememberLatest
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

@Composable
fun HandoffScreen(vm: GameViewModel) {
    val state = rememberLatest(vm.handoff) ?: return
    var confirmQuit by remember { mutableStateOf(false) }
    BackHandler { confirmQuit = true }
    KeepScreenOn()

    val colour = if (state.mode == GameMode.CLASSIC) Neon.Cyan else Neon.teamColour(state.participantIndex)
    val multi = state.mode != GameMode.CLASSIC

    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(
                if (multi) "ROUND ${state.round} OF ${state.totalRounds}  ·  TURN ${state.turnNumber}/${state.totalTurns}" else "CLASSIC",
                style = EncryptedType.Label,
                color = Neon.Muted,
            )
            Spacer(Modifier.height(16.dp))
            if (multi) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColourDot(colour, size = 18.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        state.participantName.uppercase(),
                        color = colour,
                        fontFamily = EncryptedType.Mono,
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(shadow = Shadow(colour.copy(alpha = 0.6f), blurRadius = 24f)),
                    )
                }
                Text("YOU'RE UP", style = EncryptedType.Label, color = Neon.White, modifier = Modifier.padding(top = 6.dp))
            } else {
                Text(
                    "READY?",
                    color = colour,
                    fontFamily = EncryptedType.Mono,
                    fontWeight = FontWeight.Black,
                    fontSize = 44.sp,
                    style = androidx.compose.ui.text.TextStyle(shadow = Shadow(colour.copy(alpha = 0.6f), blurRadius = 24f)),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                instructions(state.mode, state.participantName),
                color = Neon.White.copy(alpha = 0.85f),
                fontSize = 17.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 460.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${formatClock(state.roundSeconds * 1000L)} on the clock",
                style = EncryptedType.Label,
                color = colour,
            )
            Spacer(Modifier.height(32.dp))
            NeonButton(
                if (multi) "Start turn" else "Start", vm::startCountdown,
                Modifier.widthIn(max = 460.dp).fillMaxWidth(),
                colour = colour, icon = Icons.Filled.PlayArrow, height = 76.dp, fontSize = 24.sp,
            )
            if (multi && state.turnNumber > 1) {
                Spacer(Modifier.height(32.dp))
                Text("SCOREBOARD", style = EncryptedType.Label, color = Neon.Muted, modifier = Modifier.padding(bottom = 10.dp))
                Scoreboard(state.standings, Modifier.widthIn(max = 460.dp), highlightIndex = state.participantIndex)
            }
        }

        IconButton(onClick = { confirmQuit = true }, modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Quit game", tint = Neon.Muted)
        }

        CountdownOverlay(vm.countdown, colour)
    }

    if (confirmQuit) {
        QuitDialog(onConfirm = { confirmQuit = false; vm.openHome() }, onDismiss = { confirmQuit = false })
    }
}

private fun instructions(mode: GameMode, name: String): String = when (mode) {
    GameMode.CLASSIC ->
        "Read each phrase out loud, faster and faster, until the hidden phrase clicks. Then reveal the answer."
    GameMode.TEAM_BATTLE ->
        "Pick a reader from $name. Read each phrase aloud while your teammates shout their guesses."
    GameMode.PASS_AND_PLAY ->
        "Hand the phone to $name. Say each phrase aloud and crack as many as you can."
    GameMode.PARTY -> "Read them out together."
}

@Composable
private fun CountdownOverlay(count: Int?, colour: androidx.compose.ui.graphics.Color) {
    if (count == null) return
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().background(Neon.Background.copy(alpha = 0.92f)),
    ) {
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                (scaleIn(tween(300), initialScale = 2.2f) + fadeIn(tween(200))) togetherWith
                    (scaleOut(tween(250), targetScale = 0.5f) + fadeOut(tween(200)))
            },
            label = "countdown",
        ) { n ->
            Text(
                text = if (n == 0) "GO!" else "$n",
                color = if (n == 0) Neon.White else colour,
                fontFamily = EncryptedType.Mono,
                fontWeight = FontWeight.Black,
                fontSize = 140.sp,
                style = androidx.compose.ui.text.TextStyle(shadow = Shadow(colour, blurRadius = 48f)),
            )
        }
    }
}
