# Smart-Finance-Manager: Use Case Diagram Details

This document outlines the detailed use cases for the **Smart-Finance-Manager** Android application to assist in drawing a comprehensive Use Case Diagram. The application features a role-based architecture, meaning functionality is segmented based on the type of user.

## 1. Actors (User Roles)

The system identifies the following distinct actors:
1. **Unregistered User (Guest)**
2. **Authenticated User (General)** - *Base actor for all logged-in roles*
3. **Student**
4. **Company Worker**
5. **Business Owner**
6. **Multiple Account Holder**

*(Note: "Student", "Company Worker", "Business Owner", and "Multiple Account Holder" inherit all use cases from the "Authenticated User" actor).*

---

## 2. Use Cases by Actor

### 2.1 Unregistered User (Guest)
*   **Register Account:** Register using email/password and select a specific user role.
*   **Log In:** Authenticate using email and password.
*   **Reset Password:** Recover access via forgot password flow.

### 2.2 Authenticated User (General)
These use cases are available to **all** logged-in users regardless of their specific role.
*   **Manage Security (PIN Lock):**
    *   Set up a 4-digit PIN lock.
    *   Enter PIN to unlock the app on startup.
    *   Recover/Reset PIN (clears PIN and forces re-login).
    *   Update security preferences (Enable/Change/Disable PIN from the dashboard).
*   **Manage Profile:** Update personal details based on the user's role (e.g., Student Profile, Worker Profile).
*   **Manage Loans:**
    *   Add a new loan and calculate EMI.
    *   View active loans list.
    *   Compare multiple loan offers side-by-side.
    *   Export loan analytics (generate PDF/CSV reports).
*   **Manage Savings:**
    *   View current savings balance via the Savings Widget.
    *   Add or update savings goals/milestones.
    *   Track savings progress.
    *   Generate savings reports.
*   **Manage Subscriptions:**
    *   Add new recurring subscriptions.
    *   View subscription details and lists.
    *   Configure notification alerts for upcoming subscription renewals.
    *   Generate subscription reports.
*   **Manage Utility Bills:**
    *   Register new utility bills.
    *   Update utility bill statuses.
    *   View utility bill reports.
*   **Manage Budget:** Utilize the budget calculator and planner.
*   **View Notifications:** Read incoming system alerts, reminders, and alerts via the notification panel.

### 2.3 Student
*   **View Student Dashboard:** Access the role-specific dashboard layout.
*   **Manage Student Budget:** Track university and course-related budgeting.

### 2.4 Company Worker
*   **View Worker Dashboard:** Access the worker-specific dashboard.
*   **Manage Expense Claims:**
    *   Create and submit new expense claims.
    *   View history and lists of expense claims.
    *   Generate expense claim reports.
*   **Manage Payslips:** Upload, view, and track monthly payslips.
*   **Manage Work Tasks:** Add and monitor task deadlines.
*   **Track Income:** Track monthly salary and designation details.

### 2.5 Business Owner
*   **View Business Dashboard:** Access the business-specific dashboard.
*   **Manage Business Entities:** Add and manage different business profiles (e.g., branches or distinct companies).
*   **Manage Invoices (B2B Hub):**
    *   Create new corporate invoices.
    *   View invoice details and outstanding balances.
    *   Send automated email reminders for unpaid invoices.
*   **Manage Revenue & Expenses:** Track business income, calculate revenue, and manage broad business expenses.

### 2.6 Multiple Account Holder
*   **View Multi-Account Dashboard:** Access a unified dashboard spanning multiple accounts.
*   **Manage Linked Accounts:** Track metrics across a primary workspace and linked secondary accounts.
*   **Transfer Funds:** Execute and log transfers between linked accounts and view transfer history.

---

## 3. Key System Systems (Secondary Actors)
When drawing the Use Case Diagram, you may also want to include the following external systems that the app interacts with:
*   **Firebase Authentication:** Handles user sign-up, sign-in, and password resets.
*   **Firestore Database:** Stores user profiles, loans, savings, subscriptions, and role-specific sub-collections.
*   **Firebase Cloud Messaging (FCM):** Sends push notifications for reminders and alerts.
*   **Android MediaStore API:** Handles the saving of exported reports (PDF/CSV) to the local device storage.
