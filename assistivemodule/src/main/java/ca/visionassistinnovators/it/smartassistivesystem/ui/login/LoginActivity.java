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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.ui.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private CheckBox cbRemember;
    private Button btnGoogle;

    private static final String TEST_EMAIL = "aaa@bbb.com";
    private static final String TEST_PASSWORD = "Admin101!";

    private static final String PREF_NAME = "sas_prefs";
    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_EMAIL = "saved_email";

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cb_remember);
        btnGoogle = findViewById(R.id.btnGoogle);

        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean remembered = sp.getBoolean(KEY_REMEMBER, false);
        if (remembered) {
            String savedEmail = sp.getString(KEY_EMAIL, "");
            email.setText(savedEmail);
            cbRemember.setChecked(true);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        // 🔹 Google Sign-In Configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 🔹 Handle “Continue with Google”
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        TextView tvSignUp = findViewById(R.id.tv_sign_up);
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        loginBtn.setOnClickListener(v -> doLogin());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String userEmail = account.getEmail();
            String userName = account.getDisplayName();

            Toast.makeText(this, "Welcome " + userName, Toast.LENGTH_SHORT).show();

            // ✅ Go to home after successful login
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();

        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void doLogin() {
        String uEmail = email.getText().toString().trim();
        String uPass = password.getText().toString().trim();

        if (uEmail.isEmpty() || uPass.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uEmail.equalsIgnoreCase(TEST_EMAIL) && uPass.equals(TEST_PASSWORD)) {
            SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            if (cbRemember.isChecked()) {
                editor.putBoolean(KEY_REMEMBER, true);
                editor.putString(KEY_EMAIL, uEmail);
            } else {
                editor.putBoolean(KEY_REMEMBER, false);
                editor.remove(KEY_EMAIL);
            }
            editor.apply();

            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials. Use aaa@bbb.com / Admin101!", Toast.LENGTH_LONG).show();
        }
    }
}
