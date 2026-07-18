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

## Firebase Firestore Structure (high‑level)
- Root collection: **`users`** (document ID = Firebase Auth UID).
- Important fields on the user document (see `DATABASE.md`):
  - `monthlySavingAmount` (String → double)
  - `monthlySalary` (double, stored inside the **worker_profile** sub‑collection)
  - `currentSavings` (String → double)
- **Sub‑collections** (all under `users/{uid}`):
  - `worker_profile` → field `monthlySalary` (double)
  - `student_profile` → fields `university`, `course`, `studentId`
  - `business_profile` → `businessName`, `regNumber`, `industryType`
  - `multi_profile` → `linkedAccountsCount`, `primaryWorkspace`
  - `loans` → each loan document has `monthlyEmi`, `principalAmount`, etc.
  - `utilities` → each document has `amount` (double)
  - `subscriptions` → each document has `monthlyCost` (double) – **our code now sums this field** instead of a hard‑coded 1500.
  - **`accounts`** (used by `MultiAccountDashboardActivity`) → each document must contain:
    - `name` (String)
    - `balance` (double)
    - `accountNumber` (String)

> When adding new data to Firestore, follow the exact field names above; any typo will result in missing UI values.

---

## Key Activity Entry Points & Data Loading
- **Login flow** → `LoginFormActivity` → on successful sign‑in routes to:
  - `StudentDashboardActivity` (role "Student")
  - `WorkerDashboardActivity` (role "Company worker")
  - `StudentWorkerHybridDashboardActivity` (hybrid view, also uses worker profile)
  - `MultiAccountDashboardActivity` (role "Multiple account holder")
  - `BusinessDashboardActivity` (role "Business owner")
- **Data fetching patterns** (all use the same pattern):
  ```java
  FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
  if (user != null) {
      FirebaseFirestore.getInstance()
          .collection("users").document(user.getUid())
          .<subcollection-or-document>.get() ...
  }
  ```
- **WorkerDashboardActivity** now calls `loadSalaryFromFirestore(uid)` to populate `txtEarnings`.
- **StudentWorkerHybridDashboardActivity** falls back to `LKR 0.00` if no salary is present.
- **InvoiceHubActivity** now calculates `totalOutstandingAmount` by summing `amount` from the `utilities` sub‑collection (via `loadOutstandingAmountFromFirestore`).
- **MultiAccountDashboardActivity** loads the accounts list from `users/{uid}/accounts` using the new `loadAccountsFromFirestore()` helper.

---

## Gradle Version Catalog
- Dependencies are declared in `gradle/libs.versions.toml`. When adding a new library, edit that file and reference it via `libs.<alias>` in `app/build.gradle.kts`.
- Some dependencies (Firestore, iTextPDF, MPAndroidChart, JavaMail) are declared directly in `app/build.gradle.kts` outside the version catalog.

---

## Common Gotchas
- **Hard‑coded defaults** were removed. If a user's profile lacks a field (e.g., `monthlySalary`), the UI now shows `LKR 0.00` instead of a placeholder.
- **Subscription aggregation** now expects a field `monthlyCost` per subscription document. Existing mock data with a fixed 1500 value will need to be migrated.
- **Accounts list**: the old static string arrays were replaced. Ensure that an `accounts` sub‑collection exists for each user; otherwise the dashboard shows "No Account".
- **Locale formatting**: all currency strings are built with `String.format(Locale.US, "LKR %.2f", value)`. Supplying non‑numeric strings will cause a `NumberFormatException`.
- **Firebase initialization**: the project relies on `google-services.json` (already present). Do not modify the package name; it's defined as `com.example.smartfinancialmanagement` in the `android {}` block.
- **Compose screens**: Only a few screens use Jetpack Compose (`SavingsPassportActivity`, `StudentSavingActivity`, `StudentEventActivity`, budget planner). Most UI is XML layouts.
- **No ProGuard/R8**: Release builds have `isMinifyEnabled = false`.

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
- **Layout files** – `app/src/main/res/layout/` (85+ XML layouts).
- **Color tokens** – `app/src/main/res/values/colors.xml` (150+ color definitions).

---

*This file is intentionally concise: it only contains details that an OpenCode agent would otherwise miss (commands, Firestore field names, design system conventions, and migration points).*
