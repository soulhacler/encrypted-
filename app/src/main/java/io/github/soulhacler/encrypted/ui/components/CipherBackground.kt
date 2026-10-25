package io.github.soulhacler.encrypted.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon
import kotlin.random.Random

private const val COLUMNS = 14
private const val GLYPHS_PER_COLUMN = 36
private const val CIPHER_GLYPHS = "ABCDEF0123456789#%&*+=<>/\\?!$@"

/**
 * The game's backdrop: slow columns of faint cipher glyphs drifting down a dark screen.
 * Deliberately low contrast so it never competes with the text on top.
 */
@Composable
fun CipherBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(Neon.Background)) {
        CipherRain(Modifier.fillMaxSize(), intensity)
        content()
    }
}

@Composable
private fun CipherRain(modifier: Modifier, intensity: Float) {
    val measurer = rememberTextMeasurer()
    val layouts = remember(measurer) {
        val random = Random(2024)
        val style = TextStyle(fontFamily = EncryptedType.Mono, fontSize = 14.sp, lineHeight = 20.sp)
        List(COLUMNS) {
            val column = List(GLYPHS_PER_COLUMN) { CIPHER_GLYPHS[random.nextInt(CIPHER_GLYPHS.length)] }
            measurer.measure(column.joinToString("\n"), style)
        }
    }
    val speeds = remember { List(COLUMNS) { 0.6f + Random(it * 31).nextFloat() * 0.9f } }
    val offsets = remember { List(COLUMNS) { Random(it * 17 + 3).nextFloat() } }

    val transition = rememberInfiniteTransition(label = "cipherRain")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 40_000, easing = LinearEasing)),
        label = "progress",
    )

    Canvas(modifier) {
        val columnWidth = size.width / COLUMNS
        layouts.forEachIndexed { i, layout ->
            val travel = size.height + layout.size.height
            val y = ((progress * speeds[i] * 2f + offsets[i]) % 1f) * travel - layout.size.height
            val colour = if (i % 3 == 1) Neon.Purple else Neon.Cyan
            drawText(
                textLayoutResult = layout,
                color = colour,
                topLeft = Offset(i * columnWidth + (columnWidth - layout.size.width) / 2f, y),
                alpha = (0.05f + 0.04f * (i % 2)) * intensity,
            )
        }
        // Darken the edges so content in the middle stands out.
        drawRect(
            Brush.radialGradient(
                colors = listOf(Color.Transparent, Neon.Background.copy(alpha = 0.85f)),
                center = center,
                radius = size.maxDimension * 0.75f,
            ),
        )
    }
}
