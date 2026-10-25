package io.github.soulhacler.encrypted.core.data

import io.github.soulhacler.encrypted.core.model.Categories
import io.github.soulhacler.encrypted.core.model.Category
import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.core.model.Puzzle
import io.github.soulhacler.encrypted.core.model.PuzzlePack

/**
 * Decides which packs the player can use. Version 1 only has free content; a future billing
 * integration would provide an implementation backed by purchases.
 */
fun interface Entitlements {
    fun isUnlocked(pack: PuzzlePack): Boolean

    companion object {
        val FreeOnly = Entitlements { !it.premium }
    }
}

/** Every puzzle the player can currently use, across all unlocked packs. */
class PuzzleLibrary(
    val packs: List<PuzzlePack>,
    entitlements: Entitlements = Entitlements.FreeOnly,
) {
    val unlockedPacks: List<PuzzlePack> = packs.filter(entitlements::isUnlocked)

    val puzzles: List<Puzzle> = unlockedPacks.flatMap { it.puzzles }.distinctBy { it.id }

    /** Categories with at least one playable puzzle, built-ins first, then pack-defined ones. */
    val categories: List<Category> = run {
        val all = (Categories.builtIn + unlockedPacks.flatMap { it.categories }).distinctBy { it.id }
        val used = puzzles.mapTo(HashSet()) { it.category }
        all.filter { it.id in used }
    }

    /** [Categories.RANDOM_MIX] followed by every real category. */
    val selectableCategories: List<Category> = listOf(Categories.RANDOM_MIX) + categories

    fun category(id: String): Category? = selectableCategories.firstOrNull { it.id == id }

    fun pool(categoryId: String, difficulty: Difficulty?): List<Puzzle> = puzzles.filter {
        (categoryId == Categories.RANDOM_MIX.id || it.category == categoryId) &&
            (difficulty == null || it.difficulty == difficulty)
    }

    fun count(categoryId: String, difficulty: Difficulty? = null): Int = pool(categoryId, difficulty).size
}
