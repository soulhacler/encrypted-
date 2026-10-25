package io.github.soulhacler.encrypted.core

import io.github.soulhacler.encrypted.core.data.PuzzleLibrary
import io.github.soulhacler.encrypted.core.data.PuzzlePackParser
import io.github.soulhacler.encrypted.core.model.Categories
import io.github.soulhacler.encrypted.core.model.Difficulty
import io.github.soulhacler.encrypted.core.model.PuzzlePack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Checks the real puzzle packs that ship in the app. */
class PuzzleDatabaseTest {
    private val packs: List<PuzzlePack> = run {
        val dir = File(checkNotNull(System.getProperty("encrypted.packsDir")) { "packsDir not set" })
        val files = dir.listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }
        check(files.isNotEmpty()) { "No packs found in $dir" }
        files.map { PuzzlePackParser.parse(it.readText()) }
    }
    private val library = PuzzleLibrary(packs)

    @Test
    fun `every pack is valid`() {
        packs.forEach { pack ->
            val problems = PuzzlePackParser.validate(pack)
            assertTrue("${pack.id}:\n" + problems.joinToString("\n"), problems.isEmpty())
        }
    }

    @Test
    fun `ids are unique across all packs`() {
        val ids = packs.flatMap { p -> p.puzzles.map { it.id } }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `pack ids are unique`() {
        assertEquals(packs.size, packs.map { it.id }.toSet().size)
    }

    @Test
    fun `free content has at least 300 puzzles`() {
        assertTrue("Only ${library.puzzles.size} free puzzles", library.puzzles.size >= 300)
    }

    @Test
    fun `every built-in category has plenty of puzzles at each difficulty used`() {
        Categories.builtIn.forEach { category ->
            val count = library.count(category.id)
            assertTrue("${category.name} has only $count puzzles", count >= 40)
            assertTrue("${category.name} has too few easy puzzles", library.count(category.id, Difficulty.EASY) >= 10)
        }
    }

    @Test
    fun `each difficulty has enough puzzles for several games`() {
        Difficulty.entries.forEach { difficulty ->
            val count = library.count(Categories.RANDOM_MIX.id, difficulty)
            assertTrue("$difficulty has only $count", count >= 80)
        }
    }

    @Test
    fun `every category offers some hard puzzles`() {
        Categories.builtIn.forEach { category ->
            val hard = library.count(category.id, Difficulty.HARD)
            assertTrue("${category.name} has only $hard hard puzzles", hard >= 5)
        }
    }

    @Test
    fun `phrases are short enough to fit on screen`() {
        library.puzzles.forEach {
            assertTrue("${it.id} phrase too long", it.encrypted.length <= 60)
            assertTrue("${it.id} answer too long", it.answer.length <= 60)
        }
    }
}
