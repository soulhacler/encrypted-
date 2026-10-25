package io.github.soulhacler.encrypted.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How hard a puzzle is to decode. */
@Serializable
enum class Difficulty(val label: String) {
    @SerialName("easy")
    EASY("Easy"),

    @SerialName("medium")
    MEDIUM("Medium"),

    @SerialName("hard")
    HARD("Hard"),
}

/**
 * One card: a string of real words that, said aloud quickly, sounds like [answer].
 *
 * [category] is a [Category.id]. IDs are stable and unique across every pack, so they can be
 * used to remember which cards a player has already seen.
 */
@Serializable
data class Puzzle(
    val id: String,
    val encrypted: String,
    val answer: String,
    val category: String,
    val difficulty: Difficulty,
)

/**
 * A bundle of puzzles shipped together. The free game ships one pack; later releases can add
 * more packs (Christmas, adult party, etc.) and mark them [premium] so they are only playable
 * once unlocked. See [io.github.soulhacler.encrypted.core.data.Entitlements].
 */
@Serializable
data class PuzzlePack(
    val id: String,
    val name: String,
    val description: String = "",
    val premium: Boolean = false,
    val version: Int = 1,
    /** Extra categories this pack introduces, on top of [Categories.builtIn]. */
    val categories: List<Category> = emptyList(),
    val puzzles: List<Puzzle>,
)
