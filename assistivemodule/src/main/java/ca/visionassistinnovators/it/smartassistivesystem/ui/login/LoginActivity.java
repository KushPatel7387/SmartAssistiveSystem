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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.ui.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private CheckBox cbRemember;

    // prof's test account
    private static final String TEST_EMAIL = "aaa@bbb.com";
    private static final String TEST_PASSWORD = "Admin101!";

    // shared pref
    private static final String PREF_NAME = "sas_prefs";
    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_EMAIL = "saved_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cb_remember);

        // load pref
        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean remembered = sp.getBoolean(KEY_REMEMBER, false);
        if (remembered) {
            String savedEmail = sp.getString(KEY_EMAIL, "");
            email.setText(savedEmail);
            cbRemember.setChecked(true);

            // auto-skip to home (optional, but nice)
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        loginBtn.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String uEmail = email.getText().toString().trim();
        String uPass = password.getText().toString().trim();

        if (uEmail.isEmpty() || uPass.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // check against prof's account
        if (uEmail.equalsIgnoreCase(TEST_EMAIL) && uPass.equals(TEST_PASSWORD)) {

            // save remember me
            if (cbRemember.isChecked()) {
                SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sp.edit()
                        .putBoolean(KEY_REMEMBER, true)
                        .putString(KEY_EMAIL, uEmail)
                        .apply();
            } else {
                // clear
                SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sp.edit()
                        .putBoolean(KEY_REMEMBER, false)
                        .remove(KEY_EMAIL)
                        .apply();
            }

            // go to home
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials. Use aaa@bbb.com / Admin101!", Toast.LENGTH_LONG).show();
        }
    }
}
