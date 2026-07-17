# Smart-Finance-Manager

## Project Overview
Smart-Finance-Manager is a comprehensive personal finance management Android application. It provides users with role-based dashboard tools to track income, manage subscriptions, monitor loans, and handle utility bills. The app features real-time calculators, PIN-based app lock, data export capability, and a unified premium dark glassmorphic UI theme tailored to the user's role.

### Main Technologies
- **Platform:** Android
- **Language:** Java (JDK 11+)
- **Build System:** Gradle (Kotlin DSL)
- **Backend Services:** Firebase (Authentication, Cloud Firestore, Realtime Database)
- **UI Components:** Material Design 3, CardView, ConstraintLayout, BottomNavigationView

### Architecture & Routing
The project follows a role-based Activity-based architecture:
*   **Startup & Routing:** [MainActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/MainActivity.java) checks Firebase auth state, then routes via the PIN lock system before landing on the role dashboard.
    *   If logged in + PIN set → [PinLockActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/PinLockActivity.java)
    *   If logged in + no PIN → [PinSetupActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/PinSetupActivity.java)
    *   If not logged in → Welcome screen (Login / Sign Up)
*   **Authentication & Registration:**
    *   [LoginFormActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/LoginFormActivity.java) handles login, error messages, and account suspension/deactivation checks.
    *   [ChooseRoleActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/ChooseRoleActivity.java) captures the registration role.
    *   [RegisterActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/RegisterActivity.java) writes base profiles and role-specific subcollections using Firestore atomic `WriteBatch` writes.
*   **PIN Lock System:**
    *   [PinHelper.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/PinHelper.java) — utility class for hashing, storing, verifying and clearing PINs in `SharedPreferences`.
    *   [PinSetupActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/PinSetupActivity.java) — two-stage PIN creation flow (Enter → Confirm → Save). Includes a Skip option.
    *   [PinLockActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/PinLockActivity.java) — PIN entry screen with shake animation on wrong attempts, 5-attempt lockout, and "Forgot PIN?" fallback to login.
*   **Role Dashboards:**
    *   `Student` → [StudentDashboardActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/StudentDashboardActivity.java)
    *   `Company worker` → [WorkerDashboardActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/WorkerDashboardActivity.java)
    *   `Business owner` → [BusinessDashboardActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/BusinessDashboardActivity.java)
    *   `Multiple account holder` → [MultiAccountDashboardActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/MultiAccountDashboardActivity.java)

---

## Feature Implementation Status

### 1. Loan Management (Fully Implemented)
*   **Active Loans:** [LoanFormActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/LoanFormActivity.java) lists active loans from the `users/{uid}/loans` subcollection.
*   **Loan Form & EMI Calc:** [LoanAddActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/LoanAddActivity.java) calculates monthly installments on the fly using dark-themed input fields.
*   **Compare Loans:** [LoanCompareActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/LoanCompareActivity.java) performs side-by-side comparison of loan offers. Each card ([item_loan_compare_card.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/item_loan_compare_card.xml)) shows live EMI / Total Interest / Total Payable in coloured result pills.
*   **Analytics Export:** [LoanReportActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/LoanReportActivity.java) exports active loans to PDF and CSV formats under the modern Android MediaStore API.

### 2. Saving Manager (Partially Implemented)
*   [SavingManagerActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/SavingManagerActivity.java) links to [SavingPlanActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/SavingPlanActivity.java) which saves savings target milestones and updates progress bars dynamically.
*   Dashboards display a **Savings Widget Card** (`cardSavingsWidget`) showing the current savings amount with an **Update Savings** button that writes the value to Firestore and reflects it across all features (loan affordability, subscription budget, utility checks).

### 3. Subscription Manager (Partially Implemented)
*   [SubscriptionManagerActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/SubscriptionManagerActivity.java) directs users to [SubscriptionActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/SubscriptionActivity.java) to define notification preferences.

### 4. Utility Bill Manager & B2B Invoice Hub (UI Shells)
*   [UtilityManagerActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/UtilityManagerActivity.java) handles bills registration and reports navigation via mockup forms.
*   [InvoiceHubActivity.java](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/InvoiceHubActivity.java) is a dashboard layout with mock data for corporate outstanding balances.

### 5. PIN Lock (Fully Implemented)
*   Numeric-only 4-digit PIN set by the user after first login.
*   PIN is hashed (not stored in plaintext) using `String.valueOf(pin.hashCode())` in `SharedPreferences`.
*   On app launch, authenticated users see the PIN entry numpad instead of the dashboard directly.
*   **Forgot PIN:** tapping "Forgot PIN? Sign in with password" clears the stored hash and routes to `LoginFormActivity`. After re-login, the user can set a new PIN.
*   **Lockout:** 5 consecutive wrong attempts clears the PIN automatically and redirects to login.
*   Back button is overridden in `PinLockActivity` to prevent bypass.
*   **Dashboard Security Options:** A dedicated security button (`btnSecurity` with lock icon) is integrated into all four role-based dashboard headers, letting users enable, change, or disable their PIN lock configuration at any time.

---

## UI Design System

### Theme
The entire app uses a **premium dark glassmorphic theme** consistently across all screens:
- **Background:** `@drawable/bg_dashboard` (deep navy gradient)
- **Hero headers:** `@drawable/bg_hero_card` (slightly lighter navy with gradient)
- **Glass cards:** `@color/glass_card_bg` with `@color/glass_card_border` stroke (1dp)
- **Input fields:** `@drawable/bg_input_dark` (dark navy with teal border, 14dp radius)
- **Primary accent:** `@color/hero_accent` (teal `#00D4AA`)
- **Danger:** `@color/danger_text` (red)
- **Success:** `@color/pill_positive_text` (green)

### Redesigned Screens (Latest Session)
| Screen | Key Change |
|--------|-----------|
| [activity_login_form.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/activity_login_form.xml) | White → Dark hero + glass form |
| [activity_register.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/activity_register.xml) | White card → Dark hero bar + glass form card |
| [activity_choose_role.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/activity_choose_role.xml) | Light flat → Dark glass role cards with coloured icons |
| [activity_subscription.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/activity_subscription.xml) | White → Dark bg + glass option cards + teal checkboxes |
| [activity_saving_plan.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/activity_saving_plan.xml) | White → Dark bg + hero header |
| [item_loan_compare_card.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/item_loan_compare_card.xml) | White card → Dark glass card with coloured result pills |
| [activity_loan_add_loan.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/activity_loan_add_loan.xml) | Light input fields → Dark `bg_input_dark` fields |
| [activity_pin_setup.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/activity_pin_setup.xml) | New — dark numpad for PIN creation |
| [activity_pin_lock.xml](file:///Users/bosethrathnayake/LNBTI%20SEM4/financial%20manager/Smart-Finance-Manager/app/src/main/res/layout/activity_pin_lock.xml) | New — dark numpad for PIN entry |

### Key Drawables Added
| File | Purpose |
|------|---------|
| `bg_input_dark.xml` | Dark navy input field background with teal stroke |
| `bg_pill_accent.xml` | Teal outlined pill for badges and tags |
| `pin_dot_empty.xml` | Teal outlined circle — empty PIN dot |
| `pin_dot_filled.xml` | Solid teal circle — filled PIN dot |

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
    - All screens must use the unified **premium dark glassmorphic theme** (`bg_dashboard`, `glass_card_bg`, `hero_accent`).
    - Use `bg_input_dark` for all input field containers — never use plain white or `input_field_bg` on dark screens.
    - Use rounded `MaterialCardView` with `glass_card_border` stroke (1dp) and `cardElevation="0dp"`.
    - Use emoji-based icons within `bg_function_icon_*` rounded backgrounds for a modern feel.
    - Primary colors: Dark Navy (`#071A33`), Teal Accent (`#00D4AA`), White (`#FFFFFF`).
    - Register all new activities in `app/src/main/AndroidManifest.xml`.

### PIN Lock Convention
- Always check `PinHelper.isPinSet(context)` before navigating a logged-in user to a dashboard.
- The PIN gating happens in `MainActivity` — do not replicate the check elsewhere.
- Clear the PIN only via `PinHelper.clearPin(context)` to ensure consistent state.

### Testing Practices
- Unit tests should be placed in `app/src/test/java`.
- Instrumented tests (UI tests) should be placed in `app/src/androidTest/java`.

### Contribution Guidelines
- Before adding new features, ensure they align with the dark glassmorphic visual style.
- Register all new activities in `app/src/main/AndroidManifest.xml`.
- Use the `libs.versions.toml` file for dependency management.
- All new input fields must use `@drawable/bg_input_dark` and `@color/text_on_dark_primary` for text.

---

## Roadmap & Improvement Suggestions

### 1. Architectural Improvements
- **Implement MVVM Pattern:** Transition from Activity-based logic to Model-View-ViewModel to separate business logic from UI components.
- **BaseActivity/BaseFragment:** Create base classes to handle shared logic (e.g., Logout, Profile Setup, PIN check) to reduce code duplication.
- **Dependency Injection:** Integrate **Hilt** to manage Firebase and system service instances efficiently.

### 2. UI & UX Enhancements
- **Single Activity Architecture:** Use a single `MainActivity` with **Fragments** and the **Navigation Component** for smoother tab transitions and state preservation.
- **Loading States:** Implement **Shimmer effects** or custom ProgressBars for Firestore data fetching operations.
- **Empty States:** Add illustrative empty state views for lists (Invoices, Subscriptions) when no data is available.
- **PIN Biometric Fallback:** Add fingerprint/face unlock as an alternative to the numeric PIN using `BiometricPrompt`.

### 3. Feature Roadmap
- **Role-Based Repositories:** Abstract profile-specific logic into dedicated Repository classes.
- **Extend Data Export:** Implement PDF/CSV generation functionality for Utility Bills and B2B Invoices matching the existing Loans pattern.
- **Push Notifications:** Integrate **Firebase Cloud Messaging (FCM)** for automated payment and renewal reminders.
- **Loan Affordability Check:** Use current savings amount from the Savings Widget to automatically flag whether new loan EMIs are affordable after deducting subscriptions and utility bills.

### 4. Security & Performance
- **Localization:** Move hardcoded strings to `strings.xml` to support future internationalization.
- **Firestore Security:** Audit and tighten Firestore Rules to ensure strict `uid`-based access control.
- **Offline Persistence:** Explicitly enable Firestore offline data persistence for a seamless offline experience.
- **Code Optimization:** Configure R8/Proguard rules to optimize the release build and protect against reverse engineering.
- **PIN Security Upgrade:** Replace `hashCode()` with a proper cryptographic hash (e.g., SHA-256 via `MessageDigest`) for PIN storage.
