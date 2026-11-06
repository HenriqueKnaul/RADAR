package br.udesc.radar

import androidx.test.ext.junit.runners.AndroidJUnit4
import br.udesc.radar.util.ShakeGesture
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShakeGestureTest {
    private val rest = floatArrayOf(0f, 0f, 9.8f)
    private val peak = floatArrayOf(26f, 0f, 9.8f)

    @Test
    fun requiresTwoSeparatePeaksAndDebouncesRepeatedShakes() {
        val gesture = ShakeGesture()
        assertFalse(gesture.detect(0, rest))
        assertFalse(gesture.detect(100, peak))
        assertFalse(gesture.detect(180, peak))
        assertFalse(gesture.detect(200, rest))
        assertTrue(gesture.detect(300, peak))
        assertFalse(gesture.detect(400, rest))
        assertFalse(gesture.detect(500, peak))
        assertFalse(gesture.detect(600, rest))
        assertFalse(gesture.detect(700, peak))
        assertFalse(gesture.detect(2_400, rest))
        assertFalse(gesture.detect(2_500, peak))
        assertFalse(gesture.detect(2_600, rest))
        assertTrue(gesture.detect(2_700, peak))
    }

    @Test
    fun resetDiscardsPeakCapturedBeforeLeavingDashboard() {
        val gesture = ShakeGesture()
        assertFalse(gesture.detect(100, peak))
        gesture.resetPeaks()
        assertFalse(gesture.detect(350, peak))
    }
}
