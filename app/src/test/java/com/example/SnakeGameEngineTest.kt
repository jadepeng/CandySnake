package com.example

import com.example.game.Direction
import com.example.game.GameMode
import com.example.game.SnakeGameEngine
import com.example.game.SpeedPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SnakeGameEngineTest {

    @Test
    fun `two-step input queue prevents instant 180 reversal`() {
        val engine = SnakeGameEngine(cols = 16, rows = 18, random = Random(42))
        engine.reset(GameMode.CLASSIC, SpeedPreset.NORMAL)

        // Initially moving RIGHT; LEFT must be ignored
        assertFalse(engine.enqueueDirection(Direction.LEFT))
        // Queue UP then LEFT (legal 2-tick U-turn)
        assertTrue(engine.enqueueDirection(Direction.UP))
        assertTrue(engine.enqueueDirection(Direction.LEFT))

        val tick1 = engine.tick()
        assertFalse(tick1.crashed)
        assertEquals(Direction.UP, engine.direction)

        val tick2 = engine.tick()
        assertFalse(tick2.crashed)
        assertEquals(Direction.LEFT, engine.direction)
    }

    @Test
    fun `classic mode crashes on wall while endless mode wraps seamlessly`() {
        val classic = SnakeGameEngine(cols = 16, rows = 18, random = Random(7))
        classic.reset(GameMode.CLASSIC, SpeedPreset.NORMAL)
        var crashed = false
        repeat(20) {
            if (!crashed) {
                crashed = classic.tick().crashed
            }
        }
        assertTrue("Classic mode must crash on wall collision", crashed)

        val endless = SnakeGameEngine(cols = 16, rows = 18, random = Random(7))
        endless.reset(GameMode.ENDLESS, SpeedPreset.NORMAL)
        var endlessCrashed = false
        repeat(24) {
            if (!endlessCrashed) {
                endlessCrashed = endless.tick().crashed
            }
        }
        assertFalse("Endless mode should wrap around edges without crashing", endlessCrashed)
        assertTrue(endless.snake.first().x in 0 until endless.cols)
    }
}
