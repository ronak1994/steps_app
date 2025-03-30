package com.developerodin.myapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * StepCounterServiceModule: Bridge between React Native and the native StepCounterService.
 * This module provides methods to start and stop the step counting service, and handles
 * communication between the service and React Native.
 */
class StepCounterServiceModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val TAG = "StepCounterServiceModule"
        private const val STEP_UPDATE_EVENT = "stepUpdate"
        private const val SERVICE_STATUS_EVENT = "serviceStatus"
        private const val ERROR_EVENT = "error"

        @Volatile
        private var instance: StepCounterServiceModule? = null

        fun getInstance(): StepCounterServiceModule? = instance
    }

    private var isServiceRunning = false
    private lateinit var stepDataManager: StepDataManager

    init {
        instance = this
        // Initialize StepDataManager
        stepDataManager = StepDataManager.getInstance(reactApplicationContext)
        // Load saved step data
        loadSavedStepData()
    }

    /**
     * Loads saved step data and sends it to React Native
     */
    private fun loadSavedStepData() {
        try {
            val totalSteps = stepDataManager.getTotalSteps()
            Log.d(TAG, "Loading saved step data: $totalSteps")
            sendStepUpdate(totalSteps)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved step data", e)
            sendError("Failed to load saved step data: ${e.message}")
        }
    }

    override fun getName() = "StepCounterService"

    /**
     * Starts the step counting service.
     * This method launches the StepCounterService as a foreground service.
     */
    @ReactMethod
    fun startService(promise: Promise) {
        try {
            val context = reactApplicationContext
            val serviceIntent = Intent(context, StepCounterService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            isServiceRunning = true
            sendServiceStatus(true)
            promise.resolve(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
            sendError("Failed to start service: ${e.message}")
            promise.reject("SERVICE_ERROR", e)
        }
    }

    /**
     * Stops the step counting service.
     * This method stops the StepCounterService and cleans up resources.
     */
    @ReactMethod
    fun stopService(promise: Promise) {
        try {
            val context = reactApplicationContext
            context.stopService(Intent(context, StepCounterService::class.java))
            isServiceRunning = false
            sendServiceStatus(false)
            promise.resolve(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service", e)
            sendError("Failed to stop service: ${e.message}")
            promise.reject("SERVICE_ERROR", e)
        }
    }

    /**
     * Sends step count updates to React Native.
     * This method is called by the StepCounterService when new step data is available.
     */
    fun sendStepUpdate(steps: Int) {
        try {
            val params = Arguments.createMap().apply {
                putInt("steps", steps)
            }
            sendEvent(STEP_UPDATE_EVENT, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending step update", e)
        }
    }

    /**
     * Sends service status updates to React Native.
     * This method is called when the service starts or stops.
     */
    fun sendServiceStatus(isRunning: Boolean) {
        try {
            val params = Arguments.createMap().apply {
                putBoolean("isRunning", isRunning)
            }
            sendEvent(SERVICE_STATUS_EVENT, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending service status", e)
        }
    }

    /**
     * Sends error messages to React Native.
     * This method is called when an error occurs in the service.
     */
    fun sendError(message: String) {
        try {
            val params = Arguments.createMap().apply {
                putString("message", message)
            }
            sendEvent(ERROR_EVENT, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending error message", e)
        }
    }

    /**
     * Sends events to React Native.
     * This is a helper method that ensures events are sent safely.
     */
    private fun sendEvent(eventName: String, params: WritableMap?) {
        try {
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending event: $eventName", e)
        }
    }
} 