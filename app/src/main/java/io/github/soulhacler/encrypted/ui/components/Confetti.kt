package io.github.soulhacler.encrypted.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import io.github.soulhacler.encrypted.ui.theme.Neon
import kotlin.random.Random

private class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val spin: Float,
    val size: Float,
    val colour: Color,
)

/** A one-off burst of neon confetti for the winner. */
@Composable
fun Confetti(modifier: Modifier = Modifier, count: Int = 140, durationSeconds: Float = 5f) {
    val particles = remember {
        val r = Random(System.nanoTime())
        List(count) {
            Particle(
                x = r.nextFloat(),
                y = -r.nextFloat() * 0.5f,
                vx = (r.nextFloat() - 0.5f) * 0.25f,
                vy = 0.18f + r.nextFloat() * 0.25f,
                spin = (r.nextFloat() - 0.5f) * 720f,
                size = 6f + r.nextFloat() * 10f,
                colour = Neon.TeamColours[r.nextInt(Neon.TeamColours.size)],
            )
        }
    }
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (time < durationSeconds) {
            time = (withFrameNanos { it } - start) / 1_000_000_000f
        }
    }
    if (time >= durationSeconds) return

    Canvas(modifier) {
        val fade = (1f - (time - durationSeconds + 1f).coerceIn(0f, 1f))
        particles.forEach { p ->
            val px = (p.x + p.vx * time) * size.width
            val py = (p.y + p.vy * time + 0.05f * time * time) * size.height
            if (py < -p.size || py > size.height + p.size) return@forEach
            rotate(p.spin * time, pivot = Offset(px, py)) {
                drawRect(
                    color = p.colour.copy(alpha = fade),
                    topLeft = Offset(px - p.size / 2, py - p.size / 4),
                    size = Size(p.size, p.size / 2),
                )
            }
        }
    }
}
