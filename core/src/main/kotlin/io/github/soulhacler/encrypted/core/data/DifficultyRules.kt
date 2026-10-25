package io.github.soulhacler.encrypted.core.data

import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.core.model.Puzzle

/**
 * Objective limits on how hard a puzzle may claim to be, based on how well its spelling
 * disguises the answer. A puzzle may always be labelled easier than these rules allow
 * (some well-known phrases click instantly), but never harder.
 *
 * - **Easy**: at least two words, no answer word visible, spelling clearly different.
 * - **Medium**: as easy, more disguised, and the answer is two words or a long word.
 * - **Hard**: heavily disguised and a longer answer (3+ words or 12+ letters), or moderately
 *   disguised and long (4+ words or 16+ letters), where the re-split across many words is
 *   what makes it hard.
 */
object DifficultyRules {
    const val EASY_MAX_SIMILARITY = 0.84
    const val MEDIUM_MAX_SIMILARITY = 0.78
    const val HARD_MAX_SIMILARITY = 0.66
    const val LONG_HARD_MAX_SIMILARITY = 0.75

    /** Short words that give nothing away when they appear in both phrases. */
    private val stopWords = setOf(
        "the", "and", "of", "a", "an", "to", "in", "on", "is", "it", "its",
        "my", "me", "you", "your", "for", "at", "be",
    )

    /** The hardest difficulty this puzzle may be labelled, or null if it gives the answer away. */
    fun hardestAllowed(puzzle: Puzzle): Difficulty? {
        val similarity = similarity(puzzle.encrypted, puzzle.answer)
        val shared = sharedWords(puzzle).size
        val answerWords = Regex("[A-Za-z']+").findAll(puzzle.answer).count()
        val answerLetters = PuzzlePackParser.normalise(puzzle.answer).length
        val encryptedWords = puzzle.encrypted.trim().split(Regex("\\s+")).size

        return when {
            encryptedWords < 2 || shared > 0 -> null
            (similarity <= HARD_MAX_SIMILARITY && (answerWords >= 3 || answerLetters >= 12)) ||
                (similarity <= LONG_HARD_MAX_SIMILARITY && (answerWords >= 4 || answerLetters >= 16)) -> Difficulty.HARD
            similarity <= MEDIUM_MAX_SIMILARITY && (answerWords >= 2 || answerLetters >= 8) -> Difficulty.MEDIUM
            similarity <= EASY_MAX_SIMILARITY -> Difficulty.EASY
            else -> null
        }
    }

    /** Meaningful words (3+ letters, not stop words) that appear in both phrases. */
    fun sharedWords(puzzle: Puzzle): Set<String> = contentWords(puzzle.encrypted) intersect contentWords(puzzle.answer)

    private fun contentWords(text: String): Set<String> =
        Regex("[a-z]+").findAll(text.lowercase().replace("'", ""))
            .map { it.value }
            .filter { it.length >= 3 && it !in stopWords }
            .toSet()

    /**
     * How alike two phrases look on the page, from 0 (nothing in common) to 1 (identical),
     * ignoring case, spaces and punctuation. This is the Ratcliff/Obershelp ratio, the same
     * measure as Python's difflib.SequenceMatcher, so authoring scripts agree with this test.
     */
    fun similarity(a: String, b: String): Double {
        val x = PuzzlePackParser.normalise(a)
        val y = PuzzlePackParser.normalise(b)
        if (x.isEmpty() && y.isEmpty()) return 1.0
        return 2.0 * matchingCharacters(x, 0, x.length, y, 0, y.length) / (x.length + y.length)
    }

    private fun matchingCharacters(a: String, aLo: Int, aHi: Int, b: String, bLo: Int, bHi: Int): Int {
        // Longest common block; ties go to the earliest position in a, then in b (as difflib).
        var bestI = aLo
        var bestJ = bLo
        var bestLength = 0
        for (i in aLo until aHi) {
            for (j in bLo until bHi) {
                var k = 0
                while (i + k < aHi && j + k < bHi && a[i + k] == b[j + k]) k++
                if (k > bestLength) {
                    bestI = i
                    bestJ = j
                    bestLength = k
                }
            }
        }
        if (bestLength == 0) return 0
        return bestLength +
            matchingCharacters(a, aLo, bestI, b, bLo, bestJ) +
            matchingCharacters(a, bestI + bestLength, aHi, b, bestJ + bestLength, bHi)
    }
}
