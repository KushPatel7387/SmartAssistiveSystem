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

/**
 * Package-private write-only utilities.
 * Keeps raw write access confined to this package.
 */
final class PrefsWriter {
    private PrefsWriter() {}

    static void saveRememberEmail(Context c, boolean remember, String email) {
        Prefs.putBoolean(c, Prefs.KEY_REMEMBER, remember);
        Prefs.putString(c, Prefs.KEY_EMAIL, remember ? safe(email) : "");
    }

    static void saveGuardian(Context c, String phone) {
        Prefs.putString(c, Prefs.KEY_GUARDIAN, safe(phone));
    }

    static void clearAll(Context c) {
        Prefs.clear(c);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
