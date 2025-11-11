package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginManager {

    private final FirebaseAuth mAuth;

    public interface LoginCallback {
        void onSuccess(String email);
        void onFailure(String errorMessage);
    }

    public EmailLoginManager() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void login(String email, String password, LoginCallback callback) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        String userEmail = (mAuth.getCurrentUser() != null)
                                ? mAuth.getCurrentUser().getEmail()
                                : email;

                        callback.onSuccess(userEmail);

                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Unknown error";

                        callback.onFailure(msg);
                    }
                });
    }
}
