/**
 * Course Section: OCA
 * Team Members
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util;

import android.content.Context;

/**
 * Public, whitelisted facade. Other packages call these methods.
 * Internally delegates to package-private writers in this package.
 */
public final class LoginPrefsFacade {
    private LoginPrefsFacade() {}

    public static void saveRememberEmail(Context c, boolean remember, String email) {
        PrefsWriter.saveRememberEmail(c, remember, email);
    }

    public static void saveGuardian(Context c, String phone) {
        PrefsWriter.saveGuardian(c, phone);
    }

    // NEW: save logged-in user phone (from Firebase "users" node)
    public static void saveUserPhone(Context c, String phone) {
        PrefsWriter.saveUserPhone(c, phone);
    }

    public static void clearAll(Context c) {
        PrefsWriter.clearAll(c);
    }
}
