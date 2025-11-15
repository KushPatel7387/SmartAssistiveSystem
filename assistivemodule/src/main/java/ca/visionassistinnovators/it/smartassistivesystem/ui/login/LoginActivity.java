package ca.visionassistinnovators.it.smartassistivesystem.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.ui.home.HomeActivity;
import ca.visionassistinnovators.it.smartassistivesystem.ui.util.LoginPrefsFacade;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.*;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn, btnGoogle;
    private CheckBox cbRemember;

    private GoogleSignInClient googleClient;

    private FirebaseAuth firebaseAuth;
    private LoginBusinessLogic loginBusinessLogic;

    private final LoginValidator loginValidator = new LoginValidator();
    private final EmailLoginManager emailLoginManager = new EmailLoginManager();
    private final GoogleLoginManager googleLoginManager = new GoogleLoginManager();
    private com.google.android.material.textfield.TextInputLayout tilEmail, tilPassword;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) return;

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                try {
                    GoogleSignInAccount acct = task.getResult(ApiException.class);

                    if (acct != null) {
                        googleLoginManager.loginWithGoogle(
                                acct.getIdToken(),
                                new GoogleLoginManager.GoogleCallback() {
                                    @Override
                                    public void onSuccess(String emailFromCallback) {
                                        if (emailFromCallback != null) {
                                            // Business logic: remember-me handling
                                            if (loginBusinessLogic != null) {
                                                loginBusinessLogic.handleRememberMe(
                                                        cbRemember.isChecked(),
                                                        emailFromCallback
                                                );
                                            } else {
                                                // Fallback: direct facade if somehow null
                                                LoginPrefsFacade.saveRememberEmail(
                                                        LoginActivity.this,
                                                        cbRemember.isChecked(),
                                                        emailFromCallback
                                                );
                                            }
                                        }
                                        goHome();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );

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
        tilEmail    = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);

        // Firebase + business logic
        firebaseAuth = FirebaseAuth.getInstance();
        loginBusinessLogic = new LoginBusinessLogic(this, firebaseAuth);

        // Clear error when user starts typing
        email.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
            }
        });

        password.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }
        });

        // ─────────────────────────────────────────────
        // Auto-login / remember-me logic (business)
        // ─────────────────────────────────────────────

        if (loginBusinessLogic.shouldAutoLogin()) {
            goHome();
            finish();
            return;
        }

        // If remember-me is OFF but Firebase still has a user, sign out:
        loginBusinessLogic.ensureSessionMatchesRememberPreference();

        // Restore remembered email + checkbox state
        String rememberedEmail = loginBusinessLogic.getRememberedEmail();
        if (!rememberedEmail.isEmpty()) {
            email.setText(rememberedEmail);
            cbRemember.setChecked(true);
        }

        // ─────────────────────────────────────────────
        // Google Sign-In setup
        // ─────────────────────────────────────────────

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> googleLauncher.launch(googleClient.getSignInIntent()));

        // Go to Register screen
        findViewById(R.id.tv_sign_up).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Email/password login
        loginBtn.setOnClickListener(v -> doPasswordLogin());
    }

    private void doPasswordLogin() {
        String uEmail = email.getText().toString().trim();
        String uPass  = password.getText().toString().trim();

        // Empty check (business rule already extracted to validator)
        if (loginValidator.isEmailOrPasswordEmpty(uEmail, uPass)) {
            Toast.makeText(this, R.string.err_enter_email_password, Toast.LENGTH_SHORT).show();
            return;
        }

        LoginValidator.ValidationResult result = loginValidator.validate(uEmail, uPass);
        if (!result.isValid()) {
            String msg = result.getMessage();

            if (result.isEmailError()) {
                tilEmail.setError(msg);
                tilPassword.setError(null);
            } else {
                tilPassword.setError(msg);
                tilEmail.setError(null);
            }

            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }

        // All good → clear errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Proceed with Firebase (wrapped in EmailLoginManager)
        emailLoginManager.login(uEmail, uPass, new EmailLoginManager.LoginCallback() {
            @Override
            public void onSuccess(String emailFromCallback) {
                // Business logic: remember-me
                if (loginBusinessLogic != null) {
                    loginBusinessLogic.handleRememberMe(cbRemember.isChecked(), emailFromCallback);
                } else {
                    // Fallback to previous behavior if something goes wrong
                    LoginPrefsFacade.saveRememberEmail(LoginActivity.this, cbRemember.isChecked(), emailFromCallback);
                }
                goHome();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(LoginActivity.this,
                        getString(R.string.err_login_failed_fmt, errorMessage),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    // TEXT WATCHER TO CLEAR ERRORS
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
