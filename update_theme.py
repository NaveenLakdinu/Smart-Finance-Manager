import os
import re

base_dir = "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager"
files = [
    "app/src/main/res/layout/activity_worker_payslip.xml",
    "app/src/main/res/layout/activity_worker_tasks.xml",
    "app/src/main/res/layout/item_task.xml",
    "app/src/main/res/layout/activity_expense_claims.xml",
    "app/src/main/res/layout/activity_expense_claim_add.xml",
    "app/src/main/res/layout/activity_expense_claim_list.xml",
    "app/src/main/res/layout/activity_expense_claim_history.xml",
    "app/src/main/res/layout/item_expense_claim.xml",
    "app/src/main/res/layout/activity_invoice_hub.xml",
    "app/src/main/res/layout/activity_invoice_detail.xml",
    "app/src/main/res/layout/activity_create_invoice.xml",
    "app/src/main/res/layout/activity_business_analytic.xml",
    "app/src/main/res/layout/item_business_invoice.xml",
    "app/src/main/res/layout/activity_student_budget.xml",
    "app/src/main/res/layout/activity_student_subscription.xml",
    "app/src/main/res/layout/activity_student_profile.xml",
    "app/src/main/res/layout/fragment_student_profile.xml",
    "app/src/main/res/layout/activity_role_upgrade.xml",
    "app/src/main/res/layout/activity_account_settings.xml",
    "app/src/main/res/layout/dialog_add_expense.xml",
    "app/src/main/res/layout/dialog_add_expense_claim.xml",
    "app/src/main/res/layout/dialog_add_revenue.xml",
    "app/src/main/res/layout/dialog_add_task.xml",
    "app/src/main/res/layout/dialog_generate_payslip.xml",
    "app/src/main/res/layout/dialog_transfer_confirm.xml",
    "app/src/main/res/layout/item_dashboard_transaction.xml",
    "app/src/main/res/layout/item_transfer.xml",
    "app/src/main/res/layout/item_summary_bill.xml",
    "app/src/main/res/layout/activity_financial_reports.xml",
    "app/src/main/res/layout/activity_transfer.xml",
    "app/src/main/res/layout/activity_subscription_report.xml"
]

def replace_hex(match):
    color = match.group(0).upper()
    purples = ['#3A2CC5', '#2D1EB0', '#4A3AF5', '#5B4BF7', '#7C6FE0', '#B0A8FF', '#8B7FFF', '#1E60FF', '#448BFF', '#1E3D7A', '#1A4080', '#B0C6E8', '#7C3AED', '#8B5CF6']
    if color in purples:
        return '@color/forest_300'
    return match.group(0)

def update_card(match):
    card = match.group(0)
    if 'app:cardBackgroundColor' not in card:
        card = card.replace('>', '\n        app:cardBackgroundColor="@color/forest_700">', 1)
    else:
        card = re.sub(r'app:cardBackgroundColor="[^"]+"', 'app:cardBackgroundColor="@color/forest_700"', card)
        
    if 'app:strokeColor' not in card:
        card = card.replace('>', '\n        app:strokeColor="@color/forest_600">', 1)
    else:
        card = re.sub(r'app:strokeColor="[^"]+"', 'app:strokeColor="@color/forest_600"', card)
        
    if 'app:strokeWidth' not in card:
        card = card.replace('>', '\n        app:strokeWidth="1dp">', 1)
    
    if 'app:cardElevation' not in card:
        card = card.replace('>', '\n        app:cardElevation="0dp">', 1)
    else:
        card = re.sub(r'app:cardElevation="[^"]+"', 'app:cardElevation="0dp"', card)
        
    return card

for f_name in files:
    f_path = os.path.join(base_dir, f_name)
    if not os.path.exists(f_path):
        print(f"Skipping {f_name} (not found)")
        continue
        
    with open(f_path, "r") as f:
        content = f.read()
        
    orig_content = content
    
    # 1. MaterialCardView
    content = re.sub(r'<com\.google\.android\.material\.card\.MaterialCardView[^>]+>', update_card, content)
    
    # 2. Hex colors replacement for purples
    content = re.sub(r'#[0-9a-fA-F]{6,8}', replace_hex, content)
    
    # 3. Replace @color/purple_ and @color/blue_ with @color/forest_300
    content = re.sub(r'@color/purple_[0-9]+', '@color/forest_300', content)
    content = re.sub(r'@color/blue_[0-9]+', '@color/forest_300', content)
    
    # 4. CTA buttons -> @drawable/bg_btn_primary_green, text -> @color/btn_primary_text
    content = re.sub(r'android:background="@drawable/btn_login_gradient"', 'android:background="@drawable/bg_btn_primary_green"', content)
    content = re.sub(r'android:background="@drawable/bg_btn_primary"', 'android:background="@drawable/bg_btn_primary_green"', content)
    content = re.sub(r'android:textColor="@color/text_on_dark_primary"(\s*.*(?:btn|Button))', r'android:textColor="@color/btn_primary_text"\1', content)
    # Generic button text colors inside buttons
    # Since regex is basic, we will specifically target standard button patterns
    
    # 5. Amounts -> @color/forest_100
    # Find text views for amounts usually having id *Amount or *Salary or *Net or *Gross
    # This is tricky with regex, we can do manual check for payslip
    if "activity_worker_payslip" in f_name:
        content = content.replace('android:textColor="@color/text_on_dark_primary"\n                        android:textSize="32sp"', 'android:textColor="@color/forest_100"\n                        android:textSize="32sp"')
        
    # 6. Specifics for Tasks
    if "activity_worker_tasks" in f_name or "item_task" in f_name:
        content = content.replace('@drawable/tag_blue', '@drawable/bg_pill_success_green')
        
    # 7. Progress bars -> @drawable/bg_progress_green
    content = content.replace('@drawable/bg_progress', '@drawable/bg_progress_green')
    content = content.replace('@drawable/bg_progress_blue', '@drawable/bg_progress_green')
    
    # 8. Dialog specific root bg -> @color/forest_700
    if "dialog_" in f_name:
        content = re.sub(r'android:background="@drawable/bg_dialog[^"]*"', 'android:background="@color/forest_700"', content)
        content = re.sub(r'android:background="@color/surface[^"]*"', 'android:background="@color/forest_700"', content)
        
    if content != orig_content:
        with open(f_path, "w") as f:
            f.write(content)
        print(f"Updated {f_name}")
    else:
        print(f"No changes needed for {f_name}")

print("\nDone! Please verify the changes.")
