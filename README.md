# Retasк

A minimal, no-nonsense task manager for Android.

[![CI Pipeline](https://github.com/abhisheksharm-3/retask/actions/workflows/ci.yaml/badge.svg)](https://github.com/abhisheksharm-3/retask/actions/workflows/ci.yaml)

<p align="center">
  <img src="images/logo.png" alt="Retasк logo" width="200" />
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

### Architecture

Retasк follows the recommended modern Android architecture using the Room persistence library with the MVVM pattern. This architecture separates concerns and creates a maintainable, testable codebase.

#### Architecture Components

The application follows a layered architecture with the following components:

1. **Entity (Task)**
   - Defines the structure of database tables
   - Represents the fundamental data model of the app
   - Annotated with `@Entity` to specify database table information
   - Example: The `Task` class defining properties like `id`, `title`, `dueDate`, and `colorHex`

2. **Data Access Object (TaskDao)**
   - Provides methods to access and manipulate data in the database
   - Contains SQL queries annotated with Room annotations (`@Query`, `@Insert`, etc.)
   - Defines a clean API for database interactions
   - Returns reactive data streams using Kotlin Flow for real-time UI updates

3. **Database (TaskDatabase)**
   - Serves as the main access point to the SQLite database
   - Configured using the `@Database` annotation
   - Implemented as a singleton using a companion object for efficiency
   - Provides access to DAO instances
   - Handles database creation and version management

4. **Repository (TaskRepository)**
   - Acts as a mediator between data sources and the rest of the app
   - Provides a clean API that abstracts the underlying implementation details
   - Serves as a single source of truth for application data
   - Handles business logic for task creation, updates, and deletion
   - Example: `createTask()` method that generates UUIDs and calculates due dates

5. **ViewModel**
   - Prepares and manages data for the UI
   - Survives configuration changes (like screen rotations)
   - Uses the repository to access data
   - Provides UI-ready data streams to the Composables
   - Handles UI events and delegates to the repository for data operations

6. **UI Layer (Compose)**
   - Jetpack Compose UI elements that display data to users
   - Observes data from ViewModels using `collectAsState()`
   - Handles user interactions and passes events to ViewModels
   - Remains focused on display logic rather than business logic

#### Data Flow

1. **Read Operation Flow:**
```
UI <- ViewModel <- Repository <- DAO <- Database
```
- UI observes data from ViewModel
- ViewModel exposes data from Repository (via Flow)
- Repository gets data from DAO
- DAO queries the database and returns results as a Flow
- Any database changes automatically propagate through the Flow to update the UI

2. **Write Operation Flow:**
```
UI -> ViewModel -> Repository -> DAO -> Database
```
- User initiates an action (e.g., creating a new task)
- UI passes the event to ViewModel
- ViewModel calls appropriate Repository method
- Repository performs business logic and calls DAO methods
- DAO executes SQL operations on the database
- Database changes trigger Flow updates back to the UI

#### Thread Management

- Database operations run on background threads using Kotlin Coroutines
- `suspend` functions in the DAO, Repository, and ViewModel handle asynchronous operations
- UI remains responsive while database operations are in progress
- Flow collects data on the main thread for safe UI updates

#### Key Architectural Benefits

- **Separation of Concerns**: Each component has a specific responsibility
- **Testability**: Components can be tested in isolation
- **Maintainability**: Changes to one layer don't require changes to others
- **Scalability**: New features can be added without modifying existing code
- **Reactivity**: UI automatically updates when underlying data changes

### Building the Project

1. Clone the repository
   ```bash
   git clone https://github.com/abhisheksharm-3/retask.git
   ```

2. Open in Android Studio

3. Build using Gradle
   ```bash
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

The pipeline configuration can be found in `.github/workflows/ci.yaml`.

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please ensure your code follows the project's coding standards and includes appropriate tests.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Privacy Policy

Retasк doesn't collect any user data. All your tasks stay on your device.

---

<p align="center">
  Made with ❤️ for simplicity
</p>