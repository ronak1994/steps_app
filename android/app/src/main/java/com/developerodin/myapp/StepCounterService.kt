package com.developerodin.myapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * StepCounterService: A foreground service that counts steps using the device's step counter sensor.
 * This service runs in the background and updates both the UI and notification with step counts.
 */
class StepCounterService : Service(), SensorEventListener {
    companion object {
        private const val TAG = "StepCounterService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "StepCounterChannel"
        private const val CHANNEL_NAME = "Step Counter"
        private const val MAX_RETRY_COUNT = 3
        private const val RETRY_DELAY_MS = 1000L
        
        // Method to update steps through the module
        fun updateSteps(steps: Int) {
            StepCounterServiceModule.getInstance()?.sendStepUpdate(steps)
        }
    }

    // Service components
    private var notificationManager: NotificationManager? = null
    private var sensorManager: SensorManager? = null
    private var stepCountSensor: Sensor? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var stepDataManager: StepDataManager

    // Step counting variables
    private var initialStepCount: Int = -1  // Initial step count when service starts
    private var currentStepCount: Int = 0   // Current step count from sensor
    private var isCountingSteps = false     // Flag to track if we're counting steps
    private var totalSteps: Int = 0         // Total steps since service started
    private var retryCount = 0              // Counter for retry attempts

    /**
     * Called when the service is first created.
     * Initializes the notification channel, sensor manager, and step counter sensor.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        
        try {
            // Initialize data manager
            stepDataManager = StepDataManager.getInstance(this)

            // Set up notification manager and channel
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel()

            // Set up sensor manager and step counter sensor
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            stepCountSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            
            // Create intent to open app when notification is tapped
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            pendingIntent = PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            
            if (stepCountSensor == null) {
                Log.e(TAG, "No step counter sensor found on device")
                // Notify UI about sensor unavailability
                StepCounterServiceModule.getInstance()?.sendError("No step counter sensor found on device")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            StepCounterServiceModule.getInstance()?.sendError("Failed to initialize service: ${e.message}")
            handleError(e)
        }
    }

    /**
     * Creates the notification channel for Android 8.0 and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Step Counter Service Channel"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Called when the service is started.
     * Creates a foreground notification and starts step counting.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")

        try {
            // Create and show notification
            val notification = createNotification("Starting step counter...")
            startForeground(NOTIFICATION_ID, notification)

            // Load saved step data
            loadSavedStepData()

            // Start counting steps
            startStepCounting()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
            StepCounterServiceModule.getInstance()?.sendError("Failed to start service: ${e.message}")
            handleError(e)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    /**
     * Loads saved step data from persistent storage
     */
    private fun loadSavedStepData() {
        try {
            // Check if we need to reset for a new GMT day
            if (stepDataManager.shouldResetSteps()) {
                Log.d(TAG, "New GMT day detected, resetting step data")
                stepDataManager.resetStepsForNewDay()
                initialStepCount = -1
                currentStepCount = 0
                totalSteps = 0
            } else {
                initialStepCount = stepDataManager.getInitialStepCount()
                currentStepCount = stepDataManager.getLastStepCount()
                totalSteps = stepDataManager.getTotalSteps()
            }

            Log.d(TAG, "Loaded saved data - Initial: $initialStepCount, Current: $currentStepCount, Total: $totalSteps")
            
            // Update UI with saved data
            StepCounterServiceModule.getInstance()?.sendStepUpdate(totalSteps)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved step data", e)
            StepCounterServiceModule.getInstance()?.sendError("Failed to load saved step data: ${e.message}")
            handleError(e)
        }
    }

    /**
     * Saves current step data to persistent storage
     */
    private fun saveStepData() {
        try {
            stepDataManager.saveStepData(currentStepCount, initialStepCount, totalSteps)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving step data", e)
            StepCounterServiceModule.getInstance()?.sendError("Failed to save step data: ${e.message}")
            handleError(e)
        }
    }

    /**
     * Starts listening for step count updates from the sensor.
     */
    private fun startStepCounting() {
        if (!isCountingSteps && stepCountSensor != null) {
            try {
                sensorManager?.registerListener(
                    this,
                    stepCountSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                isCountingSteps = true
                retryCount = 0 // Reset retry count on successful start
                Log.d(TAG, "Step counting started")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start step counting", e)
                StepCounterServiceModule.getInstance()?.sendError("Failed to start step counting: ${e.message}")
                handleError(e)
            }
        } else if (stepCountSensor == null) {
            Log.e(TAG, "Cannot start step counting: No sensor available")
            StepCounterServiceModule.getInstance()?.sendError("No step counter sensor available")
        }
    }

    /**
     * Stops listening for step count updates.
     */
    private fun stopStepCounting() {
        if (isCountingSteps) {
            try {
                sensorManager?.unregisterListener(this)
                isCountingSteps = false
                saveStepData() // Save data before resetting
                initialStepCount = -1
                currentStepCount = 0
                totalSteps = 0
                retryCount = 0 // Reset retry count
                Log.d(TAG, "Step counting stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop step counting", e)
                StepCounterServiceModule.getInstance()?.sendError("Failed to stop step counting: ${e.message}")
                handleError(e)
            }
        }
    }

    /**
     * Creates a notification with the given message.
     */
    private fun createNotification(message: String): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * Updates the notification with the current step count.
     */
    private fun updateNotification() {
        val stepsSinceStart = if (initialStepCount == -1) 0 else currentStepCount - initialStepCount
        val message = "Steps: $stepsSinceStart"
        val notification = createNotification(message)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Called when the step counter sensor reports new data.
     * Updates the step count and notifies the UI.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            try {
                // Check if we need to reset for a new GMT day
                if (stepDataManager.shouldResetSteps()) {
                    Log.d(TAG, "New GMT day detected during step counting, resetting data")
                    stepDataManager.resetStepsForNewDay()
                    initialStepCount = -1
                    currentStepCount = 0
                    totalSteps = 0
                }

                val steps = event.values[0].toInt()
                
                // Set initial step count if not set
                if (initialStepCount == -1) {
                    initialStepCount = steps
                    Log.d(TAG, "Initial step count: $initialStepCount")
                }
                
                currentStepCount = steps
                totalSteps = currentStepCount - initialStepCount
                Log.d(TAG, "Steps updated - Total: $totalSteps")
                
                // Update UI through the module
                StepCounterServiceModule.getInstance()?.sendStepUpdate(totalSteps)
                
                // Update notification
                updateNotification()
                
                // Save data periodically (every 100 steps)
                if (totalSteps % 100 == 0) {
                    saveStepData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing step count", e)
                StepCounterServiceModule.getInstance()?.sendError("Error processing step count: ${e.message}")
                handleError(e)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used but required by SensorEventListener interface
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Called when the service is being destroyed.
     * Cleans up resources and stops step counting.
     */
    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        try {
            stopStepCounting()
            saveStepData() // Save final data
            stopForeground(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
            StepCounterServiceModule.getInstance()?.sendError("Error stopping service: ${e.message}")
            handleError(e)
        }
        super.onDestroy()
    }

    /**
     * Handles errors with retry mechanism
     */
    private fun handleError(e: Exception) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++
            Log.d(TAG, "Attempting retry $retryCount of $MAX_RETRY_COUNT")
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    when (e) {
                        is IllegalStateException -> {
                            // Retry sensor registration
                            if (isCountingSteps) {
                                startStepCounting()
                            }
                        }
                        else -> {
                            // For other errors, try to save data
                            saveStepData()
                        }
                    }
                } catch (retryError: Exception) {
                    Log.e(TAG, "Retry failed", retryError)
                }
            }, RETRY_DELAY_MS)
        } else {
            Log.e(TAG, "Max retry attempts reached", e)
            StepCounterServiceModule.getInstance()?.sendError("Service encountered persistent errors")
        }
    }
} 