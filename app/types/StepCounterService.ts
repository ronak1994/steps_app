import { NativeEventEmitter, NativeModules } from 'react-native';

const { StepCounterService } = NativeModules;

if (!StepCounterService) {
  throw new Error('StepCounterService native module is not available. Did you forget to run pod install?');
}

const eventEmitter = new NativeEventEmitter(StepCounterService);

export interface StepUpdateEvent {
  steps: number;
}

export interface ServiceStatusEvent {
  isRunning: boolean;
}

export interface ErrorEvent {
  message: string;
}

export interface StepCounterServiceInterface {
  startService(): Promise<void>;
  stopService(): Promise<void>;
  addStepUpdateListener(callback: (event: StepUpdateEvent) => void): () => void;
  addServiceStatusListener(callback: (event: ServiceStatusEvent) => void): () => void;
  addErrorListener(callback: (event: ErrorEvent) => void): () => void;
}

const StepCounterServiceAPI: StepCounterServiceInterface = {
  startService: async () => {
    try {
      await StepCounterService.startService();
    } catch (error) {
      console.error('Failed to start service:', error);
      throw error;
    }
  },

  stopService: async () => {
    try {
      await StepCounterService.stopService();
    } catch (error) {
      console.error('Failed to stop service:', error);
      throw error;
    }
  },

  addStepUpdateListener: (callback) => {
    const subscription = eventEmitter.addListener('stepUpdate', callback);
    return () => subscription.remove();
  },

  addServiceStatusListener: (callback) => {
    const subscription = eventEmitter.addListener('serviceStatus', callback);
    return () => subscription.remove();
  },

  addErrorListener: (callback) => {
    const subscription = eventEmitter.addListener('error', callback);
    return () => subscription.remove();
  },
};

export default StepCounterServiceAPI; 