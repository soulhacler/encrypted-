package io.github.soulhacler.encrypted.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

/**
 * The ENCRYPTED wordmark: a cyan-to-purple gradient with a neon glow and an occasional
 * red/cyan "signal glitch". Decrypts itself when it first appears.
 */
@Composable
fun EncryptedLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 44.sp,
    decryptMillis: Int = 900,
    startDelayMillis: Int = 0,
) {
    val glitch by rememberInfiniteTransition(label = "glitch").animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4200
                0f at 3700
                3f at 3740
                -2f at 3800
                1.5f at 3860
                0f at 3920
            },
        ),
        label = "glitchOffset",
    )
    val base = TextStyle(
        fontFamily = EncryptedType.Mono,
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        letterSpacing = (fontSize.value * 0.12f).sp,
    )
    Box(modifier) {
        if (glitch != 0f) {
            Text("ENCRYPTED", style = base, color = Neon.Red.copy(alpha = 0.6f), modifier = Modifier.offset(x = (-glitch).dp))
            Text("ENCRYPTED", style = base, color = Neon.Cyan.copy(alpha = 0.6f), modifier = Modifier.offset(x = glitch.dp))
        }
        ScrambleText(
            text = "ENCRYPTED",
            style = base.copy(brush = Neon.Brand, shadow = Shadow(Neon.Cyan.copy(alpha = 0.55f), blurRadius = 28f)),
            durationMillis = decryptMillis,
            startDelayMillis = startDelayMillis,
        )
    }
}
