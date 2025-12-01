/**
 * Course Section: OCA
 * Team Members
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Locale;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.VoiceCommandRouter;
import ca.visionassistinnovators.it.smartassistivesystem.ui.login.LoginActivity;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawerLayout;

    // Voice assistant
    private ActivityResultLauncher<String> audioPermissionLauncher;
    private ActivityResultLauncher<Intent> speechRecognizerLauncher;
    private final VoiceCommandRouter voiceRouter = new VoiceCommandRouter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Top-level destinations (including help)
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.navigation_location,
                R.id.nav_sos,
                R.id.nav_alerts,
                R.id.nav_sensors,
                R.id.nav_fall_detection,
                R.id.nav_profile,
                R.id.nav_feedback,
                R.id.nav_settings,
                R.id.nav_help
        ).setOpenableLayout(drawerLayout).build();

        // Toolbar + Drawer + Navigation
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Drawer click logic (with explicit Home handling)
        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                showLogoutDialog();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (id == R.id.nav_home) {
                // Always go to Home fragment
                navController.popBackStack(R.id.nav_home, false);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            drawerLayout.closeDrawer(GravityCompat.START);
            return handled;
        });

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Explicit Home handling for bottom navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navController.popBackStack(R.id.nav_home, false);
                return true;
            }

            return NavigationUI.onNavDestinationSelected(item, navController);
        });


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle(R.string.exit_app1)
                        .setMessage(R.string.are_you_sure_you_want_to_exit1)
                        .setIcon(R.drawable.ic_exit)
                        .setPositiveButton(R.string.yes1, (d, w) -> finishAffinity())
                        .setNegativeButton("Stay", (d, w) -> d.dismiss())
                        .show();
            }
        });

        // ─────────────────────────────────────────────
        // Voice assistant: permission + speech launchers
        // ─────────────────────────────────────────────
        audioPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startSpeechRecognition();
                    } else {
                        Toast.makeText(
                                HomeActivity.this,
                                R.string.voice_permission_denied,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches =
                                result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                        if (matches != null && !matches.isEmpty()) {
                            String spoken = matches.get(0);
                            handleVoiceCommand(spoken);
                        } else {
                            Toast.makeText(
                                    HomeActivity.this,
                                    R.string.voice_not_understood,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you really want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {

                    FirebaseAuth.getInstance().signOut();

                    GoogleSignInClient gsc = GoogleSignIn.getClient(
                            this,
                            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .build()
                    );
                    gsc.signOut();

                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // main.xml already has: Help, About, Voice
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Top-right menu items (Help, About, Voice)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_about) {
            navController.navigate(R.id.nav_about);
            return true;
        }

        if (id == R.id.action_help) {
            navController.navigate(R.id.nav_help);
            return true;
        }

        if (id == R.id.action_voice) {
            launchVoiceAssistant();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // ─────────────────────────────────────────────
    // Voice assistant helpers
    // ─────────────────────────────────────────────

    private void launchVoiceAssistant() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );
        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.voice_hint)
        );

        try {
            Toast.makeText(this, R.string.voice_listening_toast, Toast.LENGTH_SHORT).show();
            speechRecognizerLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    this,
                    R.string.voice_not_supported,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void handleVoiceCommand(String spoken) {
        Toast.makeText(
                this,
                getString(R.string.voice_result_pattern, spoken),
                Toast.LENGTH_SHORT
        ).show();

        VoiceCommandRouter.Destination dest = voiceRouter.resolve(spoken);

        if (navController == null) {
            navController = Navigation.findNavController(
                    this,
                    R.id.nav_host_fragment_content_main
            );
        }

        switch (dest) {
            case HOME:
                navController.popBackStack(R.id.nav_home, false);
                break;

            case SENSORS:
                navController.navigate(R.id.nav_sensors);
                break;

            case FEEDBACK:
                navController.navigate(R.id.nav_feedback);
                break;

            case SETTINGS:
                navController.navigate(R.id.nav_settings);
                break;

            case PATIENTS:
                // Patients are currently in SOS fragment
                navController.navigate(R.id.nav_sos);
                break;

            case UNKNOWN:
            default:
                Toast.makeText(
                        this,
                        R.string.voice_not_understood,
                        Toast.LENGTH_SHORT
                ).show();
                break;
        }
    }
}
