package ca.visionassistinnovators.it.smartassistivesystem.ui.util;

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

    public static void clearAll(Context c) {
        PrefsWriter.clearAll(c);
    }
}
