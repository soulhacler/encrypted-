package io.github.soulhacler.encrypted.core.engine

import io.github.soulhacler.encrypted.core.model.Puzzle

/** What happened to a card the player saw. */
enum class CardOutcome(val points: Int) {
    CORRECT(1),
    INCORRECT(0),
    SKIPPED(0),
}

data class CardResult(val puzzle: Puzzle, val outcome: CardOutcome)

object Scoring {
    fun points(results: List<CardResult>): Int = results.sumOf { it.outcome.points }

    fun count(results: List<CardResult>, outcome: CardOutcome): Int =
        results.count { it.outcome == outcome }
}
