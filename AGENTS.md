# AGENTS.md – Quick Guidance for OpenCode Agents

**Purpose** – Capture repo‑specific quirks that are easy to overlook.  Anything not listed here is either obvious from the code/config or not required for typical development tasks.

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
- Dependencies are declared in `gradle/libs.versions.toml`.  When adding a new library, edit that file and reference it via `libs.<alias>` in `app/build.gradle.kts`.

---

## Common Gotchas
- **Hard‑coded defaults** were removed. If a user’s profile lacks a field (e.g., `monthlySalary`), the UI now shows `LKR 0.00` instead of a placeholder.
- **Subscription aggregation** now expects a field `monthlyCost` per subscription document. Existing mock data with a fixed 1500 value will need to be migrated.
- **Accounts list**: the old static string arrays were replaced. Ensure that an `accounts` sub‑collection exists for each user; otherwise the dashboard shows “No Account”.
- **Locale formatting**: all currency strings are built with `String.format(Locale.US, "LKR %.2f", value)`.  Supplying non‑numeric strings will cause a `NumberFormatException`.
- **Firebase initialization**: the project relies on `google-services.json` (already present). Do not modify the package name; it’s defined as `com.example.smartfinancialmanagement` in the `android {}` block.

---

## Testing Tips
- Unit tests run on the JVM; they cannot access Android framework classes. Use mocking (e.g., Mockito) for `FirebaseAuth` / `FirebaseFirestore` if needed.
- Instrumented UI tests require an emulator or device. Use `./gradlew connectedAndroidTest` after launching an emulator (`adb devices` to confirm).
- Lint is non‑blocking (`abortOnError = false`), but run it locally to catch style warnings.

---

## Where to Find More Info
- **Database schema** – `DATABASE.md` (full field list).
- **Gradle build options** – `settings.gradle.kts`, `gradle/libs.versions.toml`.
- **Project‑specific notes** – `.idea/*.xml` (Android Studio settings) if you need UI layout IDs.

---

*This file is intentionally concise: it only contains details that an OpenCode agent would otherwise miss (commands, Firestore field names, data‑loading expectations, and migration points).*
