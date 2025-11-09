/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.ui.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn, btnGoogle;
    private CheckBox cbRemember;

    private static final String PREF_NAME    = "sas_prefs";
    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_EMAIL    = "saved_email";

    private GoogleSignInClient googleClient;
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) return;
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount acct = task.getResult(ApiException.class);
                    if (acct != null) {
                        firebaseAuthWithGoogle(acct.getIdToken());
                    } else {
                        Toast.makeText(this, R.string.err_no_google_account, Toast.LENGTH_SHORT).show();
                    }
                } catch (ApiException e) {
                    Toast.makeText(this, getString(R.string.err_google_signin_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email      = findViewById(R.id.editTextEmail);
        password   = findViewById(R.id.editTextPassword);
        loginBtn   = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cb_remember);
        btnGoogle  = findViewById(R.id.btnGoogle);

        mAuth = FirebaseAuth.getInstance();

        // Auto-navigate if already signed in
        if (mAuth.getCurrentUser() != null) {
            goHome();
            return;
        }

        // Prefill remembered email
        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (sp.getBoolean(KEY_REMEMBER, false)) {
            email.setText(sp.getString(KEY_EMAIL, ""));
            cbRemember.setChecked(true);
        }

        // Google Sign-In (default_web_client_id is generated from google-services.json)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> googleLauncher.launch(googleClient.getSignInIntent()));

        TextView tvSignUp = findViewById(R.id.tv_sign_up);
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        loginBtn.setOnClickListener(v -> doPasswordLogin());
    }

    private void doPasswordLogin() {
        String uEmail = email.getText().toString().trim();
        String uPass  = password.getText().toString().trim();

        if (uEmail.isEmpty() || uPass.isEmpty()) {
            Toast.makeText(this, R.string.err_enter_email_password, Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase email/password login (no hardcoded backdoor)
        mAuth.signInWithEmailAndPassword(uEmail, uPass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveRemember(uEmail);
                        goHome();
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : getString(R.string.err_unknown);
                        Toast.makeText(this, getString(R.string.err_login_failed_fmt, msg), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(this, R.string.err_no_google_token, Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential cred = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(cred)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String gEmail = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getEmail() : null;
                        if (gEmail != null) saveRemember(gEmail);
                        goHome();
                    } else {
                        Toast.makeText(this, R.string.err_google_auth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveRemember(String mail) {
        SharedPreferences.Editor ed = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        if (cbRemember.isChecked()) {
            ed.putBoolean(KEY_REMEMBER, true);
            ed.putString(KEY_EMAIL, mail);
        } else {
            ed.putBoolean(KEY_REMEMBER, false);
            ed.remove(KEY_EMAIL);
        }
        ed.apply();
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
