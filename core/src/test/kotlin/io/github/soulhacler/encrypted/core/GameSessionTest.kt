package io.github.soulhacler.encrypted.core

import io.github.soulhacler.encrypted.core.engine.CardOutcome
import io.github.soulhacler.encrypted.core.engine.GameConfig
import io.github.soulhacler.encrypted.core.engine.GameMode
import io.github.soulhacler.encrypted.core.engine.GameSession
import io.github.soulhacler.encrypted.core.engine.GameSession.CardFace
import io.github.soulhacler.encrypted.core.engine.GameSession.Phase
import io.github.soulhacler.encrypted.core.engine.PuzzleDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameSessionTest {
    private fun session(config: GameConfig, poolSize: Int = 200) =
        GameSession(config, PuzzleDeck(puzzles(poolSize), Random(9)))

    private val classic = GameConfig(GameMode.CLASSIC, listOf("Sam"))
    private val teams = GameConfig(GameMode.TEAM_BATTLE, listOf("Cyan", "Purple"), roundsPerParticipant = 3)

    private fun GameSession.correct() {
        reveal()
        markCorrect()
    }

    private fun GameSession.wrong() {
        reveal()
        markIncorrect()
    }

    // --- Round progression ---

    @Test
    fun `classic game is a single turn`() {
        val game = session(classic)
        assertEquals(1, game.totalTurns)
        assertEquals(Phase.READY, game.phase)
        game.startTurn()
        assertEquals(Phase.PLAYING, game.phase)
        assertNotNull(game.currentCard)
        game.endTurn()
        assertEquals(Phase.GAME_OVER, game.phase)
    }

    @Test
    fun `reveal then judge moves to a fresh hidden card`() {
        val game = session(classic)
        game.startTurn()
        val first = game.currentCard!!
        assertEquals(CardFace.HIDDEN, game.cardFace)

        game.reveal()
        assertEquals(CardFace.REVEALED, game.cardFace)
        assertEquals(first, game.currentCard)

        game.markCorrect()
        assertEquals(CardFace.HIDDEN, game.cardFace)
        assertTrue(game.currentCard != first)
        assertEquals(2, game.cardsDealtThisTurn)
    }

    @Test
    fun `can't judge before revealing or skip after revealing`() {
        val game = session(classic)
        game.startTurn()
        assertThrows(IllegalStateException::class.java) { game.markCorrect() }
        game.reveal()
        assertThrows(IllegalStateException::class.java) { game.skip() }
    }

    @Test
    fun `can't play outside a turn`() {
        val game = session(classic)
        assertThrows(IllegalStateException::class.java) { game.reveal() }
        assertThrows(IllegalStateException::class.java) { game.skip() }
        game.startTurn()
        assertThrows(IllegalStateException::class.java) { game.startTurn() }
        game.endTurn()
        assertThrows(IllegalStateException::class.java) { game.nextTurn() }
    }

    @Test
    fun `scores a turn`() {
        val game = session(classic)
        game.startTurn()
        game.correct()
        game.skip()
        game.correct()
        game.wrong()
        game.correct()
        assertEquals(3, game.scoreOf(0)) // live score during the turn
        val record = game.endTurn()

        assertEquals(3, record.score)
        assertEquals(
            listOf(CardOutcome.CORRECT, CardOutcome.SKIPPED, CardOutcome.CORRECT, CardOutcome.INCORRECT, CardOutcome.CORRECT),
            record.results.map { it.outcome },
        )
        assertEquals(3, game.scoreOf(0))
    }

    @Test
    fun `a card still hidden when time runs out is not scored`() {
        val game = session(classic)
        game.startTurn()
        game.correct()
        val record = game.endTurn(CardOutcome.CORRECT)
        assertEquals(1, record.results.size)
        assertEquals(1, record.score)
        assertNull(game.currentCard)
    }

    @Test
    fun `a revealed card can be judged after time runs out`() {
        val game = session(classic)
        game.startTurn()
        game.correct()
        game.reveal()
        val record = game.endTurn(CardOutcome.CORRECT)
        assertEquals(2, record.score)
    }

    @Test
    fun `no card repeats across the whole game`() {
        val game = session(GameConfig(GameMode.PASS_AND_PLAY, listOf("A", "B", "C"), roundsPerParticipant = 2), poolSize = 500)
        val seen = mutableListOf<String>()
        while (true) {
            game.startTurn()
            repeat(20) { i ->
                seen += game.currentCard!!.id
                if (i % 3 == 0) game.skip() else game.correct()
            }
            game.endTurn()
            if (game.phase == Phase.GAME_OVER) break
            game.nextTurn()
        }
        assertEquals(120, seen.size)
        assertEquals(seen.size, seen.toSet().size)
        assertTrue(game.dealtPuzzleIds.containsAll(seen))
    }

    @Test
    fun `party mode is one open-ended turn`() {
        val game = session(GameConfig(GameMode.PARTY, listOf("Everyone")), poolSize = 10)
        game.startTurn()
        repeat(25) { game.correct() } // more cards than the pool: reshuffles instead of stopping
        assertTrue(game.deckReshuffled)
        assertNotNull(game.currentCard)
        game.endTurn()
        assertEquals(Phase.GAME_OVER, game.phase)
    }

    // --- Team switching ---

    @Test
    fun `teams alternate every turn`() {
        val game = session(teams)
        assertEquals(6, game.totalTurns)
        val order = mutableListOf<Pair<String, Int>>()
        while (true) {
            order += game.currentParticipant to game.currentRound
            game.startTurn()
            game.endTurn()
            if (game.phase == Phase.GAME_OVER) break
            assertEquals(Phase.TURN_OVER, game.phase)
            game.nextTurn()
        }
        assertEquals(
            listOf("Cyan" to 1, "Purple" to 1, "Cyan" to 2, "Purple" to 2, "Cyan" to 3, "Purple" to 3),
            order,
        )
    }

    @Test
    fun `three teams rotate in order`() {
        val game = session(GameConfig(GameMode.TEAM_BATTLE, listOf("A", "B", "C"), roundsPerParticipant = 2))
        val order = mutableListOf<Int>()
        while (true) {
            order += game.currentParticipantIndex
            assertEquals(if (game.isLastTurn) null else (game.currentParticipantIndex + 1) % 3, game.nextParticipantIndex)
            game.startTurn()
            game.endTurn()
            if (game.phase == Phase.GAME_OVER) break
            game.nextTurn()
        }
        assertEquals(listOf(0, 1, 2, 0, 1, 2), order)
    }

    @Test
    fun `scores accumulate per team and standings rank them`() {
        val game = session(teams)
        val correctPerTurn = listOf(2, 5, 4, 1, 0, 3) // Cyan: 2+4+0=6, Purple: 5+1+3=9
        correctPerTurn.forEachIndexed { turn, correct ->
            game.startTurn()
            repeat(correct) { game.correct() }
            game.skip()
            game.endTurn()
            if (turn < correctPerTurn.lastIndex) game.nextTurn()
        }
        assertEquals(6, game.scoreOf(0))
        assertEquals(9, game.scoreOf(1))

        val standings = game.standings()
        assertEquals(listOf("Purple", "Cyan"), standings.map { it.name })
        assertEquals(listOf(1, 2), standings.map { it.rank })
        assertEquals(3, standings[0].skipped)
        assertEquals(listOf("Purple"), game.winners().map { it.name })
        assertEquals(6, game.completedTurns.size)
    }

    @Test
    fun `tied teams share first place`() {
        val game = session(GameConfig(GameMode.TEAM_BATTLE, listOf("A", "B", "C"), roundsPerParticipant = 1))
        listOf(3, 3, 1).forEachIndexed { turn, correct ->
            game.startTurn()
            repeat(correct) { game.correct() }
            game.endTurn()
            if (turn < 2) game.nextTurn()
        }
        assertEquals(listOf("A", "B"), game.winners().map { it.name })
        assertEquals(listOf(1, 1, 3), game.standings().map { it.rank })
    }

    @Test
    fun `pass and play keeps individual scores`() {
        val game = session(GameConfig(GameMode.PASS_AND_PLAY, listOf("Ana", "Ben", "Cat"), roundsPerParticipant = 1))
        listOf(1, 4, 2).forEachIndexed { turn, correct ->
            game.startTurn()
            repeat(correct) { game.correct() }
            game.wrong()
            game.endTurn()
            if (turn < 2) game.nextTurn()
        }
        assertEquals(listOf(1, 4, 2), (0..2).map { game.scoreOf(it) })
        assertEquals("Ben", game.winners().single().name)
        assertFalse(game.standings().any { it.incorrect != 1 })
    }

    // --- Config validation ---

    @Test
    fun `rejects invalid setups`() {
        assertThrows(IllegalArgumentException::class.java) { GameConfig(GameMode.TEAM_BATTLE, listOf("Solo")) }
        assertThrows(IllegalArgumentException::class.java) { GameConfig(GameMode.CLASSIC, listOf("A", "B")) }
        assertThrows(IllegalArgumentException::class.java) { GameConfig(GameMode.PASS_AND_PLAY, listOf("A", " ")) }
        assertThrows(IllegalArgumentException::class.java) { GameConfig(GameMode.CLASSIC, listOf("A"), roundSeconds = 5) }
        assertThrows(IllegalArgumentException::class.java) {
            GameConfig(GameMode.TEAM_BATTLE, listOf("A", "B"), roundsPerParticipant = 0)
        }
        assertThrows(IllegalArgumentException::class.java) { GameSession(classic, PuzzleDeck(emptyList())) }
    }
}
