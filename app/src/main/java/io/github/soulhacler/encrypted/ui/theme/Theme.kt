package io.github.soulhacler.encrypted.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Neon {
    val Background = Color(0xFF0B0F19)
    val Surface = Color(0xFF121829)
    val SurfaceHigh = Color(0xFF1A2236)
    val Outline = Color(0xFF2A3550)
    val Cyan = Color(0xFF00E5FF)
    val Purple = Color(0xFFA855F7)
    val Red = Color(0xFFFF4560)
    val White = Color(0xFFFFFFFF)
    val Muted = Color(0xFF8A94B0)
    val Gold = Color(0xFFFFC94D)

    val Brand = Brush.linearGradient(listOf(Cyan, Purple))

    /** One accent per team or player, in turn order. */
    val TeamColours = listOf(Cyan, Purple, Red, Gold, Color(0xFF4DFFB0), Color(0xFFFF8A3D))

    fun teamColour(index: Int) = TeamColours[index % TeamColours.size]
}

/** Monospace for the cipher-style chrome; a heavy sans for anything that must be read aloud. */
object EncryptedType {
    val Mono = FontFamily.Monospace
    val Sans = FontFamily.SansSerif

    val Phrase = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    val Label = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, fontSize = 13.sp)
}

private val colours = darkColorScheme(
    primary = Neon.Cyan,
    onPrimary = Neon.Background,
    secondary = Neon.Purple,
    onSecondary = Neon.White,
    tertiary = Neon.Red,
    error = Neon.Red,
    background = Neon.Background,
    onBackground = Neon.White,
    surface = Neon.Surface,
    onSurface = Neon.White,
    surfaceVariant = Neon.SurfaceHigh,
    onSurfaceVariant = Neon.Muted,
    surfaceContainer = Neon.Surface,
    surfaceContainerHigh = Neon.SurfaceHigh,
    surfaceContainerHighest = Neon.SurfaceHigh,
    outline = Neon.Outline,
)

private val base = Typography()

private val typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black),
    displayMedium = base.displayMedium.copy(fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black),
    displaySmall = base.displaySmall.copy(fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Bold),
    labelLarge = base.labelLarge.copy(fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
)

@Composable
fun EncryptedTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colours, typography = typography, content = content)
}
