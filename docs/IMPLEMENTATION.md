# Implementation Guide

## Setup Instructions

### 1. Project Setup

1. Create a new Expo project:
```bash
npx create-expo-app -t expo-template-blank-typescript step-counter-app
cd step-counter-app
```

2. Install dependencies:
```bash
npm install
```

3. Create native Android project:
```bash
npx expo prebuild
```

### 2. Android Configuration

1. Add required permissions to `android/app/src/main/AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

2. Add service declaration:
```xml
<service
    android:name=".StepCounterService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="shortService" />
```

## Implementation Steps

### 1. TypeScript Interface

1. Create `app/types/StepCounterService.ts`:
```typescript
import { NativeEventEmitter, NativeModules } from 'react-native';

export const STEP_UPDATE = 'stepUpdate';
export const SERVICE_STATUS = 'serviceStatus';
export const ERROR = 'error';

interface StepCounterServiceType {
  startService(): Promise<void>;
  stopService(): Promise<void>;
}

const StepCounterService = NativeModules.StepCounterService as StepCounterServiceType;
export const stepCounterEventEmitter = new NativeEventEmitter(StepCounterService);
```

### 2. Main UI

1. Create `app/(tabs)/index.tsx`:
```typescript
export default function TabOneScreen() {
  const [serviceRunning, setServiceRunning] = useState(false);
  const [stepCount, setStepCount] = useState(0);
  const [logs, setLogs] = useState<string[]>([]);
  const [permissions, setPermissions] = useState({
    activityRecognition: false,
    notification: false,
  });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    checkAndRequestPermissions();
    setupEventListeners();
  }, []);

  const setupEventListeners = () => {
    const stepSubscription = stepCounterEventEmitter.addListener(
      STEP_UPDATE,
      (event) => {
        setStepCount(event.steps);
        addLog(`Steps updated: ${event.steps}`);
      }
    );
    // ... other listeners
  };
}
```

### 3. Native Service

1. Create `StepCounterService.kt`:
```kotlin
class StepCounterService : Service(), SensorEventListener {
    companion object {
        private const val TAG = "StepCounterService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "StepCounterChannel"
    }

    private var notificationManager: NotificationManager? = null
    private var sensorManager: SensorManager? = null
    private var stepCountSensor: Sensor? = null
    private lateinit var stepDataManager: StepDataManager

    override fun onCreate() {
        super.onCreate()
        setupService()
    }

    private fun setupService() {
        stepDataManager = StepDataManager.getInstance(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCountSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }
}
```

### 4. Data Manager

1. Create `StepDataManager.kt`:
```kotlin
class StepDataManager private constructor(private val context: Context) {
    companion object {
        private const val PREF_NAME = "StepDataPrefs"
        private const val KEY_LAST_STEP_COUNT = "lastStepCount"
        private const val KEY_INITIAL_STEP_COUNT = "initialStepCount"
        private const val KEY_TOTAL_STEPS = "totalSteps"
        private const val KEY_LAST_RESET_DATE = "lastResetDate"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveStepData(lastStepCount: Int, initialStepCount: Int, totalSteps: Int) {
        prefs.edit()
            .putInt(KEY_LAST_STEP_COUNT, lastStepCount)
            .putInt(KEY_INITIAL_STEP_COUNT, initialStepCount)
            .putInt(KEY_TOTAL_STEPS, totalSteps)
            .putLong(KEY_LAST_SAVE_TIME, System.currentTimeMillis())
            .apply()
    }
}
```

### 5. React Native Bridge

1. Create `StepCounterServiceModule.kt`:
```kotlin
class StepCounterServiceModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val STEP_UPDATE_EVENT = "stepUpdate"
        private const val SERVICE_STATUS_EVENT = "serviceStatus"
        private const val ERROR_EVENT = "error"
    }

    @ReactMethod
    fun startService(promise: Promise) {
        try {
            val context = reactApplicationContext
            val serviceIntent = Intent(context, StepCounterService::class.java)
            context.startForegroundService(serviceIntent)
            isServiceRunning = true
            sendServiceStatus(true)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("SERVICE_ERROR", e)
        }
    }
}
```

## Testing Implementation

### 1. Service Testing
```kotlin
@Test
fun testServiceLifecycle() {
    val service = StepCounterService()
    service.onCreate()
    assertTrue(service.isCountingSteps)
    service.onDestroy()
    assertFalse(service.isCountingSteps)
}
```

### 2. Data Manager Testing
```kotlin
@Test
fun testDataPersistence() {
    val manager = StepDataManager.getInstance(context)
    manager.saveStepData(100, 0, 100)
    assertEquals(100, manager.getTotalSteps())
}
```

### 3. UI Testing
```typescript
test('step count updates correctly', () => {
  const { getByText } = render(<TabOneScreen />);
  fireEvent.press(getByText('Start Service'));
  expect(getByText('0 steps')).toBeTruthy();
});
```

## Deployment

### 1. Build Configuration
1. Update `app.json`:
```json
{
  "expo": {
    "android": {
      "permissions": [
        "ACTIVITY_RECOGNITION",
        "FOREGROUND_SERVICE",
        "POST_NOTIFICATIONS"
      ]
    }
  }
}
```

### 2. Build Process
```bash
# Development build
npx expo run:android

# Production build
cd android
./gradlew assembleRelease
```

## Maintenance

### 1. Logging
```kotlin
private fun logError(message: String, error: Exception) {
    Log.e(TAG, message, error)
    StepCounterServiceModule.getInstance()?.sendError(message)
}
```

### 2. Error Recovery
```kotlin
private fun handleError(e: Exception) {
    if (retryCount < MAX_RETRY_COUNT) {
        retryCount++
        Handler(Looper.getMainLooper()).postDelayed({
            when (e) {
                is IllegalStateException -> startStepCounting()
                else -> saveStepData()
            }
        }, RETRY_DELAY_MS)
    }
}
```

### 3. Data Validation
```kotlin
private fun validateStepData(steps: Int): Boolean {
    return steps >= 0 && steps <= MAX_STEPS_PER_DAY
}
```

## Troubleshooting

### 1. Common Issues
- Service not starting
- Step count not updating
- Data persistence issues
- GMT reset problems

### 2. Debugging Steps
1. Check logs
2. Verify permissions
3. Test sensor availability
4. Validate data storage

### 3. Recovery Procedures
1. Restart service
2. Clear data
3. Reinstall app
4. Factory reset 