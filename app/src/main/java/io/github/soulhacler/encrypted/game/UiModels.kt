package io.github.soulhacler.encrypted.game

import io.github.soulhacler.encrypted.core.engine.CardOutcome
import io.github.soulhacler.encrypted.core.engine.GameConfig
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.core.engine.GameSession.CardFace
import io.github.soulhacler.encrypted.core.engine.Standing
import io.github.soulhacler.encrypted.core.engine.TurnRecord
import io.github.soulhacler.encrypted.core.model.Categories
import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.core.model.Puzzle

enum class Screen { SPLASH, HOME, SETUP, HANDOFF, PLAY, TURN_SUMMARY, RESULTS, SETTINGS, HOW_TO_PLAY }

/** What the player has picked on the setup screen so far. */
data class SetupState(
    val mode: GameMode = GameMode.CLASSIC,
    val categoryId: String = Categories.RANDOM_MIX.id,
    val difficulty: Difficulty? = null,
    val roundSeconds: Int = GameConfig.DEFAULT_ROUND_SECONDS,
    val names: List<String> = listOf("You"),
    val rounds: Int = 1,
)

/** Everything the play screen shows, apart from the clock (see GameViewModel.timeLeftMillis). */
data class PlayState(
    val mode: GameMode,
    val participantIndex: Int,
    val participantName: String,
    val round: Int,
    val totalRounds: Int,
    val categoryName: String,
    val card: Puzzle?,
    val face: CardFace,
    val cardNumber: Int,
    val turnScore: Int,
    val totalMillis: Long,
    val paused: Boolean = false,
    /** Time ran out while an answer was showing: waiting for a final right/wrong. */
    val timeUp: Boolean = false,
)

/** A right/wrong/skip just recorded; [seq] makes each one a distinct event for animations. */
data class Judgement(val outcome: CardOutcome, val seq: Int)

/** Who is about to play, shown on the hand-over screen. */
data class HandoffState(
    val mode: GameMode,
    val participantIndex: Int,
    val participantName: String,
    val round: Int,
    val totalRounds: Int,
    val turnNumber: Int,
    val totalTurns: Int,
    val roundSeconds: Int,
    val standings: List<Standing>,
)

data class TurnSummary(
    val record: TurnRecord,
    val name: String,
    val nextName: String?,
    val standings: List<Standing>,
    val timeRanOut: Boolean,
)

data class GameResults(
    val mode: GameMode,
    val standings: List<Standing>,
    val winners: List<Standing>,
    val turns: List<TurnRecord>,
    val roundSeconds: Int,
    /** Classic only: the best score before this game. */
    val previousBest: Int?,
    val newBest: Boolean = false,
)
