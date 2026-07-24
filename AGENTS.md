# AGENTS.md

## Build & Test Commands
- **Assemble Debug APK**: `./gradlew assembleDebug`
- **Run unit tests** (JVM only): `./gradlew test`
- **Run Android instrumentation tests**: `./gradlew connectedAndroidTest`
- **Run lint only**: `./gradlew lint`
- **Run a single test class**: `./gradlew test --tests "*LoanCompareActivity*"`
- **Clean rebuild** (after dependency changes): `./gradlew clean assembleDebug`

> Use `./gradlew` from repo root. No system-wide Gradle required.

---

## Project Overview
- **Language**: Java (Jetpack Compose for a few screens: SavingsPassport, StudentSaving, StudentEvent, budget planner)
- **Architecture**: Single-module Android app, no multi-module setup
- **Package**: `com.example.smartfinancialmanagement`
- **Min SDK**: 24, **Target SDK**: 36, **Compile SDK**: 36
- **Java version**: 11
- **Entry point**: `MainActivity` → `LoginFormActivity` → role-based dashboards
- **ProGuard/R8**: Enabled for release (`isMinifyEnabled = true`)

---

## Dependencies
- Declared in `gradle/libs.versions.toml` (reference via `libs.<alias>`)
- Some declared directly in `app/build.gradle.kts`: Firestore, iTextPDF, MPAndroidChart, JavaMail, Facebook SDK
- Key services: Firebase Auth, Realtime Database, Firestore, FCM
- Charts: MPAndroidChart
- PDF: iTextPDF
- Background: WorkManager

---

## UI Design System (Dark Purple Theme)

All XML layouts use a **dark purple glassmorphic** design. Use `@color/` references, **never inline hex**.

### Color Tokens (`colors.xml`)
- **Backgrounds**: `@color/surface_primary` (#3A2CC5), `@color/dash_bg_mid` (#2D1EB0), `@color/dash_bg_light` (#4A3AF5)
- **Cards**: `@color/surface_card` (#4A3AF5), `@color/surface_card_border` (#5B4BF7)
- **Text**: `@color/text_primary` (white), `@color/text_secondary` (#B0A8FF), `@color/text_muted` (#8B7FFF)
- **Accent**: `@color/hero_accent` (#7C6FE0)
- **Buttons**: `@color/btn_primary` (#7C6FE0), `@color/btn_primary_text` (white)
- **Status**: `@color/badge_success_text` (#7CFFAA), `@color/badge_warning_text` (#FFD866)

### Card Pattern
- `MaterialCardView` with `app:cardBackgroundColor="@color/glass_card_bg"`, `app:strokeColor="@color/glass_card_border"` (1dp), `app:cardCornerRadius="16dp"`, `app:cardElevation="0dp"`

### Key Drawables
- `bg_dashboard.xml` — Purple gradient background (used by 49 layouts)
- `bg_glass_card.xml` — Card background (uses `@color/surface_card` + border)
- `bg_input_dark.xml` — Dark input field background

### Animations (`res/anim/`, 13 files)
- `fade_in.xml`, `slide_up.xml`, `slide_up_from_bottom.xml`, `scale_fade_in.xml`, `expand_width.xml`, `pulse_repeat.xml`
- `slide_in_right.xml`, `slide_out_left.xml`, `slide_in_left.xml`, `slide_out_right.xml`
- `card_enter.xml`, `flip_in.xml`, `flip_out.xml`

### Activity Transitions
Dashboard activities use `overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)` after `startActivity()`. Cards animate in with staggered fade-in + slide-up via `animateCards()` method.

---

## Firebase Firestore Structure
Root: **`users`** (doc ID = Auth UID)
- Fields: `monthlySavingAmount`, `currentSavings`, `fcmToken`, `role`, `status`
- Sub-collections: `worker_profile`, `student_profile`, `business_profile`, `multi_profile`, `accounts`, `loans`, `utilities`, `subscriptions`, `tasks`, `expense_claims`, `payslips`

> Field names must match exactly. Typos cause missing UI values.

---

## Dashboard Routing
`LoginFormActivity` → routes by `role` field:
- "Student" → `StudentDashboardActivity`
- "Company worker" → `WorkerDashboardActivity`
- "Business owner" → `BusinessDashboardActivity`
- "Multiple account holder" → `MultiAccountDashboardActivity`
- Hybrid → `StudentWorkerHybridDashboardActivity`

---

## Notification System (3-Layer)
1. **FCM Push**: `SmartFinanceMessagingService` (token saved on login)
2. **AlarmManager**: `SubscriptionNotificationScheduler` (3 alarms per subscription)
3. **WorkManager**: `SubscriptionWorker` (daily fallback)
- Permissions: `POST_NOTIFICATIONS` (Android 13+), `SCHEDULE_EXACT_ALARM` (Android 12+), `RECEIVE_BOOT_COMPLETED`

---

## Common Gotchas
- **Hard-coded text in XML**: Never leave hardcoded text in TextViews — always initialize programmatically or leave empty. Values flash before Firestore loads.
- **AndroidManifest**: Every new Activity, Service, and Receiver must be registered.
- **Firestore orderBy**: If any document lacks the `orderBy` field, the query crashes. Use `.get()` without `orderBy` or ensure all docs have it.
- **AlertDialog theme**: Use `new AlertDialog.Builder(this)` (not `R.style.Theme_SmartFinance_Dialog`).
- **Locale formatting**: Currency uses `String.format(Locale.US, "Rs %,.2f", value)`.
- **ProGuard**: Release builds are obfuscated. Test release builds separately.
- **Compose vs XML**: Only a few screens use Compose. Most UI is XML layouts.
- **Version catalog**: Some dependencies are in `libs.versions.toml`, others directly in `build.gradle.kts`.
- **Lint**: `abortOnError = false`. Run locally to catch warnings.
- **Tests**: Only `ExampleUnitTest.java` exists. Unit tests run on JVM, no Android framework access.

---

## Branches
- `main` — production
- `boseth-branch` — active development
- `bhanuka-branch`, `naveen-branch`, `sahanya-branch`, `navanjana-branch` — other contributors
