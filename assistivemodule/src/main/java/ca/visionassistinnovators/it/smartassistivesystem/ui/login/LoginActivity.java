/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.EmailLoginManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.GoogleLoginManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.LoginBusinessLogic;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.LoginValidator;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.LoginPrefsFacade;
import ca.visionassistinnovators.it.smartassistivesystem.ui.location.PatientLocationService;
import ca.visionassistinnovators.it.smartassistivesystem.ui.home.HomeActivity;

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

    // Google Launcher
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
                                            loginBusinessLogic.handleRememberMe(
                                                    cbRemember.isChecked(),
                                                    emailFromCallback
                                            );
                                        }

                                        syncUserProfileToPrefs();

                                        // ⭐ Start Patient Location Service
                                        startPatientLocationService();

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
                    Toast.makeText(this,
                            getString(R.string.err_google_signin_failed, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
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
        tilEmail   = findViewById(R.id.til_email);
        tilPassword= findViewById(R.id.til_password);

        firebaseAuth = FirebaseAuth.getInstance();
        loginBusinessLogic = new LoginBusinessLogic(this, firebaseAuth);

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

        if (loginBusinessLogic.shouldAutoLogin()) {
            goHome();
            finish();
            return;
        }

        loginBusinessLogic.ensureSessionMatchesRememberPreference();

        String rememberedEmail = loginBusinessLogic.getRememberedEmail();
        if (!rememberedEmail.isEmpty()) {
            email.setText(rememberedEmail);
            cbRemember.setChecked(true);
        }

        // Google Sign-In Setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> googleLauncher.launch(googleClient.getSignInIntent()));

        findViewById(R.id.tv_sign_up).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        loginBtn.setOnClickListener(v -> doPasswordLogin());
    }

    private void doPasswordLogin() {
        String uEmail = email.getText().toString().trim();
        String uPass  = password.getText().toString().trim();

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

        tilEmail.setError(null);
        tilPassword.setError(null);

        emailLoginManager.login(uEmail, uPass, new EmailLoginManager.LoginCallback() {
            @Override
            public void onSuccess(String emailFromCallback) {

                loginBusinessLogic.handleRememberMe(cbRemember.isChecked(), emailFromCallback);

                syncUserProfileToPrefs();

                // ⭐ Start live location upload
                startPatientLocationService();

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

    private void syncUserProfileToPrefs() {
        if (firebaseAuth == null) return;

        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current == null) return;

        String uid = current.getUid();

        DatabaseReference userRef = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference(getString(R.string.users))
                .child(uid);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) return;

            Object phoneObj = snapshot.child(getString(R.string.phone_)).getValue();
            if (phoneObj != null) {
                String phoneStr = String.valueOf(phoneObj);
                LoginPrefsFacade.saveUserPhone(LoginActivity.this, phoneStr);
            }
        });
    }

    // ⭐ NEW — Start Background Live Location Service
    private void startPatientLocationService() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    3000
            );
            return;
        }

        Intent svc = new Intent(this, PatientLocationService.class);
        startService(svc);
    }

    // Clear errors when typing
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
