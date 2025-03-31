import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import StepCounterService from '../types/StepCounterService';

export default function StepCounter() {
    const [steps, setSteps] = useState(0);
    const [isServiceRunning, setIsServiceRunning] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [isCheckingStatus, setIsCheckingStatus] = useState(true);

    useEffect(() => {
        // Check service status on mount
        checkServiceStatus();

        // Set up event listeners
        const stepUpdateUnsubscribe = StepCounterService.addStepUpdateListener((event) => {
            setSteps(event.steps);
        });

        const serviceStatusUnsubscribe = StepCounterService.addServiceStatusListener((event) => {
            setIsServiceRunning(event.isRunning);
            setIsCheckingStatus(false);
        });

        const errorUnsubscribe = StepCounterService.addErrorListener((event) => {
            setError(event.message);
            setIsCheckingStatus(false);
        });

        return () => {
            stepUpdateUnsubscribe();
            serviceStatusUnsubscribe();
            errorUnsubscribe();
        };
    }, []);

    const checkServiceStatus = async () => {
        try {
            setIsCheckingStatus(true);
            const status = await StepCounterService.checkServiceStatus();
            setIsServiceRunning(status.isRunning);
            setSteps(status.steps);
            setIsCheckingStatus(false);
        } catch (error) {
            console.error('Failed to check service status:', error);
            setError('Failed to check service status');
            setIsCheckingStatus(false);
        }
    };

    const handleStartService = async () => {
        try {
            setError(null);
            await StepCounterService.startService();
        } catch (error) {
            console.error('Failed to start service:', error);
            setError('Failed to start service');
        }
    };

    const handleStopService = async () => {
        try {
            setError(null);
            await StepCounterService.stopService();
        } catch (error) {
            console.error('Failed to stop service:', error);
            setError('Failed to stop service');
        }
    };

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Step Counter</Text>
            <Text style={styles.steps}>{steps} steps</Text>
            
            <TouchableOpacity
                style={[
                    styles.button,
                    isServiceRunning ? styles.stopButton : styles.startButton,
                    isCheckingStatus && styles.checkingButton
                ]}
                onPress={isServiceRunning ? handleStopService : handleStartService}
                disabled={isCheckingStatus}
            >
                <Text style={styles.buttonText}>
                    {isCheckingStatus ? 'Checking Status...' : (isServiceRunning ? 'Stop Service' : 'Start Service')}
                </Text>
            </TouchableOpacity>

            {error && <Text style={styles.error}>{error}</Text>}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 20,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 20,
    },
    steps: {
        fontSize: 48,
        marginBottom: 30,
    },
    button: {
        paddingHorizontal: 20,
        paddingVertical: 10,
        borderRadius: 8,
        minWidth: 150,
        alignItems: 'center',
    },
    startButton: {
        backgroundColor: '#4CAF50',
    },
    stopButton: {
        backgroundColor: '#f44336',
    },
    checkingButton: {
        backgroundColor: '#808080',
    },
    buttonText: {
        color: 'white',
        fontSize: 16,
        fontWeight: 'bold',
    },
    error: {
        color: 'red',
        marginTop: 20,
    },
}); 