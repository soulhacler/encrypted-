package io.github.soulhacler.encrypted.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

fun formatClock(millis: Long): String {
    val seconds = ((millis + 999) / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}

/** Circular countdown. Turns purple, then red and pulsing, as time runs out. */
@Composable
fun TimerRing(
    timeLeftMillis: () -> Long,
    totalMillis: Long,
    modifier: Modifier = Modifier,
    diameter: Dp = 116.dp,
) {
    val left = timeLeftMillis()
    val fraction = (left.toFloat() / totalMillis).coerceIn(0f, 1f)
    val urgent = left <= 10_000
    val colour by animateColorAsState(
        when {
            urgent -> Neon.Red
            fraction < 0.5f -> Neon.Purple
            else -> Neon.Cyan
        },
        label = "timerColour",
    )
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "pulseScale",
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(diameter).scale(if (urgent && left > 0) pulse else 1f)) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.08f
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(Neon.Outline, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            drawArc(colour, -90f, 360f * fraction, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatClock(left),
                color = colour,
                fontFamily = EncryptedType.Mono,
                fontWeight = FontWeight.Black,
                fontSize = (diameter.value * 0.22f).sp,
            )
            Text("TIME LEFT", color = Neon.Muted, fontFamily = EncryptedType.Mono, fontSize = (diameter.value * 0.075f).sp)
        }
    }
}
