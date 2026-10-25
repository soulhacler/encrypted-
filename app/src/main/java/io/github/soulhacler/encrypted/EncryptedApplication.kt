package io.github.soulhacler.encrypted

import android.app.Application
import io.github.soulhacler.encrypted.data.PuzzleRepository
import io.github.soulhacler.encrypted.data.SettingsRepository
import io.github.soulhacler.encrypted.feedback.Haptics
import io.github.soulhacler.encrypted.feedback.SoundEffects

/** Holds the app-wide singletons. Simple manual dependency injection. */
class EncryptedApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(app: Application) {
    val puzzles = PuzzleRepository(app)
    val settings = SettingsRepository(app)
    val sounds by lazy { SoundEffects(app) }
    val haptics by lazy { Haptics(app) }
}
