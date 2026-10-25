package io.github.soulhacler.encrypted.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.core.engine.CardOutcome
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.components.ColourDot
import io.github.soulhacler.encrypted.ui.components.NeonButton
import io.github.soulhacler.encrypted.ui.components.rememberLatest
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

@Composable
fun TurnSummaryScreen(vm: GameViewModel) {
    val summary = rememberLatest(vm.turnSummary) ?: return
    var confirmQuit by remember { mutableStateOf(false) }
    BackHandler { confirmQuit = true }

    val record = summary.record
    val colour = Neon.teamColour(record.participantIndex)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (summary.timeRanOut) "TIME'S UP!" else "TURN OVER",
                color = Neon.Red, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 30.sp,
                style = TextStyle(shadow = Shadow(Neon.Red.copy(alpha = 0.5f), blurRadius = 24f)),
            )
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColourDot(colour, size = 14.dp)
                Spacer(Modifier.width(10.dp))
                Text(summary.name.uppercase(), color = colour, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
            Text("CRACKED", style = EncryptedType.Label, color = Neon.Muted, modifier = Modifier.padding(top = 4.dp))
            CountUp(record.score, colour)
            OutcomeTotals(record.results.map { it.outcome })

            Spacer(Modifier.height(24.dp))
            Text("SCOREBOARD", style = EncryptedType.Label, color = Neon.Muted, modifier = Modifier.padding(bottom = 10.dp))
            Scoreboard(summary.standings, highlightIndex = record.participantIndex)

            Spacer(Modifier.height(28.dp))
            summary.nextName?.let { next ->
                NeonButton("Pass to $next", vm::nextTurn, Modifier.fillMaxWidth(), colour = Neon.Cyan, icon = Icons.Filled.PlayArrow)
            }

            if (record.results.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                Text("THIS TURN", style = EncryptedType.Label, color = Neon.Muted, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                record.results.forEach { CardResultRow(it) }
            }
        }
    }

    if (confirmQuit) {
        QuitDialog(onConfirm = { confirmQuit = false; vm.openHome() }, onDismiss = { confirmQuit = false })
    }
}

/** A big number that counts up from zero. */
@Composable
fun CountUp(target: Int, colour: Color, fontSizeSp: Int = 96) {
    val value = remember(target) { Animatable(0f) }
    LaunchedEffect(target) {
        value.animateTo(target.toFloat(), tween(durationMillis = (300 + target * 60).coerceAtMost(1400), easing = FastOutSlowInEasing))
    }
    Text(
        "${value.value.toInt()}",
        color = colour,
        fontFamily = EncryptedType.Mono,
        fontWeight = FontWeight.Black,
        fontSize = fontSizeSp.sp,
        textAlign = TextAlign.Center,
        style = TextStyle(shadow = Shadow(colour.copy(alpha = 0.6f), blurRadius = 36f)),
    )
}

@Composable
fun OutcomeTotals(results: List<CardOutcome>) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Total("✓", results.count { it == CardOutcome.CORRECT }, Neon.Cyan)
        Total("✗", results.count { it == CardOutcome.INCORRECT }, Neon.Red)
        Total("↷", results.count { it == CardOutcome.SKIPPED }, Neon.Purple)
    }
}

@Composable
private fun Total(mark: String, count: Int, colour: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(mark, color = colour, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Spacer(Modifier.width(6.dp))
        Text("$count", color = Neon.White, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
