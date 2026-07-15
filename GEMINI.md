# Smart-Finance-Manager

## Project Overview
Smart-Finance-Manager is a comprehensive personal finance management Android application. It provides users with tools to track income, manage subscriptions, monitor loans, and utility bills. The app features automated reminders and financial reporting to help users maintain a healthy financial state.

### Main Technologies
- **Platform:** Android
- **Language:** Java
- **Build System:** Gradle (Kotlin DSL)
- **Backend Services:** Firebase (Authentication, Realtime Database, Firestore)
- **UI Components:** Material Design 3, CardView, ConstraintLayout

### Architecture
The project follows a standard Android Activity-based architecture.
- **Activities:** Handle UI logic and user interaction (e.g., `DashboardActivity`, `LoginFormActivity`, `RegisterActivity`, `MultiAccountActivity`).
- **Layouts:** Defined in XML within `app/src/main/res/layout`.
- **Resources:** Drawables, colors, strings, and themes are managed in `app/src/main/res/values`.

---

## Building and Running

### Prerequisites
- Android Studio Jellyfish or newer.
- JDK 11 or newer.
- Firebase `google-services.json` file in the `app/` directory (already present).

### Key Commands
- **Build the project:**
  ```bash
  ./gradlew assembleDebug
  ```
- **Run Unit Tests:**
  ```bash
  ./gradlew test
  ```
- **Run Instrumented Tests:**
  ```bash
  ./gradlew connectedAndroidTest
  ```
- **Install on Device:**
  ```bash
  ./gradlew installDebug
  ```
- **Clean the project:**
  ```bash
  ./gradlew clean
  ```

---

## Development Conventions

### Coding Style
- **Language:** Java is the primary language for source code.
- **Naming:** Follow standard Java and Android naming conventions (e.g., `activity_dashboard.xml` for layouts, `DashboardActivity.java` for classes).
- **UI/UX:** 
    - Maintain a consistent look using the "Smart Financial Management" theme.
    - Use rounded `CardView` components with elevation (typically 8dp to 14dp).
    - Use emoji-based icons within circular or rounded backgrounds for a modern feel.
    - Primary colors: Dark Blue (`#071A33`), Brand Green (`#4CAF50`), and White (`#FFFFFF`).

### Testing Practices
- Unit tests should be placed in `app/src/test/java`.
- Instrumented tests (UI tests) should be placed in `app/src/androidTest/java`.

### Contribution Guidelines
- Before adding new features, ensure they align with the visual style established in existing activities like `DashboardActivity`.
- Register all new activities in `app/src/main/AndroidManifest.xml`.
- Use the `libs.versions.toml` file for dependency management.

---

## Roadmap & Improvement Suggestions

### 1. Architectural Improvements
- **Implement MVVM Pattern:** Transition from Activity-based logic to Model-View-ViewModel to separate business logic from UI components.
- **BaseActivity/BaseFragment:** Create base classes to handle shared logic (e.g., Logout, Profile Setup) to reduce code duplication.
- **Dependency Injection:** Integrate **Hilt** to manage Firebase and system service instances efficiently.

### 2. UI & UX Enhancements
- **Single Activity Architecture:** Use a single `MainActivity` with **Fragments** and the **Navigation Component** for smoother tab transitions and state preservation.
- **Loading States:** Implement **Shimmer effects** or custom ProgressBars for Firestore data fetching operations.
- **Empty States:** Add illustrative empty state views for lists (Invoices, Subscriptions) when no data is available.

### 3. Feature Roadmap
- **Role-Based Repositories:** Abstract profile-specific logic into dedicated Repository classes.
- **Data Export:** Add functionality to export reports (Analytics, Loans) to **PDF or CSV** formats.
- **Push Notifications:** Integrate **Firebase Cloud Messaging (FCM)** for automated payment and renewal reminders.

### 4. Security & Performance
- **Localization:** Move hardcoded strings to `strings.xml` to support future internationalization.
- **Firestore Security:** Audit and tighten Firestore Rules to ensure strict `uid`-based access control.
- **Offline Persistence:** Explicitly enable Firestore offline data persistence for a seamless offline experience.
- **Code Optimization:** Configure R8/Proguard rules to optimize the release build and protect against reverse engineering.
