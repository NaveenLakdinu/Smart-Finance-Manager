# Smart Finance Manager

> A comprehensive personal finance management Android application with role-based dashboards, real-time Firebase sync, automated subscription reminders, and PDF/CSV export capabilities.

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Tech Stack](#tech-stack)
4. [Architecture](#architecture)
5. [User Roles & Dashboards](#user-roles--dashboards)
6. [Feature Modules](#feature-modules)
7. [UI Design System](#ui-design-system)
8. [Notification System](#notification-system)
9. [Database Schema](#database-schema)
10. [Build & Run](#build--run)
11. [Project Structure](#project-structure)
12. [Testing](#testing)
13. [Test Account](#test-account)

---

## Overview

**Smart Finance Manager** is an Android app built for LNBTI SEM4 as a financial management platform. It supports five distinct user roles — Student, Company Worker, Business Owner, Student-Worker Hybrid, and Multi Account Holder — each with a tailored dashboard and feature set. The app syncs all data in real-time with Firebase Firestore and provides automated notifications for upcoming subscription renewals and invoice due dates.

---

## Features

| Feature | Status |
|---|---|
| Role-based user registration & login | ✅ Fully Implemented |
| PIN lock with 5-attempt lockout | ✅ Fully Implemented |
| Student Dashboard (savings, loans, subscriptions, utilities) | ✅ Fully Implemented |
| Worker Dashboard (payslip, tasks, expense claims) | ✅ Fully Implemented |
| Business Owner Dashboard (invoices, revenue, analytics) | ✅ Fully Implemented |
| Multi Account Dashboard (multi-account management) | ✅ Fully Implemented |
| Student-Worker Hybrid Dashboard | ✅ Fully Implemented |
| Loan Manager (add, compare, EMI calculator, PDF/CSV export) | ✅ Fully Implemented |
| Saving Manager (goals, progress tracking, savings passport) | ✅ Fully Implemented |
| Subscription Manager (add, list, detail, report) | ✅ Fully Implemented |
| Automated subscription notifications (AlarmManager + WorkManager + FCM) | ✅ Fully Implemented |
| Utility Bill Manager (register, update, report) | ✅ Fully Implemented |
| Expense Claims (add, list, history, report) | ✅ Fully Implemented |
| Invoice Hub (create, detail, email reminders) | ✅ Fully Implemented |
| Worker Payslip (view, share, PDF download) | ✅ Fully Implemented |
| Worker Tasks (add, progress tracking, filtering) | ✅ Fully Implemented |
| Budget Planner (Jetpack Compose) | ✅ Fully Implemented |
| Analytics & Charts (MPAndroidChart) | ✅ Fully Implemented |
| Account Transfer history | ✅ Fully Implemented |
| Role Upgrade | ✅ Fully Implemented |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java (JDK 11) |
| **Platform** | Android — Min SDK 24, Target SDK 36, Compile SDK 37 |
| **Build System** | Gradle with Kotlin DSL (`build.gradle.kts`) |
| **UI** | XML Layouts + Material Design 3 + Jetpack Compose (select screens) |
| **Backend** | Firebase Auth, Firestore, Realtime Database, FCM |
| **Charts** | MPAndroidChart |
| **PDF Export** | iTextPDF |
| **Background Work** | WorkManager + AlarmManager |
| **Email** | JavaMail API |
| **Dependency Management** | `gradle/libs.versions.toml` (Version Catalog) |

---

## Architecture

The app uses a **single-module, Activity-based architecture** with role-based routing.

```
MainActivity
    │
    ├── [Logged out] ──► Welcome Screen ──► LoginFormActivity / RegisterActivity
    │
    └── [Logged in]
            │
            ├── [PIN set]    ──► PinLockActivity ──► Role Dashboard
            └── [No PIN]     ──► PinSetupActivity ──► Role Dashboard
```

### Role-Based Routing (from `LoginFormActivity`)

```
role = "Student"                  ──► StudentDashboardActivity
role = "Company worker"           ──► WorkerDashboardActivity
role = "Business owner"           ──► BusinessDashboardActivity
role = "Multiple account holder"  ──► MultiAccountDashboardActivity
role = "Hybrid"                   ──► StudentWorkerHybridDashboardActivity
```

---

## User Roles & Dashboards

### 👨‍🎓 Student Dashboard
- Current balance & budget-left cards (live Firestore aggregation across incomes, loans, savings, subscriptions, utility bills)
- Savings widget with quick-update button (writes to Firestore instantly)
- Achievement level badge — Bronze / Silver / Gold Saver (based on total savings)
- Quick-access cards: Loan Manager, Subscription Manager, Saving Manager, Utility Manager
- Budget Planner & Savings Passport shortcuts

### 👷 Worker Dashboard
- Monthly salary overview from `worker_profile.monthlySalary`
- Quick actions: Payslip, Worker Tasks, Expense Claims, Saving Manager
- Loan and subscription summary cards
- Expense analytics entry point

### 🏢 Business Owner Dashboard
- Revenue & expense management
- Invoice Hub (B2B invoicing with email reminders)
- Business analytics with MPAndroidChart bar/pie charts
- Financial reports hub

### 🔗 Multi Account Dashboard
- Multiple account card views with live balances
- Quick actions: Transfer, Statements, Loans, Subscriptions, Savings, Utilities, Cards, Add Account
- Real-time balance aggregation across accounts

### 🎓👷 Student-Worker Hybrid Dashboard
- Combined student and worker feature access in a unified dashboard

---

## Feature Modules

### 💰 Loan Manager
| Activity | Purpose |
|---|---|
| `LoanFormActivity` | Lists active loans from Firestore |
| `LoanAddActivity` | Add a new loan with live EMI calculator |
| `LoanCompareActivity` | Side-by-side comparison of two loan offers (EMI, total interest, total payable) |
| `LoanDetailsActivity` | Full loan detail view |
| `LoanReportActivity` | Exports loan data to PDF & CSV via Android MediaStore API |

### 🏦 Saving Manager
| Activity | Purpose |
|---|---|
| `SavingManagerActivity` | Hub: savings overview & shortcuts |
| `SavingListActivity` | RecyclerView list of all saving goals |
| `SavingAddGoalActivity` | Create a new saving goal |
| `SavingUpdateGoalActivity` | Edit an existing goal |
| `SavingDetailsActivity` | Detailed view with transaction log & progress bar |
| `SavingsPassportActivity` | Achievement passport screen (Jetpack Compose) |
| `SavingGenerateReportActivity` | Report generation form |
| `SavingReportResultActivity` | Visual savings report |

### 🔔 Subscription Manager
| Activity | Purpose |
|---|---|
| `SubscriptionManagerActivity` | Hub: total monthly cost & shortcuts |
| `AddSubscriptionActivity` | Add subscription (name, cost, payment day 1–31, cycle, service type) |
| `SubscriptionListActivity` | Full list; tap to open detail, long-press to delete |
| `SubscriptionDetailActivity` | View / edit / delete; "Mark as Paid" auto-advances renewal date |
| `SubscriptionReportActivity` | Bar chart of subscription costs via MPAndroidChart |

### ⚡ Utility Bill Manager
| Activity | Purpose |
|---|---|
| `UtilityManagerActivity` | Hub: bill list, add bill, access reports |
| `RegisterBillActivity` | Register a new utility bill |
| `UtilityBillActivity` | Bill details view |
| `UpdateBillActivity` | Update bill amount or payment status |
| `UtilityReportActivity` | Visual report with charts |
| `UtilityReportFormActivity` | Report filter/generation form |

### 🧾 Expense Claims (Worker)
| Activity | Purpose |
|---|---|
| `ExpenseClaimsActivity` | Entry hub |
| `ExpenseClaimListActivity` | Filterable list (All / Pending / Approved / Rejected / Drafts) |
| `ExpenseClaimAddActivity` | Submit a new claim with category, amount, date & receipt count |
| `ExpenseClaimHistoryActivity` | Historical claims view |
| `ExpenseClaimReportActivity` | Expense report with analytics |

### 📄 Invoice Hub (Business)
| Activity | Purpose |
|---|---|
| `InvoiceHubActivity` | Dashboard of all outstanding invoices |
| `CreateInvoiceActivity` | Create a new B2B invoice |
| `InvoiceDetailsActivity` | View full invoice detail |
| `InvoiceEmailSender` | Sends invoice reminder emails via JavaMail |
| `InvoiceReminderScheduler` | Schedules AlarmManager-based payment due-date alerts |

### 📊 Analytics & Reports
| Activity | Purpose |
|---|---|
| `AnalyticsActivity` | Business analytics with bar/pie charts |
| `FinancialReportsActivity` | Report selection hub |
| `ReportSummaryActivity` | Summary report view |
| `ReportListSelectionActivity` | Choose which report to generate |

### 💸 Transfers
| Activity | Purpose |
|---|---|
| `TransferActivity` | Initiate a fund transfer with confirmation dialog |
| `TransferHistoryActivity` | View past transfer history |

---

## UI Design System

The entire app follows a **premium dark glassmorphic theme**.

### Color Palette
| Token | Hex | Usage |
|---|---|---|
| `surface_primary` | `#0A1628` | Screen backgrounds |
| `glass_card_bg` | `#1A2F50` | All card backgrounds |
| `hero_accent` | `#00D4AA` | Primary CTAs & teal accents |
| `text_primary` | near-white | Main content text |
| `btn_danger` | red | Destructive actions |
| `badge_success_text` | green | Positive status indicators |

> Always use `@color/` references — **never** inline hex values in layouts.

### Card Pattern
All cards use `MaterialCardView` with:
- `cardBackgroundColor` = `@color/glass_card_bg`
- `strokeColor` = `@color/glass_card_border` (1dp)
- `cardCornerRadius` = 16dp (content), 24dp (hero)
- `cardElevation` = 0dp

### Typography (8 levels via `themes.xml`)
`Display (28sp)` → `Headline (24sp)` → `TitleLarge (20sp)` → `Title (16sp)` → `BodyLarge (16sp)` → `Body (14sp)` → `Caption (12sp)` → `Small (11sp)`

Apply via `android:textAppearance="@style/TextAppearance.App.*"`.

### Spacing System
4dp grid: `4 / 8 / 12 / 16 / 20 / 24 / 32 / 48dp` via `@dimen/spacing_*`

---

## Notification System

Smart Finance Manager uses a **3-layer notification architecture** for maximum reliability:

### Layer 1 — FCM Push Notifications
- `SmartFinanceMessagingService` handles token refresh and incoming FCM messages
- FCM token is saved to `users/{uid}/fcmToken` on every login
- Works even when the app is killed (Google maintains the connection)

### Layer 2 — AlarmManager (Exact Timing)
- `SubscriptionNotificationScheduler` schedules 3 alarms per subscription:
  - **Day −2**: "Due in 2 days"
  - **Day −1**: "Due tomorrow"
  - **Day 0**: "Due today" + auto-advances the renewal date to next cycle
- Uses `setExactAndAllowWhileIdle` for Doze mode compatibility
- `SubscriptionAlarmReceiver` handles alarm delivery and shows the notification

### Layer 3 — WorkManager (Fallback Guarantee)
- `SubscriptionWorker` runs daily to catch any alarms missed during Doze
- `SubscriptionBootReceiver` reschedules all alarms after device reboot (`BOOT_COMPLETED` + `MY_PACKAGE_REPLACED`)

### Invoice Reminders
- `InvoiceReminderScheduler` + `InvoiceReminderReceiver` handle payment due-date alerts for B2B invoices

---

## Database Schema

### `users/{uid}` — root document
| Field | Type | Description |
|---|---|---|
| `name` | String | Full name |
| `email` | String | Email address |
| `mobile` | String | Phone number |
| `role` | String | "Student", "Company worker", "Business owner", etc. |
| `status` | String | "active", "suspended", "deactivated" |
| `monthlySavingAmount` | String → double | Monthly saving target |
| `currentSavings` | String → double | Current savings amount |
| `fcmToken` | String | FCM push token |

### Sub-collections under `users/{uid}`
| Sub-collection | Key Fields |
|---|---|
| `student_profile` | `university`, `course`, `studentId` |
| `worker_profile` | `companyName`, `designation`, `monthlySalary` |
| `business_profile` | `businessName`, `regNumber`, `industryType` |
| `multi_profile` | `linkedAccountsCount`, `primaryWorkspace` |
| `loans` | `loanName`, `principalAmount`, `interestRate`, `durationMonths`, `monthlyEmi`, `createdAt` |
| `tasks` | `title`, `description`, `priority`, `status`, `dueDate`, `progress`, `subtasksCompleted`, `subtasksTotal`, `workerEmail` |
| `expense_claims` | `title`, `category`, `amount`, `expenseDate`, `status`, `receiptCount`, `workerEmail`, `approvedBy`, `rejectedReason` |
| `payslips` | `monthYear`, `basicSalary`, `transportAllowance`, `performanceBonus`, `netSalary`, `daysWorked`, `ytdGross` |
| `accounts` | `name`, `balance`, `accountNumber` |
| `subscriptions` | `name`, `amount`, `paymentDay`, `billingCycle`, `renewDate`, `status`, `logoType`, `createdAt` |
| `utility_bills` | `amount`, `status`, `type` |
| `incomes` | `amount`, source fields |
| `savings` | `goalName`, `targetAmount`, `currentAmount`, `status` |
| `budgetPlans` | `semesterIncome`, `createdAt` |

> **Field names are exact** — any typo causes missing UI values. See [DATABASE.md](DATABASE.md) for the complete schema.

---

## Build & Run

### Prerequisites
- Android Studio Jellyfish or newer
- JDK 11+
- `google-services.json` in `app/` (already present in repo)
- Android device or emulator (Min SDK 24)

### Common Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (JVM only — no emulator needed)
./gradlew test

# Run a specific test class
./gradlew test --tests "*LoanCompareActivity*"

# Run Android instrumented tests (emulator/device required)
./gradlew connectedAndroidTest

# Lint check (non-blocking)
./gradlew lint

# Clean + rebuild (recommended after dependency changes)
./gradlew clean assembleDebug

# Install on connected device
./gradlew installDebug
```

> Always use the `gradlew` wrapper — no system-wide Gradle installation required.

### Adding Dependencies
1. Edit `gradle/libs.versions.toml` — add version entry and library alias
2. Reference via `libs.<alias>` in `app/build.gradle.kts`
3. Run `./gradlew clean assembleDebug` and sync Gradle in Android Studio

---

## Project Structure

```
Smart-Finance-Manager/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/smartfinancialmanagement/
│   │       │   ├── MainActivity.java               ← Entry point & auth/PIN routing
│   │       │   ├── LoginFormActivity.java           ← Login + account status checks
│   │       │   ├── RegisterActivity.java            ← Registration with Firestore batch writes
│   │       │   ├── ChooseRoleActivity.java          ← Role selection screen
│   │       │   ├── PinLockActivity.java             ← PIN entry with shake animation & lockout
│   │       │   ├── PinSetupActivity.java            ← Two-stage PIN creation flow
│   │       │   ├── PinHelper.java                   ← PIN hashing & SharedPreferences utility
│   │       │   ├── StudentDashboardActivity.java
│   │       │   ├── WorkerDashboardActivity.java
│   │       │   ├── BusinessDashboardActivity.java
│   │       │   ├── MultiAccountDashboardActivity.java
│   │       │   ├── StudentWorkerHybridDashboardActivity.java
│   │       │   ├── Loan*.java                       ← Loan feature (5 activities + model + adapter)
│   │       │   ├── Saving*.java                     ← Saving feature (8 activities + model + adapter)
│   │       │   ├── Subscription*.java               ← Subscription feature (7 classes incl. scheduler)
│   │       │   ├── Utility*.java                    ← Utility bills (6 activities + adapter)
│   │       │   ├── ExpenseClaim*.java               ← Expense claims (6 classes)
│   │       │   ├── Invoice*.java                    ← Invoice hub (5 classes incl. email sender)
│   │       │   ├── Worker*.java                     ← Worker payslip & tasks
│   │       │   ├── Analytics*.java                  ← Business analytics & charts
│   │       │   ├── Transfer*.java                   ← Fund transfer feature
│   │       │   ├── BudgetPlannerActivity.java       ← Jetpack Compose budget planner
│   │       │   ├── SavingsPassportActivity.java     ← Jetpack Compose savings passport
│   │       │   ├── SmartFinanceMessagingService.java ← FCM token & message handling
│   │       │   ├── SubscriptionNotificationScheduler.java  ← AlarmManager scheduler
│   │       │   ├── SubscriptionAlarmReceiver.java   ← Alarm broadcast handler
│   │       │   ├── SubscriptionBootReceiver.java    ← Reschedules alarms on reboot
│   │       │   ├── SubscriptionWorker.java          ← WorkManager daily fallback
│   │       │   └── CurrencyHelper.java              ← LKR formatting utility
│   │       ├── res/
│   │       │   ├── layout/          ← 102 XML layout files
│   │       │   ├── values/
│   │       │   │   ├── colors.xml   ← 150+ color tokens
│   │       │   │   ├── dimens.xml   ← Spacing system (4dp grid)
│   │       │   │   ├── strings.xml  ← All string resources
│   │       │   │   └── themes.xml   ← Typography scale & Material theme
│   │       │   └── drawable/        ← Backgrounds, icons, card shapes
│   │       └── AndroidManifest.xml  ← All activities, services & receivers registered
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml           ← Version catalog for all dependencies
├── DATABASE.md                      ← Full Firestore schema reference
├── AGENTS.md                        ← AI agent / build command quick reference
└── README.md                        ← This file
```

---

## Testing

| Test Type | Location | Command |
|---|---|---|
| Unit Tests (JVM) | `app/src/test/java` | `./gradlew test` |
| Instrumented UI Tests | `app/src/androidTest/java` | `./gradlew connectedAndroidTest` |
| Lint | — | `./gradlew lint` |

> Unit tests cannot access Android framework classes — use Mockito to mock `FirebaseAuth` and `FirebaseFirestore`. Lint is non-blocking (`abortOnError = false`).

---

## Test Account

```
Email:    test@gmail.com
Password: test@123
```

---

## Key Conventions

- **All screens** must follow the dark glassmorphic theme — no plain white cards or plain backgrounds.
- **Input fields** must use `@drawable/bg_input_dark` with `@color/text_on_dark_primary` text color.
- **Never hardcode text** in XML layout TextViews — always set values programmatically to avoid flash-before-load.
- **Every new Activity, Service, and BroadcastReceiver** must be registered in `AndroidManifest.xml`.
- **Currency formatting**: always use `String.format(Locale.US, "LKR %.2f", value)`.
- **PIN gating** is handled only in `MainActivity` — do not replicate the check in dashboards.
