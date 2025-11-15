/**
 * Course Section: OCA
 * Team Members
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences helper.
 * - PUBLIC readers (usable anywhere)
 * - PACKAGE-PRIVATE writers (only code in this package can write)
 */
public final class Prefs {
    private Prefs() {}

    private static final String PREF = "sas_prefs";

    public static final String KEY_REMEMBER    = "remember_me";
    public static final String KEY_EMAIL       = "saved_email";
    public static final String KEY_GUARDIAN    = "pref_guardian_number";
    // NEW: logged-in user phone (for feedback, profile, etc.)
    public static final String KEY_USER_PHONE  = "user_phone";

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // ---- PUBLIC READERS ----
    public static boolean getBoolean(Context c, String k, boolean def) {
        return sp(c).getBoolean(k, def);
    }

    public static String getString(Context c, String k, String def) {
        return sp(c).getString(k, def);
    }

    // ---- PACKAGE-PRIVATE WRITERS ----
    static void putBoolean(Context c, String k, boolean v) {
        sp(c).edit().putBoolean(k, v).apply();
    }

    static void putString(Context c, String k, String v) {
        sp(c).edit().putString(k, v).apply();
    }

    static void clear(Context c) {
        sp(c).edit().clear().apply();
    }
}
