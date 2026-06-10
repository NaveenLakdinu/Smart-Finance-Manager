# Smart Finance Manager (FinGuard)

## Project Overview
Smart Finance Manager is a comprehensive personal financial management ecosystem designed to help users track income, manage subscriptions, monitor loans, and utility bills. It features automated reminders and detailed financial reporting.

The project consists of two primary components:
1.  **Android Mobile App:** A Java-based Android application (this directory) for end-users to manage their personal finances.
2.  **Admin Web Portal:** A React-based web dashboard (referenced in `PROJECT_SUMMARY.md`) for administrators to monitor user activity and system metrics.

### Core Technologies
- **Mobile:** Java, Android SDK, Gradle
- **Web Portal:** React 19, Vite 8, Material UI
- **Backend:** Firebase (Authentication, Firestore, Cloud Functions, Hosting)
- **Security:** AES-256-GCM field-level encryption for sensitive data (shared between Mobile and Web)

## Building and Running

### Android Mobile App
The mobile app uses the Gradle build system.

- **Build Project:** `./gradlew build`
- **Run Unit Tests:** `./gradlew test`
- **Install on Device:** `./gradlew installDebug`
- **Clean Project:** `./gradlew clean`

### Admin Web Portal
(Note: Source files for the web portal may reside in a separate directory or submodule as per `PROJECT_SUMMARY.md`)

- **Installation:** `npm install`
- **Development:** `npm run dev`
- **Production Build:** `npm run build`
- **Deployment:** `npx firebase-tools deploy`

## Development Conventions

### Coding Style
- **Android (Java):** Follow standard Android Java coding conventions. Use PascalCase for classes, camelCase for methods and variables.
- **Layouts:** Use XML-based layouts in `app/src/main/res/layout`. Follow Material Design guidelines.
- **Firebase:** Use the modular Firebase SDK where applicable. Ensure all Firestore writes are protected by server-side rules.

### Data Security
- Sensitive fields (salary, loan balances, transaction amounts) must be encrypted using AES-256-GCM before storage in Firestore.
- The encryption key is managed via environment variables (`VITE_ENCRYPTION_KEY`).

### Error Handling
- Use user-friendly error messages, especially for authentication and database failures.
- Implement UI-level protections (e.g., disabling buttons during async operations) to prevent duplicate data submission.

## Directory Structure
- `app/`: Main Android application module.
  - `src/main/java/`: Java source files.
  - `src/main/res/`: Android resources (layouts, drawables, strings).
  - `google-services.json`: Firebase configuration file.
- `PROJECT_SUMMARY.md`: Detailed documentation for the Admin Web Portal.
- `build.gradle.kts`: Root-level build configuration.
- `settings.gradle.kts`: Project settings and module inclusion.
