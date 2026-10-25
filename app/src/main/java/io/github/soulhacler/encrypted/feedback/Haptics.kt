package io.github.soulhacler.encrypted.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class Buzz { TAP, CORRECT, INCORRECT, WARNING, TIME_UP }

/** Light, short vibrations. Uses the system's predefined effects where available. */
class Haptics(context: Context) {
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }

    fun buzz(type: Buzz) {
        val v = vibrator?.takeIf { it.hasVibrator() } ?: return
        val effect = when (type) {
            Buzz.TAP -> predefined(VibrationEffect.EFFECT_TICK) ?: oneShot(15)
            Buzz.CORRECT -> predefined(VibrationEffect.EFFECT_CLICK) ?: oneShot(35)
            Buzz.INCORRECT -> predefined(VibrationEffect.EFFECT_DOUBLE_CLICK) ?: waveform(0, 30, 60, 30)
            Buzz.WARNING -> predefined(VibrationEffect.EFFECT_TICK) ?: oneShot(20)
            Buzz.TIME_UP -> waveform(0, 120, 80, 120, 80, 250)
        }
        runCatching { v.vibrate(effect) }
    }

    private fun predefined(id: Int): VibrationEffect? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.createPredefined(id) else null

    private fun oneShot(ms: Long) = VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)

    private fun waveform(vararg timings: Long) = VibrationEffect.createWaveform(timings, -1)
}
