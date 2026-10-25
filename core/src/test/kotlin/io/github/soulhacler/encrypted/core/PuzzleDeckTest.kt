package io.github.soulhacler.encrypted.core

import io.github.soulhacler.encrypted.core.engine.PuzzleDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PuzzleDeckTest {
    @Test
    fun `deals every card exactly once before repeating`() {
        val pool = puzzles(50)
        val deck = PuzzleDeck(pool, Random(1))
        val dealt = List(50) { deck.draw()!!.id }

        assertEquals(50, dealt.toSet().size)
        assertEquals(pool.map { it.id }.toSet(), dealt.toSet())
        assertEquals(0, deck.reshuffles)
    }

    @Test
    fun `shuffles rather than dealing in database order`() {
        val pool = puzzles(50)
        val dealt = PuzzleDeck(pool, Random(42)).let { deck -> List(50) { deck.draw()!!.id } }
        assertNotEquals(pool.map { it.id }, dealt)
    }

    @Test
    fun `different seeds give different orders`() {
        val pool = puzzles(30)
        val a = PuzzleDeck(pool, Random(1)).let { d -> List(30) { d.draw()!!.id } }
        val b = PuzzleDeck(pool, Random(2)).let { d -> List(30) { d.draw()!!.id } }
        assertNotEquals(a, b)
    }

    @Test
    fun `cards seen in earlier games come last`() {
        val pool = puzzles(20)
        val seen = pool.take(15).map { it.id }.toSet()
        val deck = PuzzleDeck(pool, Random(7), seenIds = seen)
        val firstFive = List(5) { deck.draw()!!.id }.toSet()

        assertEquals(pool.drop(15).map { it.id }.toSet(), firstFive)
        val nextFifteen = List(15) { deck.draw()!!.id }.toSet()
        assertEquals(seen, nextFifteen)
    }

    @Test
    fun `reshuffles when the pool runs out instead of stopping`() {
        val deck = PuzzleDeck(puzzles(3), Random(3))
        val dealt = List(10) { deck.draw() }
        assertTrue(dealt.all { it != null })
        assertTrue(deck.reshuffles >= 3)
    }

    @Test
    fun `never deals the same card twice in a row across a reshuffle`() {
        repeat(200) { seed ->
            val deck = PuzzleDeck(puzzles(2), Random(seed))
            var previous = deck.draw()!!.id
            repeat(20) {
                val next = deck.draw()!!.id
                assertNotEquals("seed $seed", previous, next)
                previous = next
            }
        }
    }

    @Test
    fun `ignores duplicate ids in the pool`() {
        val pool = puzzles(5) + puzzles(5)
        val deck = PuzzleDeck(pool, Random(0))
        assertEquals(5, deck.poolSize)
        assertEquals(5, List(5) { deck.draw()!!.id }.toSet().size)
    }

    @Test
    fun `tracks dealt ids`() {
        val deck = PuzzleDeck(puzzles(10), Random(0))
        val ids = List(4) { deck.draw()!!.id }
        assertEquals(ids.toSet(), deck.dealtIds)
        assertEquals(6, deck.remainingBeforeReshuffle)
    }

    @Test
    fun `empty pool deals nothing`() {
        val deck = PuzzleDeck(emptyList())
        assertTrue(deck.isEmpty)
        assertNull(deck.draw())
    }
}
