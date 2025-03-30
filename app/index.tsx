import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Alert } from 'react-native';
import StepCounterService, { StepUpdateEvent, ServiceStatusEvent, ErrorEvent } from './types/StepCounterService';

export default function App() {
  const [isServiceRunning, setIsServiceRunning] = useState(false);
  const [stepCount, setStepCount] = useState(0);
  const [logs, setLogs] = useState<string[]>([]);
  const [hasPermissions, setHasPermissions] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Add event listeners
    const stepUpdateSubscription = StepCounterService.addStepUpdateListener((event: StepUpdateEvent) => {
      setStepCount(event.steps);
      addLog(`Steps updated: ${event.steps}`);
    });

    const serviceStatusSubscription = StepCounterService.addServiceStatusListener((event: ServiceStatusEvent) => {
      setIsServiceRunning(event.isRunning);
      addLog(`Service status: ${event.isRunning ? 'Running' : 'Stopped'}`);
    });

    const errorSubscription = StepCounterService.addErrorListener((event: ErrorEvent) => {
      setError(event.message);
      addLog(`Error: ${event.message}`);
    });

    // Cleanup subscriptions
    return () => {
      stepUpdateSubscription();
      serviceStatusSubscription();
      errorSubscription();
    };
  }, []);

  const addLog = (message: string) => {
    setLogs(prevLogs => [...prevLogs, `${new Date().toLocaleTimeString()}: ${message}`]);
  };

  const handleStartService = async () => {
    try {
      await StepCounterService.startService();
      addLog('Service started');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start service');
      addLog(`Error starting service: ${err instanceof Error ? err.message : 'Unknown error'}`);
    }
  };

  const handleStopService = async () => {
    try {
      await StepCounterService.stopService();
      addLog('Service stopped');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to stop service');
      addLog(`Error stopping service: ${err instanceof Error ? err.message : 'Unknown error'}`);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Step Counter App</Text>
      
      <View style={styles.statusContainer}>
        <Text style={styles.statusText}>
          Service Status: {isServiceRunning ? 'Running' : 'Stopped'}
        </Text>
        <Text style={styles.stepCount}>Steps: {stepCount}</Text>
      </View>

      {error && (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      )}

      <View style={styles.buttonContainer}>
        <TouchableOpacity
          style={[styles.button, isServiceRunning ? styles.stopButton : styles.startButton]}
          onPress={isServiceRunning ? handleStopService : handleStartService}
        >
          <Text style={styles.buttonText}>
            {isServiceRunning ? 'Stop Service' : 'Start Service'}
          </Text>
        </TouchableOpacity>
      </View>

      <ScrollView style={styles.logContainer}>
        {logs.map((log, index) => (
          <Text key={index} style={styles.logText}>{log}</Text>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  statusContainer: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 10,
    marginBottom: 20,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  statusText: {
    fontSize: 16,
    marginBottom: 10,
  },
  stepCount: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  errorContainer: {
    backgroundColor: '#ffebee',
    padding: 10,
    borderRadius: 5,
    marginBottom: 20,
  },
  errorText: {
    color: '#c62828',
    fontSize: 14,
  },
  buttonContainer: {
    marginBottom: 20,
  },
  button: {
    padding: 15,
    borderRadius: 8,
    alignItems: 'center',
  },
  startButton: {
    backgroundColor: '#4caf50',
  },
  stopButton: {
    backgroundColor: '#f44336',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  logContainer: {
    flex: 1,
    backgroundColor: '#fff',
    padding: 10,
    borderRadius: 10,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  logText: {
    fontSize: 12,
    color: '#666',
    marginBottom: 5,
  },
}); 