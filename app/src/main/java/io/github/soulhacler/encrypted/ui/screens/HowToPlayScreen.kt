package io.github.soulhacler.encrypted.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.ui.components.NeonButton
import io.github.soulhacler.encrypted.ui.components.ScrambleText
import io.github.soulhacler.encrypted.ui.components.ScreenHeader
import io.github.soulhacler.encrypted.ui.components.SectionLabel
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

private val examples = listOf(
    "Olive yew" to "I love you",
    "Eye scream" to "Ice cream",
    "Aisle bee back" to "I'll be back",
    "Hap pea bird day" to "Happy birthday",
)

@Composable
fun HowToPlayScreen(vm: GameViewModel) {
    BackHandler { vm.openHome() }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("How to play", onBack = vm::openHome, modifier = Modifier.padding(horizontal = 4.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                Text(
                    "Every card is a string of real words that sound like nonsense. Say them out loud, quickly, " +
                        "and they turn into a familiar phrase, person, place, film or song.",
                    color = Neon.White.copy(alpha = 0.9f), fontSize = 16.sp, lineHeight = 23.sp,
                )

                SectionLabel("Try one")
                DemoCard(onReveal = vm::playRevealPreview)

                SectionLabel("Each turn")
                Step(1, "Read the encrypted phrase aloud. Then again, faster.")
                Step(2, "Play with the stress and rhythm until it clicks.")
                Step(3, "Tap REVEAL ANSWER, then CORRECT or WRONG. Or SKIP a stinker.")
                Step(4, "Crack as many as you can before the clock runs out.")

                SectionLabel("Modes")
                GameMode.entries.forEach { mode ->
                    Row(Modifier.padding(bottom = 12.dp)) {
                        Text(mode.symbol(), color = mode.accent(), fontSize = 22.sp, modifier = Modifier.width(36.dp))
                        Column {
                            Text(mode.title.uppercase(), color = mode.accent(), fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black)
                            Text(mode.tagline, color = Neon.White.copy(alpha = 0.85f), fontSize = 14.sp)
                        }
                    }
                }

                SectionLabel("Scoring")
                Text("Correct: +1  ·  Wrong: 0  ·  Skip: 0", color = Neon.White, fontFamily = EncryptedType.Mono)
                Text(
                    "There's no speech recognition: you're the judge. Play fair!",
                    color = Neon.Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp),
                )

                SectionLabel("Top tips")
                Text(
                    "• Don't read the words, listen to the sounds.\n" +
                        "• A British accent helps: \"water\" is \"wor-tuh\".\n" +
                        "• Get someone else to read it to you.\n" +
                        "• Stuck? Skip it. There are plenty more.",
                    color = Neon.White.copy(alpha = 0.9f), fontSize = 15.sp, lineHeight = 24.sp,
                )
                Spacer(Modifier.height(28.dp))
                NeonButton("Got it", vm::openHome, Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Step(number: Int, text: String) {
    Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("0$number", color = Neon.Cyan, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.width(44.dp))
        Text(text, color = Neon.White, fontSize = 15.sp)
    }
}

@Composable
private fun DemoCard(onReveal: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    val (encrypted, answer) = examples[index]
    val shape = RoundedCornerShape(22.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Neon.Surface)
            .border(1.5.dp, Neon.Purple, shape)
            .clickable(role = Role.Button) {
                if (revealed) {
                    index = (index + 1) % examples.size
                    revealed = false
                } else {
                    revealed = true
                    onReveal()
                }
            }
            .padding(24.dp),
    ) {
        Text(
            "“${encrypted.uppercase()}”",
            style = EncryptedType.Phrase.copy(fontSize = 34.sp, shadow = Shadow(Neon.Purple.copy(alpha = 0.5f), blurRadius = 24f)),
            color = Neon.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        AnimatedContent(targetState = revealed, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "demo") { open ->
            if (open) {
                ScrambleText(
                    answer.uppercase(),
                    style = EncryptedType.Phrase.copy(fontSize = 28.sp, shadow = Shadow(Neon.Cyan.copy(alpha = 0.6f), blurRadius = 24f)),
                    color = Neon.Cyan,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text("TAP TO REVEAL", style = EncryptedType.Label, color = Neon.Cyan)
            }
        }
        if (revealed) {
            Text("Tap for another", color = Neon.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

