# Graph Report - .  (2026-07-13)

## Corpus Check
- 86 files · ~113,094 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 712 nodes · 1380 edges · 50 communities (44 shown, 6 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 34 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- User Dashboard UI
- Loan Details UI
- User Login & Auth
- Utility Bills Adapter
- PIN Security System
- Loan Offer Comparison
- Worker Dashboard UI
- Savings Target Milestones
- Loan Data Model
- Loan List UI
- Multi-Account Dashboard
- Student-Worker Hybrid Dashboard
- Business Owner Dashboard
- Utility Bill Registration
- Loan Analytics Report
- Expense Management Flow
- Active Loans Form
- Loan Application Form
- Revenue Management Flow
- Financial Analytics UI
- B2B Invoice Creation
- Utility Report Generation
- Corporate Invoice Hub
- User Role Upgrade
- Savings Widget Manager
- Student Budget Settings
- Corporate Invoice Details
- Registration Role Selection
- Expense Claims Manager
- Multi-Account Management
- Notification Preferences UI
- Subscription Budget Flow
- Utility Manager Shell
- Utility Report Panel
- Report List UI
- Utility Report UI Shell
- Android Instrumented Tests
- Student Profile Details
- Student Savings UI
- Worker Payslip UI
- Gradle Build Wrapper
- Java Unit Tests
- Graphify Rule & Workflows
- Project Design System
- Admin Security Architecture
- Project Documentation

## God Nodes (most connected - your core abstractions)
1. `Loan` - 35 edges
2. `LoginFormActivity` - 26 edges
3. `DashboardActivity` - 22 edges
4. `RegisterActivity` - 22 edges
5. `UtilityBill` - 21 edges
6. `PinLockActivity` - 20 edges
7. `LoanCompareActivity` - 19 edges
8. `MultiAccountDashboardActivity` - 19 edges
9. `SavingPlanActivity` - 19 edges
10. `StudentWorkerHybridDashboardActivity` - 18 edges

## Surprising Connections (you probably didn't know these)
- `BusinessDashboardActivity` --shares_data_with--> `Business Profile Subcollection`  [INFERRED]
  app/src/main/java/com/example/smartfinancialmanagement/BusinessDashboardActivity.java → DATABASE.md
- `MultiAccountDashboardActivity` --shares_data_with--> `Multi Profile Subcollection`  [INFERRED]
  app/src/main/java/com/example/smartfinancialmanagement/MultiAccountDashboardActivity.java → DATABASE.md
- `StudentDashboardActivity` --shares_data_with--> `Student Profile Subcollection`  [INFERRED]
  app/src/main/java/com/example/smartfinancialmanagement/StudentDashboardActivity.java → DATABASE.md
- `Firebase Authentication Email Already In Use Remediation` --rationale_for--> `RegisterActivity`  [INFERRED]
  Screenshot 2026-06-08 at 21.51.34.png → app/src/main/java/com/example/smartfinancialmanagement/RegisterActivity.java
- `WorkerDashboardActivity` --shares_data_with--> `Worker Profile Subcollection`  [INFERRED]
  app/src/main/java/com/example/smartfinancialmanagement/WorkerDashboardActivity.java → DATABASE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **PIN Lock Security Flow** — app_src_main_java_com_example_smartfinancialmanagement_mainactivity_mainactivity, app_src_main_java_com_example_smartfinancialmanagement_pinlockactivity_pinlockactivity, app_src_main_java_com_example_smartfinancialmanagement_pinsetupactivity_pinsetupactivity, app_src_main_java_com_example_smartfinancialmanagement_pinhelper_pinhelper [EXTRACTED 1.00]
- **Role-Based Dashboards** — app_src_main_java_com_example_smartfinancialmanagement_studentdashboardactivity_studentdashboardactivity, app_src_main_java_com_example_smartfinancialmanagement_workerdashboardactivity_workerdashboardactivity, app_src_main_java_com_example_smartfinancialmanagement_businessdashboardactivity_businessdashboardactivity, app_src_main_java_com_example_smartfinancialmanagement_multiaccountdashboardactivity_multiaccountdashboardactivity [EXTRACTED 1.00]
- **Role-Specific Database Subcollections** — database_student_profile, database_worker_profile, database_business_profile, database_multi_profile [EXTRACTED 1.00]

## Communities (50 total, 6 thin omitted)

### Community 0 - "User Dashboard UI"
Cohesion: 0.07
Nodes (18): Adapter, DashboardActivity, Bundle, LinearLayout, MaterialCardView, Override, RecyclerView, TextView (+10 more)

### Community 1 - "Loan Details UI"
Cohesion: 0.07
Nodes (26): Bundle, EditText, ImageView, MaterialButton, Override, Spinner, LoanDetailsActivity, Bundle (+18 more)

### Community 2 - "User Login & Auth"
Cohesion: 0.08
Nodes (20): Bundle, EditText, FirebaseAuth, FirebaseFirestore, ImageView, MaterialButton, Override, TextView (+12 more)

### Community 3 - "Utility Bills Adapter"
Cohesion: 0.08
Nodes (18): Context, ImageView, NonNull, Override, TextView, View, ViewGroup, OnUtilityClickListener (+10 more)

### Community 4 - "PIN Security System"
Cohesion: 0.10
Nodes (15): Context, PinHelper, Bundle, LinearLayout, Override, TextView, View, PinLockActivity (+7 more)

### Community 5 - "Loan Offer Comparison"
Cohesion: 0.17
Nodes (12): ComparisonData, Bundle, EditText, FirebaseFirestore, ImageView, LinearLayout, MaterialButton, MaterialCardView (+4 more)

### Community 6 - "Worker Dashboard UI"
Cohesion: 0.14
Nodes (13): Bundle, MaterialCardView, Override, TextView, View, WorkerDashboardActivity, Business Profile Subcollection, Firebase Firestore Database Schema (+5 more)

### Community 7 - "Savings Target Milestones"
Cohesion: 0.18
Nodes (10): Bundle, EditText, ImageView, LinearLayout, MaterialButton, Override, ProgressBar, Spinner (+2 more)

### Community 9 - "Loan List UI"
Cohesion: 0.20
Nodes (10): Context, ImageView, NonNull, Override, TextView, View, ViewGroup, LoanAdapter (+2 more)

### Community 10 - "Multi-Account Dashboard"
Cohesion: 0.22
Nodes (7): Bundle, LinearLayout, MaterialCardView, Override, TextView, View, MultiAccountDashboardActivity

### Community 11 - "Student-Worker Hybrid Dashboard"
Cohesion: 0.22
Nodes (7): Bundle, FirebaseAuth, FirebaseFirestore, Override, TextView, View, StudentWorkerHybridDashboardActivity

### Community 12 - "Business Owner Dashboard"
Cohesion: 0.22
Nodes (7): BusinessDashboardActivity, Bundle, MaterialCardView, Override, RecyclerView, TextView, View

### Community 13 - "Utility Bill Registration"
Cohesion: 0.21
Nodes (8): Bundle, Button, EditText, FirebaseFirestore, ImageView, Override, Spinner, RegisterBillActivity

### Community 14 - "Loan Analytics Report"
Cohesion: 0.21
Nodes (9): Bundle, CheckBox, FirebaseFirestore, ImageView, MaterialButton, MaterialCardView, Override, LoanReportActivity (+1 more)

### Community 15 - "Expense Management Flow"
Cohesion: 0.25
Nodes (7): ExpenseManagementActivity, Bundle, Button, EditText, Override, Spinner, TextView

### Community 16 - "Active Loans Form"
Cohesion: 0.24
Nodes (8): Bundle, FirebaseFirestore, ImageView, MaterialCardView, Override, RecyclerView, TextView, LoanFormActivity

### Community 17 - "Loan Application Form"
Cohesion: 0.25
Nodes (7): Bundle, EditText, ImageView, MaterialButton, Override, TextView, LoanAddActivity

### Community 18 - "Revenue Management Flow"
Cohesion: 0.26
Nodes (7): Bundle, Button, EditText, Override, Spinner, TextView, RevenueManagementActivity

### Community 19 - "Financial Analytics UI"
Cohesion: 0.28
Nodes (7): ActivityResultLauncher, AnalyticsActivity, Bundle, MaterialButton, MaterialCardView, Override, TextView

### Community 20 - "B2B Invoice Creation"
Cohesion: 0.27
Nodes (6): CreateInvoiceActivity, Bundle, MaterialButton, Override, TextView, TextInputEditText

### Community 21 - "Utility Report Generation"
Cohesion: 0.29
Nodes (7): GetReportActivity, Bundle, Button, EditText, ImageView, Override, Spinner

### Community 22 - "Corporate Invoice Hub"
Cohesion: 0.29
Nodes (6): InvoiceHubActivity, Bundle, Override, RecyclerView, TextView, FloatingActionButton

### Community 23 - "User Role Upgrade"
Cohesion: 0.29
Nodes (8): Bundle, FirebaseAuth, FirebaseFirestore, ImageView, MaterialButton, Override, ProgressBar, RoleUpgradeActivity

### Community 24 - "Savings Widget Manager"
Cohesion: 0.29
Nodes (6): Bundle, ImageView, Override, TextView, View, SavingManagerActivity

### Community 25 - "Student Budget Settings"
Cohesion: 0.25
Nodes (7): Bundle, Override, StudentBudgetActivity, Bundle, Override, WorkerTasksActivity, AppCompatActivity

### Community 26 - "Corporate Invoice Details"
Cohesion: 0.33
Nodes (5): InvoiceDetailsActivity, Bundle, MaterialButton, Override, TextView

### Community 27 - "Registration Role Selection"
Cohesion: 0.36
Nodes (6): ChooseRoleActivity, Bundle, ImageView, MaterialButton, MaterialCardView, Override

### Community 28 - "Expense Claims Manager"
Cohesion: 0.36
Nodes (5): ExpenseClaimsActivity, Bundle, ImageView, Override, View

### Community 29 - "Multi-Account Management"
Cohesion: 0.36
Nodes (5): Bundle, ImageView, MaterialButton, Override, MultiAccountActivity

### Community 30 - "Notification Preferences UI"
Cohesion: 0.36
Nodes (6): Bundle, CheckBox, ImageView, MaterialButton, Override, SubscriptionActivity

### Community 31 - "Subscription Budget Flow"
Cohesion: 0.36
Nodes (5): Bundle, ImageView, Override, View, SubscriptionManagerActivity

### Community 32 - "Utility Manager Shell"
Cohesion: 0.36
Nodes (5): Bundle, ImageView, Override, View, UtilityManagerActivity

### Community 33 - "Utility Report Panel"
Cohesion: 0.36
Nodes (5): Bundle, ImageView, Override, Spinner, UtilityReportActivity

### Community 35 - "Report List UI"
Cohesion: 0.39
Nodes (6): Bundle, Button, ImageView, Override, ReportListSelectionActivity, CardView

### Community 36 - "Utility Report UI Shell"
Cohesion: 0.47
Nodes (4): Bundle, ImageView, Override, ReportPanelActivity

### Community 37 - "Android Instrumented Tests"
Cohesion: 0.60
Nodes (3): ExampleInstrumentedTest, Test, RunWith

### Community 38 - "Student Profile Details"
Cohesion: 0.50
Nodes (3): Bundle, Override, StudentProfileActivity

### Community 39 - "Student Savings UI"
Cohesion: 0.50
Nodes (3): Bundle, Override, StudentSavingActivity

### Community 40 - "Worker Payslip UI"
Cohesion: 0.50
Nodes (3): Bundle, Override, WorkerPayslipActivity

### Community 41 - "Gradle Build Wrapper"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **7 isolated node(s):** `Graphify Knowledge Graph Rule`, `Graphify Workflow`, `Firebase Firestore Database Schema`, `Loans Subcollection`, `Smart Finance Manager Project Overview` (+2 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DashboardActivity` connect `User Dashboard UI` to `Student Budget Settings`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **Why does `UtilityBillActivity` connect `Utility Bills Adapter` to `Student Budget Settings`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **Why does `LoanFormActivity` connect `Active Loans Form` to `Loan Data Model`, `Loan List UI`, `Loan Attribute Model`, `Student Budget Settings`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **What connects `Graphify Knowledge Graph Rule`, `Graphify Workflow`, `Firebase Firestore Database Schema` to the rest of the system?**
  _7 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `User Dashboard UI` be split into smaller, more focused modules?**
  _Cohesion score 0.07390648567119155 - nodes in this community are weakly interconnected._
- **Should `Loan Details UI` be split into smaller, more focused modules?**
  _Cohesion score 0.07482993197278912 - nodes in this community are weakly interconnected._
- **Should `User Login & Auth` be split into smaller, more focused modules?**
  _Cohesion score 0.08244680851063829 - nodes in this community are weakly interconnected._