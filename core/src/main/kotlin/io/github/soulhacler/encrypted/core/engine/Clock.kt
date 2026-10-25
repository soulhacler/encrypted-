package io.github.soulhacler.encrypted.core.engine

/** A monotonic millisecond clock. Injected so timers can be tested without waiting. */
fun interface Clock {
    fun nowMillis(): Long

    companion object {
        /** Monotonic system clock; unaffected by the user changing the time of day. */
        val System: Clock = Clock { java.lang.System.nanoTime() / 1_000_000 }
    }
}
