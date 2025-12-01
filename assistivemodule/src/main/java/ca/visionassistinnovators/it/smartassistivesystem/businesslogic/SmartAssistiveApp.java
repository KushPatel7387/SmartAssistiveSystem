package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import ca.visionassistinnovators.it.smartassistivesystem.R;

/**
 * Global Application class.
 * Enables Firebase disk persistence + keeps key nodes synced for offline use.
 */
public class SmartAssistiveApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

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
