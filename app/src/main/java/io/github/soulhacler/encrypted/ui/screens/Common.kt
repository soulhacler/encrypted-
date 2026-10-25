package io.github.soulhacler.encrypted.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.core.engine.CardOutcome
import io.github.soulhacler.encrypted.core.engine.CardResult
import io.github.soulhacler.encrypted.core.engine.Standing
import io.github.soulhacler.encrypted.ui.components.ColourDot
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

/** Stops the screen dimming while a round is being played. */
@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

@Composable
fun QuitDialog(onConfirm: () -> Unit, onDismiss: () -> Unit, title: String = "QUIT GAME?") {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Neon.SurfaceHigh,
        title = { Text(title, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black) },
        text = { Text("Scores for this game will be lost.", color = Neon.Muted) },
        confirmButton = { TextButton(onConfirm) { Text("QUIT", color = Neon.Red, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onDismiss) { Text("KEEP PLAYING", color = Neon.Cyan, fontWeight = FontWeight.Bold) } },
    )
}

/** Ranked list of scores with a bar showing each share of the lead. */
@Composable
fun Scoreboard(standings: List<Standing>, modifier: Modifier = Modifier, highlightIndex: Int? = null) {
    val top = standings.maxOfOrNull { it.score }?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        standings.forEach { row ->
            val colour = Neon.teamColour(row.participantIndex)
            val shape = RoundedCornerShape(14.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(shape)
                    .background(Neon.Surface)
                    .border(if (row.participantIndex == highlightIndex) 2.dp else 1.dp, if (row.participantIndex == highlightIndex) colour else Neon.Outline, shape),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(row.score.toFloat() / top)
                        .background(colour.copy(alpha = 0.18f)),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 14.dp),
                ) {
                    Text("#${row.rank}", color = Neon.Muted, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                    ColourDot(colour)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        row.name, color = Neon.White, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                    )
                    Text("${row.score}", color = colour, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 22.sp)
                }
            }
        }
    }
}

/** One played card: outcome mark, the answer, and the encrypted phrase underneath. */
@Composable
fun CardResultRow(result: CardResult) {
    val (mark, colour) = when (result.outcome) {
        CardOutcome.CORRECT -> "✓" to Neon.Cyan
        CardOutcome.INCORRECT -> "✗" to Neon.Red
        CardOutcome.SKIPPED -> "↷" to Neon.Purple
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(mark, color = colour, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(34.dp))
        Column(Modifier.weight(1f)) {
            Text(result.puzzle.answer, color = Neon.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("“${result.puzzle.encrypted}”", color = Neon.Muted, fontSize = 13.sp)
        }
    }
}
