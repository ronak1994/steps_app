import { StyleSheet, View, Button, Text, Platform, ScrollView, PermissionsAndroid } from 'react-native';
import { useEffect, useState } from 'react';
import { StepCounterService, stepCounterEventEmitter, STEP_UPDATE, SERVICE_STATUS } from '../types/StepCounterService';

export default function TabOneScreen() {
  // State for service and step count
  const [serviceRunning, setServiceRunning] = useState(false);
  const [stepCount, setStepCount] = useState(0);
  // State for logs
  const [logs, setLogs] = useState<string[]>([]);
  // State for permissions
  const [permissions, setPermissions] = useState({
    activityRecognition: false,
    notification: false,
  });

  // Helper function to add logs
  const addLog = (message: string) => {
    setLogs(prevLogs => [...prevLogs, `${new Date().toLocaleTimeString()}: ${message}`]);
  };

  // Function to check and request permissions
  const checkAndRequestPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        // Check Activity Recognition permission
        const activityRecognitionGranted = await PermissionsAndroid.check(
          PermissionsAndroid.PERMISSIONS.ACTIVITY_RECOGNITION
        );
        
        // Check Notification permission for Android 13+
        let notificationGranted = true;
        if (Platform.Version >= 33) {
          notificationGranted = await PermissionsAndroid.check(
            PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
          );
        }

        // Update permissions state
        setPermissions({
          activityRecognition: activityRecognitionGranted,
          notification: notificationGranted,
        });

        // Request permissions if not granted
        if (!activityRecognitionGranted || !notificationGranted) {
          const permissionsToRequest = [];
          if (!activityRecognitionGranted) {
            permissionsToRequest.push(PermissionsAndroid.PERMISSIONS.ACTIVITY_RECOGNITION);
          }
          if (!notificationGranted && Platform.Version >= 33) {
            permissionsToRequest.push(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
          }

          const result = await PermissionsAndroid.requestMultiple(permissionsToRequest);
          
          // Update permissions state after request
          setPermissions({
            activityRecognition: result[PermissionsAndroid.PERMISSIONS.ACTIVITY_RECOGNITION] === 'granted',
            notification: result[PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS] === 'granted' || Platform.Version < 33,
          });

          // Log permission results
          addLog('Permission Results:');
          addLog(`Activity Recognition: ${result[PermissionsAndroid.PERMISSIONS.ACTIVITY_RECOGNITION]}`);
          if (Platform.Version >= 33) {
            addLog(`Notifications: ${result[PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS]}`);
          }
        }
      } catch (err) {
        addLog(`Error checking permissions: ${err instanceof Error ? err.message : String(err)}`);
      }
    }
  };

  useEffect(() => {
    // Check permissions when component mounts
    checkAndRequestPermissions();

    // Subscribe to step updates
    const stepSubscription = stepCounterEventEmitter.addListener(STEP_UPDATE, (event) => {
      addLog(`Steps updated: ${event.steps}`);
      setStepCount(event.steps);
    });

    // Subscribe to service status updates
    const statusSubscription = stepCounterEventEmitter.addListener(SERVICE_STATUS, (event) => {
      addLog(`Service status: ${event.isRunning ? 'Running' : 'Stopped'}`);
      setServiceRunning(event.isRunning);
    });

    // Cleanup subscriptions when component unmounts
    return () => {
      stepSubscription.remove();
      statusSubscription.remove();
    };
  }, []);

  const toggleService = async () => {
    // Check if all required permissions are granted
    if (!permissions.activityRecognition || !permissions.notification) {
      addLog('Cannot start service: Required permissions not granted');
      return;
    }

    try {
      addLog(`Attempting to ${serviceRunning ? 'stop' : 'start'} service...`);
      if (serviceRunning) {
        await StepCounterService.stopService();
        addLog('Service stopped successfully');
      } else {
        await StepCounterService.startService();
        addLog('Service started successfully');
      }
    } catch (error) {
      addLog(`Error: ${error instanceof Error ? error.message : String(error)}`);
    }
  };

  return (
    <View style={styles.container}>
      {/* Main Controls */}
      <View style={styles.controls}>
        <Text style={styles.title}>Step Counter Demo</Text>
        
        {/* Permission Status */}
        <View style={styles.permissionStatus}>
          <Text style={styles.permissionTitle}>Required Permissions:</Text>
          <Text style={[
            styles.permissionText,
            permissions.activityRecognition ? styles.permissionGranted : styles.permissionDenied
          ]}>
            Activity Recognition: {permissions.activityRecognition ? '✓ Granted' : '✗ Not Granted'}
          </Text>
          <Text style={[
            styles.permissionText,
            permissions.notification ? styles.permissionGranted : styles.permissionDenied
          ]}>
            Notifications: {permissions.notification ? '✓ Granted' : '✗ Not Granted'}
          </Text>
        </View>

        <Text style={styles.stepCount}>{stepCount} steps</Text>
        <Button
          title={serviceRunning ? 'Stop Service' : 'Start Service'}
          onPress={toggleService}
          disabled={!permissions.activityRecognition || !permissions.notification}
        />
        <Text style={styles.status}>
          Service Status: {serviceRunning ? 'Running' : 'Stopped'}
        </Text>
      </View>

      {/* Debug Logs */}
      <View style={styles.logContainer}>
        <Text style={styles.logTitle}>Debug Logs:</Text>
        <ScrollView style={styles.logScroll}>
          {logs.map((log, index) => (
            <Text key={index} style={styles.logText}>{log}</Text>
          ))}
        </ScrollView>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#fff',
  },
  controls: {
    alignItems: 'center',
    marginBottom: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  permissionStatus: {
    width: '100%',
    padding: 10,
    backgroundColor: '#f5f5f5',
    borderRadius: 5,
    marginBottom: 20,
  },
  permissionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 5,
  },
  permissionText: {
    fontSize: 14,
    marginBottom: 3,
  },
  permissionGranted: {
    color: '#4CAF50',
  },
  permissionDenied: {
    color: '#F44336',
  },
  stepCount: {
    fontSize: 48,
    marginBottom: 30,
  },
  status: {
    marginTop: 20,
    fontSize: 16,
    color: '#666',
  },
  logContainer: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 5,
    padding: 10,
  },
  logTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  logScroll: {
    flex: 1,
  },
  logText: {
    fontSize: 12,
    marginBottom: 5,
    color: '#333',
  },
});
