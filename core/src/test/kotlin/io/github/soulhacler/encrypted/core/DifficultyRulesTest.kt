package io.github.soulhacler.encrypted.core

import io.github.soulhacler.encrypted.core.data.DifficultyRules
import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.core.model.Puzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DifficultyRulesTest {
    private fun p(encrypted: String, answer: String) =
        Puzzle("T-001", encrypted, answer, "everyday", Difficulty.EASY)

    @Test
    fun `similarity matches Python difflib`() {
        // Expected values from difflib.SequenceMatcher(None, a, b).ratio() on normalised text.
        assertEquals(0.5, DifficultyRules.similarity("Olive yew", "I love you"), 1e-9)
        assertEquals(0.75, DifficultyRules.similarity("abcd", "bcda"), 1e-9)
        assertEquals(0.5454545454, DifficultyRules.similarity("Venus", "Venice"), 1e-9)
        assertEquals(
            0.7428571428,
            DifficultyRules.similarity("Hare reap otter rand the fill oss off us tone", "Harry Potter and the Philosopher's Stone"),
            1e-9,
        )
        assertEquals(1.0, DifficultyRules.similarity("Ice-cream!", "ice cream"), 1e-9)
    }

    @Test
    fun `a visible answer word gives the game away`() {
        assertNull(DifficultyRules.hardestAllowed(p("Bag cove the net", "Back of the net")))
        assertEquals(setOf("net"), DifficultyRules.sharedWords(p("Bag cove the net", "Back of the net")))
    }

    @Test
    fun `stop words may appear in both`() {
        assertEquals(emptySet<String>(), DifficultyRules.sharedWords(p("Shaw nov thud Ed", "Shaun of the Dead")))
    }

    @Test
    fun `a single sound-alike word is not a puzzle`() {
        assertNull(DifficultyRules.hardestAllowed(p("Venus", "Venice")))
    }

    @Test
    fun `barely respelled phrases are rejected`() {
        assertNull(DifficultyRules.hardestAllowed(p("Head dover heels", "Head over heels")))
        assertNull(DifficultyRules.hardestAllowed(p("Knotting ham", "Nottingham")))
    }

    @Test
    fun `short answers can't be hard however well disguised`() {
        assertEquals(Difficulty.MEDIUM, DifficultyRules.hardestAllowed(p("Ella font", "Elephant")))
    }

    @Test
    fun `long, well disguised phrases can be hard`() {
        assertEquals(Difficulty.HARD, DifficultyRules.hardestAllowed(p("Coo lazer queue come bar", "Cool as a cucumber")))
        assertEquals(
            Difficulty.HARD,
            DifficultyRules.hardestAllowed(p("Hare reap otter rand the fill oss off us tone", "Harry Potter and the Philosopher's Stone")),
        )
    }
}
