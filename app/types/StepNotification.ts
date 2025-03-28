import { NativeEventEmitter, NativeModules } from 'react-native';

const { StepNotificationModule } = NativeModules;

interface StepNotificationInterface {
  showNotification(stepCount: number): Promise<string>;
  hideNotification(): Promise<string>;
}

export const StepNotification: StepNotificationInterface = {
  showNotification: (stepCount: number) => {
    return StepNotificationModule.showNotification(stepCount);
  },
  hideNotification: () => {
    return StepNotificationModule.hideNotification();
  }
}; 