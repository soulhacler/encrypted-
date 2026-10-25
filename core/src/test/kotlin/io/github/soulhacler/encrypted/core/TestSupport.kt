package io.github.soulhacler.encrypted.core

import io.github.soulhacler.encrypted.core.engine.Clock
import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.core.model.Puzzle

/** A clock the test moves by hand. */
class FakeClock(var now: Long = 1_000L) : Clock {
    override fun nowMillis(): Long = now
    fun advance(millis: Long) {
        now += millis
    }
}

fun puzzle(
    id: String,
    category: String = "everyday",
    difficulty: Difficulty = Difficulty.EASY,
) = Puzzle(id = id, encrypted = "enc $id", answer = "ans $id", category = category, difficulty = difficulty)

fun puzzles(count: Int, category: String = "everyday", difficulty: Difficulty = Difficulty.EASY) =
    (1..count).map { puzzle("$category-$it", category, difficulty) }
