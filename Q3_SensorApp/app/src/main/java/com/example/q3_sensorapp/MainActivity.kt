package com.example.q3_sensorapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null

    private lateinit var tvAccelerometer: TextView
    private lateinit var tvLight: TextView
    private lateinit var tvProximity: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAccelerometer = findViewById(R.id.tvAccelerometer)
        tvLight = findViewById(R.id.tvLight)
        tvProximity = findViewById(R.id.tvProximity)

        // Get the sensor manager service from the system
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Get required sensors from the device
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        // If any sensor is not available, show message on screen
        if (accelerometer == null) {
            tvAccelerometer.text = "Accelerometer sensor not available"
        }

        if (lightSensor == null) {
            tvLight.text = "Light sensor not available"
        }

        if (proximitySensor == null) {
            tvProximity.text = "Proximity sensor not available"
        }
    }

    override fun onResume() {
        super.onResume()

        // Register sensor listeners when app comes to foreground
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        lightSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        proximitySensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()

        // Unregister listener to save battery and avoid unnecessary updates
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }

        when (event.sensor.type) {

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                tvAccelerometer.text = "X: $x\nY: $y\nZ: $z"
            }

            Sensor.TYPE_LIGHT -> {
                val lightValue = event.values[0]
                tvLight.text = "Light: $lightValue"
            }

            Sensor.TYPE_PROXIMITY -> {
                val proximityValue = event.values[0]
                tvProximity.text = "Proximity: $proximityValue"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No extra work needed here for this basic app
    }
}