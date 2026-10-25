package io.github.soulhacler.encrypted.core

import io.github.soulhacler.encrypted.core.data.Entitlements
import io.github.soulhacler.encrypted.core.data.PuzzleLibrary
import io.github.soulhacler.encrypted.core.model.Categories
import io.github.soulhacler.encrypted.core.model.Category
import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.core.model.PuzzlePack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleLibraryTest {
    private val free = PuzzlePack(
        id = "core",
        name = "Core",
        puzzles = puzzles(4, "films_tv", Difficulty.EASY) +
            puzzles(3, "music", Difficulty.HARD) +
            puzzles(2, "food", Difficulty.MEDIUM),
    )
    private val christmas = PuzzlePack(
        id = "xmas",
        name = "Christmas",
        premium = true,
        categories = listOf(Category("christmas", "Christmas", "🎄")),
        puzzles = puzzles(5, "christmas", Difficulty.EASY),
    )

    @Test
    fun `filters by category`() {
        val library = PuzzleLibrary(listOf(free))
        assertEquals(4, library.count("films_tv"))
        assertTrue(library.pool("music", null).all { it.category == "music" })
    }

    @Test
    fun `filters by difficulty`() {
        val library = PuzzleLibrary(listOf(free))
        assertEquals(3, library.count(Categories.RANDOM_MIX.id, Difficulty.HARD))
        assertEquals(0, library.count("films_tv", Difficulty.HARD))
        assertEquals(2, library.count("food", Difficulty.MEDIUM))
    }

    @Test
    fun `random mix draws from every category`() {
        val library = PuzzleLibrary(listOf(free))
        assertEquals(9, library.count(Categories.RANDOM_MIX.id))
    }

    @Test
    fun `premium packs stay locked by default`() {
        val library = PuzzleLibrary(listOf(free, christmas))
        assertEquals(listOf("core"), library.unlockedPacks.map { it.id })
        assertEquals(0, library.count("christmas"))
        assertFalse(library.categories.any { it.id == "christmas" })
    }

    @Test
    fun `unlocked premium packs add their puzzles and categories`() {
        val library = PuzzleLibrary(listOf(free, christmas), Entitlements { true })
        assertEquals(5, library.count("christmas"))
        assertEquals(14, library.count(Categories.RANDOM_MIX.id))
        assertTrue(library.categories.any { it.id == "christmas" })
    }

    @Test
    fun `only lists categories that have puzzles, random mix first`() {
        val library = PuzzleLibrary(listOf(free))
        assertEquals(listOf("films_tv", "music", "food"), library.categories.map { it.id })
        assertEquals(Categories.RANDOM_MIX, library.selectableCategories.first())
    }
}
