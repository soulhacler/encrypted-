package io.github.soulhacler.encrypted.core.engine

/**
 * A pausable countdown that measures real elapsed time rather than counting ticks.
 *
 * The UI can poll [remainingMillis] as often or as rarely as it likes (and miss frames during
 * animations) without the timer drifting: the remaining time is always derived from the clock.
 */
class RoundTimer(
    val durationMillis: Long,
    private val clock: Clock = Clock.System,
) {
    enum class State { NOT_STARTED, RUNNING, PAUSED, FINISHED }

    init {
        require(durationMillis > 0) { "Duration must be positive" }
    }

    /** Time accumulated in previous running stretches (before the last pause). */
    private var banked = 0L
    private var runningSince = 0L

    var state: State = State.NOT_STARTED
        private set

    fun start() {
        check(state == State.NOT_STARTED) { "Timer already started" }
        runningSince = clock.nowMillis()
        state = State.RUNNING
    }

    /** Freezes the countdown. Does nothing unless the timer is running. */
    fun pause() {
        if (state != State.RUNNING) return
        banked += clock.nowMillis() - runningSince
        state = if (banked >= durationMillis) State.FINISHED else State.PAUSED
    }

    /** Continues after [pause]. Does nothing unless the timer is paused. */
    fun resume() {
        if (state != State.PAUSED) return
        runningSince = clock.nowMillis()
        state = State.RUNNING
    }

    /** Stops the timer immediately, as if it had run out. */
    fun finish() {
        if (state == State.RUNNING) banked += clock.nowMillis() - runningSince
        state = State.FINISHED
    }

    fun elapsedMillis(): Long {
        val elapsed = when (state) {
            State.NOT_STARTED -> 0L
            State.RUNNING -> banked + (clock.nowMillis() - runningSince)
            State.PAUSED -> banked
            State.FINISHED -> maxOf(banked, durationMillis)
        }
        return elapsed.coerceIn(0L, durationMillis)
    }

    fun remainingMillis(): Long = durationMillis - elapsedMillis()

    /** True once the full duration has elapsed, whether or not anyone has checked yet. */
    val isExpired: Boolean
        get() = state == State.FINISHED || remainingMillis() == 0L

    /** 1.0 at the start, 0.0 when time is up. */
    fun fractionRemaining(): Float = remainingMillis().toFloat() / durationMillis

    /** Whole seconds left, rounded up, so the display shows "1" until the very end. */
    fun secondsRemaining(): Int = ((remainingMillis() + 999) / 1000).toInt()
}
