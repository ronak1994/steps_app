package com.developerodin.myapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class StepNotificationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val CHANNEL_ID = "step_counter_channel"
    private val NOTIFICATION_ID = 1
    private var notificationManager: NotificationManager? = null

    init {
        createNotificationChannel()
        notificationManager = reactContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun getName(): String = "StepNotificationModule"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Step Counter"
            val descriptionText = "Shows current step count"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = reactApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @ReactMethod
    fun showNotification(stepCount: Double, promise: Promise) {
        try {
            val intent = reactApplicationContext.packageManager.getLaunchIntentForPackage(reactApplicationContext.packageName)
            val pendingIntent = PendingIntent.getActivity(
                reactApplicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(reactApplicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Step Counter")
                .setContentText("Steps: ${stepCount.toInt()}")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager?.notify(NOTIFICATION_ID, notification)
            promise.resolve("Notification updated successfully")
        } catch (e: Exception) {
            promise.reject("NOTIFICATION_ERROR", e.message)
        }
    }

    @ReactMethod
    fun hideNotification(promise: Promise) {
        try {
            notificationManager?.cancel(NOTIFICATION_ID)
            promise.resolve("Notification hidden successfully")
        } catch (e: Exception) {
            promise.reject("NOTIFICATION_ERROR", e.message)
        }
    }
} 