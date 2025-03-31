# Step Counter App Architecture

## Overview

The Step Counter App uses a hybrid architecture combining React Native for the UI layer and native Android components for core step counting functionality. The app implements a robust step counting service that runs in the background, persists data, and handles daily resets at GMT 00:00.

## System Architecture

### 1. React Native Layer (TypeScript)
- **UI Components**: Displays step counts, service status, and error messages
- **Native Module Interface**: Communicates with the Android service
- **Event Handling**: Manages step updates and service status changes

### 2. Native Bridge Layer (Kotlin)
- **StepCounterServiceModule**: Bridge between React Native and native service
- **Event Emission**: Sends updates to React Native
- **Error Handling**: Propagates errors to the UI

### 3. Android Service Layer (Kotlin)
- **StepCounterService**: Foreground service for step counting
- **StepDataManager**: Handles data persistence and GMT resets
- **Sensor Management**: Interfaces with hardware step counter

## Component Architecture

### StepCounterService
- **Responsibilities**:
  - Manages step counter sensor
  - Maintains foreground service
  - Handles step count updates
  - Manages data persistence
  - Implements GMT reset logic
  - Error recovery with retry mechanism

- **Key Features**:
  - Automatic service recovery
  - Periodic data saving
  - Real-time UI updates
  - Notification management
  - Error handling with retries

### StepDataManager
- **Responsibilities**:
  - Persists step counting data
  - Manages GMT-based resets
  - Handles data loading/saving
  - Maintains consistency across restarts

- **Key Features**:
  - SharedPreferences storage
  - GMT date tracking
  - Data reset management
  - Error recovery

### React Native Bridge
- **Responsibilities**:
  - Exposes native functionality to JS
  - Manages event communication
  - Handles permissions
  - Error propagation

## Data Flow

1. **Step Count Update**:
```
Sensor → StepCounterService → StepDataManager → React Native UI
```

2. **GMT Reset**:
```
GMT Check → Reset Decision → Data Reset → Service Update → UI Update
```

3. **Error Recovery**:
```
Error Detection → Retry Mechanism → Error Propagation → UI Notification
```

## Design Patterns

1. **Singleton Pattern**
   - Used in StepDataManager
   - Ensures single instance for data management

2. **Observer Pattern**
   - Step count updates
   - Service status changes
   - Error notifications

3. **Repository Pattern**
   - Data persistence layer
   - Abstracts storage implementation

## Security Considerations

1. **Data Protection**
   - Secure storage using SharedPreferences
   - Private mode data access
   - No sensitive data exposure

2. **Permission Management**
   - Runtime permission handling
   - Graceful permission denial handling
   - Clear user communication

## Performance Considerations

1. **Battery Optimization**
   - Efficient sensor sampling
   - Periodic data saving
   - Optimized GMT checks

2. **Memory Management**
   - Proper service cleanup
   - Resource release
   - Memory leak prevention

3. **Data Efficiency**
   - Minimal storage operations
   - Efficient data structures
   - Optimized calculations

## Error Handling Strategy

1. **Service Level**
   - Sensor errors
   - Storage errors
   - Runtime exceptions
   - Retry mechanism

2. **Data Level**
   - Corruption handling
   - Recovery mechanisms
   - Data validation

3. **UI Level**
   - User notifications
   - Error state display
   - Recovery options

## Testing Strategy

1. **Unit Tests**
   - Step counting logic
   - GMT reset functionality
   - Data persistence

2. **Integration Tests**
   - Service communication
   - Event propagation
   - Permission handling

3. **UI Tests**
   - User interactions
   - State management
   - Error displays

## Future Considerations

1. **Scalability**
   - Additional sensor support
   - Multiple activity types
   - Enhanced analytics

2. **Maintainability**
   - Modular components
   - Clear documentation
   - Consistent patterns

3. **Feature Expansion**
   - Activity recognition
   - Health metrics
   - Social features 