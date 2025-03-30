package com.developerodin.myapp

import android.util.Log
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class StepCounterServicePackage : ReactPackage {
    companion object {
        private const val TAG = "StepCounterServicePackage"
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        try {
            return listOf(StepCounterServiceModule(reactContext))
        } catch (e: Exception) {
            Log.e(TAG, "Error creating native modules", e)
            return emptyList()
        }
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
} 