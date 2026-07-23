#!/usr/bin/env python3
"""Third-pass: fix final stragglers — reds, blues in main/item files."""
import os, re

LAYOUT_DIR = "app/src/main/res/layout"
SKIP_FILES = {"activity_savings_passport.xml"}

COLOR_MAP3 = {
    # Red danger icons/text (delete buttons, error states) → danger_text token
    "#f87171": "@color/danger_text",   # red-400
    "#ef4444": "@color/danger_text",   # red-500

    # Blue splash/main screen elements → forest green equivalents
    "#4a90d9": "@color/forest_300",    # medium blue bg → sage green
    "#5599cc": "@color/forest_300",    # blue shadow → sage
    "#aaccff": "@color/forest_300",    # light blue icon tint → sage
    "#1e3d7a": "@color/forest_800",    # dark blue text → forest_800
    "#2d5a9e": "@color/forest_600",    # medium blue icon → forest_600

    # Cyan icon in utility → sage green
    "#38bdf8": "@color/forest_300",    # sky-400 → sage

    # Amber in student loans → warning token
    "#ca8a04": "@color/pill_warning_text",  # amber-600 → warning text
}

def fix_file(fpath):
    with open(fpath, "r", encoding="utf-8") as f:
        original = f.read()
    content = original
    for hex_color, token in COLOR_MAP3.items():
        # Don't touch tools: namespace attributes (IDE-only)
        lines = content.split('\n')
        new_lines = []
        for line in lines:
            if 'tools:background' in line or 'tools:' in line:
                new_lines.append(line)
                continue
            pattern = re.compile(re.escape(hex_color), re.IGNORECASE)
            new_lines.append(pattern.sub(token, line))
        content = '\n'.join(new_lines)
    if content != original:
        with open(fpath, "w", encoding="utf-8") as f:
            f.write(content)
        return True
    return False

base = LAYOUT_DIR
fixed = 0
for fname in sorted(os.listdir(base)):
    if not fname.endswith(".xml") or fname in SKIP_FILES:
        continue
    fpath = os.path.join(base, fname)
    if fix_file(fpath):
        print(f"  ✅ Fixed: {fname}")
        fixed += 1

print(f"\nFixed {fixed} files in third pass.")
