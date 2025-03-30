# Quick Start Guide

## Prerequisites

Before you begin, ensure you have:
- Node.js (v14 or later)
- Expo CLI (`npm install -g expo-cli`)
- Android Studio
- An Android device or emulator with step counter sensor

## Quick Setup

### 1. Create Project
```bash
# Create new Expo project
npx create-expo-app -t expo-template-blank-typescript step-counter-app
cd step-counter-app

# Install dependencies
npm install

# Create native Android project
npx expo prebuild
```

### 2. Configure Android

1. Open `android/app/src/main/AndroidManifest.xml` and add permissions:
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

### 3. Create Essential Files

1. Create TypeScript interface (`app/types/StepCounterService.ts`):
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

2. Create main UI (`app/(tabs)/index.tsx`):
```typescript
import { View, Text, Button } from 'react-native';
import { StepCounterService, stepCounterEventEmitter, STEP_UPDATE } from '../types/StepCounterService';

export default function TabOneScreen() {
  const [stepCount, setStepCount] = useState(0);
  const [serviceRunning, setServiceRunning] = useState(false);

  useEffect(() => {
    const subscription = stepCounterEventEmitter.addListener(
      STEP_UPDATE,
      (event) => setStepCount(event.steps)
    );
    return () => subscription.remove();
  }, []);

  const toggleService = async () => {
    try {
      if (serviceRunning) {
        await StepCounterService.stopService();
      } else {
        await StepCounterService.startService();
      }
      setServiceRunning(!serviceRunning);
    } catch (error) {
      console.error('Service error:', error);
    }
  };

  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Text style={{ fontSize: 24, marginBottom: 20 }}>
        Steps: {stepCount}
      </Text>
      <Button
        title={serviceRunning ? 'Stop Service' : 'Start Service'}
        onPress={toggleService}
      />
    </View>
  );
}
```

### 4. Run the App

1. Start the development server:
```bash
npx expo start
```

2. Run on Android:
```bash
npx expo run:android
```

## Basic Usage

1. **Starting the Service**
   - Launch the app
   - Tap "Start Service"
   - Grant required permissions when prompted

2. **Monitoring Steps**
   - Step count updates in real-time
   - Count resets at GMT 00:00
   - Service continues in background

3. **Stopping the Service**
   - Tap "Stop Service"
   - Step count persists until next GMT reset

## Common Issues

### 1. Service Not Starting
- Check permissions in Settings
- Ensure device has step counter sensor
- Restart app and try again

### 2. Step Count Not Updating
- Verify sensor is working
- Check if service is running
- Try restarting service

### 3. App Crashing
- Check logs for errors
- Verify all permissions
- Reinstall app if needed

## Next Steps

1. **Explore Features**
   - Background counting
   - GMT-based resets
   - Error handling
   - Data persistence

2. **Customize UI**
   - Add step goals
   - Implement history view
   - Create settings screen

3. **Add Features**
   - Step history
   - Data export
   - Goal tracking
   - Statistics

## Development Tips

### 1. Debugging
- Use `adb logcat` to monitor native logs
- Add `console.log` statements in React Native code
- Check Logcat with tag "StepCounterService" for service logs
- Use React Native Debugger for UI debugging

### 2. Testing
- Test on multiple Android versions
- Verify background behavior
- Test GMT reset functionality
- Check permission handling
- Test service recovery after app kill

### 3. Performance Optimization
- Minimize UI updates
- Use `useCallback` for event handlers
- Implement proper cleanup in `useEffect`
- Save data periodically, not on every step
- Use `SharedPreferences` efficiently

### 4. Best Practices
- Always handle service lifecycle properly
- Implement proper error recovery
- Clean up event listeners
- Handle component unmounting
- Use TypeScript for type safety
- Follow Android service guidelines

### 5. Common Pitfalls
- Don't forget to unregister sensor listeners
- Handle permission changes
- Manage service state properly
- Clean up resources in `onDestroy`
- Handle timezone changes

### 6. Development Workflow
1. Make changes in React Native code
2. Test UI changes immediately
3. Modify native code when needed
4. Rebuild native project
5. Test thoroughly on device
6. Check background behavior
7. Verify data persistence

### 7. Testing Scenarios
- App in foreground
- App in background
- App killed
- Device reboot
- Timezone changes
- Permission changes
- Sensor availability
- Data persistence
- GMT reset

### 8. Code Organization
- Keep native code modular
- Use constants for configuration
- Implement proper error handling
- Follow consistent naming
- Document complex logic
- Use TypeScript interfaces

### 9. Development Tools
- Android Studio for native code
- VS Code for React Native
- React Native Debugger
- Chrome DevTools
- Android Device Monitor
- Logcat Viewer

### 10. Release Checklist
- Test on multiple devices
- Verify all permissions
- Check background behavior
- Test GMT reset
- Validate data persistence
- Review error handling
- Check memory usage
- Test battery impact
- Verify notifications
- Review crash reports

## Resources

- [Architecture Overview](ARCHITECTURE.md)
- [Component Details](COMPONENTS.md)
- [Implementation Guide](IMPLEMENTATION.md)
- [API Reference](API.md)
- [Troubleshooting](TROUBLESHOOTING.md)

## Support

Need help? Check:
1. [Documentation](docs/)
2. [GitHub Issues](https://github.com/yourusername/step-counter-app/issues)
3. Create new issue if needed 