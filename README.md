# React Native Step Counter App

A robust step counter application that runs in the background, built with React Native and Kotlin.

## Features

- 🔄 Background step counting
- 💾 Persistent data storage
- 🌐 GMT-based daily reset (00:00 GMT)
- 📱 Real-time step updates
- 🔔 Foreground service with notification
- ⚠️ Comprehensive error handling
- 🔄 Automatic recovery mechanisms

## Prerequisites

- Node.js (v14 or later)
- Expo CLI
- Android Studio
- Kotlin development environment
- Android device or emulator with step counter sensor

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/step-counter-app.git
cd step-counter-app
```

2. Install dependencies:
```bash
npm install
```

3. Create native Android project:
```bash
npx expo prebuild
```

4. Open Android Studio and sync the project:
```bash
cd android
./gradlew clean
```

## Project Structure

```
├── app/                      # React Native (TypeScript) layer
│   ├── (tabs)/
│   │   └── index.tsx        # Main UI
│   └── types/
│       └── StepCounterService.ts  # TypeScript interface
│
└── android/                  # Native Android (Kotlin) layer
    └── app/src/main/java/com/yourpackage/
        ├── StepCounterService.kt         # Background service
        ├── StepCounterServiceModule.kt   # React Native bridge
        ├── StepCounterServicePackage.kt  # Package registration
        ├── StepDataManager.kt           # Data persistence
        └── MainApplication.kt           # App configuration
```

## Documentation

Detailed documentation is available in the following sections:

1. [Architecture Overview](docs/ARCHITECTURE.md)
2. [Component Details](docs/COMPONENTS.md)
3. [Implementation Guide](docs/IMPLEMENTATION.md)
4. [API Reference](docs/API.md)
5. [Troubleshooting](docs/TROUBLESHOOTING.md)

## Quick Start

1. Start the development server:
```bash
npx expo start
```

2. Run on Android:
```bash
npx expo run:android
```

## Configuration

### Required Permissions

Add the following permissions to `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Service Configuration

The step counter service is configured in `StepCounterService.kt`:

```kotlin
private const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "StepCounterChannel"
private const val CHANNEL_NAME = "Step Counter"
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- React Native team
- Expo team
- Android Sensor API
- Contributors and maintainers

## Support

For support, please:
1. Check the [documentation](docs/)
2. Search [existing issues](https://github.com/yourusername/step-counter-app/issues)
3. Create a new issue if needed

## Roadmap

- [ ] Add step goal setting
- [ ] Implement step history
- [ ] Add data export
- [ ] Support for iOS
- [ ] Add unit tests
- [ ] Improve error recovery

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and changes.
