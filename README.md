# Retask: Your Mindful Task Manager

<p align="center">
  <img src="https://raw.githubusercontent.com/abhisheksharm-3/retask/main/images/logo.png" alt="Retask logo" width="200" />
</p>

<p align="center">
  <a href="https://github.com/abhisheksharm-3/retask/actions/workflows/ci.yaml"><img src="https://github.com/abhisheksharm-3/retask/actions/workflows/ci.yaml/badge.svg" alt="CI Pipeline"></a>
  <img src="https://img.shields.io/badge/API-34%2B-blue" alt="API Level">
  <img src="https://img.shields.io/badge/License-MIT-orange" alt="License">
</p>

<p align="center">
  A clean, modern, and distraction-free task manager built with 100% Kotlin and Jetpack Compose, designed to help you focus on what matters.
</p>

---

## ✨ Features

- **Clean & Modern UI**: A beautiful, minimalist interface built with Material 3.
- **Adaptive Layouts**: A polished user experience on phones and tablets, in both portrait and landscape.
- **Dynamic Theming**: Adapts to your system's light/dark mode and supports Material You dynamic colors on Android 12+.
- **Task Reminders**: Never miss a deadline with precise, scheduled notifications.
- **Fully Offline**: All your data is stored securely and privately on your device.
- **100% Kotlin & Compose**: Built with the latest and greatest in Android development.

## 🛠️ Built With

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Asynchronicity**: Kotlin Coroutines & Flow for reactive state management.
- **Database**: [Room](https://developer.android.com/jetpack/androidx/releases/room) for local persistence.
- **Dependency Injection**: Manual (via ViewModel factories).
- **Scheduling**: `AlarmManager` for precise and battery-efficient task reminders.

## 🚀 For Developers

### Project Structure

The project follows a clean, feature-oriented architecture:
-   **`data`**: Contains the Room database (`dao`, `database`), data models (`model`), and the Repository pattern.
-   **`service` & `receiver`**: Handles background work, including scheduling alarms and receiving broadcasts.
-   **`ui`**: Contains all Jetpack Compose UI code, separated into `screens`, `components`, `theme`, and the `ViewModel`.
-   **`utils`**: Utility and helper classes, such as the `NotificationHelper`.

### Development Setup

1.  **Requirements**:
    -   Android Studio Koala (2024.1.1) or newer
    -   Android SDK 34+
    -   JDK 17

2.  **Getting Started**:
    ```bash
    # Clone the repository
    git clone [https://github.com/abhisheksharm-3/retask.git](https://github.com/abhisheksharm-3/retask.git)

    # Build the project in Android Studio or via command line
    ./gradlew build
    ```

### Download & Requirements

-   **Latest Version**: Download the APK from [GitHub Releases](https://github.com/abhisheksharm-3/retask/releases).
-   **Compatibility**: Requires **Android 14 (API 34)** or higher.

---

<p align="center">
  Made with ❤️ for simplicity and modern Android development.
</p>