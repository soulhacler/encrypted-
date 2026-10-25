package io.github.soulhacler.encrypted.core.engine

import io.github.soulhacler.encrypted.core.model.Puzzle

/** One participant's finished turn. */
data class TurnRecord(
    val turnIndex: Int,
    val participantIndex: Int,
    val round: Int,
    val results: List<CardResult>,
) {
    val score: Int get() = Scoring.points(results)
}

data class Standing(
    val participantIndex: Int,
    val name: String,
    val score: Int,
    val correct: Int,
    val incorrect: Int,
    val skipped: Int,
    /** 1 for first place. Tied participants share a rank. */
    val rank: Int,
)

/**
 * The rules of a game, independent of any UI or clock.
 *
 * A game is a fixed sequence of turns. Participants take turns in order (A, B, C, A, B, C...),
 * so in Team Battle the teams alternate. Each turn: [startTurn], then any mix of [reveal] plus
 * [markCorrect]/[markIncorrect], or [skip]; then [endTurn] when time runs out. Between turns,
 * [nextTurn] hands over to the next participant.
 *
 * The session deals from one [PuzzleDeck] for the whole game, so no card repeats across turns.
 */
class GameSession(
    val config: GameConfig,
    private val deck: PuzzleDeck,
) {
    enum class Phase {
        /** Waiting for the current participant to start their turn. */
        READY,
        PLAYING,
        /** The turn has ended and another participant is up next. */
        TURN_OVER,
        GAME_OVER,
    }

    enum class CardFace { HIDDEN, REVEALED }

    init {
        require(!deck.isEmpty) { "No puzzles to play with" }
    }

    val participants: List<String> get() = config.participants
    val totalTurns: Int get() = config.totalTurns

    var phase: Phase = Phase.READY
        private set

    var turnIndex: Int = 0
        private set

    var currentCard: Puzzle? = null
        private set

    var cardFace: CardFace = CardFace.HIDDEN
        private set

    /** Cards dealt so far in this turn, including the one on screen. */
    var cardsDealtThisTurn: Int = 0
        private set

    private val turnResults = mutableListOf<CardResult>()
    private val completed = mutableListOf<TurnRecord>()

    val currentParticipantIndex: Int get() = turnIndex % participants.size
    val currentParticipant: String get() = participants[currentParticipantIndex]

    /** 1-based round number of the current turn. */
    val currentRound: Int get() = turnIndex / participants.size + 1

    val isLastTurn: Boolean get() = turnIndex == totalTurns - 1

    /** Who plays after the current turn, or null if this is the last turn. */
    val nextParticipantIndex: Int?
        get() = if (isLastTurn) null else (turnIndex + 1) % participants.size

    val currentTurnResults: List<CardResult> get() = turnResults.toList()
    val completedTurns: List<TurnRecord> get() = completed.toList()

    /** Every card dealt during this game; feed into the next game to avoid repeats. */
    val dealtPuzzleIds: Set<String> get() = deck.dealtIds

    /** True once the deck has had to reuse cards because the pool ran out. */
    val deckReshuffled: Boolean get() = deck.reshuffles > 0

    fun startTurn() {
        check(phase == Phase.READY) { "Can't start a turn in phase $phase" }
        phase = Phase.PLAYING
        turnResults.clear()
        cardsDealtThisTurn = 0
        dealNext()
    }

    fun reveal() {
        checkPlaying()
        cardFace = CardFace.REVEALED
    }

    fun markCorrect() = judge(CardOutcome.CORRECT)

    fun markIncorrect() = judge(CardOutcome.INCORRECT)

    /** Passes on the current card without revealing it. Scores nothing. */
    fun skip() {
        checkPlaying()
        check(cardFace == CardFace.HIDDEN) { "Card already revealed; mark it right or wrong" }
        record(CardOutcome.SKIPPED)
        dealNext()
    }

    /**
     * Ends the current turn. If time ran out after the answer was revealed, pass the player's
     * verdict as [finalOutcome] so the last card still counts. A card still hidden when time
     * runs out is simply not scored.
     */
    fun endTurn(finalOutcome: CardOutcome? = null): TurnRecord {
        checkPlaying()
        if (finalOutcome != null && cardFace == CardFace.REVEALED) record(finalOutcome)
        currentCard = null
        cardFace = CardFace.HIDDEN
        val record = TurnRecord(turnIndex, currentParticipantIndex, currentRound, turnResults.toList())
        completed += record
        phase = if (isLastTurn) Phase.GAME_OVER else Phase.TURN_OVER
        return record
    }

    fun nextTurn() {
        check(phase == Phase.TURN_OVER) { "Can't move on in phase $phase" }
        turnIndex++
        phase = Phase.READY
    }

    /** Total score for a participant, including the turn in progress. */
    fun scoreOf(participantIndex: Int): Int {
        val finished = completed.filter { it.participantIndex == participantIndex }.sumOf { it.score }
        val live = if (phase == Phase.PLAYING && participantIndex == currentParticipantIndex) {
            Scoring.points(turnResults)
        } else {
            0
        }
        return finished + live
    }

    /** Scores for everyone, best first. Ties share a rank and keep turn order. */
    fun standings(): List<Standing> {
        val rows = participants.indices.map { index ->
            val results = completed.filter { it.participantIndex == index }.flatMap { it.results }
            Standing(
                participantIndex = index,
                name = participants[index],
                score = scoreOf(index),
                correct = Scoring.count(results, CardOutcome.CORRECT),
                incorrect = Scoring.count(results, CardOutcome.INCORRECT),
                skipped = Scoring.count(results, CardOutcome.SKIPPED),
                rank = 0,
            )
        }.sortedWith(compareByDescending<Standing> { it.score }.thenBy { it.participantIndex })
        return rows.map { row -> row.copy(rank = 1 + rows.count { it.score > row.score }) }
    }

    /** Everyone sharing first place. More than one means a draw. */
    fun winners(): List<Standing> = standings().filter { it.rank == 1 }

    private fun judge(outcome: CardOutcome) {
        checkPlaying()
        check(cardFace == CardFace.REVEALED) { "Reveal the answer first" }
        record(outcome)
        dealNext()
    }

    private fun record(outcome: CardOutcome) {
        val card = checkNotNull(currentCard)
        turnResults += CardResult(card, outcome)
    }

    private fun dealNext() {
        currentCard = deck.draw()
        cardFace = CardFace.HIDDEN
        cardsDealtThisTurn++
    }

    private fun checkPlaying() {
        check(phase == Phase.PLAYING) { "Not playing (phase $phase)" }
    }
}
