# Smart Finance Manager — Comprehensive UI Improvement Plan

**Target:** Make the app look polished, modern, and professional — like a premium fintech app.
**Unified Direction:** Dark Navy Glassmorphic theme (#0A1628 bg, #1A2F50 glass cards, #00D4AA teal accent, #F0F6FF text)
**Total Layout Files:** 85
**Files with Hardcoded Hex Colors:** ~35
**Total Inline Hex Occurrences:** ~400+

---

## Current State Summary

### Theme Map (5 conflicting themes)

| Theme | Screens | Status |
|-------|---------|--------|
| **Dark Navy Glassmorphic** (target) | Worker/Business/Multi-Account dashboards, Login, Register, PIN Lock, Transfer, Utility, Invoice, Loan form/compare, Saving plan/add/update | ✅ Dominant — ~60 screens |
| **Hybrid Dark+White cards** | Student budget, Student loans, Student profile, Account settings, Financial reports, Loan details | ⚠️ Needs conversion — ~6 screens |
| **Full Light theme** | Student subscription (#F0F4FA), Student event (#F4F7FB) | ❌ Complete theme mismatch — 2 screens |
| **Goal deep dark** | Saving manager, Saving list, Saving details | ⚠️ Slightly different dark palette (#090D1A vs #0A1628) — 3 screens |
| **Savings Passport parchment** | Savings passport | ⚠️ Unique brand theme — 1 screen (special case) |

### Design System Files Already Defined
- `colors.xml`: 100+ tokens including `dash_*`, `glass_*`, `text_on_dark_*`, `qa_*`, `goal_*`, `sp_*`
- `themes.xml`: Material Components base theme (minimal)
- `styles.xml`: Empty (only CircleImageView style)
- Reference files (zero hardcoded hex): `activity_loan_form.xml`, `item_loan_compare_card.xml`, `activity_pin_lock.xml`, `activity_register.xml`

---

## PHASE 0: Foundation — Color & Typography System Expansion

**Priority:** CRITICAL — Must come first; everything else depends on it
**Risk Level:** LOW — Only adds new resources; no layout changes
**Expected Impact:** Enables all subsequent phases; zero visual change yet

### Files to Modify
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values/styles.xml`

### Changes

#### A. Expand `colors.xml` with missing tokens

The existing token set is good for the glassmorphic dashboard but is missing tokens needed for the student screens and dialogs. Add:

```xml
<!-- ══════════════════════════════════════════ -->
<!-- SEMANTIC ALIASES (map intent → value)      -->
<!-- ══════════════════════════════════════════ -->

<!-- Primary surfaces -->
<color name="surface_primary">@color/dash_bg_deep</color>
<color name="surface_card">@color/glass_card_bg</color>
<color name="surface_card_border">@color/glass_card_border</color>
<color name="surface_elevated">@color/dash_bg_mid</color>

<!-- Semantic text -->
<color name="text_primary">@color/text_on_dark_primary</color>
<color name="text_secondary_dark">@color/text_on_dark_secondary</color>
<color name="text_muted">@color/text_on_dark_muted</color>

<!-- Action buttons -->
<color name="btn_primary">@color/hero_accent</color>           <!-- #00D4AA teal -->
<color name="btn_primary_pressed">#00B896</color>
<color name="btn_secondary">@color/glass_card_bg</color>
<color name="btn_danger">@color/danger_text</color>             <!-- #FB7185 -->
<color name="btn_danger_bg">@color/danger_bg</color>            <!-- #2D0F1A -->

<!-- Spinner / dropdown -->
<color name="spinner_text">@color/text_on_dark_primary</color>
<color name="spinner_bg">@color/glass_card_bg</color>
<color name="spinner_divider">@color/divider_dark</color>

<!-- Input fields -->
<color name="input_bg">@color/glass_card_bg</color>
<color name="input_stroke">@color/glass_card_border</color>
<color name="input_text">@color/text_on_dark_primary</color>
<color name="input_hint">@color/text_on_dark_muted</color>

<!-- Progress bar -->
<color name="progress_track">#1A3050</color>
<color name="progress_fill">@color/hero_accent</color>

<!-- Tab / Segmented control -->
<color name="tab_active_bg">@color/hero_accent</color>
<color name="tab_active_text">@color/dash_bg_deep</color>
<color name="tab_inactive_bg">@color/glass_card_bg</color>
<color name="tab_inactive_text">@color/text_on_dark_secondary</color>

<!-- Badge / Pill -->
<color name="badge_success_bg">@color/pill_positive_bg</color>
<color name="badge_success_text">@color/pill_positive_text</color>
<color name="badge_warning_bg">@color/pill_warning_bg</color>
<color name="badge_warning_text">@color/pill_warning_text</color>
<color name="badge_info_bg">#162D4A</color>
<color name="badge_info_text">#38BDF8</color>

<!-- Dialog -->
<color name="dialog_bg">@color/glass_card_bg</color>
<color name="dialog_scrim">#80000000</color>

<!-- Status bar -->
<color name="status_bar">#070F1E</color>

<!-- Bottom Navigation -->
<color name="bottom_nav_bg">@color/dash_bg_mid</color>
<color name="bottom_nav_active">@color/hero_accent</color>
<color name="bottom_nav_inactive">@color/text_on_dark_muted</color>

<!-- Currency symbol -->
<color name="currency_symbol">@color/hero_accent</color>
```

#### B. Expand `themes.xml` with comprehensive theme

```xml
<!-- Dark Glassmorphic Theme (primary) -->
<style name="Theme.SmartFinance.Dark" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <item name="colorPrimary">@color/hero_accent</item>
    <item name="colorPrimaryVariant">@color/hero_accent_dim</item>
    <item name="colorSecondary">@color/hero_accent</item>
    <item name="android:windowBackground">@color/dash_bg_deep</item>
    <item name="android:statusBarColor">@color/status_bar</item>
    <item name="android:navigationBarColor">@color/dash_bg_mid</item>
    <item name="android:textColorPrimary">@color/text_on_dark_primary</item>
    <item name="android:textColorSecondary">@color/text_on_dark_secondary</item>
    <item name="android:textColorHint">@color/text_on_dark_muted</item>
    <item name="colorOnPrimary">@color/dash_bg_deep</item>
    <item name="colorSurface">@color/glass_card_bg</item>
    <item name="colorOnSurface">@color/text_on_dark_primary</item>
    <item name="bottomNavigationStyle">@style/Widget.App.BottomNav.Dark</item>
</style>

<!-- Bottom Navigation Dark Style -->
<style name="Widget.App.BottomNav.Dark" parent="Widget.MaterialComponents.BottomNavigationView.Colored">
    <item name="android:background">@color/bottom_nav_bg</item>
    <item name="itemIconTint">@color/bottom_nav_color</item>
    <item name="itemTextColor">@color/bottom_nav_color</item>
</style>

<!-- Dark Card Style -->
<style name="Widget.App.Card.Dark" parent="Widget.MaterialComponents.CardView">
    <item name="cardBackgroundColor">@color/glass_card_bg</item>
    <item name="strokeColor">@color/glass_card_border</item>
    <item name="strokeWidth">1dp</item>
    <item name="cardCornerRadius">16dp</item>
    <item name="cardElevation">0dp</item>
</style>

<!-- Dark Dialog Theme -->
<style name="Theme.SmartFinance.Dialog" parent="ThemeOverlay.MaterialComponents.Dialog.Alert">
    <item name="dialogCornerRadius">20dp</item>
    <item name="colorSurface">@color/glass_card_bg</item>
    <item name="android:textColorPrimary">@color/text_on_dark_primary</item>
</style>

<!-- Dark Button Styles -->
<style name="Widget.App.Button.Teal" parent="Widget.MaterialComponents.Button">
    <item name="backgroundTint">@color/hero_accent</item>
    <item name="android:textColor">@color/dash_bg_deep</item>
    <item name="cornerRadius">12dp</item>
    <item name="android:textAllCaps">false</item>
    <item name="android:fontFamily">sans-serif-medium</item>
</style>

<style name="Widget.App.Button.Outline.Dark" parent="Widget.MaterialComponents.Button.OutlinedButton">
    <item name="strokeColor">@color/glass_card_border</item>
    <item name="android:textColor">@color/text_on_dark_primary</item>
    <item name="backgroundTint">@android:color/transparent</item>
    <item name="cornerRadius">12dp</item>
</style>

<style name="Widget.App.Button.Text.Dark" parent="Widget.MaterialComponents.Button.TextButton">
    <item name="android:textColor">@color/hero_accent</item>
</style>
```

#### C. Create `res/color/bottom_nav_color.xml` (color state list)

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/hero_accent" android:state_checked="true"/>
    <item android:color="@color/text_on_dark_muted"/>
</selector>
```

### Verification
After Phase 0, build the project with `./gradlew assembleDebug` to confirm no resource conflicts.

---

## PHASE 1: Critical Visual Fixes (Broken/Unreadable Elements)

**Priority:** URGENT — These are actual usability bugs
**Risk Level:** LOW — Small targeted fixes
**Expected Impact:** HIGH — Fixes unreadable text, broken layouts, jarring elements

### 1A. Hybrid Dashboard — White BottomNavigationView
- **File:** `activity_student_worker_hybrid_dashboard.xml`
- **Change:** Replace `android:background="#FFFFFF"` with `android:background="@color/bottom_nav_bg"` and add `app:itemIconTint="@color/bottom_nav_color"` + `app:itemTextColor="@color/bottom_nav_color"`
- **Impact:** Removes the most visually jarring element in the app
- **Risk:** LOW

### 1B. Spinner Text Invisible on Dark Backgrounds
- **Files:** `spinner_dropdown_item.xml`, `spinner_item.xml`
- **Change:** Replace `android:textColor="#111827"` with `android:textColor="@color/spinner_text"` (resolves to `@color/text_on_dark_primary`). Also add spinner background for dropdown.
- **Impact:** Dropdown selections become readable on all dark screens
- **Risk:** LOW

### 1C. Business Dashboard — Red Logout Icon
- **File:** `activity_business_owner_dashboard.xml`
- **Change:** Replace `android:tint="#FF4466"` on the logout icon with `android:tint="@color/text_on_dark_secondary"` (or `@color/danger_text` if intent is danger). Match other dashboard logout styles.
- **Impact:** Removes inconsistent red icon
- **Risk:** LOW

### 1D. Transfer Button — Wrong Green
- **File:** `activity_transfer.xml`
- **Change:** Replace `app:backgroundTint="#4CAF50"` with `app:backgroundTint="@color/btn_primary"` (#00D4AA teal). Replace root `android:background="#0A1628"` with `@color/surface_primary` or `@drawable/bg_dashboard`.
- **Impact:** Button becomes consistent with all other CTA buttons
- **Risk:** LOW

### 1E. Student Subscription & Event — Full Light Theme Conversion
- **Files:** `activity_student_subscription.xml`, `activity_student_event.xml`
- **Change:** Complete theme conversion:
  - Replace `#F0F4FA` / `#F4F7FB` root backgrounds with `@drawable/bg_dashboard` or `@color/surface_primary`
  - Convert all `#FFFFFF` card backgrounds to `@color/glass_card_bg`
  - Convert dark text colors (`#111827`, `#212121`) to `@color/text_primary`
  - Convert `#1E60FF` / `#3B82F6` blue accents to `@color/hero_accent` or keep as `@color/qa_blue_icon`
  - Convert light-label colors to `@color/text_secondary_dark`
  - Replace `CardView` with `MaterialCardView`
- **Impact:** Two of the worst theme-clashing screens become consistent
- **Risk:** MEDIUM — Full theme rewrite of 2 screens; needs careful testing

### 1F. Student Budget & Loans — White Cards on Dark Background
- **Files:** `activity_student_budget.xml`, `activity_student_loans.xml`
- **Change:** Convert white card backgrounds to glass card style:
  - `app:cardBackgroundColor="#FFFFFF"` → `@color/glass_card_bg` with `app:strokeColor="@color/glass_card_border"`
  - `#111827` text → `@color/text_primary`
  - `#1E60FF` accents → `@color/hero_accent` or `@color/qa_blue_icon`
  - `#9CA3AF`, `#6B7280` secondary text → `@color/text_secondary_dark`
  - Replace `#F3F4F6` dividers with `@color/divider_dark`
  - Fix `tools:context=".MainActivity"` → correct activity
- **Impact:** Removes jarring light-on-dark card clash
- **Risk:** MEDIUM — Large files with many color changes

### 1G. Student Profile — Hybrid Theme Fix
- **File:** `activity_student_profile.xml`
- **Change:** Same as 1F — convert light section (`#F8FAFC` bg, `#FFFFFF` cards) to glass card style
- **Impact:** Consistent dark theme
- **Risk:** MEDIUM

### 1H. Account Settings & Financial Reports — Hybrid Theme Fix
- **Files:** `activity_account_settings.xml`, `activity_financial_reports.xml`
- **Change:** Convert `#071A37` hardcoded bg → `@color/surface_primary`, white cards → `@color/glass_card_bg`
- **Impact:** Consistent dark theme
- **Risk:** LOW-MEDIUM

### 1I. Loan Details — Hardcoded Root Background
- **File:** `activity_loan_details.xml`
- **Change:** Replace hardcoded `#111827` root background with `@drawable/bg_dashboard`. Convert white form card to glass card.
- **Impact:** Consistent with all other loan screens
- **Risk:** LOW

### 1J. Student Dashboard — Cyan vs Teal Inconsistency
- **File:** `activity_student_dashboard.xml`
- **Change:** Replace all `#06B6D4` (cyan) with `@color/hero_accent` (#00D4AA teal). This appears in avatar card bg, decorative circles, progress bars, and pill text.
- **Impact:** Aligns student dashboard with the rest of the app's accent color
- **Risk:** LOW — Simple color swap

### 1K. Registration Progress Bar Visibility
- **File:** `activity_register.xml`
- **Change:** Verify progress bar colors have sufficient contrast against dark background. If `progress_inactive` drawable is too dark, adjust `@color/progress_track` to be slightly lighter.
- **Impact:** Users can see their registration progress
- **Risk:** LOW

### Verification
After Phase 1, manually test these specific screens on a device/emulator:
- Login → Register flow
- Student dashboard
- Student subscription
- Student event
- Hybrid dashboard (check BottomNav)
- Business owner dashboard (check logout icon)
- Transfer screen (check button)
- All spinner dropdowns

---

## PHASE 2: Hardcoded Color Elimination — High-Count Files

**Priority:** HIGH — Eliminates maintenance nightmare, enables future theming
**Risk Level:** LOW per file — Simple find/replace patterns
**Expected Impact:** MEDIUM — No visible change; each file uses `@color/` tokens instead of hex literals

### Color Mapping Table (reference for all replacements)

| Hardcoded Hex | Token | Used In |
|---------------|-------|---------|
| `#F0F6FF` | `@color/text_primary` | Dashboards, transfers |
| `#7A9CC0` | `@color/text_secondary_dark` | Dashboards, dialogs |
| `#4A6A8A` | `@color/text_muted` | Hints, disabled text |
| `#00D4AA` | `@color/hero_accent` | Teal accent everywhere |
| `#1A2F50` / `#1A2D40` | `@color/glass_card_bg` | Card backgrounds |
| `#2A4A70` / `#2E4A62` | `@color/glass_card_border` | Card strokes |
| `#1C2B40` / `#1C2D40` | `@color/qa_blue_bg` | Icon backgrounds |
| `#14253A` | `@color/dash_bg_mid` | Elevated surfaces |
| `#1F3A5A` | `@color/glass_card_border` | Strokes |
| `#1E3A6E` | `@color/brand_dark_blue` | Button backgrounds |
| `#4ADE80` | `@color/pill_positive_text` | Green accent |
| `#0F3320` | `@color/pill_positive_bg` | Green pill bg |
| `#38BDF8` | `@color/qa_blue_icon` | Blue accent |
| `#A78BFA` | `@color/qa_purple_icon` | Purple accent |
| `#F59E0B` | `@color/qa_amber_icon` | Amber accent |
| `#FCD34D` | `@color/pill_warning_text` | Warning text |
| `#FB7185` | `@color/danger_text` | Red/danger |
| `#00000000` | `@android:color/transparent` | Transparent |
| `#9CA3AF` | `@color/text_secondary_dark` | Secondary labels |
| `#6B7280` | `@color/text_muted` | Muted text |
| `#374151` | `@color/text_secondary_dark` | Body text |
| `#111827` | `@color/text_primary` | Dark text on light → now on dark |
| `#FFFFFF` (text) | `@color/text_primary` | White text |
| `#FFFFFF` (card bg) | `@color/glass_card_bg` | Card backgrounds |

### Files to Update (ordered by hardcoded hex count, highest first)

#### Batch 2A — Dashboard Screens (155 hex literals → 0)
| File | Hex Count | Notes |
|------|-----------|-------|
| `activity_worker_dashboard.xml` | 43 | Map all 14 unique colors to tokens |
| `activity_student_dashboard.xml` | 38 | Already partially addressed in Phase 1J; finish remaining |
| `activity_multi_account_dashboard.xml` | 35 | Same palette as worker dashboard |
| `activity_business_owner_dashboard.xml` | 19 | Finish after Phase 1C fix |
| `activity_student_profile.xml` | 31 | Finish after Phase 1G conversion |

#### Batch 2B — Student Screens (118 hex literals → 0)
| File | Hex Count | Notes |
|------|-----------|-------|
| `activity_student_loans.xml` | 38+ | Finish after Phase 1F conversion |
| `activity_student_budget.xml` | 27 | Finish after Phase 1F conversion |
| `activity_student_subscription.xml` | 23 | Finish after Phase 1E conversion |
| `activity_student_event.xml` | 22 | Finish after Phase 1E conversion |

#### Batch 2C — Other Screens (133 hex literals → 0)
| File | Hex Count | Notes |
|------|-----------|-------|
| `activity_financial_reports.xml` | 31 | Finish after Phase 1H conversion |
| `activity_utility_manager.xml` | 17 | Map to dashboard tokens |
| `activity_saving_manager_function.xml` | 15 | Map to dashboard tokens |
| `activity_subscription_manager_function.xml` | 15 | Map to dashboard tokens |
| `activity_invoice_hub.xml` | 15 | Map to dashboard tokens |
| `dialog_add_expense.xml` | 12 | Map to dashboard tokens |
| `dialog_add_revenue.xml` | 12 | Map to dashboard tokens |
| `activity_transfer.xml` | 10 | Finish after Phase 1D fix |
| `activity_role_upgrade.xml` | 10 | Map to dashboard tokens |
| `activity_account_settings.xml` | 9 | Finish after Phase 1H conversion |
| `activity_loan_details.xml` | 9 | Finish after Phase 1I conversion |
| `dialog_notifications.xml` | 4 | Map to dashboard tokens |
| `activity_loan_calculator.xml` | 3 | Minor fix |
| `activity_loan_compare.xml` | 1 | Single button text |

#### Batch 2D — Goal Screens (align palette)
| File | Hex Count | Notes |
|------|-----------|-------|
| `activity_saving_details.xml` | 2 | Align goal_* tokens with main palette |
| `activity_saving_manager_function.xml` | (in 2C) | Already listed |

### Implementation Approach Per File
1. Open the file
2. For each hardcoded hex color, find the matching token from the mapping table
3. Replace inline hex with `@color/token_name`
4. Run `./gradlew assembleDebug` after each batch to verify no broken references

### Verification
After Phase 2:
- `grep -r "#[0-9A-Fa-f]\{6\}" app/src/main/res/layout/ | wc -l` should be ~0
- Build succeeds
- All screens look identical (no visual change)

---

## PHASE 3: Component Consistency

**Priority:** HIGH — Consistent interaction patterns across the app
**Risk Level:** LOW-MEDIUM — Component-level changes
**Expected Impact:** HIGH — App feels cohesive and professionally built

### 3A. Bottom Navigation — Unified Dark Style
- **Files:** All activities with `BottomNavigationView`
  - `activity_student_worker_hybrid_dashboard.xml` (already fixed in 1A)
  - `activity_worker_dashboard.xml`
  - `activity_business_owner_dashboard.xml`
  - `activity_multi_account_dashboard.xml`
  - `activity_student_dashboard.xml`
- **Changes:**
  - Background: `@color/bottom_nav_bg`
  - Active icon/text: `@color/bottom_nav_active`
  - Inactive icon/text: `@color/bottom_nav_inactive`
  - Use `@color/bottom_nav_color` color state list
- **Impact:** Unified navigation experience
- **Risk:** LOW

### 3B. Back Button — Single Implementation
- **Current:** 5 different back button implementations across the app
  - Some use `@drawable/ic_arrow_back`
  - Some use `@drawable/ic_back`
  - Some use a system back arrow
  - Some have no back button at all
- **Files to audit:** All activity layouts with back navigation
- **Change:**
  - Create a single `@drawable/ic_back_arrow` (24dp, white, Material-style)
  - Apply consistently: 40dp circle button with `@drawable/bg_back_circle`, `android:tint="@color/text_on_dark_primary"`, `contentDescription="@string/back"`
  - Standardize: `marginStart="16dp"`, `marginTop="48dp"` (below status bar)
- **Impact:** Consistent navigation affordance
- **Risk:** LOW

### 3C. Button Styles — Unified Palette
- **Current:** 5 different button colors: `#00D4AA`, `#4CAF50`, `#4ADE80`, `#1E60FF`, `#1E3A6E`
- **Target:**
  - Primary CTA: `@color/btn_primary` (#00D4AA) with `@style/Widget.App.Button.Teal`
  - Secondary: `@color/btn_secondary` (glass card) with `@style/Widget.App.Button.Outline.Dark`
  - Danger: `@color/btn_danger` (#FB7185) with danger style
  - Text-only: `@style/Widget.App.Button.Text.Dark`
- **Files to audit:** All button definitions across layouts
- **Impact:** Consistent call-to-action hierarchy
- **Risk:** LOW

### 3D. Card Component — Unified MaterialCardView
- **Current:** Mix of `CardView`, `MaterialCardView`, and bare `LinearLayout` with background
- **Change:**
  - All cards → `com.google.android.material.card.MaterialCardView`
  - Background: `@color/glass_card_bg`
  - Stroke: `@color/glass_card_border`, 1dp
  - Corner radius: 16dp (or 12dp for compact items)
  - Elevation: 0dp (flat dark theme)
  - Apply `@style/Widget.App.Card.Dark`
- **Files:** All layouts with card-like containers
- **Impact:** Consistent card language throughout app
- **Risk:** LOW-MEDIUM

### 3E. Dialog Styling
- **Files:** `dialog_add_expense.xml`, `dialog_add_revenue.xml`, `dialog_notifications.xml`
- **Changes:**
  - Background: `@color/dialog_bg` with 20dp corners
  - Add scrim: `@color/dialog_scrim`
  - Text colors: `@color/text_primary` for titles, `@color/text_secondary_dark` for body
  - Apply `@style/Theme.SmartFinance.Dialog` programmatically in Java/Kotlin
  - Button styling: primary teal for confirm, outline for cancel
- **Impact:** Professional dialog experience
- **Risk:** LOW

### 3F. Input Field Styling
- **Files:** All form screens (loan form, register, transfer, add goal, etc.)
- **Change:**
  - Background: `@color/input_bg` with `@color/input_stroke` border
  - Corner radius: 12dp
  - Text color: `@color/input_text`
  - Hint color: `@color/input_hint`
  - Focus state: border changes to `@color/hero_accent`
  - Create a shared drawable `bg_input_dark.xml` if needed
- **Impact:** Consistent form UX
- **Risk:** LOW

### 3G. Currency Symbol Unification
- **Files:** `activity_saving_add_goal.xml` (uses "$"), all others use "LKR"/"Rs."
- **Change:** Replace `$` with `Rs.` or `LKR` consistently. Ideally create a `@string/currency_format` string resource.
- **Impact:** Professional currency handling
- **Risk:** LOW

### Verification
After Phase 3:
- Navigate through every screen and verify bottom nav, back buttons, cards, dialogs all look consistent
- Test form input focus states
- Verify currency displays consistently

---

## PHASE 4: Typography & Spacing System

**Priority:** MEDIUM — Refinement that elevates the entire experience
**Risk Level:** LOW — Mostly resource additions
**Expected Impact:** MEDIUM-HIGH — App feels structured and intentional

### 4A. Typography Scale
- **File:** Create `app/src/main/res/values/typography.xml` (or extend themes.xml)
- **Current:** 20+ text sizes from 9sp to 36sp with no system
- **Target Scale:**

```xml
<!-- Display / Hero -->
<style name="TextAppearance.App.Display" parent="TextAppearance.MaterialComponents.Headline1">
    <item name="android:textSize">28sp</item>
    <item name="android:fontFamily">sans-serif-medium</item>
    <item name="android:textColor">@color/text_primary</item>
</style>

<!-- Headline -->
<style name="TextAppearance.App.Headline" parent="TextAppearance.MaterialComponents.Headline2">
    <item name="android:textSize">24sp</item>
    <item name="android:fontFamily">sans-serif-medium</item>
    <item name="android:textColor">@color/text_primary</item>
</style>

<!-- Title Large -->
<style name="TextAppearance.App.TitleLarge" parent="TextAppearance.MaterialComponents.Headline4">
    <item name="android:textSize">20sp</item>
    <item name="android:fontFamily">sans-serif-medium</item>
    <item name="android:textColor">@color/text_primary</item>
</style>

<!-- Title -->
<style name="TextAppearance.App.Title" parent="TextAppearance.MaterialComponents.Headline5">
    <item name="android:textSize">16sp</item>
    <item name="android:fontFamily">sans-serif-medium</item>
    <item name="android:textColor">@color/text_primary</item>
</style>

<!-- Body Large -->
<style name="TextAppearance.App.BodyLarge">
    <item name="android:textSize">16sp</item>
    <item name="android:textColor">@color/text_primary</item>
</style>

<!-- Body -->
<style name="TextAppearance.App.Body">
    <item name="android:textSize">14sp</item>
    <item name="android:textColor">@color/text_primary</item>
</style>

<!-- Caption -->
<style name="TextAppearance.App.Caption">
    <item name="android:textSize">12sp</item>
    <item name="android:textColor">@color/text_secondary_dark</item>
</style>

<!-- Small -->
<style name="TextAppearance.App.Small">
    <item name="android:textSize">11sp</item>
    <item name="android:textColor">@color/text_muted</item>
</style>
```

### 4B. Apply Typography to Layouts
- **Approach:** Replace inline `android:textSize` + `android:textColor` combos with `android:textAppearance="@style/TextAppearance.App.Body"` etc.
- **Priority files:** All 85 layout files
- **Time-saving:** Do this alongside Phase 2 (when you're already editing each file for color tokens)

### 4C. Spacing System
- **Current:** Padding/margin varies between 8dp, 12dp, 16dp, 20dp, 24dp, 32dp
- **Target:** Standardize to 4dp grid
  - `4dp` — micro (icon padding)
  - `8dp` — small (inline elements)
  - `12dp` — compact (chip/pill padding)
  - `16dp` — default (screen margins, card padding)
  - `20dp` — comfortable (section spacing)
  - `24dp` — spacious (major section breaks)
  - `32dp` — hero (top spacing, large gaps)
  - `48dp` — above-the-fold (below status bar)
- **Implementation:** Add dimension resources in `res/values/dimens.xml`:
  ```xml
  <dimen name="spacing_xs">4dp</dimen>
  <dimen name="spacing_sm">8dp</dimen>
  <dimen name="spacing_md">12dp</dimen>
  <dimen name="spacing_default">16dp</dimen>
  <dimen name="spacing_lg">20dp</dimen>
  <dimen name="spacing_xl">24dp</dimen>
  <dimen name="spacing_xxl">32dp</dimen>
  <dimen name="spacing_hero">48dp</dimen>
  ```
- **Priority:** Apply during Phase 2 edits. Focus on the worst offenders (student screens with mixed spacing).

### 4D. Hybrid Dashboard Typography Compression Fix
- **File:** `activity_student_worker_hybrid_dashboard.xml`
- **Issue:** Text sizes 1-3sp smaller than other dashboards
- **Change:** Audit all text sizes and match them to worker/business dashboard equivalents
- **Impact:** Removes compressed feel
- **Risk:** LOW

### Verification
- Visually compare text sizes across dashboards on device
- Check that spacing looks even and consistent

---

## PHASE 5: Savings Passport & Goal Module Alignment

**Priority:** MEDIUM — Special-case modules that need careful handling
**Risk Level:** MEDIUM — These have unique visual identities
**Expected Impact:** MEDIUM — Consistent module feel without losing brand identity

### 5A. Goal Module — Palette Alignment
- **Files:** `activity_saving_manager.xml`, `activity_saving_list.xml`, `activity_saving_details.xml`
- **Current:** Uses `goal_bg` (#090D1A) which is close to but different from `dash_bg_deep` (#0A1628)
- **Change:** Decision needed:
  - **Option A:** Merge goal colors into dashboard palette (change `goal_bg` → `dash_bg_deep`, `goal_surface` → `glass_card_bg`, etc.)
  - **Option B:** Keep goal-specific palette but align accents (`goal_mint` is similar to `hero_accent`)
- **Recommendation:** Option A — merge into main palette. The visual difference between #090D1A and #0A1628 is imperceptible to users but creates maintenance burden.
- **Impact:** Fewer unique colors to maintain
- **Risk:** MEDIUM — May subtly change the goal screens' look

### 5B. Savings Passport — Keep Unique Theme, But...
- **File:** `activity_savings_passport.xml`
- **Current:** Parchment/brass theme — completely unique
- **Change:** Keep the unique brand identity (it's a passport metaphor that works), but:
  1. Replace the single hardcoded `#B3FFFFFF` with a `@color/` token
  2. Ensure the top header area uses a dark navy bar to visually connect with the rest of the app
  3. Consider adding the standard back button from Phase 3B
- **Impact:** Passport stays unique but connects to the app's navigation patterns
- **Risk:** LOW

### Verification
- Savings flow still looks premium and distinct
- Goal screens now share the same dark palette

---

## PHASE 6: Polish & Hardcoded Data Cleanup

**Priority:** LOW-MEDIUM — Final refinement
**Risk Level:** LOW
**Expected Impact:** MEDIUM — Removes leftover prototype artifacts

### 6A. Replace Hardcoded Sample Data with `tools:text`
- **Files:** All layouts with hardcoded user data
  - `activity_student_dashboard.xml`: "Aiden Ramirez", "AR"
  - `activity_student_worker_hybrid_dashboard.xml`: "H", "studentworker@hybrid.com"
  - `activity_business_owner_dashboard.xml`: "business@email.com"
  - `activity_student_loans.xml`: "Student Loan", "Bank of Ceylon", "Rs.245,000"
  - `activity_student_budget.xml`: "Rs. 168,000", "Rs.933"
  - `activity_student_subscription.xml`: "Netflix Rs.1490", "Spotify Rs.399"
  - `activity_student_event.xml`: Goal names, dates, tips
- **Change:** Replace `android:text="value"` with `tools:text="value"` for all mock data
- **Impact:** Clean XML, no visual change (tools:text only shows in design preview)
- **Risk:** LOW

### 6B. Remove Dead Layout Code
- **Files:**
  - `activity_student_loans.xml`: Duplicate `cardEmiAlertStatic` (appears to be dead code)
  - `activity_business_owner_dashboard.xml`: `recentSection` with `visibility="gone"`
  - `activity_student_event.xml`: Duplicate goal cards
- **Impact:** Cleaner XML
- **Risk:** LOW (verify in Java/Kotlin that no code references these)

### 6C. Accessibility Improvements
- **Add to all icon-only buttons:** `android:contentDescription="@string/..."` 
- **Create string resources:** `@string/back`, `@string/close`, `@string/logout`, etc.
- **Impact:** Accessibility compliance
- **Risk:** LOW

### 6D. Deprecated API Cleanup
- Replace `androidx.cardview.widget.CardView` → `com.google.android.material.card.MaterialCardView`
- Replace `Switch` → `SwitchCompat` or `SwitchMaterial`
- Replace `android:layout_alignParentRight` → `android:layout_alignParentEnd`
- Replace deprecated `app:tint` → `app:iconTint` where applicable
- **Impact:** Forward compatibility
- **Risk:** LOW

### 6E. Remove `tools:context` Errors
- Fix `activity_student_budget.xml`: `tools:context=".MainActivity"` → correct activity
- **Impact:** Clean Android Studio previews
- **Risk:** LOW

---

## Implementation Roadmap

```
Week 1:  Phase 0 (Foundation) + Phase 1 (Critical Fixes)
         → Build & test every affected screen
         
Week 2:  Phase 2 Batch A+B (Dashboard + Student screens)
         → These are the highest-impact files
         
Week 3:  Phase 2 Batch C+D (Other screens + Goal alignment)
         + Phase 3 (Component consistency)
         → Build & full regression test
         
Week 4:  Phase 4 (Typography + Spacing)
         + Phase 5 (Savings/Goal modules)
         + Phase 6 (Polish & cleanup)
         → Final QA pass
```

---

## Risk Mitigation

1. **Build after every phase** — `./gradlew assembleDebug` must pass before moving on
2. **Test on device** — Emulators may not render all fonts/textures correctly
3. **One screen at a time** — Never batch-modify without verifying the individual screen
4. **Keep backup branch** — Create `ui-modernization` branch before starting
5. **Gradual rollout** — Can merge phase by phase; each phase is independently valuable

---

## Expected Outcomes

| Metric | Before | After |
|--------|--------|-------|
| Visual themes | 5 | 1 (+ passport as special case) |
| Hardcoded hex colors | ~400+ | 0 |
| Inconsistent button colors | 5 different greens/blues | 3 semantic styles (primary/secondary/danger) |
| Typography scale | 20+ sizes | 7 standardized levels |
| BottomNav style | White on dark (hybrid) | Consistent dark on all screens |
| Card components | Mix of CardView/MaterialCardView/LinearLayout | Unified MaterialCardView |
| Dialog styling | Raw/unstyled | Rounded glass cards with scrim |
| Spinner readability | Invisible on dark bg | Fully readable |
| Currency display | Mix of $, Rs., LKR | Single unified currency format |
| Registration progress | Nearly invisible | Clearly visible |
| Overall impression | Fragmented prototype | Cohesive premium fintech app |

---

*This plan is designed to be executed incrementally. Each phase produces a buildable, testable state. No phase requires a complete rewrite of any screen.*
