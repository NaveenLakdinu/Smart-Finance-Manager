# Smart Finance Manager Database Documentation

This document provides a detailed overview of the Firebase Firestore database structure for the Smart Finance Manager application. It also includes instructions for creating an Entity-Relationship (ER) diagram based on this structure.

## 1. Database Overview

The application uses **Firebase Firestore**, a NoSQL document database, to store user data. The database is structured around a main `users` collection, with subcollections for role-specific information and loans.

**Connection:** The application connects to Firebase Firestore using the Firebase SDK for Android. The connection is automatically managed by the SDK. The configuration is stored in the `google-services.json` file.

## 2. Database Schema

### 2.1. `users` Collection

This is the root collection that stores the primary information for each user.

*   **Document ID:** The user's UID from Firebase Authentication.
*   **Description:** Each document in this collection represents a single user.

| Field Name              | Data Type | Description                                         |
| ----------------------- | --------- | --------------------------------------------------- |
| `uid`                   | `String`  | The user's unique ID from Firebase Authentication.  |
| `name`                  | `String`  | The user's full name.                               |
| `age`                   | `String`  | The user's age.                                     |
| `email`                 | `String`  | The user's email address.                           |
| `mobile`                | `String`  | The user's mobile number.                           |
| `role`                  | `String`  | The user's role (e.g., "Student", "Company worker"). |
| `timestamp`             | `long`    | The timestamp of when the user account was created. |
| `hasLoan`               | `boolean` | `true` if the user has a loan.                      |
| `loanAmount`            | `String`  | The total amount of the loan.                       |
| `monthlyInstallment`    | `String`  | The monthly loan installment amount.                |
| `monthsPaid`            | `String`  | The number of months the loan has been paid.        |
| `paymentMethod`         | `String`  | The payment method for the loan.                    |
| `hasSavingPlan`         | `boolean` | `true` if the user has a saving plan.               |
| `savingGoalName`        | `String`  | The name of the user's saving goal.                 |
| `savingTargetAmount`    | `String`  | The target amount for the saving goal.              |
| `savingTargetDate`      | `String`  | The target date to reach the saving goal.           |
| `monthlySavingAmount`   | `String`  | The amount the user saves monthly.                  |
| `currentSavings`        | `String`  | The current amount saved.                           |
| `receiveUpdates`        | `boolean` | `true` if the user wants to receive updates.        |
| `checkEmail`            | `boolean` | `true` if the user wants email notifications.       |
| `checkSms`              | `boolean` | `true` if the user wants SMS notifications.         |
| `checkPush`             | `boolean` | `true` if the user wants push notifications.        |
| `checkReport`           | `boolean` | `true` if the user wants to receive reports.        |
| `checkPromo`            | `boolean` | `true` if the user wants to receive promotions.     |

### 2.2. Role-Specific Subcollections

Depending on the user's `role`, a subcollection is created under the user's document to store role-specific data. The document ID for this subcollection is always `profile_data`.

#### 2.2.1. `student_profile` Subcollection

*   **Parent Document:** `users/{uid}` (where `role` is "Student")
*   **Document ID:** `profile_data`

| Field Name     | Data Type | Description                  |
| -------------- | --------- | ---------------------------- |
| `university`   | `String`  | The user's university name.  |
| `course`       | `String`  | The user's course of study.  |
| `studentId`    | `String`  | The user's student ID number.|

#### 2.2.2. `worker_profile` Subcollection

*   **Parent Document:** `users/{uid}` (where `role` is "Company worker")
*   **Document ID:** `profile_data`

| Field Name        | Data Type | Description                  |
| ----------------- | --------- | ---------------------------- |
| `companyName`     | `String`  | The user's company name.     |
| `designation`     | `String`  | The user's job title.        |
| `monthlySalary`   | `double`  | The user's monthly salary.   |

#### 2.2.3. `business_profile` Subcollection

*   **Parent Document:** `users/{uid}` (where `role` is "Business owner")
*   **Document ID:** `profile_data`

| Field Name       | Data Type | Description                       |
| ---------------- | --------- | --------------------------------- |
| `businessName`   | `String`  | The user's business name.         |
| `regNumber`      | `String`  | The business registration number. |
| `industryType`   | `String`  | The industry of the business.     |

#### 2.2.4. `multi_profile` Subcollection

*   **Parent Document:** `users/{uid}` (where `role` is "Multiple account holder")
*   **Document ID:** `profile_data`

| Field Name            | Data Type | Description                               |
| --------------------- | --------- | ----------------------------------------- |
| `linkedAccountsCount` | `int`     | The number of linked accounts.            |
| `primaryWorkspace`    | `String`  | The user's primary workspace.             |

### 2.3. `loans` Subcollection

This subcollection is created if the user has a loan (`hasLoan` is `true`).

*   **Parent Document:** `users/{uid}`
*   **Document ID:** Auto-generated by Firestore.

| Field Name        | Data Type | Description                       |
| ----------------- | --------- | --------------------------------- |
| `loanName`        | `String`  | The name of the loan.             |
| `principalAmount` | `double`  | The principal amount of the loan. |
| `interestRate`    | `double`  | The interest rate of the loan.    |
| `durationMonths`  | `int`     | The duration of the loan in months. |
| `monthlyEmi`      | `double`  | The monthly EMI amount.           |
| `createdAt`       | `long`    | The timestamp of when the loan was created. |

## 3. Instructions for ER Diagram

Since Firestore is a NoSQL database, a traditional ER diagram is not a perfect fit. However, you can create a logical ER diagram to represent the relationships between the collections and subcollections.

Here’s how you can represent the Firestore schema in an ER diagram:

1.  **Entities:** Each collection and subcollection can be represented as an entity.
    *   `Users`
    *   `Student Profile`
    *   `Worker Profile`
    *   `Business Profile`
    *   `Multi Profile`
    *   `Loans`

2.  **Attributes:** The fields in each document can be represented as attributes of the corresponding entity. Mark the document ID as the primary key for each entity.

3.  **Relationships:**
    *   **`Users` and Role-Specific Profiles:** This is a one-to-one relationship. A `Users` document has one role-specific profile document. You can represent this with a connecting line and a cardinality of `1` to `1`. Since the role-specific profiles are subcollections, you can also represent this as a composite relationship, where the profile entities are weak entities that depend on the `Users` entity.
    *   **`Users` and `Loans`:** This is a one-to-many relationship. A `Users` document can have multiple `Loans` documents. You can represent this with a connecting line and a cardinality of `1` to `N`.

**Example ER Diagram Representation:**

```
+-----------------+      +---------------------+
|      Users      |      |  Role-Specific (1)  |
|-----------------|      |---------------------|
| PK  uid         |------| FK  uid             |
|     name        |      |     ... (fields)    |
|     age         |      +---------------------+
|     ... (fields)|
+-----------------+
        |
        | 1..N
        |
+-----------------+
|      Loans      |
|-----------------|
| PK  loanId      |
| FK  uid         |
|     loanName    |
|     ... (fields)|
+-----------------+
```

**Note:** In the diagram, `Role-Specific (1)` represents one of the four role-specific profile entities (`Student Profile`, `Worker Profile`, `Business Profile`, `Multi Profile`). You can create separate entities for each role profile and connect them to the `Users` entity with a one-to-one relationship.
