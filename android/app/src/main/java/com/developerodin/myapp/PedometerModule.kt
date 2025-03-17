package com.developerodin.myapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class PedometerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepCount = 0.0
    private var initialStepCount: Double? = null  // ✅ Store initial step count as Double
    private var listenersCount = 0

    init {
        sensorManager = reactContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        Log.d("PedometerModule", "PedometerModule initialized ✅")
    }

    override fun getName(): String = "PedometerModule"

    override fun initialize() {
        super.initialize()
        initialStepCount = null  // ✅ Reset initial step count on app restart
        Log.d("PedometerModule", "Pedometer Module Registered ✅")
    }

    @ReactMethod
    fun startStepCounting(promise: Promise) {
        if (stepSensor == null) {
            promise.reject("NO_SENSOR", "No step counter sensor found")
            Log.e("PedometerModule", "No step counter sensor found ❌")
            return
        }
        sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        Log.d("PedometerModule", "Step counting started ✅")
        promise.resolve("Step counter started")
    }

    @ReactMethod
    fun stopStepCounting(promise: Promise) {
        sensorManager?.unregisterListener(this)
        Log.d("PedometerModule", "Step counting stopped ❌")
        promise.resolve("Step counter stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (initialStepCount == null) {
                initialStepCount = event.values[0].toDouble() // ✅ Convert to Double
            }

            val adjustedStepCount = event.values[0].toDouble() - initialStepCount!! // ✅ Ensure both are Double
            stepCount = maxOf(0.0, adjustedStepCount) // ✅ Ensure step count is never negative

            sendEvent("StepUpdate", stepCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("PedometerModule", "Sensor accuracy changed: $accuracy")
    }

    @ReactMethod
    fun addListener(eventName: String) {
        Log.d("PedometerModule", "Listener added for: $eventName ✅")
        listenersCount += 1
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        Log.d("PedometerModule", "Listeners removed: $count ❌")
        listenersCount -= count
    }

    private fun sendEvent(eventName: String, data: Double) {
        if (listenersCount > 0) {
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit(eventName, data)
        }
    }
}
