package io.github.soulhacler.encrypted.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.core.engine.CardOutcome
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.game.GameResults
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.components.Confetti
import io.github.soulhacler.encrypted.ui.components.NeonButton
import io.github.soulhacler.encrypted.ui.components.rememberLatest
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

@Composable
fun ResultsScreen(vm: GameViewModel) {
    val results = rememberLatest(vm.results) ?: return
    BackHandler { vm.openHome() }

    val celebrate = when (results.mode) {
        GameMode.CLASSIC -> results.standings.first().score > 0
        GameMode.PARTY -> results.standings.first().score > 0
        else -> true
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        ) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                when (results.mode) {
                    GameMode.CLASSIC -> ClassicHeadline(results)
                    GameMode.PARTY -> PartyHeadline(results)
                    else -> WinnerHeadline(results)
                }

                Spacer(Modifier.height(28.dp))
                NeonButton(
                    if (results.mode == GameMode.PARTY) "Play again" else "Rematch", vm::rematch,
                    Modifier.fillMaxWidth(), icon = Icons.Filled.Refresh,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    NeonButton("New game", vm::newGame, Modifier.weight(1f), colour = Neon.Purple, filled = false, height = 58.dp, fontSize = 16.sp)
                    NeonButton("Home", vm::openHome, Modifier.weight(1f), colour = Neon.White, filled = false, height = 58.dp, fontSize = 16.sp, icon = Icons.Filled.Home)
                }

                val cards = results.turns.flatMap { it.results }
                if (results.mode.maxParticipants == 1 && cards.isNotEmpty()) {
                    Spacer(Modifier.height(28.dp))
                    Text("CARDS PLAYED", style = EncryptedType.Label, color = Neon.Muted, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    cards.forEach { CardResultRow(it) }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        if (celebrate) Confetti(Modifier.fillMaxSize())
    }
}

@Composable
private fun ClassicHeadline(results: GameResults) {
    val score = results.standings.first().score
    Text("FINAL SCORE", style = EncryptedType.Label, color = Neon.Muted, modifier = Modifier.padding(top = 16.dp))
    CountUp(score, Neon.Cyan, fontSizeSp = 120)
    OutcomeTotals(results.turns.flatMap { it.results }.map { it.outcome })
    Spacer(Modifier.height(16.dp))
    when {
        results.newBest -> Badge("NEW BEST SCORE!", Neon.Gold)
        results.previousBest != null -> Text(
            "Best for ${results.roundSeconds}s rounds: ${results.previousBest}",
            color = Neon.Muted, fontFamily = EncryptedType.Mono,
        )
    }
    Spacer(Modifier.height(8.dp))
    Text(classicVerdict(score), color = Neon.White, fontSize = 18.sp, textAlign = TextAlign.Center)
}

private fun classicVerdict(score: Int): String = when {
    score == 0 -> "The code holds… for now."
    score < 4 -> "Not bad. Say them faster!"
    score < 8 -> "Nice decoding!"
    score < 12 -> "Code breaker!"
    else -> "Master cryptographer!"
}

@Composable
private fun PartyHeadline(results: GameResults) {
    val cards = results.turns.flatMap { it.results }
    val cracked = cards.count { it.outcome == CardOutcome.CORRECT }
    Text("SESSION OVER", style = EncryptedType.Label, color = Neon.Muted, modifier = Modifier.padding(top = 16.dp))
    CountUp(cracked, Neon.Gold, fontSizeSp = 110)
    Text(
        if (cracked == 1) "code cracked together" else "codes cracked together",
        color = Neon.White, fontSize = 18.sp,
    )
    Text("${cards.size} cards played", color = Neon.Muted, fontFamily = EncryptedType.Mono, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun WinnerHeadline(results: GameResults) {
    val winners = results.winners
    val draw = winners.size > 1
    val glow by rememberInfiniteTransition(label = "trophy").animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "trophyScale",
    )
    Spacer(Modifier.height(12.dp))
    Text("🏆", fontSize = 72.sp, modifier = Modifier.scale(glow))
    Text(if (draw) "IT'S A DRAW!" else "WINNER", style = EncryptedType.Label.copy(fontSize = 16.sp), color = Neon.Gold)
    Spacer(Modifier.height(6.dp))
    winners.forEach { w ->
        val colour = Neon.teamColour(w.participantIndex)
        Text(
            w.name.uppercase(),
            color = colour,
            fontFamily = EncryptedType.Mono,
            fontWeight = FontWeight.Black,
            fontSize = 36.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = Shadow(colour.copy(alpha = 0.7f), blurRadius = 32f)),
        )
    }
    Text(
        "${winners.first().score} ${if (winners.first().score == 1) "point" else "points"}",
        color = Neon.White, fontFamily = EncryptedType.Mono, fontSize = 18.sp,
    )
    Spacer(Modifier.height(24.dp))
    Text("FINAL STANDINGS", style = EncryptedType.Label, color = Neon.Muted, modifier = Modifier.padding(bottom = 10.dp))
    Scoreboard(results.standings, highlightIndex = if (draw) null else winners.first().participantIndex)
}

@Composable
private fun Badge(text: String, colour: androidx.compose.ui.graphics.Color) {
    val shape = RoundedCornerShape(50)
    Text(
        text,
        color = colour,
        fontFamily = EncryptedType.Mono,
        fontWeight = FontWeight.Black,
        fontSize = 16.sp,
        modifier = Modifier
            .clip(shape)
            .background(colour.copy(alpha = 0.15f))
            .border(1.5.dp, colour, shape)
            .padding(horizontal = 18.dp, vertical = 8.dp),
    )
}
