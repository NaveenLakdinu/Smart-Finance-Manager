#!/usr/bin/env python3
import os
import re

ROOT_DIR = "app/src/main"

# Mapping of old names (without extensions) to new names
RENAME_MAP = {
    # Drawables
    "icon_role_blue": "icon_role_secondary",
    "icon_role_purple": "icon_role_tertiary",
    "bg_function_icon_blue": "bg_function_icon_secondary",
    "tag_blue": "tag_secondary",
    "tag_purple": "tag_tertiary",
    "bg_circle_blue": "bg_circle_secondary",
    "icon_circle_blue": "icon_circle_secondary",
    "icon_circle_purple": "icon_circle_tertiary",
    "bg_pill_purple": "bg_pill_tertiary",
    "bg_circular_progress_blue": "bg_circular_progress_secondary",
    "ob_icon_blue": "ob_icon_secondary",
    "ob_icon_purple": "ob_icon_tertiary",
    
    # Colors
    "button_blue": "button_secondary",
    "button_light_blue": "button_light_secondary",
    "brand_dark_blue": "brand_dark_secondary",
    "action_blue_bg": "action_secondary_bg",
    "action_blue_icon": "action_secondary_icon",
    "qa_blue_bg": "qa_secondary_bg",
    "qa_blue_icon": "qa_secondary_icon",
    "qa_purple_bg": "qa_tertiary_bg",
    "qa_purple_icon": "qa_tertiary_icon",
    "goal_blue": "goal_secondary",
    "pill_purple_bg": "pill_tertiary_bg",
    "pill_purple_border": "pill_tertiary_border",
    "pill_blue_bg": "pill_secondary_bg",
    "pill_blue_text": "pill_secondary_text",
}

# Hex color direct replacements for remaining blue/purple hardcoded java/xml
HEX_REPLACE = {
    "#5BA8D4": "@color/forest_300",
    "#9B8BFA": "@color/forest_300",
    "#1A3040": "@color/forest_800",
    "#5B8CFF": "@color/forest_300",
    "#223E66": "@color/forest_700",
}

# Rename drawable files first
drawable_dir = os.path.join(ROOT_DIR, "res/drawable")
if os.path.exists(drawable_dir):
    for filename in os.listdir(drawable_dir):
        base, ext = os.path.splitext(filename)
        if base in RENAME_MAP:
            old_path = os.path.join(drawable_dir, filename)
            new_path = os.path.join(drawable_dir, RENAME_MAP[base] + ext)
            os.rename(old_path, new_path)
            print(f"Renamed: {filename} -> {RENAME_MAP[base] + ext}")

# Now find and replace in all .xml and .java files
for root, dirs, files in os.walk(ROOT_DIR):
    for filename in files:
        if filename.endswith(".xml") or filename.endswith(".java"):
            filepath = os.path.join(root, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
            
            new_content = content
            
            # Replace all occurrences of old names with new names
            for old_name, new_name in RENAME_MAP.items():
                # Word boundary match to avoid partial replacements
                pattern = r'\b' + re.escape(old_name) + r'\b'
                new_content = re.sub(pattern, new_name, new_content)
                
            # Replace hex codes
            for old_hex, new_hex in HEX_REPLACE.items():
                pattern = re.compile(re.escape(old_hex), re.IGNORECASE)
                # If it's a java file, and we are replacing with a color resource, this logic might be flawed
                # But looking at where they are used:
                # "#5B8CFF" in ConfettiView.java (Color.parseColor) -> needs valid hex. We'll use "#4ade80" (forest_300 hex)
                # "#223E66" in LoanReportActivity.java -> needs valid hex. We'll use "#163832" (forest_700 hex)
                # "#5BA8D4" & "#9B8BFA" in colors.xml -> can be @color/...
                
                # Let's adjust Java hex replacements specifically
                if filename.endswith(".java"):
                    if old_hex.upper() == "#5B8CFF":
                        new_content = re.sub(pattern, "#4ade80", new_content)
                    elif old_hex.upper() == "#223E66":
                        new_content = re.sub(pattern, "#163832", new_content)
                else:
                    new_content = re.sub(pattern, new_hex, new_content)
                    
            # Handle Color.blue in Java
            if filename.endswith(".java"):
                # "android.R.color.holo_blue_dark" -> "android.R.color.holo_green_dark"
                new_content = new_content.replace("holo_blue_dark", "holo_green_dark")
            
            if new_content != content:
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(new_content)
                print(f"Updated content in: {filepath}")

print("Done purging blue and purple references.")
