package io.github.soulhacler.encrypted

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.EncryptedApp
import io.github.soulhacler.encrypted.ui.theme.EncryptedTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            EncryptedTheme {
                EncryptedApp(viewModel)
            }
        }
    }
}
