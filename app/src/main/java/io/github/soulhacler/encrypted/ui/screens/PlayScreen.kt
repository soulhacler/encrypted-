package io.github.soulhacler.encrypted.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.core.engine.CardOutcome
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.core.engine.GameSession.CardFace
import io.github.soulhacler.encrypted.game.GameViewModel
import io.github.soulhacler.encrypted.game.Judgement
import io.github.soulhacler.encrypted.game.PlayState
import io.github.soulhacler.encrypted.ui.components.ColourDot
import io.github.soulhacler.encrypted.ui.components.NeonButton
import io.github.soulhacler.encrypted.ui.components.ScrambleText
import io.github.soulhacler.encrypted.ui.components.TimerRing
import io.github.soulhacler.encrypted.ui.components.rememberLatest
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

@Composable
fun PlayScreen(vm: GameViewModel) {
    val state = rememberLatest(vm.play) ?: return
    var confirmEndParty by remember { mutableStateOf(false) }
    KeepScreenOn()
    BackHandler {
        if (state.mode.timed) vm.pause() else confirmEndParty = true
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val landscape = maxWidth > maxHeight * 1.15f
        val phraseWidth = if (landscape) maxWidth * 0.6f else maxWidth

        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            TopBar(state, timeLeft = { vm.timeLeftMillis }, onPause = vm::pause, onEnd = { confirmEndParty = true }, compact = landscape)
            if (landscape) {
                Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CardArea(state, phraseWidth, Modifier.weight(1f))
                    Spacer(Modifier.width(16.dp))
                    Controls(state, vm, Modifier.width(300.dp))
                }
            } else {
                CardArea(state, phraseWidth, Modifier.weight(1f).fillMaxWidth())
                Controls(state, vm, Modifier.fillMaxWidth().padding(bottom = 8.dp))
            }
        }

        JudgementFlash(vm.judgement)

        if (state.paused) PausedOverlay(onResume = vm::resume, onQuit = vm::openHome)
    }

    if (confirmEndParty) {
        QuitDialog(
            title = "END SESSION?",
            onConfirm = {
                confirmEndParty = false
                if (state.mode.timed) vm.openHome() else vm.endParty()
            },
            onDismiss = { confirmEndParty = false },
        )
    }
}

@Composable
private fun TopBar(state: PlayState, timeLeft: () -> Long, onPause: () -> Unit, onEnd: () -> Unit, compact: Boolean) {
    val colour = participantColour(state)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (state.mode.timed) {
            SmallPill("II", Neon.Muted, onPause, contentDescription = "Pause")
        } else {
            SmallPill("END", Neon.Gold, onEnd, contentDescription = "End session")
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.mode != GameMode.CLASSIC && state.mode != GameMode.PARTY) {
                    ColourDot(colour)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    when (state.mode) {
                        GameMode.CLASSIC -> "CLASSIC"
                        GameMode.PARTY -> "PARTY MODE"
                        else -> state.participantName.uppercase()
                    },
                    color = colour, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1,
                )
            }
            Text(
                if (state.mode.competitive) "SCORE ${state.turnScore}" else "${state.turnScore} CRACKED",
                color = Neon.White, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 26.sp,
            )
            if (state.categoryName.isNotEmpty()) {
                Text(state.categoryName.uppercase(), style = EncryptedType.Label.copy(fontSize = 11.sp), color = Neon.Muted)
            }
        }
        if (state.mode.timed) {
            TimerRing(timeLeftMillis = timeLeft, totalMillis = state.totalMillis, diameter = if (compact) 88.dp else 112.dp)
        }
    }
}

@Composable
private fun SmallPill(text: String, colour: Color, onClick: () -> Unit, contentDescription: String) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(shape)
            .border(1.5.dp, colour, shape)
            .clickable(onClickLabel = contentDescription, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(text, color = colour, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 16.sp)
    }
}

/** Font size that keeps a phrase big but on at most a few lines. */
private fun phraseSize(text: String, width: Dp): TextUnit {
    val base = when {
        text.length <= 10 -> 60f
        text.length <= 16 -> 52f
        text.length <= 24 -> 44f
        text.length <= 34 -> 38f
        text.length <= 44 -> 33f
        else -> 29f
    }
    val scale = (width.value / 380f).coerceIn(0.75f, 1.5f)
    return (base * scale).sp
}

@Composable
private fun CardArea(state: PlayState, width: Dp, modifier: Modifier) {
    val card = state.card ?: return
    AnimatedContent(
        targetState = card,
        transitionSpec = {
            (slideInHorizontally(tween(350, easing = FastOutSlowInEasing)) { it / 2 } + fadeIn(tween(300))) togetherWith
                (slideOutHorizontally(tween(250)) { -it / 2 } + fadeOut(tween(200)))
        },
        contentKey = { it.id },
        label = "card",
        modifier = modifier,
    ) { puzzle ->
        val revealed = state.face == CardFace.REVEALED && card.id == puzzle.id
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
        ) {
            Text(
                "// ENCRYPTED #${state.cardNumber.toString().padStart(2, '0')}  ·  ${puzzle.difficulty.label.uppercase()}",
                style = EncryptedType.Label.copy(fontSize = 11.sp),
                color = Neon.Muted,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "“${puzzle.encrypted.uppercase()}”",
                style = EncryptedType.Phrase.copy(
                    fontSize = phraseSize(puzzle.encrypted, width),
                    lineHeight = phraseSize(puzzle.encrypted, width) * 1.12f,
                    shadow = Shadow(Neon.Purple.copy(alpha = 0.45f), blurRadius = 30f),
                ),
                color = Neon.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 720.dp),
            )
            Spacer(Modifier.height(18.dp))
            if (revealed) {
                RevealedAnswer(puzzle.answer, width)
            } else {
                SayItOutLoud()
            }
        }
    }
}

@Composable
private fun SayItOutLoud() {
    val pulse by rememberInfiniteTransition(label = "say").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "sayAlpha",
    )
    Text(
        "SAY IT OUT LOUD!",
        style = EncryptedType.Label.copy(fontSize = 16.sp, letterSpacing = 4.sp),
        color = Neon.Cyan,
        modifier = Modifier.alpha(pulse),
    )
}

@Composable
private fun RevealedAnswer(answer: String, width: Dp) {
    val pop = remember(answer) { Animatable(0.6f) }
    LaunchedEffect(answer) {
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(pop.value)) {
        Box(
            Modifier
                .width(120.dp)
                .height(2.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, Neon.Cyan, Color.Transparent))),
        )
        Spacer(Modifier.height(12.dp))
        Text("DECRYPTED", style = EncryptedType.Label, color = Neon.Cyan)
        Spacer(Modifier.height(8.dp))
        val size = (phraseSize(answer, width).value * 0.85f).sp
        ScrambleText(
            text = answer.uppercase(),
            style = EncryptedType.Phrase.copy(
                fontSize = size,
                lineHeight = size * 1.1f,
                shadow = Shadow(Neon.Cyan.copy(alpha = 0.7f), blurRadius = 32f),
            ),
            color = Neon.Cyan,
            textAlign = TextAlign.Center,
            durationMillis = 650,
        )
    }
}

@Composable
private fun Controls(state: PlayState, vm: GameViewModel, modifier: Modifier) {
    val competitive = state.mode.competitive
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedVisibility(visible = state.timeUp, enter = expandVertically() + fadeIn()) {
            Text(
                "TIME'S UP! DID YOU GET THAT LAST ONE?",
                style = EncryptedType.Label.copy(fontSize = 15.sp),
                color = Neon.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            )
        }
        AnimatedContent(
            targetState = state.face,
            transitionSpec = { (slideInVertically { it / 3 } + fadeIn()) togetherWith fadeOut(tween(120)) },
            label = "controls",
        ) { face ->
            if (face == CardFace.HIDDEN) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeonButton("Reveal answer", vm::reveal, Modifier.fillMaxWidth(), colour = Neon.Cyan, height = 80.dp, fontSize = 22.sp)
                    NeonButton("Skip", vm::skip, Modifier.fillMaxWidth(), colour = Neon.Purple, filled = false, height = 60.dp)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeonButton(
                        if (competitive) "Correct" else "Got it", vm::markCorrect, Modifier.weight(1f),
                        colour = Neon.Cyan, height = 92.dp, fontSize = 20.sp, icon = Icons.Filled.Check,
                    )
                    NeonButton(
                        if (competitive) "Wrong" else "Next", vm::markIncorrect, Modifier.weight(1f),
                        colour = if (competitive) Neon.Red else Neon.Purple, filled = false, height = 92.dp, fontSize = 20.sp,
                        icon = if (competitive) Icons.Filled.Close else Icons.Filled.PlayArrow,
                    )
                }
            }
        }
    }
}

/** A quick "+1" / "✗" / "SKIP" burst over the card when an answer is recorded. */
@Composable
private fun JudgementFlash(judgement: Judgement?) {
    if (judgement == null) return
    val progress = remember(judgement.seq) { Animatable(0f) }
    LaunchedEffect(judgement.seq) { progress.animateTo(1f, tween(750)) }
    if (progress.value >= 1f) return
    val (text, colour) = when (judgement.outcome) {
        CardOutcome.CORRECT -> "+1" to Neon.Cyan
        CardOutcome.INCORRECT -> "✗" to Neon.Red
        CardOutcome.SKIPPED -> "SKIP" to Neon.Purple
    }
    val p = progress.value
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(colour.copy(alpha = 0.18f * (1f - p))),
        )
        Text(
            text,
            color = colour.copy(alpha = 1f - p * p),
            fontFamily = EncryptedType.Mono,
            fontWeight = FontWeight.Black,
            fontSize = 96.sp,
            style = TextStyle(shadow = Shadow(colour, blurRadius = 40f)),
            modifier = Modifier.scale(0.8f + p * 0.7f),
        )
    }
}

@Composable
private fun PausedOverlay(onResume: () -> Unit, onQuit: () -> Unit) {
    var confirmQuit by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Neon.Background.copy(alpha = 0.97f))
            // Swallow taps so nothing underneath can be pressed.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(
                "PAUSED", color = Neon.Cyan, fontFamily = EncryptedType.Mono, fontWeight = FontWeight.Black, fontSize = 48.sp,
                style = TextStyle(shadow = Shadow(Neon.Cyan.copy(alpha = 0.6f), blurRadius = 30f)),
            )
            Text("The clock is stopped and the card is hidden.", color = Neon.Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            NeonButton("Resume", onResume, Modifier.widthIn(max = 420.dp).fillMaxWidth(), icon = Icons.Filled.PlayArrow)
            Spacer(Modifier.height(12.dp))
            NeonButton("Quit game", { confirmQuit = true }, Modifier.widthIn(max = 420.dp).fillMaxWidth(), colour = Neon.Red, filled = false, height = 60.dp)
        }
    }
    if (confirmQuit) {
        QuitDialog(onConfirm = { confirmQuit = false; onQuit() }, onDismiss = { confirmQuit = false })
    }
}

private fun participantColour(state: PlayState): Color = when (state.mode) {
    GameMode.CLASSIC -> Neon.Cyan
    GameMode.PARTY -> Neon.Gold
    else -> Neon.teamColour(state.participantIndex)
}
