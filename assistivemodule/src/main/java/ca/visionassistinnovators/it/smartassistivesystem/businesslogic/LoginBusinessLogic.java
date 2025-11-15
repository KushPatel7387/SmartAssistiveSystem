package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.visionassistinnovators.it.smartassistivesystem.ui.util.LoginPrefsFacade;
import ca.visionassistinnovators.it.smartassistivesystem.ui.util.Prefs;

/**
 * Handles non-UI login rules:
 *  - Remember-me preference
 *  - Auto-login decision
 *  - Keeping Firebase session in sync with remember-me
 */
public class LoginBusinessLogic {

    private final Context appContext;
    private final FirebaseAuth auth;

    public LoginBusinessLogic(Context context, FirebaseAuth auth) {
        this.appContext = context.getApplicationContext();
        this.auth = auth;
    }

    // ─────────────────────────────────────────────
    // Remember-me rules
    // ─────────────────────────────────────────────

    public boolean isRememberMeEnabled() {
        return Prefs.getBoolean(appContext, Prefs.KEY_REMEMBER, false);
    }

    public String getRememberedEmail() {
        if (!isRememberMeEnabled()) {
            return "";
        }
        String email = Prefs.getString(appContext, Prefs.KEY_EMAIL, "");
        return email != null ? email : "";
    }

    /**
     * Should we skip the login screen and go directly to Home?
     * Only if:
     *  - Remember-me is ON, and
     *  - Firebase still has a logged-in user.
     */
    public boolean shouldAutoLogin() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return isRememberMeEnabled() && currentUser != null;
    }

    /**
     * If remember-me is OFF but Firebase still has a session,
     * sign the user out so they must login again.
     */
    public void ensureSessionMatchesRememberPreference() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (!isRememberMeEnabled() && currentUser != null) {
            auth.signOut();
        }
    }

    /**
     * After successful login (email/password or Google),
     * update the remember-me preference + stored email.
     */
    public void handleRememberMe(boolean rememberChecked, String email) {
        LoginPrefsFacade.saveRememberEmail(appContext, rememberChecked, email);
    }
}
