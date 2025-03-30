# API Reference

## React Native Interface

### StepCounterService

The main interface for interacting with the native step counter service.

#### Methods

##### startService()
```typescript
startService(): Promise<void>
```
Starts the step counter service.

**Returns**: Promise that resolves when the service starts successfully.

**Throws**: 
- `SERVICE_ERROR`: If the service fails to start

**Example**:
```typescript
try {
  await StepCounterService.startService();
  console.log('Service started successfully');
} catch (error) {
  console.error('Failed to start service:', error);
}
```

##### stopService()
```typescript
stopService(): Promise<void>
```
Stops the step counter service.

**Returns**: Promise that resolves when the service stops successfully.

**Throws**:
- `SERVICE_ERROR`: If the service fails to stop

**Example**:
```typescript
try {
  await StepCounterService.stopService();
  console.log('Service stopped successfully');
} catch (error) {
  console.error('Failed to stop service:', error);
}
```

#### Events

##### stepUpdate
```typescript
interface StepUpdateEvent {
  steps: number;
}
```
Emitted when the step count is updated.

**Example**:
```typescript
stepCounterEventEmitter.addListener(STEP_UPDATE, (event: StepUpdateEvent) => {
  console.log('Steps updated:', event.steps);
});
```

##### serviceStatus
```typescript
interface ServiceStatusEvent {
  isRunning: boolean;
}
```
Emitted when the service status changes.

**Example**:
```typescript
stepCounterEventEmitter.addListener(SERVICE_STATUS, (event: ServiceStatusEvent) => {
  console.log('Service status:', event.isRunning ? 'Running' : 'Stopped');
});
```

##### error
```typescript
interface ErrorEvent {
  message: string;
}
```
Emitted when an error occurs.

**Example**:
```typescript
stepCounterEventEmitter.addListener(ERROR, (event: ErrorEvent) => {
  console.error('Error:', event.message);
});
```

## Native Android API

### StepCounterService

#### Methods

##### onCreate()
```kotlin
override fun onCreate()
```
Called when the service is created.

**Responsibilities**:
- Initialize service components
- Set up sensor manager
- Create notification channel
- Initialize data manager

##### onStartCommand()
```kotlin
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
```
Called when the service is started.

**Parameters**:
- `intent`: The intent that started the service
- `flags`: Additional flags
- `startId`: Unique integer representing this start request

**Returns**: 
- `START_STICKY`: Service will be restarted if killed
- `START_NOT_STICKY`: Service will not be restarted

##### onSensorChanged()
```kotlin
override fun onSensorChanged(event: SensorEvent?)
```
Called when sensor data changes.

**Parameters**:
- `event`: The sensor event containing step data

**Responsibilities**:
- Process step count
- Update UI
- Save data
- Handle GMT reset

### StepDataManager

#### Methods

##### getInstance()
```kotlin
fun getInstance(context: Context): StepDataManager
```
Gets the singleton instance of StepDataManager.

**Parameters**:
- `context`: Application context

**Returns**: StepDataManager instance

##### saveStepData()
```kotlin
fun saveStepData(lastStepCount: Int, initialStepCount: Int, totalSteps: Int)
```
Saves step counting data.

**Parameters**:
- `lastStepCount`: Current step count
- `initialStepCount`: Initial step count
- `totalSteps`: Total steps for the day

##### getTotalSteps()
```kotlin
fun getTotalSteps(): Int
```
Gets the total steps for the current day.

**Returns**: Total step count

##### shouldResetSteps()
```kotlin
fun shouldResetSteps(): Boolean
```
Checks if steps should be reset based on GMT time.

**Returns**: true if steps should be reset

### StepCounterServiceModule

#### Methods

##### startService()
```kotlin
@ReactMethod
fun startService(promise: Promise)
```
Starts the step counter service.

**Parameters**:
- `promise`: Promise to resolve/reject

##### stopService()
```kotlin
@ReactMethod
fun stopService(promise: Promise)
```
Stops the step counter service.

**Parameters**:
- `promise`: Promise to resolve/reject

##### sendStepUpdate()
```kotlin
fun sendStepUpdate(steps: Int)
```
Sends step count update to React Native.

**Parameters**:
- `steps`: Current step count

##### sendServiceStatus()
```kotlin
fun sendServiceStatus(isRunning: Boolean)
```
Sends service status update to React Native.

**Parameters**:
- `isRunning`: Current service status

##### sendError()
```kotlin
fun sendError(message: String)
```
Sends error message to React Native.

**Parameters**:
- `message`: Error message

## Constants

### Service Constants
```kotlin
private const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "StepCounterChannel"
private const val CHANNEL_NAME = "Step Counter"
private const val MAX_RETRY_COUNT = 3
private const val RETRY_DELAY_MS = 1000L
```

### Data Storage Constants
```kotlin
private const val PREF_NAME = "StepDataPrefs"
private const val KEY_LAST_STEP_COUNT = "lastStepCount"
private const val KEY_INITIAL_STEP_COUNT = "initialStepCount"
private const val KEY_TOTAL_STEPS = "totalSteps"
private const val KEY_LAST_RESET_DATE = "lastResetDate"
```

### Event Constants
```typescript
export const STEP_UPDATE = 'stepUpdate';
export const SERVICE_STATUS = 'serviceStatus';
export const ERROR = 'error';
```

## Error Handling

### Service Errors
```kotlin
class ServiceError : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
```

### Data Errors
```kotlin
class DataError : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
```

## Best Practices

### 1. Service Management
- Always check service status before operations
- Handle service lifecycle properly
- Implement proper cleanup

### 2. Data Management
- Save data periodically
- Validate data integrity
- Handle GMT resets properly

### 3. Error Handling
- Log all errors
- Provide user feedback
- Implement recovery mechanisms

### 4. Event Handling
- Clean up event listeners
- Handle component unmounting
- Validate event data 