package com.developerodin.myapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * StepDataManager: Manages the persistence of step counting data.
 * This class handles saving and loading step counts, ensuring data persistence
 * between app restarts and service restarts.
 */
class StepDataManager private constructor(private val context: Context) {
    companion object {
        private const val TAG = "StepDataManager"
        private const val PREF_NAME = "StepDataPrefs"
        private const val KEY_LAST_STEP_COUNT = "lastStepCount"
        private const val KEY_INITIAL_STEP_COUNT = "initialStepCount"
        private const val KEY_TOTAL_STEPS = "totalSteps"
        private const val KEY_LAST_SAVE_TIME = "lastSaveTime"
        private const val KEY_LAST_RESET_DATE = "lastResetDate"

        @Volatile
        private var instance: StepDataManager? = null

        fun getInstance(context: Context): StepDataManager {
            return instance ?: synchronized(this) {
                instance ?: StepDataManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Saves the current step counting data to SharedPreferences.
     * This includes the last step count, initial step count, and total steps.
     */
    fun saveStepData(lastStepCount: Int, initialStepCount: Int, totalSteps: Int) {
        try {
            prefs.edit()
                .putInt(KEY_LAST_STEP_COUNT, lastStepCount)
                .putInt(KEY_INITIAL_STEP_COUNT, initialStepCount)
                .putInt(KEY_TOTAL_STEPS, totalSteps)
                .putLong(KEY_LAST_SAVE_TIME, System.currentTimeMillis())
                .apply()
            Log.d(TAG, "Step data saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving step data", e)
            throw e
        }
    }

    /**
     * Retrieves the last saved step count from SharedPreferences.
     */
    fun getLastStepCount(): Int {
        return try {
            prefs.getInt(KEY_LAST_STEP_COUNT, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last step count", e)
            0
        }
    }

    /**
     * Retrieves the initial step count from SharedPreferences.
     */
    fun getInitialStepCount(): Int {
        return try {
            prefs.getInt(KEY_INITIAL_STEP_COUNT, -1)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting initial step count", e)
            -1
        }
    }

    /**
     * Retrieves the total steps from SharedPreferences.
     */
    fun getTotalSteps(): Int {
        return try {
            prefs.getInt(KEY_TOTAL_STEPS, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total steps", e)
            0
        }
    }

    /**
     * Retrieves the timestamp of the last save operation.
     */
    fun getLastSaveTime(): Long {
        return try {
            prefs.getLong(KEY_LAST_SAVE_TIME, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last save time", e)
            0
        }
    }

    /**
     * Clears all saved step data from SharedPreferences.
     */
    fun clearStepData() {
        try {
            prefs.edit().clear().apply()
            Log.d(TAG, "Step data cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing step data", e)
            throw e
        }
    }

    /**
     * Checks if steps need to be reset based on GMT time.
     * Returns true if it's a new GMT day and steps should be reset.
     */
    fun shouldResetSteps(): Boolean {
        try {
            val currentGmtDate = LocalDate.now(ZoneOffset.UTC)
            val lastResetDateStr = prefs.getString(KEY_LAST_RESET_DATE, null)
            
            // If no last reset date, we should reset
            if (lastResetDateStr == null) {
                Log.d(TAG, "No last reset date found, resetting steps")
                saveLastResetDate(currentGmtDate)
                return true
            }

            // Parse last reset date
            val lastResetDate = try {
                LocalDate.parse(lastResetDateStr)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing last reset date: $lastResetDateStr", e)
                saveLastResetDate(currentGmtDate)
                return true
            }

            // Check if it's a new day
            val shouldReset = currentGmtDate.isAfter(lastResetDate)
            if (shouldReset) {
                Log.d(TAG, "New GMT day detected. Current: $currentGmtDate, Last: $lastResetDate")
                saveLastResetDate(currentGmtDate)
            }
            
            return shouldReset
        } catch (e: Exception) {
            Log.e(TAG, "Error checking step reset", e)
            return false
        }
    }

    private fun saveLastResetDate(date: LocalDate) {
        prefs.edit()
            .putString(KEY_LAST_RESET_DATE, date.toString())
            .apply()
        Log.d(TAG, "Saved last reset date: $date")
    }

    /**
     * Resets step data for the new GMT day.
     */
    fun resetStepsForNewDay() {
        try {
            prefs.edit()
                .putInt(KEY_LAST_STEP_COUNT, 0)
                .putInt(KEY_INITIAL_STEP_COUNT, -1)
                .putInt(KEY_TOTAL_STEPS, 0)
                .putLong(KEY_LAST_SAVE_TIME, System.currentTimeMillis())
                .apply()
            Log.d(TAG, "Step data reset for new GMT day")
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting step data", e)
            throw e
        }
    }
} 