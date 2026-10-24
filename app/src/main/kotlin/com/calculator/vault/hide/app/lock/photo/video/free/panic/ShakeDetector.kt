package com.calculator.vault.hide.app.lock.photo.video.free.panic

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

/** Fires [onShake] after two strong shakes within a short window (panic mode). */
class ShakeDetector(private val sensorManager: SensorManager, private val onShake: () -> Unit) : SensorEventListener {
    private var firstShakeAt = 0L
    private var lastTrigger = 0L
    private var running = false

    fun start() {
        if (running) return
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            running = sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        if (!running) return
        sensorManager.unregisterListener(this)
        running = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        val (x, y, z) = Triple(event.values[0], event.values[1], event.values[2])
        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        if (gForce < THRESHOLD_G) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastTrigger < COOLDOWN_MS) return
        if (now - firstShakeAt > WINDOW_MS) {
            firstShakeAt = now
        } else {
            lastTrigger = now
            firstShakeAt = 0
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private companion object {
        const val THRESHOLD_G = 2.6f
        const val WINDOW_MS = 900L
        const val COOLDOWN_MS = 2_000L
    }
}
