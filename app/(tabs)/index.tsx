import { View, Text, Button, ScrollView, PermissionsAndroid, Platform, Animated } from "react-native";
import { useEffect, useState, useRef } from "react";
import { NativeModules, NativeEventEmitter } from "react-native";
import { StepNotification } from "../types/StepNotification";

const { PedometerModule } = NativeModules;

export default function IndexScreen() {
  const [steps, setSteps] = useState<number>(0);
  const [logMessages, setLogMessages] = useState<string[]>([]);
  const [hasPermission, setHasPermission] = useState<boolean>(false);
  const [notificationEnabled, setNotificationEnabled] = useState<boolean>(false);

  // ✅ Animated value for smooth step updates
  const animatedSteps = useRef(new Animated.Value(0)).current;

  const addLog = (message: string) => {
    setLogMessages((prevLogs) => [...prevLogs, message]);
  };

  const requestPermission = async () => {
    if (Platform.OS === "android") {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACTIVITY_RECOGNITION,
          {
            title: "Activity Recognition Permission",
            message: "This app needs access to your physical activity to track steps.",
            buttonPositive: "OK",
          }
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          addLog("✅ Permission granted!");
          setHasPermission(true);
        } else {
          addLog("❌ Permission denied!");
          setHasPermission(false);
        }
      } catch (err) {
        addLog(`❌ Permission request error: ${err}`);
      }
    }
  };

  const toggleNotification = async () => {
    try {
      if (notificationEnabled) {
        await StepNotification.hideNotification();
        addLog("✅ Notification hidden");
      } else {
        await StepNotification.showNotification(steps);
        addLog("✅ Notification shown");
      }
      setNotificationEnabled(!notificationEnabled);
    } catch (error: unknown) {
      addLog(`❌ Notification error: ${error instanceof Error ? error.message : String(error)}`);
    }
  };

  useEffect(() => {
    requestPermission();

    if (!PedometerModule) {
      addLog("❌ PedometerModule is NOT available!");
      return;
    }

    addLog("✅ PedometerModule detected! Trying to start...");

    if (hasPermission) {
      PedometerModule.startStepCounting()
        .then((message: string) => addLog(`✅ Success: ${message}`))
        .catch((error: any) => addLog(`❌ Error: ${error.message || JSON.stringify(error)}`));

      const eventEmitter = new NativeEventEmitter(PedometerModule);
      const subscription = eventEmitter.addListener("StepUpdate", (newStepCount) => {
        addLog(`🚶 Steps Updated: ${newStepCount}`);

        // ✅ Animate step count updates
        Animated.timing(animatedSteps, {
          toValue: newStepCount,
          duration: 300, // Adjust duration for smoother effect
          useNativeDriver: false,
        }).start();

        setSteps(newStepCount);

        // Update notification if enabled
        if (notificationEnabled) {
          StepNotification.showNotification(newStepCount)
            .catch((error: unknown) => addLog(`❌ Notification update error: ${error instanceof Error ? error.message : String(error)}`));
        }
      });

      return () => {
        addLog("🔄 Cleaning up listeners...");
        subscription.remove();
        PedometerModule.stopStepCounting()
          .then(() => addLog("✅ Step counter stopped"))
          .catch((error: unknown) => addLog(`❌ Error stopping: ${error instanceof Error ? error.message : String(error)}`));
        if (notificationEnabled) {
          StepNotification.hideNotification()
            .catch((error: unknown) => addLog(`❌ Error hiding notification: ${error instanceof Error ? error.message : String(error)}`));
        }
      };
    } else {
      addLog("⚠️ Waiting for permissions...");
    }
  }, [hasPermission, notificationEnabled]);

  return (
    <View style={{ flex: 1, padding: 20, justifyContent: "center", alignItems: "center", backgroundColor: "#FFFFFF" }}>
      {/* ✅ Animated Text for Smooth Step Updates */}
      <Animated.Text
        style={{
          fontSize: 40,
          fontWeight: "bold",
          color: "#000",
          transform: [
            {
              scale: animatedSteps.interpolate({
                inputRange: [0, steps + 1],
                outputRange: [1, 1.1], // Slight zoom effect on step change
              }),
            },
          ],
        }}
      >
        Step Count: {steps}
      </Animated.Text>

      <Button title="Request Permission" onPress={requestPermission} />
      <Button
        title="Start Step Counting"
        onPress={() => {
          if (hasPermission) {
            PedometerModule.startStepCounting()
              .then(() => addLog("✅ Manually started step counting"))
              .catch((error: unknown) => addLog(`❌ Error starting: ${error instanceof Error ? error.message : String(error)}`));
          } else {
            addLog("⚠️ Permission not granted. Cannot start.");
          }
        }}
      />
      <Button
        title="Stop Step Counting"
        onPress={() => {
          PedometerModule.stopStepCounting()
            .then(() => addLog("✅ Manually stopped step counting"))
            .catch((error: unknown) => addLog(`❌ Error stopping: ${error instanceof Error ? error.message : String(error)}`));
        }}
      />
      <Button
        title={notificationEnabled ? "Hide Notification" : "Show Notification"}
        onPress={toggleNotification}
      />

      <Text style={{ fontSize: 20, marginTop: 20, fontWeight: "bold", color: "#000" }}>Debug Logs:</Text>
      <ScrollView style={{ marginTop: 10, width: "100%", maxHeight: 300, borderWidth: 1, padding: 10, backgroundColor: "#FFFFFF" }}>
        {logMessages.map((log, index) => (
          <Text key={index} style={{ fontSize: 14, marginBottom: 5, color: "#000" }}>{log}</Text>
        ))}
      </ScrollView>
    </View>
  );
}
