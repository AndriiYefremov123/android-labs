package com.example.lab5

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private lateinit var compassView: CompassView
    private lateinit var tvDirection: TextView
    private lateinit var tvDegrees: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvSensorInfo: TextView

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var currentAzimuth = 0f
    private var lastAzimuth = 0f

    // Low-pass filter alpha (0.0 = max smooth, 1.0 = raw)
    private val alpha = 0.15f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        compassView = findViewById(R.id.compassView)
        tvDirection = findViewById(R.id.tvDirection)
        tvDegrees = findViewById(R.id.tvDegrees)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvSensorInfo = findViewById(R.id.tvSensorInfo)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val hasAcc = accelerometer != null
        val hasMag = magnetometer != null
        tvSensorInfo.text = "Акселерометр: ${if (hasAcc) "✓" else "✗"}   Магнітометр: ${if (hasMag) "✓" else "✗"}"
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity[0] = alpha * event.values[0] + (1 - alpha) * gravity[0]
                gravity[1] = alpha * event.values[1] + (1 - alpha) * gravity[1]
                gravity[2] = alpha * event.values[2] + (1 - alpha) * gravity[2]
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic[0] = alpha * event.values[0] + (1 - alpha) * geomagnetic[0]
                geomagnetic[1] = alpha * event.values[1] + (1 - alpha) * geomagnetic[1]
                geomagnetic[2] = alpha * event.values[2] + (1 - alpha) * geomagnetic[2]
            }
        }

        val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthRad = orientation[0]
            var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            if (azimuthDeg < 0) azimuthDeg += 360f

            currentAzimuth = azimuthDeg
            updateUI(azimuthDeg)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val label = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH   -> "Висока ✓"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Середня"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW    -> "Низька"
            SensorManager.SENSOR_STATUS_UNRELIABLE      -> "Ненадійний ✗"
            else -> "Невідомо"
        }
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            tvAccuracy.text = "Точність магнітометра: $label"
        }
    }

    private fun updateUI(azimuth: Float) {
        // Smooth needle rotation animation
        val anim = RotateAnimation(
            -lastAzimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            fillAfter = true
        }
        lastAzimuth = azimuth
        compassView.azimuth = azimuth

        // Direction label
        val direction = getDirectionLabel(azimuth)
        tvDirection.text = direction
        tvDegrees.text = "${azimuth.roundToInt()}°"
    }

    private fun getDirectionLabel(degrees: Float): String {
        return when {
            degrees < 22.5  || degrees >= 337.5 -> "Північ ↑"
            degrees < 67.5  -> "Північ-Схід ↗"
            degrees < 112.5 -> "Схід →"
            degrees < 157.5 -> "Південь-Схід ↘"
            degrees < 202.5 -> "Південь ↓"
            degrees < 247.5 -> "Південь-Захід ↙"
            degrees < 292.5 -> "Захід ←"
            else            -> "Північ-Захід ↖"
        }
    }
}