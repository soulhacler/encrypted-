package io.github.soulhacler.encrypted.data

import android.content.Context
import io.github.soulhacler.encrypted.core.data.Entitlements
import io.github.soulhacler.encrypted.core.data.PuzzleLibrary
import io.github.soulhacler.encrypted.core.data.PuzzlePackParser
import io.github.soulhacler.encrypted.core.model.PuzzlePack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Loads every puzzle pack bundled in `assets/packs/`. Dropping another JSON pack into that
 * folder is all it takes to ship more content; premium packs are filtered by [entitlements].
 */
class PuzzleRepository(
    private val context: Context,
    private val entitlements: Entitlements = Entitlements.FreeOnly,
) {
    private val mutex = Mutex()
    private var cached: PuzzleLibrary? = null

    suspend fun library(): PuzzleLibrary = mutex.withLock {
        cached ?: withContext(Dispatchers.IO) { PuzzleLibrary(loadPacks(), entitlements) }.also { cached = it }
    }

    private fun loadPacks(): List<PuzzlePack> {
        val assets = context.assets
        val files = assets.list(PACKS_DIR).orEmpty().filter { it.endsWith(".json") }.sorted()
        return files.map { name ->
            val text = assets.open("$PACKS_DIR/$name").bufferedReader().use { it.readText() }
            PuzzlePackParser.parse(text)
        }
    }

    private companion object {
        const val PACKS_DIR = "packs"
    }
}
