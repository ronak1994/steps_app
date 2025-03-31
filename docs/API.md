# Step Counter App API Reference

## React Native Interface

### StepCounterService

```typescript
interface StepUpdateEvent {
  steps: number;
}

interface ServiceStatusEvent {
  isRunning: boolean;
}

interface ErrorEvent {
  message: string;
}

interface StepCounterServiceInterface {
  startService(): Promise<void>;
  stopService(): Promise<void>;
  addStepUpdateListener(callback: (event: StepUpdateEvent) => void): () => void;
  addServiceStatusListener(callback: (event: ServiceStatusEvent) => void): () => void;
  addErrorListener(callback: (event: ErrorEvent) => void): () => void;
}
```

#### Methods

##### `startService()`
- **Description**: Starts the step counting service
- **Returns**: Promise<void>
- **Throws**: Error if service fails to start
- **Example**:
```typescript
try {
  await StepCounterService.startService();
} catch (error) {
  console.error('Failed to start service:', error);
}
```

##### `stopService()`
- **Description**: Stops the step counting service
- **Returns**: Promise<void>
- **Throws**: Error if service fails to stop
- **Example**:
```typescript
try {
  await StepCounterService.stopService();
} catch (error) {
  console.error('Failed to stop service:', error);
}
```

#### Event Listeners

##### `addStepUpdateListener()`
- **Description**: Subscribes to step count updates
- **Parameters**: Callback function receiving StepUpdateEvent
- **Returns**: Unsubscribe function
- **Example**:
```typescript
const unsubscribe = StepCounterService.addStepUpdateListener((event) => {
  console.log('Steps:', event.steps);
});
```

##### `addServiceStatusListener()`
- **Description**: Subscribes to service status changes
- **Parameters**: Callback function receiving ServiceStatusEvent
- **Returns**: Unsubscribe function
- **Example**:
```typescript
const unsubscribe = StepCounterService.addServiceStatusListener((event) => {
  console.log('Service running:', event.isRunning);
});
```

##### `addErrorListener()`
- **Description**: Subscribes to error events
- **Parameters**: Callback function receiving ErrorEvent
- **Returns**: Unsubscribe function
- **Example**:
```typescript
const unsubscribe = StepCounterService.addErrorListener((event) => {
  console.error('Service error:', event.message);
});
```

## Native Android API

### StepCounterService

#### Public Methods

##### `updateSteps(steps: Int)`
- **Description**: Updates step count and notifies listeners
- **Parameters**: Current step count
- **Location**: Companion object

#### Protected Methods

##### `onCreate()`
- **Description**: Initializes service components
- **Responsibilities**:
  - Initializes StepDataManager
  - Creates notification channel
  - Sets up step counter sensor
  - Configures pending intent

##### `onStartCommand(intent: Intent?, flags: Int, startId: Int)`
- **Description**: Handles service start request
- **Returns**: START_STICKY
- **Responsibilities**:
  - Creates foreground notification
  - Loads saved step data
  - Starts step counting

##### `onSensorChanged(event: SensorEvent?)`
- **Description**: Handles step counter sensor updates
- **Responsibilities**:
  - Checks for GMT reset
  - Updates step counts
  - Notifies UI
  - Updates notification
  - Saves data periodically

### StepDataManager

#### Public Methods

##### `getInstance(context: Context)`
- **Description**: Gets singleton instance
- **Returns**: StepDataManager instance

##### `saveStepData(lastStepCount: Int, initialStepCount: Int, totalSteps: Int)`
- **Description**: Saves step counting data
- **Parameters**: Current counts and totals

##### `shouldResetSteps()`
- **Description**: Checks if GMT reset is needed
- **Returns**: Boolean indicating reset needed

##### `resetStepsForNewDay()`
- **Description**: Resets step data for new GMT day

##### `getLastStepCount()`
- **Description**: Gets last saved step count
- **Returns**: Int

##### `getInitialStepCount()`
- **Description**: Gets initial step count
- **Returns**: Int

##### `getTotalSteps()`
- **Description**: Gets total steps
- **Returns**: Int

## Events

### Step Update Event
```json
{
  "steps": number  // Current step count
}
```

### Service Status Event
```json
{
  "isRunning": boolean  // Service running status
}
```

### Error Event
```json
{
  "message": string  // Error description
}
```

## Error Handling

### Service Errors
- Sensor unavailable
- Permission denied
- Service start/stop failures
- Data persistence errors

### Recovery Mechanism
- Maximum retry count: 3
- Retry delay: 1000ms
- Error propagation to UI
- Automatic service recovery

## Data Storage

### SharedPreferences Keys
- `lastStepCount`: Last recorded step count
- `initialStepCount`: Initial step count
- `totalSteps`: Total steps for current period
- `lastSaveTime`: Timestamp of last save
- `lastResetDate`: Date of last GMT reset

## Permissions

Required Android permissions:
```xml
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION"/>
<uses-permission android:name="android.permission.BODY_SENSORS"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
``` 