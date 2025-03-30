# Component Details

## React Native Components

### 1. Main UI (`index.tsx`)

#### Purpose
Main interface for the step counter application.

#### Key Features
- Step count display
- Service control buttons
- Permission management
- Error display
- Debug logging

#### Implementation
```typescript
export default function TabOneScreen() {
  // State management
  const [serviceRunning, setServiceRunning] = useState(false);
  const [stepCount, setStepCount] = useState(0);
  const [logs, setLogs] = useState<string[]>([]);
  const [permissions, setPermissions] = useState({
    activityRecognition: false,
    notification: false,
  });
  const [error, setError] = useState<string | null>(null);

  // Event listeners
  useEffect(() => {
    const stepSubscription = stepCounterEventEmitter.addListener(
      STEP_UPDATE,
      (event) => {
        setStepCount(event.steps);
        addLog(`Steps updated: ${event.steps}`);
      }
    );
    // ... other listeners
  }, []);
```

### 2. TypeScript Interface (`StepCounterService.ts`)

#### Purpose
Defines the interface between React Native and native Android code.

#### Key Features
- Service control methods
- Event type definitions
- Event emitter setup

#### Implementation
```typescript
export const STEP_UPDATE = 'stepUpdate';
export const SERVICE_STATUS = 'serviceStatus';
export const ERROR = 'error';

interface StepCounterServiceType {
  startService(): Promise<void>;
  stopService(): Promise<void>;
}

const StepCounterService = NativeModules.StepCounterService as StepCounterServiceType;
```

## Native Android Components

### 1. Step Counter Service (`StepCounterService.kt`)

#### Purpose
Background service for step counting.

#### Key Features
- Sensor management
- Step counting
- Data persistence
- Error handling
- GMT reset

#### Implementation
```kotlin
class StepCounterService : Service(), SensorEventListener {
    companion object {
        private const val TAG = "StepCounterService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "StepCounterChannel"
    }

    // Service components
    private var notificationManager: NotificationManager? = null
    private var sensorManager: SensorManager? = null
    private var stepCountSensor: Sensor? = null
```

### 2. Data Manager (`StepDataManager.kt`)

#### Purpose
Manages data persistence and GMT-based resets.

#### Key Features
- Data storage
- GMT reset logic
- Data validation
- Error handling

#### Implementation
```kotlin
class StepDataManager private constructor(private val context: Context) {
    companion object {
        private const val PREF_NAME = "StepDataPrefs"
        private const val KEY_LAST_STEP_COUNT = "lastStepCount"
        private const val KEY_INITIAL_STEP_COUNT = "initialStepCount"
        private const val KEY_TOTAL_STEPS = "totalSteps"
        private const val KEY_LAST_RESET_DATE = "lastResetDate"
    }
```

### 3. React Native Bridge (`StepCounterServiceModule.kt`)

#### Purpose
Bridges React Native and native Android code.

#### Key Features
- Method bridging
- Event emission
- Service lifecycle management
- Error handling

#### Implementation
```kotlin
class StepCounterServiceModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val STEP_UPDATE_EVENT = "stepUpdate"
        private const val SERVICE_STATUS_EVENT = "serviceStatus"
        private const val ERROR_EVENT = "error"
    }
```

## Component Interactions

### 1. Service-UI Communication
```
Service → Bridge → EventEmitter → React Native → UI
```

### 2. Data Flow
```
Sensor → Service → DataManager → Bridge → React Native → UI
```

### 3. Error Handling
```
Error → Service → Bridge → EventEmitter → React Native → UI
```

## Component Lifecycles

### 1. Service Lifecycle
- onCreate
- onStartCommand
- onSensorChanged
- onDestroy

### 2. React Native Component Lifecycle
- Component mount
- Event listener setup
- State updates
- Component unmount

### 3. Data Manager Lifecycle
- Initialization
- Data loading
- Data saving
- GMT reset checks

## Best Practices

### 1. Service Implementation
- Use foreground service
- Handle sensor events efficiently
- Implement proper cleanup
- Manage resources carefully

### 2. Data Management
- Save data periodically
- Validate data integrity
- Handle GMT resets properly
- Implement error recovery

### 3. UI Implementation
- Handle component unmounting
- Manage event listeners
- Provide user feedback
- Handle errors gracefully

## Common Issues and Solutions

### 1. Service Issues
- Service not starting
- Sensor not available
- Data persistence problems
- GMT reset issues

### 2. UI Issues
- Step count not updating
- Service status not syncing
- Permission problems
- Error display issues

### 3. Data Issues
- Data corruption
- GMT reset problems
- Persistence failures
- State inconsistencies

## Testing Guidelines

### 1. Service Testing
- Test service lifecycle
- Verify sensor functionality
- Check data persistence
- Validate GMT resets

### 2. UI Testing
- Test component rendering
- Verify event handling
- Check state management
- Validate error display

### 3. Integration Testing
- Test service-UI communication
- Verify data flow
- Check error handling
- Validate GMT functionality 