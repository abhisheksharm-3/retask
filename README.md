# Retasк

A minimal, no-nonsense task manager for Android.

[![CI Pipeline](https://github.com/abhisheksharm-3/retask/actions/workflows/ci.yml/badge.svg)](https://github.com/abhisheksharm-3/retask/actions/workflows/ci.yml)

<p align="center">
  <img src="/images/logo.png" alt="Retasк logo" />
</p>

## About

Retasк is a clean, distraction-free task manager that focuses on what matters: helping you get things done. No complicated features, no cluttered UI—just a straightforward way to manage your tasks.

### Features

- **Minimal Design**: Clean interface without unnecessary distractions
- **Quick Task Entry**: Add tasks in seconds
- **Efficient Task Management**: Complete, delete, and edit with ease
- **No Account Required**: Works completely offline, respecting your privacy
- **Lightweight**: Small app size with minimal resource usage

## Installation

### Download

- **Latest Release**: Download the latest APK from the [Releases](https://github.com/abhisheksharm-3/retask/releases) page

### Requirements

- Android 10.0 (Oreo) or higher

## Development

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture

### Building the Project

1. Clone the repository
   ```
   git clone https://github.com/abhisheksharm-3/retask.git
   ```

2. Open in Android Studio

3. Build using Gradle
   ```
   ./gradlew build
   ```

### CI/CD Pipeline

This project uses GitHub Actions for continuous integration and delivery:

- **Automated Testing**: All code changes are automatically tested
- **Build Verification**: Ensures the app builds correctly on every push
- **Release Automation**: Creates GitHub releases with built APKs

#### Release Process

Releases are triggered via special commit messages in the format:

```
[release vX.Y.Z]
```

For example: `"Add dark mode [release v1.2.0]"`

This will automatically:
1. Run all tests and build checks
2. Generate a debug APK
3. Create a GitHub release tagged as v1.2.0
4. Attach the APK to the release

The pipeline configuration can be found in `.github/workflows/android-ci.yml`.

## Contributing

Contributions are welcome! Feel free to:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please make sure your code follows the project's coding standards and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Privacy Policy

Retasк doesn't collect any user data. All your tasks stay on your device.

---

<p align="center">
  Made with ❤️ for simplicity
</p>