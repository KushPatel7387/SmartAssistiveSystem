package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import com.google.firebase.auth.*;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class GoogleLoginManager {

    private final FirebaseAuth mAuth;

    public interface GoogleCallback {
        void onSuccess(String email);
        void onFailure(String error);
    }

    public GoogleLoginManager() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void loginWithGoogle(String idToken, GoogleCallback callback) {

        if (idToken == null) {
            callback.onFailure("Google token missing");
            return;
        }

        AuthCredential cred = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(cred)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String email = (mAuth.getCurrentUser() != null)
                                ? mAuth.getCurrentUser().getEmail()
                                : null;

                        callback.onSuccess(email);

                    } else {
                        callback.onFailure("Google authentication failed");
                    }
                });
    }
}
