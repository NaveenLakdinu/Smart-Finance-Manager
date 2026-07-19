# AGENTS.md – Quick Guidance for OpenCode Agents

**Purpose** – Capture repo‑specific quirks that are easy to overlook. Anything not listed here is either obvious from the code/config or not required for typical development tasks.

---

## Build & Test Commands
- **Assemble Debug APK**: `./gradlew assembleDebug`
- **Run unit tests** (JVM only): `./gradlew test`
- **Run Android instrumentation tests**: `./gradlew connectedAndroidTest`
- **Run lint only**: `./gradlew lint`
- **Run a single test class** (e.g. LoanCompareActivity): `./gradlew test --tests "*LoanCompareActivity*"`
- **Clean before rebuilding** (recommended after dependency changes): `./gradlew clean assembleDebug`

> The wrapper script (`gradlew` / `gradlew.bat`) in the repo root must be used – the project does **not** include a system‑wide Gradle.

---

## Project Overview
- **Language**: Java (with Jetpack Compose for a few screens)
- **Architecture**: Single-module Android app, no multi-module setup
- **Package**: `com.example.smartfinancialmanagement`
- **Min SDK**: 24, **Target SDK**: 36, **Compile SDK**: 37
- **Java version**: 11 (source + target compatibility)
- **Entry point**: `MainActivity` (landing/welcome screen) → `LoginFormActivity` → role-based dashboards

---

## Dependencies (via Version Catalog)
- Dependencies are declared in `gradle/libs.versions.toml`. When adding a new library, edit that file and reference it via `libs.<alias>` in `app/build.gradle.kts`.
- Some dependencies (Firestore, iTextPDF, MPAndroidChart, JavaMail) are declared directly in `app/build.gradle.kts` outside the version catalog.
- Key Firebase services: Auth, Realtime Database, Firestore, Cloud Messaging (FCM)
- Background work: WorkManager (`libs.work`) for guaranteed background tasks
- Charts: MPAndroidChart for bar/pie charts in reports
- PDF: iTextPDF for report generation

---

## UI Design System (Dark Glassmorphic Theme)

The app uses a unified **dark navy glassmorphic** design. All XML layouts must follow these conventions:

### Color Tokens (defined in `colors.xml`)
Use `@color/` references, **never inline hex**. Key tokens:
- **Backgrounds**: `@color/surface_primary` (#0A1628), `@color/dash_bg_mid`, `@color/dash_bg_light`
- **Cards**: `@color/surface_card` (glass_card_bg #1A2F50), `@color/surface_card_border`
- **Text**: `@color/text_primary` (near-white), `@color/text_secondary_dark`, `@color/text_muted`
- **Accent**: `@color/hero_accent` (#00D4AA teal) — primary CTA color
- **Semantic buttons**: `@color/btn_primary` (teal), `@color/btn_danger` (red)
- **Status**: `@color/badge_success_text` (green), `@color/badge_warning_text` (amber)

### Card Pattern
All cards use `MaterialCardView` with:
- `app:cardBackgroundColor="@color/glass_card_bg"`
- `app:strokeColor="@color/glass_card_border"` (1dp)
- `app:cardCornerRadius="16dp"` (content) or `"24dp"` (hero)
- `app:cardElevation="0dp"`

### Button Pattern
- Primary CTA: `app:backgroundTint="@color/hero_accent"`, dark text, 12dp corners
- Destructive: red tint or outline with `@color/danger_text`
- All buttons use `android:textAllCaps="false"`

### Spacing System (defined in `dimens.xml`)
4dp grid: 4/8/12/16/20/24/32/48dp. Use `@dimen/spacing_*` references.

### Typography (defined in `themes.xml`)
8 levels: Display (28sp), Headline (24sp), TitleLarge (20sp), Title (16sp), BodyLarge (16sp), Body (14sp), Caption (12sp), Small (11sp). Apply via `android:textAppearance="@style/TextAppearance.App.*"`.

---

## Firebase Firestore Structure
- Root collection: **`users`** (document ID = Firebase Auth UID).
- **Fields on user document**:
  - `monthlySavingAmount` (String → double)
  - `currentSavings` (String → double)
  - `fcmToken` (String — FCM push notification token)
  - `role` (String — determines dashboard routing)
  - `status` (String — "active", "suspended", "deactivated")
- **Sub‑collections** (all under `users/{uid}`):
  - `worker_profile` → field `monthlySalary` (double)
  - `student_profile` → fields `university`, `course`, `studentId`
  - `business_profile` → `businessName`, `regNumber`, `industryType`
  - `multi_profile` → `linkedAccountsCount`, `primaryWorkspace`
  - `accounts` → each doc: `name` (String), `balance` (double), `accountNumber` (String)
  - `loans` → each doc: `monthlyEmi`, `principalAmount`, etc.
  - `utilities` → each doc: `amount` (double)
  - `subscriptions` → each doc: `name`, `amount` (double), `paymentDay` (int 1-31), `billingCycle` ("Monthly"/"Yearly"), `renewDate` (String "dd/MM/yyyy"), `status`, `logoType`, `createdAt` (long)

> When adding new data to Firestore, follow the exact field names above; any typo will result in missing UI values.

---

## Key Activity Entry Points & Data Loading
- **Login flow** → `LoginFormActivity` → on successful sign‑in routes to:
  - `StudentDashboardActivity` (role "Student")
  - `WorkerDashboardActivity` (role "Company worker")
  - `StudentWorkerHybridDashboardActivity` (hybrid view)
  - `MultiAccountDashboardActivity` (role "Multiple account holder")
  - `BusinessDashboardActivity` (role "Business owner")
- **Data fetching pattern** (used everywhere):
  ```java
  FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
  if (user != null) {
      FirebaseFirestore.getInstance()
          .collection("users").document(user.getUid())
          .<subcollection-or-document>.get() ...
  }
  ```
- **LoginFormActivity** saves FCM token to Firestore on login and requests `POST_NOTIFICATIONS` permission on Android 13+.

---

## Notification System (3-Layer Reliability)

### FCM Push Notifications (server → device)
- `SmartFinanceMessagingService` handles token refresh and incoming messages
- Token saved to `users/{uid}/fcmToken` on every login
- Works even when app is killed (Google's servers maintain connection)

### AlarmManager (app → device, exact timing)
- `SubscriptionNotificationScheduler` schedules 3 alarms per subscription: day -2, day -1, day 0 at 9 AM
- `SubscriptionAlarmReceiver` shows notifications and auto-advances payment date on day 0
- Uses `setExactAndAllowWhileIdle` for Doze mode compatibility

### WorkManager Fallback (background guarantee)
- `SubscriptionWorker` runs daily to catch missed alarms
- `SubscriptionBootReceiver` reschedules all alarms after device reboot
- Registered in manifest with `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED` intent filters

### Permissions Required
- `POST_NOTIFICATIONS` (Android 13+)
- `SCHEDULE_EXACT_ALARM` (Android 12+)
- `RECEIVE_BOOT_COMPLETED` (reschedule after reboot)

---

## Subscription Management System

### Activities
- `SubscriptionManagerActivity` — Hub: shows total monthly cost, lists active subscriptions
- `AddSubscriptionActivity` — Form: plan name, cost, payment day (1-31), cycle, service type
- `SubscriptionListActivity` — RecyclerView of all subscriptions, tap for detail, long-press to delete
- `SubscriptionDetailActivity` — View/edit/delete, "Mark as Paid" advances to next cycle
- `SubscriptionReportActivity` — Total cost + MPAndroidChart bar chart

### Subscription Model (`Subscription.java`)
Fields: `documentId`, `name`, `amount`, `paymentDay`, `billingCycle`, `renewDate`, `status`, `logoType`, `createdAt`
Helper: `toMap()` for Firestore writes

### Notification Schedule Example
Subscription with `paymentDay = 25`, today is June 23:
- June 23: "Netflix Premium payment of LKR 1500 is due in 2 days"
- June 24: "Netflix Premium payment of LKR 1500 is due tomorrow"
- June 25: "Netflix Premium payment of LKR 1500 is due today!" → auto-advance to July 25

---

## Common Gotchas
- **Hard‑coded defaults**: If a user's profile lacks a field (e.g., `monthlySalary`), the UI shows `LKR 0.00`.
- **Locale formatting**: All currency strings use `String.format(Locale.US, "LKR %.2f", value)`. Non-numeric strings cause `NumberFormatException`.
- **Firebase initialization**: Relies on `google-services.json` (already present). Do not modify the package name.
- **Compose screens**: Only a few screens use Jetpack Compose (`SavingsPassportActivity`, `StudentSavingActivity`, `StudentEventActivity`, budget planner). Most UI is XML layouts.
- **No ProGuard/R8**: Release builds have `isMinifyEnabled = false`.
- **Layout XML hardcoded text**: Never leave hardcoded text in layout XML TextViews — always initialize programmatically or leave empty. Hardcoded values flash before Firestore data loads.
- **AndroidManifest registration**: Every new Activity, Service, and Receiver must be registered in `AndroidManifest.xml`.
- **Gradle sync**: After adding new dependencies to `libs.versions.toml`, Android Studio may show "Cannot resolve symbol" errors — run `./gradlew clean assembleDebug` and sync Gradle.

---

## Testing Tips
- Unit tests run on the JVM; they cannot access Android framework classes. Use mocking (e.g., Mockito) for `FirebaseAuth` / `FirebaseFirestore` if needed.
- Instrumented UI tests require an emulator or device. Use `./gradlew connectedAndroidTest` after launching an emulator (`adb devices` to confirm).
- Lint is non‑blocking (`abortOnError = false`), but run it locally to catch style warnings.
- Only one unit test file exists (`ExampleUnitTest.java`). Tests are minimal.

---

## Where to Find More Info
- **Database schema** – `DATABASE.md` (full field list).
- **Gradle build options** – `settings.gradle.kts`, `gradle/libs.versions.toml`.
- **Layout files** – `app/src/main/res/layout/` (90+ XML layouts).
- **Color tokens** – `app/src/main/res/values/colors.xml` (150+ color definitions).
