package io.github.soulhacler.encrypted.core.engine

import io.github.soulhacler.encrypted.core.model.Puzzle
import kotlin.random.Random

/**
 * Deals puzzles from a pool without repeats.
 *
 * Cards the player has already seen in earlier games ([seenIds]) go to the back of the deck,
 * so a rematch gets fresh cards for as long as there are any. If the whole pool is used up the
 * deck reshuffles rather than stopping the game, and never deals the card it has just dealt.
 */
class PuzzleDeck(
    pool: List<Puzzle>,
    private val random: Random = Random.Default,
    seenIds: Set<String> = emptySet(),
) {
    private val pool: List<Puzzle> = pool.distinctBy { it.id }
    private val queue = ArrayDeque<Puzzle>()
    private val dealt = LinkedHashSet<String>()
    private var lastDealt: Puzzle? = null

    /** How many times the deck has run out and been reshuffled. */
    var reshuffles: Int = 0
        private set

    init {
        val (seen, unseen) = this.pool.partition { it.id in seenIds }
        queue.addAll(unseen.shuffled(random))
        queue.addAll(seen.shuffled(random))
    }

    val poolSize: Int get() = pool.size
    val isEmpty: Boolean get() = pool.isEmpty()

    /** Cards left before the deck has to reshuffle. */
    val remainingBeforeReshuffle: Int get() = queue.size

    /** IDs dealt since the deck was created, in order (without duplicates). */
    val dealtIds: Set<String> get() = dealt

    /** Returns the next card, or null only if the pool is empty. */
    fun draw(): Puzzle? {
        if (pool.isEmpty()) return null
        if (queue.isEmpty()) refill()
        val card = queue.removeFirst()
        lastDealt = card
        dealt += card.id
        return card
    }

    private fun refill() {
        reshuffles++
        val reshuffled = pool.shuffled(random).toMutableList()
        // Don't deal the same card twice in a row across the reshuffle boundary.
        val last = lastDealt
        if (last != null && reshuffled.size > 1 && reshuffled.first().id == last.id) {
            reshuffled.add(reshuffled.removeAt(0))
        }
        queue.addAll(reshuffled)
    }
}
