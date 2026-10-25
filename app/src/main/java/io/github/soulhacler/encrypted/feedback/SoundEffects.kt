package io.github.soulhacler.encrypted.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import io.github.soulhacler.encrypted.R

enum class Sound(val res: Int) {
    TICK(R.raw.tick),
    TICK_FINAL(R.raw.tick_final),
    GO(R.raw.go),
    REVEAL(R.raw.reveal),
    CORRECT(R.raw.correct),
    INCORRECT(R.raw.incorrect),
    SKIP(R.raw.skip),
    TIME_UP(R.raw.time_up),
    VICTORY(R.raw.victory),
}

/** Low-latency playback of the short effects in res/raw. Plays through the media volume. */
class SoundEffects(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val ids: Map<Sound, Int> = Sound.entries.associateWith { pool.load(context, it.res, 1) }

    fun play(sound: Sound, volume: Float = 1f) {
        val id = ids[sound] ?: return
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    fun pauseAll() = pool.autoPause()

    fun resumeAll() = pool.autoResume()
}
