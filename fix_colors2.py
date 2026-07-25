#!/usr/bin/env python3
"""Second-pass fix for remaining stragglers not caught by first pass."""
import os, re

LAYOUT_DIR = "app/src/main/res/layout"
SKIP_FILES = {"activity_savings_passport.xml"}

# These are the remaining hardcoded colors found after first pass
COLOR_MAP2 = {
    # Amber/warning used as accent text
    "#22c55e": "@color/forest_300",           # green-500 → sage green (terms checkbox)
    "#06b6d4": "@color/forest_300",           # cyan → sage green (add income button)
    "#4caf50": "@color/forest_300",           # material green → sage (transfer button)
    "#ffb800": "@color/pill_warning_text",    # amber → warning text (invoice)

    # Dark navy card backgrounds
    "#0f1e36": "@color/forest_800",           # dark navy → forest_800
    "#122544": "@color/forest_700",           # dark navy → card surface
    "#0a1d37": "@color/forest_800",           # dark navy → forest_800
    "#2a1215": "@color/danger_bg",            # dark red → danger_bg
    "#222222": "@color/forest_800",           # near black → forest_800
    "#444444": "@color/forest_600",           # dark gray → forest_600 border

    # Light purples in Compose/student screens
    "#e0d4ff": "@color/forest_700",           # pale purple → card bg
    "#6d28d9": "@color/forest_300",           # violet → sage green
    "#3b82f6": "@color/forest_300",           # blue-500 → sage green (stroke)
    "#ff3333": "@color/danger_text",          # bright red → danger_text token

    # Light-themed text in student screens that switched to dark
    "#e0e8ff": "@color/text_on_dark_secondary",# pale blue text → secondary
    "#757575": "@color/text_on_dark_muted",   # gray → muted
    "#e0e0e0": "@color/forest_300",           # light gray thumb → sage
    "#f5f5f5": "@color/forest_600",           # near white track → forest_600
    "#d8e4ff": "@color/text_on_dark_secondary",# pale blue → secondary
    "#e0e7ff": "@color/text_on_dark_secondary",# pale lavender → secondary

    # Dark text on light screens that are now dark
    "#1e40af": "@color/forest_300",           # blue-800 → sage accent
    "#0f172a": "@color/text_on_dark_primary", # slate-900 → white primary
    "#64748b": "@color/text_on_dark_muted",   # slate-500 → muted
    "#475569": "@color/text_on_dark_muted",   # slate-600 → muted

    # Semantic status colors in student profile (keep as semantic)
    "#9b1c1c": "@color/danger_text",          # red dark text → danger_text
    "#92400e": "@color/pill_warning_text",    # amber dark → warning text
    "#065f46": "@color/forest_300",           # emerald dark → sage

    # Amber accent in student event
    "#d97706": "@color/pill_warning_text",    # amber-600 → warning text token
    "#f59e0b": "@color/pill_warning_text",    # amber-400 → warning text
    "#ea580c": "@color/pill_warning_text",    # orange-600 → warning text
}

def fix_file(fpath):
    with open(fpath, "r", encoding="utf-8") as f:
        original = f.read()
    content = original
    for hex_color, token in COLOR_MAP2.items():
        pattern = re.compile(re.escape(hex_color), re.IGNORECASE)
        content = pattern.sub(token, content)
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

print(f"\nFixed {fixed} files in second pass.")
