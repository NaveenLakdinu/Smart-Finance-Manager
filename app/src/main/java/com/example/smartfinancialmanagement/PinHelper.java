package com.example.smartfinancialmanagement;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * PinHelper – manages PIN storage and verification in SharedPreferences.
 * The PIN is stored as a simple hashed string to avoid plaintext storage.
 */
public class PinHelper {

    private static final String PREFS_NAME  = "PinPrefs";
    private static final String KEY_PIN     = "user_pin";
    private static final String KEY_PIN_SET = "pin_is_set";

    // ─── Public API ──────────────────────────────────────────────────────────

    /** Returns true if the user has set up a PIN. */
    public static boolean isPinSet(Context context) {
        return prefs(context).getBoolean(KEY_PIN_SET, false);
    }

    /**
     * Saves the PIN (after a simple hash) and marks it as enabled.
     *
     * @param context Application context.
     * @param pin     Raw numeric PIN string entered by the user.
     */
    public static void savePin(Context context, String pin) {
        prefs(context).edit()
                .putString(KEY_PIN, hash(pin))
                .putBoolean(KEY_PIN_SET, true)
                .apply();
    }

    /**
     * Validates the entered PIN against the stored hash.
     *
     * @param context     Application context.
     * @param enteredPin  Raw numeric PIN string entered by the user.
     * @return true if the entered PIN matches the stored PIN.
     */
    public static boolean verifyPin(Context context, String enteredPin) {
        String stored = prefs(context).getString(KEY_PIN, null);
        if (stored == null) return false;
        return stored.equals(hash(enteredPin));
    }

    /**
     * Removes the stored PIN and disables the PIN lock.
     *
     * @param context Application context.
     */
    public static void clearPin(Context context) {
        prefs(context).edit()
                .remove(KEY_PIN)
                .putBoolean(KEY_PIN_SET, false)
                .apply();
    }

    // ─── Internals ───────────────────────────────────────────────────────────

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Minimal hash using Java's built-in hashCode to avoid plain-text storage.
     * For production apps, consider using BCrypt or Android Keystore.
     */
    private static String hash(String pin) {
        return String.valueOf(pin.hashCode());
    }
}
