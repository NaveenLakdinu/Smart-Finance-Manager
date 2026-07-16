package com.example.smartfinancialmanagement;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

public class CurrencyHelper {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_CURRENCY = "currency_symbol";

    public static String getCurrencySymbol(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENCY, "LKR");
    }

    public static void setCurrencySymbol(Context context, String symbol) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENCY, symbol).apply();
    }

    public static String formatMoney(Context context, double amount) {
        String symbol = getCurrencySymbol(context);
        return symbol + " " + String.format(Locale.US, "%,.2f", amount);
    }
}
