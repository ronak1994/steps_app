# Step Counter App Implementation Guide

## Prerequisites

1. Development Environment
   - Node.js (v14 or later)
   - React Native development environment
   - Android Studio
   - Expo CLI

2. Required Dependencies
   ```json
   {
     "dependencies": {
       "react-native": "0.72.x",
       "expo": "^49.0.0"
     }
   }
   ```

## Project Setup

1. Create New Project
   ```bash
   npx create-expo-app my-step-counter
   cd my-step-counter
   ```

2. Initialize Native Code
   ```bash
   npx expo prebuild
   ```

## Android Implementation

### 1. Configure AndroidManifest.xml

Add required permissions and service declaration:

```xml
<manifest>
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION"/>
    <uses-permission android:name="android.permission.BODY_SENSORS"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

    <application>
        <service
            android:name=".StepCounterService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="shortService"/>
    </application>
</manifest>
```

### 2. Create Native Files

#### StepDataManager.kt
```kotlin
class StepDataManager private constructor(private val context: Context) {
    companion object {
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

    // Implementation methods...
}
```

#### StepCounterService.kt
```kotlin
class StepCounterService : Service(), SensorEventListener {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "StepCounterChannel"
        private const val CHANNEL_NAME = "Step Counter"
        private const val MAX_RETRY_COUNT = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    // Service implementation...
}
```

#### StepCounterServiceModule.kt
```kotlin
class StepCounterServiceModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val TAG = "StepCounterServiceModule"
        private const val STEP_UPDATE_EVENT = "stepUpdate"
        private const val SERVICE_STATUS_EVENT = "serviceStatus"
        private const val ERROR_EVENT = "error"

        @Volatile
        private var instance: StepCounterServiceModule? = null
    }

    // Module implementation...
}
```

### 3. Register Native Module

#### StepCounterPackage.kt
```kotlin
class StepCounterPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(StepCounterServiceModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
```

## React Native Implementation

### 1. TypeScript Interface

```typescript
// app/types/StepCounterService.ts

import { NativeEventEmitter, NativeModules } from 'react-native';

const { StepCounterService } = NativeModules;

if (!StepCounterService) {
    throw new Error('StepCounterService native module is not available');
}

const eventEmitter = new NativeEventEmitter(StepCounterService);

export interface StepUpdateEvent {
    steps: number;
}

export interface ServiceStatusEvent {
    isRunning: boolean;
}

export interface ErrorEvent {
    message: string;
}

export interface StepCounterServiceInterface {
    startService(): Promise<void>;
    stopService(): Promise<void>;
    addStepUpdateListener(callback: (event: StepUpdateEvent) => void): () => void;
    addServiceStatusListener(callback: (event: ServiceStatusEvent) => void): () => void;
    addErrorListener(callback: (event: ErrorEvent) => void): () => void;
}

// Implementation...
```

### 2. Main Component

```typescript
// app/index.tsx

import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import StepCounterService from './types/StepCounterService';

export default function App() {
    const [isServiceRunning, setIsServiceRunning] = useState(false);
    const [stepCount, setStepCount] = useState(0);
    const [logs, setLogs] = useState<string[]>([]);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        // Add event listeners...
        return () => {
            // Cleanup...
        };
    }, []);

    // Implementation...
}
```

## Testing Implementation

### 1. Unit Tests

```typescript
// __tests__/StepCounter.test.ts

describe('StepCounterService', () => {
    it('should initialize correctly', () => {
        // Test initialization
    });

    it('should handle step updates', () => {
        // Test step counting
    });

    it('should handle GMT reset', () => {
        // Test reset functionality
    });
});
```

### 2. Integration Tests

```kotlin
// androidTest/StepCounterServiceTest.kt

class StepCounterServiceTest {
    @Test
    fun testServiceLifecycle() {
        // Test service lifecycle
    }

    @Test
    fun testStepCounting() {
        // Test step counting
    }
}
```

## Deployment

### 1. Build Configuration

```gradle
// android/app/build.gradle

android {
    defaultConfig {
        // Configuration...
    }
    
    buildTypes {
        release {
            // Release configuration...
        }
    }
}
```

### 2. Release Build

```bash
# Generate release build
cd android
./gradlew assembleRelease
```

## Troubleshooting

### Common Issues

1. Service Not Starting
   - Check permissions
   - Verify service registration
   - Check error logs

2. Step Count Not Updating
   - Verify sensor availability
   - Check event emission
   - Debug data persistence

3. GMT Reset Issues
   - Verify date calculations
   - Check reset logic
   - Debug data storage

### Debugging Tips

1. Enable Debug Logging
   ```kotlin
   Log.d(TAG, "Detailed message")
   ```

2. Monitor Events
   ```typescript
   StepCounterService.addErrorListener((error) => {
       console.log('Error:', error);
   });
   ```

3. Test Sensor
   ```kotlin
   val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
   val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
   Log.d(TAG, "Sensor available: ${stepSensor != null}")
   ```

## Best Practices

1. Error Handling
   - Implement comprehensive error handling
   - Provide user feedback
   - Log errors for debugging

2. Battery Optimization
   - Use appropriate sensor delay
   - Implement efficient data saving
   - Optimize GMT checks

3. Data Management
   - Regular data persistence
   - Proper state management
   - Handle edge cases

4. Testing
   - Unit test core functionality
   - Integration test service
   - UI test user interactions 