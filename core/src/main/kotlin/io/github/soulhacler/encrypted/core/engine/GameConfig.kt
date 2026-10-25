package io.github.soulhacler.encrypted.core.engine

import io.github.soulhacler.encrypted.core.model.Categories
import io.github.soulhacler.encrypted.core.model.Difficulty

/** Everything chosen on the setup screen. Invalid combinations are rejected up front. */
data class GameConfig(
    val mode: GameMode,
    val participants: List<String>,
    val roundsPerParticipant: Int = mode.defaultRounds,
    val roundSeconds: Int = DEFAULT_ROUND_SECONDS,
    val categoryId: String = Categories.RANDOM_MIX.id,
    /** Null means any difficulty. */
    val difficulty: Difficulty? = null,
) {
    init {
        require(participants.size in mode.minParticipants..mode.maxParticipants) {
            "${mode.title} needs ${mode.minParticipants}-${mode.maxParticipants} participants"
        }
        require(participants.all { it.isNotBlank() }) { "Names can't be blank" }
        require(roundsPerParticipant in 1..mode.maxRounds) { "Invalid number of rounds" }
        require(roundSeconds in MIN_ROUND_SECONDS..MAX_ROUND_SECONDS) { "Invalid round length" }
    }

    /** Turns in the whole game. Party mode is a single open-ended turn. */
    val totalTurns: Int get() = participants.size * roundsPerParticipant

    companion object {
        const val DEFAULT_ROUND_SECONDS = 60
        const val MIN_ROUND_SECONDS = 10
        const val MAX_ROUND_SECONDS = 600
        val PRESET_ROUND_SECONDS = listOf(30, 60, 90, 120)
    }
}
