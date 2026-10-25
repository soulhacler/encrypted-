package io.github.soulhacler.encrypted.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.components.EncryptedLogo
import io.github.soulhacler.encrypted.ui.components.ScrambleText
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(vm: GameViewModel) {
    var minimumShown by remember { mutableStateOf(false) }
    val lock = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        launch { lock.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
        launch { logoScale.animateTo(1f, tween(1400, easing = FastOutSlowInEasing)) }
        delay(2400)
        minimumShown = true
    }
    LaunchedEffect(minimumShown, vm.library) {
        if (minimumShown && vm.library != null) vm.finishSplash()
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                minimumShown = true
            },
    ) {
        val logoSize = (maxWidth.value / 7.5f).coerceIn(30f, 64f).sp
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            SplashLock(progress = lock.value)
            Spacer(Modifier.height(28.dp))
            EncryptedLogo(fontSize = logoSize, decryptMillis = 1300, startDelayMillis = 250, modifier = Modifier.scale(logoScale.value))
            Spacer(Modifier.height(18.dp))
            ScrambleText(
                text = "SAY IT. HEAR IT. CRACK IT.",
                style = EncryptedType.Label.copy(fontSize = 14.sp),
                color = Neon.Muted,
                durationMillis = 900,
                startDelayMillis = 1100,
                textAlign = TextAlign.Center,
            )
            vm.loadError?.let {
                Spacer(Modifier.height(24.dp))
                Text("Couldn't load puzzles: $it", color = Neon.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
            }
        }
    }
}

/** A padlock whose shackle swings open as [progress] goes from 0 to 1. */
@Composable
private fun SplashLock(progress: Float) {
    Canvas(Modifier.size(width = 72.dp, height = 88.dp).alpha(progress.coerceIn(0f, 1f))) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.09f
        val bodyTop = h * 0.45f
        val lift = h * 0.12f * progress
        // Shackle: lifts and pivots open as the code is cracked.
        drawArc(
            color = Neon.Cyan,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.22f, h * 0.08f - lift),
            size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.5f),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawLine(Neon.Cyan, Offset(w * 0.22f, h * 0.33f - lift), Offset(w * 0.22f, bodyTop - lift), stroke, StrokeCap.Round)
        drawLine(Neon.Cyan, Offset(w * 0.78f, h * 0.33f - lift), Offset(w * 0.78f, bodyTop - lift * 0.2f), stroke, StrokeCap.Round)
        // Body.
        drawRoundRect(
            color = Neon.Purple,
            topLeft = Offset(w * 0.06f, bodyTop),
            size = androidx.compose.ui.geometry.Size(w * 0.88f, h * 0.52f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f),
            style = Stroke(stroke * 0.7f),
        )
        // Sound wave inside.
        val bars = listOf(0.25f, 0.55f, 0.85f, 0.5f, 0.25f)
        bars.forEachIndexed { i, amp ->
            val x = w * (0.26f + i * 0.12f)
            val mid = bodyTop + h * 0.26f
            val half = h * 0.17f * amp * progress
            drawLine(Neon.Cyan, Offset(x, mid - half), Offset(x, mid + half), stroke * 0.65f, StrokeCap.Round)
        }
    }
}
