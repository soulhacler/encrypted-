package io.github.soulhacler.encrypted.core

import io.github.soulhacler.encrypted.core.engine.CardOutcome
import io.github.soulhacler.encrypted.core.engine.CardResult
import io.github.soulhacler.encrypted.core.engine.Scoring
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {
    @Test
    fun `correct scores one and everything else scores nothing`() {
        assertEquals(1, CardOutcome.CORRECT.points)
        assertEquals(0, CardOutcome.INCORRECT.points)
        assertEquals(0, CardOutcome.SKIPPED.points)
    }

    @Test
    fun `totals a mixed round`() {
        val results = listOf(
            CardOutcome.CORRECT, CardOutcome.SKIPPED, CardOutcome.CORRECT,
            CardOutcome.INCORRECT, CardOutcome.CORRECT, CardOutcome.SKIPPED,
        ).mapIndexed { i, outcome -> CardResult(puzzle("p$i"), outcome) }

        assertEquals(3, Scoring.points(results))
        assertEquals(3, Scoring.count(results, CardOutcome.CORRECT))
        assertEquals(1, Scoring.count(results, CardOutcome.INCORRECT))
        assertEquals(2, Scoring.count(results, CardOutcome.SKIPPED))
    }

    @Test
    fun `empty round scores zero`() {
        assertEquals(0, Scoring.points(emptyList()))
    }
}
