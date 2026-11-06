package br.udesc.radar.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {
    private val manager = context.getSystemService(SensorManager::class.java)
    private val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gesture = ShakeGesture()

    fun start(): Boolean {
        val accelerometer = sensor ?: return false
        return manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        manager.unregisterListener(this)
        gesture.resetPeaks()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (gesture.detect(event.timestamp / 1_000_000, event.values)) onShake()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

internal class ShakeGesture {
    private var firstPeak: Long? = null
    private var aboveThreshold = false
    private var lastShake: Long? = null

    fun detect(now: Long, acceleration: FloatArray): Boolean {
        val (x, y, z) = acceleration
        val limit = 2.1f * SensorManager.GRAVITY_EARTH
        val high = x * x + y * y + z * z >= limit * limit
        val newPeak = high && !aboveThreshold
        aboveThreshold = high
        if (!newPeak || lastShake?.let { now - it < 2_000 } == true) return false

        val previous = firstPeak
        if (previous != null && now - previous in 80L..700L) {
            lastShake = now
            firstPeak = null
            return true
        }
        if (previous == null || now - previous > 700) firstPeak = now
        return false
    }

    fun resetPeaks() {
        firstPeak = null
        aboveThreshold = false
    }
}
