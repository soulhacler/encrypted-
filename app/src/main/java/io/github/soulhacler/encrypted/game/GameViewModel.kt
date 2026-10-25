package io.github.soulhacler.encrypted.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.soulhacler.encrypted.EncryptedApplication
import io.github.soulhacler.encrypted.core.data.PuzzleLibrary
import io.github.soulhacler.encrypted.core.engine.CardOutcome
import io.github.soulhacler.encrypted.core.engine.GameConfig
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.core.engine.GameSession
import io.github.soulhacler.encrypted.core.engine.GameSession.CardFace
import io.github.soulhacler.encrypted.core.engine.GameSession.Phase
import io.github.soulhacler.encrypted.core.engine.PuzzleDeck
import io.github.soulhacler.encrypted.core.engine.RoundTimer
import io.github.soulhacler.encrypted.core.engine.Scoring
import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.data.Settings
import io.github.soulhacler.encrypted.feedback.Buzz
import io.github.soulhacler.encrypted.feedback.Sound
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Runs the app: navigation, setup choices, and the game in progress.
 *
 * The rules live in [GameSession] (in :core); this class wires them to the clock, sounds,
 * haptics and settings, and exposes immutable snapshots for the UI to draw.
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val container = (app as EncryptedApplication).container
    private val settingsRepo = container.settings

    var screen by mutableStateOf(Screen.SPLASH)
        private set
    var library by mutableStateOf<PuzzleLibrary?>(null)
        private set
    var loadError by mutableStateOf<String?>(null)
        private set
    var settings by mutableStateOf(Settings())
        private set
    var setup by mutableStateOf(SetupState())
        private set
    var handoff by mutableStateOf<HandoffState?>(null)
        private set
    var play by mutableStateOf<PlayState?>(null)
        private set

    /** 3, 2, 1, then 0 for "GO". Null when no countdown is running. */
    var countdown by mutableStateOf<Int?>(null)
        private set
    var judgement by mutableStateOf<Judgement?>(null)
        private set
    var turnSummary by mutableStateOf<TurnSummary?>(null)
        private set
    var results by mutableStateOf<GameResults?>(null)
        private set

    // Kept separate from [play] so only the clock redraws on every tick.
    private val timeLeft = mutableLongStateOf(0L)
    val timeLeftMillis: Long get() = timeLeft.longValue

    private var session: GameSession? = null
    private var timer: RoundTimer? = null
    private var tickJob: Job? = null
    private var countdownJob: Job? = null
    private var judgementSeq = 0
    private var lastWarningSecond = -1

    /** Every card shown since the app started, so rematches and new games get fresh cards. */
    private val seenPuzzleIds = LinkedHashSet<String>()

    init {
        container.sounds // start loading sound effects now so the first one isn't silent
        viewModelScope.launch { settingsRepo.settings.collect { settings = it } }
        viewModelScope.launch {
            runCatching { container.puzzles.library() }
                .onSuccess { library = it }
                .onFailure { loadError = it.message ?: "Couldn't load puzzles" }
        }
    }

    // ---------------------------------------------------------------- navigation

    fun finishSplash() {
        if (screen == Screen.SPLASH && library != null) screen = Screen.HOME
    }

    fun openHome() {
        stopClock()
        session = null
        play = null
        handoff = null
        turnSummary = null
        results = null
        screen = Screen.HOME
    }

    fun openSettings() {
        screen = Screen.SETTINGS
    }

    fun openHowToPlay() {
        screen = Screen.HOW_TO_PLAY
    }

    fun openSetup(mode: GameMode) {
        val names = when (mode) {
            GameMode.CLASSIC -> listOf("You")
            GameMode.PARTY -> listOf("Everyone")
            GameMode.TEAM_BATTLE -> settings.teamNames.validFor(mode) ?: List(2) { defaultName(mode, it) }
            GameMode.PASS_AND_PLAY -> settings.playerNames.validFor(mode) ?: List(2) { defaultName(mode, it) }
        }
        setup = setup.copy(
            mode = mode,
            names = names,
            rounds = mode.defaultRounds,
            roundSeconds = settings.roundSeconds,
        )
        screen = Screen.SETUP
    }

    /** Back to setup with the same choices, for a different game. */
    fun newGame() {
        stopClock()
        session = null
        results = null
        screen = Screen.SETUP
    }

    // ---------------------------------------------------------------- setup

    fun selectCategory(id: String) {
        setup = setup.copy(categoryId = id)
    }

    fun selectDifficulty(difficulty: Difficulty?) {
        setup = setup.copy(difficulty = difficulty)
    }

    fun selectRoundSeconds(seconds: Int) {
        setup = setup.copy(roundSeconds = seconds.coerceIn(GameConfig.MIN_ROUND_SECONDS, GameConfig.MAX_ROUND_SECONDS))
    }

    fun selectRounds(rounds: Int) {
        setup = setup.copy(rounds = rounds.coerceIn(1, setup.mode.maxRounds))
    }

    fun renameParticipant(index: Int, name: String) {
        setup = setup.copy(names = setup.names.mapIndexed { i, old -> if (i == index) name.take(MAX_NAME_LENGTH) else old })
    }

    fun addParticipant() {
        val s = setup
        if (s.names.size >= s.mode.maxParticipants) return
        var n = s.names.size
        while (defaultName(s.mode, n) in s.names) n++
        setup = s.copy(names = s.names + defaultName(s.mode, n))
    }

    fun removeParticipant(index: Int) {
        val s = setup
        if (s.names.size <= s.mode.minParticipants) return
        setup = s.copy(names = s.names.filterIndexed { i, _ -> i != index })
    }

    /** Why the game can't start yet, or null if it can. */
    val setupProblem: String?
        get() {
            val s = setup
            val lib = library ?: return "Loading puzzles…"
            val names = s.names.map { it.trim() }
            val noun = s.mode.participantNoun
            return when {
                names.any { it.isEmpty() } -> "Every $noun needs a name"
                names.map { it.lowercase() }.toSet().size != names.size -> "Each $noun needs a different name"
                lib.count(s.categoryId, s.difficulty) == 0 -> "No puzzles for that category and difficulty"
                else -> null
            }
        }

    fun startGame() {
        val lib = library ?: return
        if (setupProblem != null) return
        val s = setup
        val names = s.names.map { it.trim() }
        val config = GameConfig(s.mode, names, s.rounds, s.roundSeconds, s.categoryId, s.difficulty)
        viewModelScope.launch {
            when (s.mode) {
                GameMode.TEAM_BATTLE -> settingsRepo.setTeamNames(names)
                GameMode.PASS_AND_PLAY -> settingsRepo.setPlayerNames(names)
                else -> Unit
            }
        }
        startSession(config, lib)
    }

    /** Same players, same settings, fresh cards. */
    fun rematch() {
        val config = session?.config ?: return
        startSession(config, library ?: return)
    }

    private fun startSession(config: GameConfig, lib: PuzzleLibrary) {
        val pool = lib.pool(config.categoryId, config.difficulty)
        if (pool.isEmpty()) return
        stopClock()
        session = GameSession(config, PuzzleDeck(pool, seenIds = seenPuzzleIds))
        turnSummary = null
        results = null
        judgement = null
        if (config.mode.timed) showHandoff() else beginTurn()
    }

    // ---------------------------------------------------------------- turns

    private fun showHandoff() {
        val game = session ?: return
        handoff = HandoffState(
            mode = game.config.mode,
            participantIndex = game.currentParticipantIndex,
            participantName = game.currentParticipant,
            round = game.currentRound,
            totalRounds = game.config.roundsPerParticipant,
            turnNumber = game.turnIndex + 1,
            totalTurns = game.totalTurns,
            roundSeconds = game.config.roundSeconds,
            standings = game.standings(),
        )
        countdown = null
        screen = Screen.HANDOFF
    }

    /** "3, 2, 1, GO!" then the turn starts. */
    fun startCountdown() {
        if (countdownJob?.isActive == true || session?.phase != Phase.READY) return
        countdownJob = viewModelScope.launch {
            for (n in 3 downTo 1) {
                countdown = n
                sound(Sound.TICK)
                buzz(Buzz.TAP)
                delay(COUNTDOWN_STEP_MILLIS)
            }
            countdown = 0
            sound(Sound.GO)
            buzz(Buzz.CORRECT)
            delay(GO_MILLIS)
            countdown = null
            beginTurn()
        }
    }

    private fun beginTurn() {
        val game = session ?: return
        if (game.phase != Phase.READY) return
        game.startTurn()
        judgement = null
        if (game.config.mode.timed) {
            val total = game.config.roundSeconds * 1000L
            timer = RoundTimer(total).also { it.start() }
            timeLeft.longValue = total
            lastWarningSecond = -1
            startTicker()
        } else {
            timer = null
        }
        play = snapshot(paused = false, timeUp = false)
        screen = Screen.PLAY
    }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                val t = timer ?: break
                timeLeft.longValue = t.remainingMillis()
                val seconds = t.secondsRemaining()
                if (seconds in 1..WARNING_SECONDS && seconds != lastWarningSecond) {
                    lastWarningSecond = seconds
                    if (settings.countdownSounds) sound(if (seconds <= 3) Sound.TICK_FINAL else Sound.TICK)
                    buzz(Buzz.WARNING)
                }
                if (t.isExpired) {
                    onTimeUp()
                    break
                }
                // The remaining time is always read from the clock, so this rate only affects
                // how smoothly the ring moves, never how long the round lasts.
                delay(TICK_MILLIS)
            }
        }
    }

    private fun onTimeUp() {
        val game = session ?: return
        timer?.finish()
        timeLeft.longValue = 0
        sound(Sound.TIME_UP)
        buzz(Buzz.TIME_UP)
        if (game.cardFace == CardFace.REVEALED) {
            // They were mid-answer: let them say whether they got it.
            play = snapshot(paused = false, timeUp = true)
        } else {
            finishTurn(finalOutcome = null, timeRanOut = true)
        }
    }

    fun reveal() {
        val game = session ?: return
        val p = play ?: return
        if (p.paused || p.timeUp || game.phase != Phase.PLAYING || game.cardFace != CardFace.HIDDEN) return
        game.reveal()
        sound(Sound.REVEAL)
        buzz(Buzz.TAP)
        play = snapshot(paused = false, timeUp = false)
    }

    fun markCorrect() = judge(CardOutcome.CORRECT)

    fun markIncorrect() = judge(CardOutcome.INCORRECT)

    private fun judge(outcome: CardOutcome) {
        val game = session ?: return
        val p = play ?: return
        if (p.paused || game.phase != Phase.PLAYING || game.cardFace != CardFace.REVEALED) return
        announce(outcome)
        if (p.timeUp) {
            finishTurn(finalOutcome = outcome, timeRanOut = true)
            return
        }
        if (outcome == CardOutcome.CORRECT) game.markCorrect() else game.markIncorrect()
        play = snapshot(paused = false, timeUp = false)
    }

    fun skip() {
        val game = session ?: return
        val p = play ?: return
        if (p.paused || p.timeUp || game.phase != Phase.PLAYING || game.cardFace != CardFace.HIDDEN) return
        game.skip()
        announce(CardOutcome.SKIPPED)
        play = snapshot(paused = false, timeUp = false)
    }

    /** Party mode's "we're done" button. */
    fun endParty() {
        if (session?.phase == Phase.PLAYING) finishTurn(finalOutcome = null, timeRanOut = false)
    }

    private fun announce(outcome: CardOutcome) {
        val competitive = session?.config?.mode?.competitive ?: true
        // In Party mode "Next" isn't a failure, so it gets a neutral swoosh instead of a buzzer.
        val shown = if (!competitive && outcome == CardOutcome.INCORRECT) CardOutcome.SKIPPED else outcome
        judgement = Judgement(shown, ++judgementSeq)
        when (shown) {
            CardOutcome.CORRECT -> {
                sound(Sound.CORRECT)
                buzz(Buzz.CORRECT)
            }
            CardOutcome.INCORRECT -> {
                sound(Sound.INCORRECT)
                buzz(Buzz.INCORRECT)
            }
            CardOutcome.SKIPPED -> {
                sound(Sound.SKIP)
                buzz(Buzz.TAP)
            }
        }
    }

    private fun finishTurn(finalOutcome: CardOutcome?, timeRanOut: Boolean) {
        val game = session ?: return
        if (game.phase != Phase.PLAYING) return
        stopClock()
        val record = game.endTurn(finalOutcome)
        seenPuzzleIds += game.dealtPuzzleIds
        play = null
        if (game.phase == Phase.GAME_OVER) {
            showResults(game)
        } else {
            turnSummary = TurnSummary(
                record = record,
                name = game.participants[record.participantIndex],
                nextName = game.nextParticipantIndex?.let { game.participants[it] },
                standings = game.standings(),
                timeRanOut = timeRanOut,
            )
            screen = Screen.TURN_SUMMARY
        }
    }

    fun nextTurn() {
        val game = session ?: return
        if (game.phase != Phase.TURN_OVER) return
        game.nextTurn()
        turnSummary = null
        showHandoff()
    }

    private fun showResults(game: GameSession) {
        val config = game.config
        val previousBest = settings.classicBest[config.roundSeconds]
        results = GameResults(
            mode = config.mode,
            standings = game.standings(),
            winners = game.winners(),
            turns = game.completedTurns,
            roundSeconds = config.roundSeconds,
            previousBest = previousBest,
        )
        screen = Screen.RESULTS
        if (config.mode.competitive) sound(Sound.VICTORY)
        if (config.mode == GameMode.CLASSIC) {
            val score = game.scoreOf(0)
            viewModelScope.launch {
                if (score > 0 && settingsRepo.submitClassicScore(config.roundSeconds, score)) {
                    results = results?.copy(newBest = true)
                }
            }
        }
    }

    // ---------------------------------------------------------------- interruptions

    fun pause() {
        val p = play ?: return
        if (p.paused || p.timeUp || !p.mode.timed) return
        timer?.pause()
        tickJob?.cancel()
        play = p.copy(paused = true)
    }

    fun resume() {
        val p = play ?: return
        if (!p.paused) return
        play = p.copy(paused = false)
        timer?.resume()
        startTicker() // notices straight away if time ran out while paused
    }

    /** Called when the app leaves the foreground (call, notification shade, home button). */
    fun onAppBackgrounded() {
        if (countdownJob?.isActive == true) {
            countdownJob?.cancel()
            countdown = null
        }
        pause()
    }

    private fun stopClock() {
        tickJob?.cancel()
        countdownJob?.cancel()
        countdown = null
        timer?.finish()
        timer = null
    }

    private fun snapshot(paused: Boolean, timeUp: Boolean): PlayState {
        val game = checkNotNull(session)
        val config = game.config
        return PlayState(
            mode = config.mode,
            participantIndex = game.currentParticipantIndex,
            participantName = game.currentParticipant,
            round = game.currentRound,
            totalRounds = config.roundsPerParticipant,
            categoryName = library?.category(config.categoryId)?.name.orEmpty(),
            card = game.currentCard,
            face = game.cardFace,
            cardNumber = game.cardsDealtThisTurn,
            turnScore = Scoring.points(game.currentTurnResults),
            totalMillis = config.roundSeconds * 1000L,
            paused = paused,
            timeUp = timeUp,
        )
    }

    // ---------------------------------------------------------------- settings

    fun setDefaultRoundSeconds(seconds: Int) {
        viewModelScope.launch { settingsRepo.setRoundSeconds(seconds) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setSoundEnabled(enabled) }
    }

    fun setCountdownSounds(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setCountdownSounds(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setHapticsEnabled(enabled) }
        if (enabled) container.haptics.buzz(Buzz.CORRECT)
    }

    fun resetBestScores() {
        viewModelScope.launch { settingsRepo.resetBestScores() }
    }

    /** A tap sound for UI previews such as the how-to-play demo card. */
    fun playRevealPreview() {
        sound(Sound.REVEAL)
        buzz(Buzz.TAP)
    }

    private fun sound(sound: Sound) {
        if (settings.soundEnabled) container.sounds.play(sound)
    }

    private fun buzz(buzz: Buzz) {
        if (settings.hapticsEnabled) container.haptics.buzz(buzz)
    }

    private fun List<String>.validFor(mode: GameMode): List<String>? =
        take(mode.maxParticipants).takeIf { it.size >= mode.minParticipants }

    companion object {
        const val MAX_NAME_LENGTH = 18
        private const val TICK_MILLIS = 33L
        private const val WARNING_SECONDS = 5
        private const val COUNTDOWN_STEP_MILLIS = 800L
        private const val GO_MILLIS = 450L

        private val TEAM_NAMES = listOf("Cyan", "Purple", "Red", "Gold", "Mint", "Blaze")

        fun defaultName(mode: GameMode, index: Int): String = when (mode) {
            GameMode.TEAM_BATTLE -> "Team " + TEAM_NAMES.getOrElse(index) { "${index + 1}" }
            else -> "Player ${index + 1}"
        }
    }
}
