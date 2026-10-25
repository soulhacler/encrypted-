package io.github.soulhacler.encrypted.core.data

import io.github.soulhacler.encrypted.core.model.Categories
import io.github.soulhacler.encrypted.core.model.PuzzlePack
import kotlinx.serialization.json.Json

object PuzzlePackParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): PuzzlePack = json.decodeFromString(PuzzlePack.serializer(), text)

    /** Returns a list of human-readable problems; empty means the pack is valid. */
    fun validate(pack: PuzzlePack): List<String> {
        val problems = mutableListOf<String>()
        val knownCategories = (Categories.builtIn + pack.categories).mapTo(HashSet()) { it.id }
        val seenIds = HashSet<String>()
        val seenAnswers = HashMap<String, String>()
        val seenEncrypted = HashMap<String, String>()

        for (p in pack.puzzles) {
            if (!seenIds.add(p.id)) problems += "Duplicate id ${p.id}"
            if (p.encrypted.isBlank()) problems += "${p.id}: blank encrypted phrase"
            if (p.answer.isBlank()) problems += "${p.id}: blank answer"
            if (p.category !in knownCategories) problems += "${p.id}: unknown category '${p.category}'"
            if (p.encrypted != p.encrypted.trim() || p.answer != p.answer.trim()) {
                problems += "${p.id}: stray whitespace"
            }
            if (normalise(p.encrypted) == normalise(p.answer)) {
                problems += "${p.id}: encrypted phrase is just the answer"
            }
            seenAnswers.put(normalise(p.answer), p.id)?.let { problems += "${p.id}: same answer as $it" }
            seenEncrypted.put(normalise(p.encrypted), p.id)?.let { problems += "${p.id}: same phrase as $it" }
        }
        return problems
    }

    /** Lowercase letters and digits only, for comparing phrases loosely. */
    fun normalise(text: String): String = text.lowercase().filter { it.isLetterOrDigit() }
}
