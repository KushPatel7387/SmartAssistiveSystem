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
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private EditText etName, etPhone, etEmail, etPassword, etConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference("users");

        etName     = findViewById(R.id.et_name);
        etPhone    = findViewById(R.id.et_phone);
        etEmail    = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirm  = findViewById(R.id.et_confirm);

        Button btnRegister  = findViewById(R.id.btn_register);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // ➤ GO TO LOGIN SCREEN
        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // ➤ REGISTER ACTION
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String conf = etConfirm.getText().toString().trim();

            if (!validate(name, email, pass, conf)) return;

            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnSuccessListener((SignInMethodQueryResult res) -> {
                        boolean exists = res.getSignInMethods() != null && !res.getSignInMethods().isEmpty();
                        if (exists) {
                            Toast.makeText(this, R.string.user_already_registered, Toast.LENGTH_SHORT).show();
                        } else {
                            createAccountAndSaveProfile(name, phone, email, pass);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, getString(R.string.auth_check_error_fmt, e.getMessage()), Toast.LENGTH_SHORT).show());
        });
    }

    private boolean validate(String name, String email, String pass, String conf) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(pass) || TextUtils.isEmpty(conf)) {
            Toast.makeText(this, R.string.all_fields_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.invalid_email_format, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, R.string.password_min_length, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!pass.equals(conf)) {
            Toast.makeText(this, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createAccountAndSaveProfile(String name, String phone, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String msg = (task.getException() != null) ? task.getException().getMessage() : getString(R.string.err_unknown);
                        Toast.makeText(this, getString(R.string.auth_error_fmt, msg), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                    if (uid == null) {
                        Toast.makeText(this, R.string.no_uid_after_registration, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserModel user = new UserModel(name, phone, email, "regular");
                    usersRef.child(uid).setValue(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, R.string.registered_success, Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, getString(R.string.db_write_error_fmt, e.getMessage()), Toast.LENGTH_SHORT).show());
                });
    }
}
