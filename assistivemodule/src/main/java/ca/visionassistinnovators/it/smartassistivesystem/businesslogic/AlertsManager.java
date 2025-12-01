/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 *
 * Business logic for loading alerts from Firebase and (optionally)
 * seeding sample alerts ONLY ONCE per user & device.
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class AlertsManager {

    public interface AlertsListener {
        void onAlertsChanged(List<AlertModel> alerts);
        void onError(String error);
    }

    private static final String USERS_NODE   = "users";
    private static final String ALERTS_CHILD = "alerts";

    // prefs so dummy alerts are only seeded once per user (per device)
    private static final String PREFS_NAME           = "alerts_prefs";
    private static final String KEY_PREFIX_SEEDED    = "dummy_seeded_";

    private final Map<String, ValueEventListener> activeListeners = new HashMap<>();

    private FirebaseDatabase getDb(Context ctx) {
        return FirebaseDatabase.getInstance(
                ctx.getString(R.string.firebase_db_url)
        );
    }

    private DatabaseReference getAlertsRef(Context ctx, String userId) {
        return getDb(ctx)
                .getReference(USERS_NODE)
                .child(userId)
                .child(ALERTS_CHILD);
    }

    // ─────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────

    public void listenForAlerts(Context ctx,
                                String userId,
                                AlertsListener listener) {

        if (TextUtils.isEmpty(userId)) {
            if (listener != null) {
                listener.onError("No user id for alerts.");
            }
            return;
        }

        DatabaseReference alertsRef = getAlertsRef(ctx, userId);

        // Prevent multiple listeners for same user
        ValueEventListener old = activeListeners.remove(userId);
        if (old != null) {
            alertsRef.removeEventListener(old);
        }

        ValueEventListener valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If no alerts in DB yet, seed dummy ONCE (per device & user)
                if (!hasSeededDummy(ctx, userId) && !snapshot.hasChildren()) {
                    seedDummyAlerts(ctx, userId, alertsRef);
                    markDummySeeded(ctx, userId);
                    // Firebase will call onDataChange() again after seed
                    return;
                }

                List<AlertModel> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    AlertModel model = child.getValue(AlertModel.class);
                    if (model != null) {
                        model.id = child.getKey();   // Firebase key
                        list.add(model);
                    }
                }

                if (listener != null) {
                    listener.onAlertsChanged(list);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) {
                    listener.onError(error.getMessage());
                }
            }
        };

        alertsRef.addValueEventListener(valueListener);
        activeListeners.put(userId, valueListener);
    }

    public void stopListening(Context ctx, String userId) {
        if (TextUtils.isEmpty(userId)) return;

        ValueEventListener listener = activeListeners.remove(userId);
        if (listener == null) return;

        getAlertsRef(ctx, userId).removeEventListener(listener);
    }

    // ─────────────────────────────────────────────
    // Dummy alerts seeding (once per user/device)
    // ─────────────────────────────────────────────

    private SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private boolean hasSeededDummy(Context ctx, String userId) {
        return getPrefs(ctx).getBoolean(KEY_PREFIX_SEEDED + userId, false);
    }

    private void markDummySeeded(Context ctx, String userId) {
        getPrefs(ctx)
                .edit()
                .putBoolean(KEY_PREFIX_SEEDED + userId, true)
                .apply();
    }

    /**
     * Insert a couple of dummy alerts to show the feature on first run.
     * Called ONLY when:
     *  - alerts list is empty AND
     *  - hasSeededDummy(...) == false
     */
    private void seedDummyAlerts(Context ctx,
                                 String userId,
                                 DatabaseReference alertsRef) {

        long now = System.currentTimeMillis();

        AlertModel a1 = new AlertModel(
                null,
                "Welcome to Smart Assistive System",
                "This is a sample alert. Real alerts will appear here when events happen.",
                now
        );

        AlertModel a2 = new AlertModel(
                null,
                "Patient Linked Successfully",
                "You can now monitor your patient’s sensors and alerts from the app.",
                now - 5 * 60_000L  // 5 minutes earlier
        );

        DatabaseReference r1 = alertsRef.push();
        a1.id = r1.getKey();
        r1.setValue(a1);

        DatabaseReference r2 = alertsRef.push();
        a2.id = r2.getKey();
        r2.setValue(a2);
    }
}
