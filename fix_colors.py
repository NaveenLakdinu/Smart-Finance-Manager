#!/usr/bin/env python3
"""
Smart Finance Manager — Color Mismatch Fix Script
Replaces ALL hardcoded hex colors in layout XML files with @color/ tokens.
Skips: savings_passport (intentional light theme), transparent variants.
"""

import os, re, sys

LAYOUT_DIR = "app/src/main/res/layout"

# Files to NEVER touch (intentional light/special theme)
SKIP_FILES = {
    "activity_savings_passport.xml",
}

# ── Complete color mapping ─────────────────────────────────────────────────────
# Key = hardcoded hex (lowercase), Value = @color/ token
COLOR_MAP = {
    # ── Old purple palette (direct purple tokens) ──
    "#3a2cc5": "@color/surface_primary",       # purple deep bg → forest_900
    "#2d1eb0": "@color/forest_800",            # purple mid bg → forest_800
    "#4a3af5": "@color/forest_700",            # purple card → forest_700
    "#5b4bf7": "@color/forest_600",            # purple border → forest_600
    "#7c6fe0": "@color/forest_300",            # purple accent → sage green
    "#b0a8ff": "@color/text_on_dark_secondary",# purple text secondary → forest_300
    "#8b7fff": "@color/text_on_dark_muted",    # purple text muted
    "#9b8bfa": "@color/forest_300",            # purple tint → sage

    # ── Near-black / dark navy (light-themed screens going dark) ──
    "#111827": "@color/forest_900",            # Tailwind gray-900 → deepest forest
    "#1b1e28": "@color/forest_900",            # dark navy → forest_900
    "#1e293b": "@color/forest_800",            # slate-800 → forest_800
    "#374151": "@color/forest_700",            # gray-700 → card surface
    "#1f2937": "@color/forest_800",            # gray-800 → forest_800
    "#4b5563": "@color/text_on_dark_secondary",# gray-600 → sage secondary text
    "#6b7280": "@color/text_on_dark_muted",    # gray-500 → muted text
    "#9ca3af": "@color/text_on_dark_muted",    # gray-400 → muted text
    "#d1d5db": "@color/text_on_dark_secondary",# gray-300 → secondary
    "#e5e7eb": "@color/forest_600",            # gray-200 → stroke/border
    "#f3f4f6": "@color/forest_700",            # gray-100 → card surface
    "#f8fafc": "@color/forest_800",            # near white bg → forest_800
    "#f0f4f8": "@color/forest_900",            # light blue-gray → deepest bg
    "#f0f4fa": "@color/forest_900",
    "#f8faff": "@color/forest_900",

    # ── Blues used as accent (now sage green) ──
    "#2a5eaa": "@color/forest_600",            # medium blue → forest_600
    "#1c5cfa": "@color/forest_300",            # bright blue → sage accent
    "#1a6af6": "@color/forest_300",            # bright blue → sage accent
    "#1a3050": "@color/forest_700",            # dark navy-blue → card
    "#1d4ed8": "@color/forest_300",            # blue-700 → sage accent
    "#1a4080": "@color/forest_600",            # dark blue → forest_600
    "#5787f6": "@color/forest_300",            # medium blue → sage
    "#60a5fa": "@color/forest_300",            # blue-400 → sage
    "#bbdefb": "@color/text_on_dark_secondary",# light blue → secondary text
    "#dbeafe": "@color/forest_600",            # blue tint → border
    "#e8f0fe": "@color/forest_700",            # pale blue → card
    "#b0c6e8": "@color/text_on_dark_secondary",# blue-gray text → secondary

    # ── Purples/violets used as accent ──
    "#9333ea": "@color/forest_300",            # purple-600 → sage accent
    "#7c3aed": "@color/forest_300",            # violet-600 → sage accent

    # ── Light card backgrounds (were on white screens) ──
    "#ebf1f6": "@color/forest_700",            # pale blue-gray → card
    "#f3f0ff": "@color/forest_700",            # pale purple → card
    "#fff5f5": "@color/danger_bg",             # pale red bg → danger_bg token
    "#ffd1d1": "@color/danger_bg",             # pale red → danger_bg

    # ── Hint / muted text ──
    "#4a607a": "@color/text_on_dark_muted",    # blue-gray hint → muted
    "#527196": "@color/text_on_dark_muted",    # slate hint → muted
    "#52759a": "@color/text_on_dark_muted",    # blue hint → muted
    "#94a3b8": "@color/text_on_dark_muted",    # slate-400 → muted
    "#cbd5e1": "@color/text_on_dark_secondary",# slate-300 → secondary
    "#5a6470": "@color/text_on_dark_muted",    # blue-gray → muted

    # ── Blue-tinted text on light backgrounds ──
    "#bce0ff": "@color/text_on_dark_secondary",# light blue text → secondary
    "#111111": "@color/text_on_dark_primary",  # near-black → white

    # ── Action greens (non-danger) ──
    "#10b981": "@color/forest_300",            # emerald-500 → sage green
    "#16a34a": "@color/forest_300",            # green-600 → sage green

    # ── Specific elements ──
    "#20000000": "#20000000",                  # keep semi-transparent overlay
    "#331a1a":   "@color/danger_bg",           # dark red tint → danger
    "#000000":   "@color/forest_900",          # black → deepest forest
}

# Colors that appear in valid semantic contexts — DO NOT remap
WHITELIST_PARTIAL = {
    "#20000000",   # semi-transparent overlay on calculator — keep
}

def fix_file(filepath):
    with open(filepath, "r", encoding="utf-8") as f:
        original = f.read()

    content = original

    for hex_color, token in COLOR_MAP.items():
        if token == hex_color:          # whitelist — skip
            continue
        # Match case-insensitively, whole color value
        pattern = re.compile(re.escape(hex_color), re.IGNORECASE)
        content = pattern.sub(token, content)

    if content != original:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        return True
    return False


def main():
    base = os.path.join(os.path.dirname(os.path.abspath(__file__)), LAYOUT_DIR)
    changed = []
    skipped = []

    for fname in sorted(os.listdir(base)):
        if not fname.endswith(".xml"):
            continue
        if fname in SKIP_FILES:
            skipped.append(fname)
            continue

        fpath = os.path.join(base, fname)
        if fix_file(fpath):
            changed.append(fname)
            print(f"  ✅ Fixed: {fname}")
        else:
            print(f"  ── Clean: {fname}")

    print(f"\n{'='*60}")
    print(f"Fixed  : {len(changed)} files")
    print(f"Clean  : {len(os.listdir(base)) - len(changed) - len(skipped)} files")
    print(f"Skipped: {len(skipped)} files (intentional theme)")
    if skipped:
        for s in skipped:
            print(f"  ⏭  {s}")


if __name__ == "__main__":
    main()
