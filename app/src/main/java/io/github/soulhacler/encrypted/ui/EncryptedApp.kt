package io.github.soulhacler.encrypted.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.game.Screen
import io.github.soulhacler.encrypted.ui.components.CipherBackground
import io.github.soulhacler.encrypted.ui.screens.HandoffScreen
import io.github.soulhacler.encrypted.ui.screens.HomeScreen
import io.github.soulhacler.encrypted.ui.screens.HowToPlayScreen
import io.github.soulhacler.encrypted.ui.screens.PlayScreen
import io.github.soulhacler.encrypted.ui.screens.ResultsScreen
import io.github.soulhacler.encrypted.ui.screens.SettingsScreen
import io.github.soulhacler.encrypted.ui.screens.SetupScreen
import io.github.soulhacler.encrypted.ui.screens.SplashScreen
import io.github.soulhacler.encrypted.ui.screens.TurnSummaryScreen

@Composable
fun EncryptedApp(vm: GameViewModel) {
    val activity = LocalContext.current.findActivity()

    // Pause the clock when a call, the notification shade or the home button interrupts play.
    // Rotating the phone also pauses the activity, but shouldn't pause the game.
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        if (activity?.isChangingConfigurations != true) vm.onAppBackgrounded()
    }

    CipherBackground(intensity = if (vm.screen == Screen.PLAY) 0.6f else 1f) {
        AnimatedContent(
            targetState = vm.screen,
            transitionSpec = {
                (fadeIn(tween(320)) + scaleIn(tween(320), initialScale = 0.97f)) togetherWith fadeOut(tween(180))
            },
            label = "screen",
        ) { screen ->
            Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                when (screen) {
                    Screen.SPLASH -> SplashScreen(vm)
                    Screen.HOME -> HomeScreen(vm)
                    Screen.SETUP -> SetupScreen(vm)
                    Screen.HANDOFF -> HandoffScreen(vm)
                    Screen.PLAY -> PlayScreen(vm)
                    Screen.TURN_SUMMARY -> TurnSummaryScreen(vm)
                    Screen.RESULTS -> ResultsScreen(vm)
                    Screen.SETTINGS -> SettingsScreen(vm)
                    Screen.HOW_TO_PLAY -> HowToPlayScreen(vm)
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
