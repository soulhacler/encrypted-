package io.github.soulhacler.encrypted.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val SCRAMBLE_GLYPHS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#%&@$?!0123456789"

/**
 * Text that "decrypts" itself: characters start as random glyphs and lock into place left to
 * right. Spaces and punctuation are shown straight away so word shapes are visible early.
 */
@Composable
fun ScrambleText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    durationMillis: Int = 700,
    startDelayMillis: Int = 0,
    maxLines: Int = Int.MAX_VALUE,
) {
    var shown by remember(text) { mutableStateOf(scramble(text, 0f)) }

    LaunchedEffect(text) {
        if (startDelayMillis > 0) delay(startDelayMillis.toLong())
        val start = withFrameMillis { it }
        var lastSwap = 0L
        while (true) {
            val now = withFrameMillis { it }
            val progress = ((now - start).toFloat() / durationMillis).coerceIn(0f, 1f)
            if (progress >= 1f) break
            if (now - lastSwap >= 45) {
                shown = scramble(text, progress)
                lastSwap = now
            }
        }
        shown = text
    }

    Text(
        text = shown,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        modifier = modifier.semantics { contentDescription = text },
    )
}

private fun scramble(text: String, progress: Float): String {
    val locked = (progress * text.length).toInt()
    return buildString(text.length) {
        text.forEachIndexed { i, c ->
            append(if (i < locked || !c.isLetterOrDigit()) c else SCRAMBLE_GLYPHS[Random.nextInt(SCRAMBLE_GLYPHS.length)])
        }
    }
}
