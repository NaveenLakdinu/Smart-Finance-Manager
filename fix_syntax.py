import os
import re

FILES = [
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/DashboardActivity.java",
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/WorkerDashboardActivity.java",
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/MultiAccountDashboardActivity.java"
]

BAD_CODE = """        View btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationPanelDialog());
        }"""

for filepath in FILES:
    if not os.path.exists(filepath):
        continue
    with open(filepath, 'r') as f:
        content = f.read()

    # Find the bad code block that's outside the method
    # It looks like:
    #     }
    #         View btnNotifications = findViewById(R.id.btnNotifications);
    #         if (btnNotifications != null) {
    #             btnNotifications.setOnClickListener(v -> showNotificationPanelDialog());
    #         }
    
    # We will remove it from there
    content = content.replace(BAD_CODE, "")

    # And insert it right before the last closing brace of onCreate()
    # Let's find onCreate using re
    
    match = re.search(r'(protected void onCreate\(Bundle savedInstanceState\) \{[\s\S]*?)(\n\s*\})', content)
    if match:
        new_onCreate = match.group(1) + "\n" + BAD_CODE + match.group(2)
        content = content[:match.start()] + new_onCreate + content[match.end():]

    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Fixed {filepath}")
