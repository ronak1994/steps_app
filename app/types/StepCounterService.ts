import { NativeEventEmitter, NativeModules } from 'react-native';

interface StepCounterServiceType {
    startService(): Promise<void>;
    stopService(): Promise<void>;
    addListener(eventType: string, listener: (event: any) => void): void;
    removeListeners(count: number): void;
}

const { StepCounterServiceModule } = NativeModules;
export const StepCounterService: StepCounterServiceType = StepCounterServiceModule;

// Event emitter for step updates and service status
export const stepCounterEventEmitter = new NativeEventEmitter(StepCounterServiceModule);

// Event types
export const STEP_UPDATE = 'onStepUpdate';
export const SERVICE_STATUS = 'onServiceStatus'; 