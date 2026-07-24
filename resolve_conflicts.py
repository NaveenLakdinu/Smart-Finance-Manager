import sys
import re
import os

files_to_check = [
    'app/src/main/res/layout/activity_student_budget.xml',
    'app/src/main/res/layout/activity_student_dashboard.xml',
    'app/src/main/res/layout/activity_financial_reports.xml'
]

for filepath in files_to_check:
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Match conflict blocks and keep HEAD version
        pattern = re.compile(r'<<<<<<< HEAD\n(.*?)\n=======\n.*?\n>>>>>>> [^\n]*\n', re.DOTALL)
        new_content, count = pattern.subn(r'\1\n', content)
        
        if count > 0:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Resolved {count} conflicts in {filepath}")
        else:
            print(f"No conflicts found in {filepath}")
