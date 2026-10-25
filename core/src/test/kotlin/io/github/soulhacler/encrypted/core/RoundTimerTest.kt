package io.github.soulhacler.encrypted.core

import io.github.soulhacler.encrypted.core.engine.RoundTimer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundTimerTest {
    private val clock = FakeClock()
    private val timer = RoundTimer(60_000, clock)

    @Test
    fun `does not count down before it starts`() {
        clock.advance(5_000)
        assertEquals(60_000, timer.remainingMillis())
        assertEquals(RoundTimer.State.NOT_STARTED, timer.state)
    }

    @Test
    fun `counts down with real elapsed time`() {
        timer.start()
        clock.advance(12_345)
        assertEquals(47_655, timer.remainingMillis())
        assertEquals(48, timer.secondsRemaining())
    }

    @Test
    fun `remaining time does not depend on how often it is read`() {
        timer.start()
        // A long animation means nobody reads the timer for a while; no time is lost or gained.
        clock.advance(30_000)
        assertEquals(30_000, timer.remainingMillis())
        repeat(100) { clock.advance(100) }
        assertEquals(20_000, timer.remainingMillis())
    }

    @Test
    fun `pausing freezes the countdown`() {
        timer.start()
        clock.advance(10_000)
        timer.pause()
        clock.advance(120_000) // phone call, app in background
        assertEquals(50_000, timer.remainingMillis())
        assertEquals(RoundTimer.State.PAUSED, timer.state)
        assertFalse(timer.isExpired)
    }

    @Test
    fun `resuming continues from where it paused`() {
        timer.start()
        clock.advance(10_000)
        timer.pause()
        clock.advance(99_000)
        timer.resume()
        clock.advance(5_000)
        assertEquals(45_000, timer.remainingMillis())
    }

    @Test
    fun `survives repeated pause and resume`() {
        timer.start()
        repeat(10) {
            clock.advance(1_000)
            timer.pause()
            clock.advance(7_777)
            timer.resume()
        }
        assertEquals(50_000, timer.remainingMillis())
    }

    @Test
    fun `pause and resume are ignored in the wrong state`() {
        timer.resume()
        assertEquals(RoundTimer.State.NOT_STARTED, timer.state)
        timer.start()
        timer.resume()
        clock.advance(1_000)
        timer.pause()
        timer.pause()
        assertEquals(59_000, timer.remainingMillis())
    }

    @Test
    fun `expires and never goes negative`() {
        timer.start()
        clock.advance(59_999)
        assertFalse(timer.isExpired)
        assertEquals(1, timer.secondsRemaining())
        clock.advance(1)
        assertTrue(timer.isExpired)
        clock.advance(10_000)
        assertEquals(0, timer.remainingMillis())
        assertEquals(0, timer.secondsRemaining())
        assertEquals(0f, timer.fractionRemaining())
    }

    @Test
    fun `pausing after expiry finishes the timer`() {
        timer.start()
        clock.advance(61_000)
        timer.pause()
        assertEquals(RoundTimer.State.FINISHED, timer.state)
        timer.resume()
        assertEquals(RoundTimer.State.FINISHED, timer.state)
    }

    @Test
    fun `finish ends the round early`() {
        timer.start()
        clock.advance(3_000)
        timer.finish()
        assertTrue(timer.isExpired)
        assertEquals(0, timer.remainingMillis())
    }

    @Test
    fun `reports fraction remaining`() {
        timer.start()
        clock.advance(15_000)
        assertEquals(0.75f, timer.fractionRemaining(), 0.0001f)
    }

    @Test
    fun `cannot start twice or have a zero duration`() {
        timer.start()
        assertThrows(IllegalStateException::class.java) { timer.start() }
        assertThrows(IllegalArgumentException::class.java) { RoundTimer(0, clock) }
    }
}
