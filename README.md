# Retasк: Your Minimal Task Manager

<p align="center">
  <img src="images/logo.png" alt="Retasк logo" width="200" />
</p>

<p align="center">
  <a href="https://github.com/abhisheksharm-3/retask/actions/workflows/ci.yaml"><img src="https://github.com/abhisheksharm-3/retask/actions/workflows/ci.yaml/badge.svg" alt="CI Pipeline"></a>
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-purple" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Latest-green" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/License-MIT-orange" alt="License">
</p>

## 🚀 For Users

### What is Retasк?

Retasк is a clean, distraction-free task manager designed to help you get things done quickly and easily. No complicated features, no cluttered interface—just a straightforward way to manage your tasks.

### Key Features

- **Minimal Design**: A clean interface that keeps you focused
- **Quick Task Entry**: Add tasks in seconds
- **Offline & Private**: Works completely offline, no data collection
- **Efficient Management**: Easy task completion, deletion, and editing
- **Lightweight**: Small app size, minimal resource usage

### Download & Requirements

- **Latest Version**: Download from [GitHub Releases](https://github.com/abhisheksharm-3/retask/releases)
- **Compatibility**: Android 10.0 (Oreo) or higher

## 👩‍💻 For Developers

### Technical Overview

Retasк is built using modern Android development practices:

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture with MVVM
- **Database**: Room for local persistence
- **State Management**: Kotlin Coroutines and Flow


### Key Architectural Components

1. **Entity (Task)**
   - Defines database table structure
   - Represents core data model

2. **Data Access Object (DAO)**
   - Provides database interaction methods
   - Uses Kotlin Flow for reactive updates

3. **Repository**
   - Mediates data sources
   - Handles business logic

4. **ViewModel**
   - Manages UI-related data
   - Handles user interactions

### Development Setup

1. **Requirements**:
   - Android Studio Hedgehog (2023.1.1) or newer
   - Android SDK 33+
   - JDK 17

2. **Getting Started**:
   ```bash
   # Clone the repository
   git clone https://github.com/abhisheksharm-3/retask.git

   # Build the project
   ./gradlew build
   ```

### Versioning

We follow [Semantic Versioning](https://semver.org/):

- `MAJOR.MINOR.PATCH`
   - **MAJOR** (X.0.0): Breaking changes
   - **MINOR** (0.X.0): New features
   - **PATCH** (0.0.X): Bug fixes

**Release Tags**:
- `alpha`: Early testing
- `beta`: Feature complete
- `rc`: Release candidate
- Stable: No tag

### Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to branch
5. Open a Pull Request

## 🔒 Privacy Policy

Retasк respects your privacy. No user data is collected—all tasks remain on your device.

## 📄 License

MIT License. See [LICENSE](LICENSE) for details.

---

<p align="center">
  Made with ❤️ for simplicity
</p>

© 2025 [@abhisheksharm-3](https://github.com/abhisheksharm-3)