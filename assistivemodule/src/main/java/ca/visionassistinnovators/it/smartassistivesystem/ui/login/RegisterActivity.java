/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.RegistrationBusinessLogic;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private EditText etFirstName, etLastName, etPhone, etEmail, etPassword, etConfirm;
    private TextInputLayout tilPassword;
    private TextView tvPasswordStrength;
    private RegistrationBusinessLogic registrationLogic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference("users");

        registrationLogic = new RegistrationBusinessLogic();

        etFirstName = findViewById(R.id.et_first_name);
        etLastName  = findViewById(R.id.et_last_name);
        etPhone     = findViewById(R.id.et_phone);
        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        etConfirm   = findViewById(R.id.et_confirm);
        tilPassword = findViewById(R.id.til_password);
        tvPasswordStrength = findViewById(R.id.tv_password_strength);

        Button btnRegister   = findViewById(R.id.btn_register);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Live password strength display
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrengthIndicator(s.toString());
            }
        });

        // GO TO LOGIN SCREEN
        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // REGISTER ACTION
        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName  = etLastName.getText().toString().trim();
            String phone     = etPhone.getText().toString().trim();
            String email     = etEmail.getText().toString().trim();
            String pass      = etPassword.getText().toString().trim();
            String conf      = etConfirm.getText().toString().trim();

            RegistrationBusinessLogic.ValidationResult result =
                    registrationLogic.validate(this, firstName, lastName, phone, email, pass, conf);

            if (!result.isValid()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            String fullName        = registrationLogic.buildFullName(firstName, lastName);
            String normalizedPhone = registrationLogic.normalizePhone(phone);

            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnSuccessListener((SignInMethodQueryResult res) -> {
                        boolean exists = res.getSignInMethods() != null && !res.getSignInMethods().isEmpty();
                        if (exists) {
                            Toast.makeText(this, R.string.user_already_registered, Toast.LENGTH_SHORT).show();
                        } else {
                            createAccountAndSaveProfile(fullName, normalizedPhone, email, pass);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    getString(R.string.auth_check_error_fmt, e.getMessage()),
                                    Toast.LENGTH_SHORT).show());
        });
    }

    private void createAccountAndSaveProfile(String fullName,
                                             String phone,
                                             String email,
                                             String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : getString(R.string.err_unknown);
                        Toast.makeText(this,
                                getString(R.string.auth_error_fmt, msg),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = mAuth.getCurrentUser() != null
                            ? mAuth.getCurrentUser().getUid()
                            : null;

                    if (uid == null) {
                        Toast.makeText(this,
                                R.string.no_uid_after_registration,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Assuming UserModel(name, phone, email, role)
                    UserModel user = new UserModel(fullName, phone, email, "regular");
                    usersRef.child(uid).setValue(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this,
                                        R.string.registered_success,
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            getString(R.string.db_write_error_fmt, e.getMessage()),
                                            Toast.LENGTH_SHORT).show());
                });
    }

    // UI helper: show password strength in tvPasswordStrength
    private void updatePasswordStrengthIndicator(String password) {
        if (tvPasswordStrength == null) return;

        RegistrationBusinessLogic.PasswordStrength strength =
                registrationLogic.getPasswordStrength(password);

        switch (strength) {
            case TOO_SHORT:
                if (password.isEmpty()) {
                    tvPasswordStrength.setText("");
                } else {
                    tvPasswordStrength.setText(getString(R.string.password_too_short));
                    tvPasswordStrength.setTextColor(
                            ContextCompat.getColor(this, R.color.password_weak)
                    );
                }
                break;

            case WEAK:
                tvPasswordStrength.setText(getString(R.string.password_strength_weak));
                tvPasswordStrength.setTextColor(
                        ContextCompat.getColor(this, R.color.password_weak)
                );
                break;

            case MEDIUM:
                tvPasswordStrength.setText(getString(R.string.password_strength_medium));
                tvPasswordStrength.setTextColor(
                        ContextCompat.getColor(this, R.color.password_medium)
                );
                break;

            case STRONG:
                tvPasswordStrength.setText(getString(R.string.password_strength_strong));
                tvPasswordStrength.setTextColor(
                        ContextCompat.getColor(this, R.color.password_strong)
                );
                break;
        }
    }
}
