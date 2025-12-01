package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.FirebaseDatabase;

import ca.visionassistinnovators.it.smartassistivesystem.R;

/**
 * Global Application class.
 * - Applies saved theme on app startup.
 * - Enables Firebase disk persistence + keeps key nodes synced for offline use.
 */
public class SmartAssistiveApp extends Application {

    private static final String PREFS_NAME = "sas_settings";
    private static final String KEY_DARK_THEME = "dark_theme";

    @Override
    public void onCreate() {
        super.onCreate();

        // ---- Apply saved theme before any UI inflates ----
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkTheme = prefs.getBoolean(KEY_DARK_THEME, false);

        AppCompatDelegate.setDefaultNightMode(
                darkTheme
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        // ---- Firebase Realtime Database setup ----
        // Enable disk persistence ONCE per process, before any other DB use
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                getString(R.string.firebase_db_url)
        );
        db.setPersistenceEnabled(true);

        // Keep core trees in sync for offline mode
        db.getReference("users").keepSynced(true);
        db.getReference("sensors").keepSynced(true);
        db.getReference("Feedback").keepSynced(true);
        db.getReference("analytics").keepSynced(true);
    }
}
