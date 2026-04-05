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

    // sensorManager is the gateway to all sensors on the device
    private lateinit var sensorManager: SensorManager

    // nullable because not every device has all three sensors
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

        // grab the system sensor service so we can access hardware sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // getDefaultSensor returns null if the sensor doesn't exist on this device
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        // let the user know if a sensor is missing rather than just showing 0.0
        if (accelerometer == null) tvAccelerometer.text = "Accelerometer sensor not available"
        if (lightSensor == null) tvLight.text = "Light sensor not available"
        if (proximitySensor == null) tvProximity.text = "Proximity sensor not available"
    }

    override fun onResume() {
        super.onResume()
        // register listeners only when the app is visible, no point running in the background
        // SENSOR_DELAY_NORMAL is fine here, we don't need super fast updates
        accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        lightSensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        proximitySensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        // unregister all listeners at once when app goes to background, saves battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // event can be null in rare cases so just bail out early
        if (event == null) return

        // each sensor fires this callback so we check which one triggered it
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // values[0], [1], [2] are x, y, z axes in m/s²
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                tvAccelerometer.text = "X: $x\nY: $y\nZ: $z"
            }
            Sensor.TYPE_LIGHT -> {
                // values[0] is ambient light level in lux
                val lightValue = event.values[0]
                tvLight.text = "Light: $lightValue"
            }
            Sensor.TYPE_PROXIMITY -> {
                // values[0] is distance in cm, some devices only return near/far (0 or 5)
                val proximityValue = event.values[0]
                tvProximity.text = "Proximity: $proximityValue"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing to do here for a basic app
    }
}