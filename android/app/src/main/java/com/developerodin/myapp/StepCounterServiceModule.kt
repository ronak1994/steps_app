package com.developerodin.myapp

import android.content.Intent
import android.os.Build
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class StepCounterServiceModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var isServiceRunning = false
    private val TAG = "StepCounterServiceModule"

    override fun getName(): String = "StepCounterServiceModule"

    @ReactMethod
    fun startService(promise: Promise) {
        try {
            if (isServiceRunning) {
                promise.reject("SERVICE_ALREADY_RUNNING", "Step counter service is already running")
                return
            }

            val intent = Intent(reactApplicationContext, StepCounterService::class.java)
            Log.d(TAG, "Starting service...")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                reactApplicationContext.startForegroundService(intent)
            } else {
                reactApplicationContext.startService(intent)
            }
            isServiceRunning = true
            sendServiceStatus(true)
            Log.d(TAG, "Service started successfully")
            promise.resolve(null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            isServiceRunning = false
            sendServiceStatus(false)
            promise.reject("START_SERVICE_ERROR", "Failed to start step counter service: ${e.message}")
        }
    }

    @ReactMethod
    fun stopService(promise: Promise) {
        try {
            if (!isServiceRunning) {
                promise.reject("SERVICE_NOT_RUNNING", "Step counter service is not running")
                return
            }

            val intent = Intent(reactApplicationContext, StepCounterService::class.java)
            reactApplicationContext.stopService(intent)
            isServiceRunning = false
            sendServiceStatus(false)
            Log.d(TAG, "Service stopped successfully")
            promise.resolve(null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop service", e)
            promise.reject("STOP_SERVICE_ERROR", "Failed to stop step counter service: ${e.message}")
        }
    }

    // Required to be implemented
    @ReactMethod
    fun addListener(eventName: String) {
    }

    @ReactMethod
    fun removeListeners(count: Int) {
    }

    private fun sendServiceStatus(isRunning: Boolean) {
        try {
            val params = Arguments.createMap().apply {
                putBoolean("isRunning", isRunning)
            }
            sendEvent("onServiceStatus", params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending service status", e)
        }
    }

    fun sendStepUpdate(steps: Int) {
        try {
            val params = Arguments.createMap().apply {
                putInt("steps", steps)
            }
            sendEvent("onStepUpdate", params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending step update", e)
        }
    }

    private fun sendEvent(eventName: String, params: WritableMap?) {
        try {
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending event: $eventName", e)
        }
    }

    companion object {
        private var instance: StepCounterServiceModule? = null

        fun getInstance(): StepCounterServiceModule? = instance

        fun updateSteps(steps: Int) {
            instance?.sendStepUpdate(steps)
        }
    }

    init {
        instance = this
    }
} 