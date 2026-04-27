package com.example.lab5

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.roundToInt
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class LevelActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var levelView: LevelView
    private lateinit var tvRoll: TextView
    private lateinit var tvPitch: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvMode: TextView

    private val gravity = FloatArray(3)
    private val alpha = 0.15f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level)

        levelView = findViewById(R.id.levelView)
        tvRoll = findViewById(R.id.tvRoll)
        tvPitch = findViewById(R.id.tvPitch)
        tvStatus = findViewById(R.id.tvStatus)
        tvMode = findViewById(R.id.tvMode)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        gravity[0] = alpha * event.values[0] + (1 - alpha) * gravity[0]
        gravity[1] = alpha * event.values[1] + (1 - alpha) * gravity[1]
        gravity[2] = alpha * event.values[2] + (1 - alpha) * gravity[2]

        val x = gravity[0]
        val y = gravity[1]
        val z = gravity[2]


        val absX = abs(x)
        val absY = abs(y)
        val absZ = abs(z)

        when {

            absZ > absX && absZ > absY -> {
                val roll = Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()
                val pitch = Math.toDegrees(atan2(y.toDouble(), z.toDouble())).toFloat()
                levelView.rollAngle = roll
                levelView.pitchAngle = pitch
                tvMode.text = "Режим: плаский (стіл)"
                tvRoll.text = "Нахил ліво/право: ${roll.roundToInt()}°"
                tvPitch.text = "Нахил вперед/назад: ${pitch.roundToInt()}°"
                val isLevel = abs(roll) < 2f && abs(pitch) < 2f
                updateStatus(isLevel)
            }


            else -> {
                val angle = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
                levelView.rollAngle = angle
                levelView.pitchAngle = 0f
                tvMode.text = "Режим: вертикальний (стіна)"
                tvRoll.text = "Відхилення: ${angle.roundToInt()}°"
                tvPitch.text = ""
                val isLevel = abs(angle) < 2f
                updateStatus(isLevel)
            }
        }
    }

    private fun updateStatus(isLevel: Boolean) {
        tvStatus.text = if (isLevel) "✓ Рівно!" else "Вирівняйте пристрій"
        tvStatus.setTextColor(
            if (isLevel) android.graphics.Color.parseColor("#66bb6a")
            else android.graphics.Color.parseColor("#ef5350")
        )
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}