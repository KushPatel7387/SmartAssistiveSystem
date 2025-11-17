/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class AlertsManager {

    private static final String USERS_NODE   = "users";
    private static final String ALERTS_CHILD = "alerts";

    public interface AlertsListener {
        void onAlertsChanged(List<AlertModel> alerts);
        void onError(String error);
    }

    private com.google.firebase.database.ValueEventListener alertsListener;

    /**
     * Path: /users/{uid}/alerts
     * Uses SAME DB URL string as other managers.
     */
    private DatabaseReference getUserAlertsRef(Context ctx, String userUid) {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                ctx.getString(R.string.firebase_db_url)
        );
        return db.getReference(USERS_NODE)
                .child(userUid)
                .child(ALERTS_CHILD);
    }

    public void listenForAlerts(Context ctx,
                                String userUid,
                                AlertsListener listener) {

        if (TextUtils.isEmpty(userUid)) {
            if (listener != null) {
                listener.onError(ctx.getString(R.string.no_user_id));
            }
            return;
        }

        if (alertsListener != null) {
            // already listening
            return;
        }

        // Seed sample alerts ONCE if this user has none
        seedSampleAlertsIfEmpty(ctx, userUid);

        alertsListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AlertModel> result = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    AlertModel model = child.getValue(AlertModel.class);
                    if (model != null) {
                        model.id = child.getKey();
                        result.add(model);
                    }
                }

                // Latest first
                Collections.sort(result, new Comparator<AlertModel>() {
                    @Override
                    public int compare(AlertModel a, AlertModel b) {
                        return Long.compare(b.timestamp, a.timestamp);
                    }
                });

                if (listener != null) {
                    listener.onAlertsChanged(result);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) {
                    listener.onError(error.getMessage());
                }
            }
        };

        getUserAlertsRef(ctx, userUid).addValueEventListener(alertsListener);
    }

    public void stopListening(Context ctx, String userUid) {
        if (alertsListener == null || TextUtils.isEmpty(userUid)) return;
        getUserAlertsRef(ctx, userUid).removeEventListener(alertsListener);
        alertsListener = null;
    }

    /**
     * Check /users/{uid}/alerts once.
     * If empty -> insert a few sample alerts.
     */
    private void seedSampleAlertsIfEmpty(Context ctx, String userUid) {
        DatabaseReference ref = getUserAlertsRef(ctx, userUid);

        ref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Already has alerts, do nothing.
                    return;
                }

                long now = System.currentTimeMillis();

                pushAlert(ref, ctx.getString(R.string.system_check_complete),
                        ctx.getString(R.string.all_modules_are_running_normally), now - 3_600_000L);

                pushAlert(ref, ctx.getString(R.string.ambient_light_is_low_consider_enabling_the_magnifier),
                        ctx.getString(R.string.low_light_detected), now - 1_800_000L);

                pushAlert(ref, ctx.getString(R.string.you_have_new_feedback_from_a_user)
                        ,
                        ctx.getString(R.string.new_feedback_received), now - 600_000L);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // ignore for seeding
            }
        });
    }

    private void pushAlert(DatabaseReference alertsRef,
                           String title,
                           String message,
                           long timestamp) {

        String key = alertsRef.push().getKey();
        if (key == null) return;

        AlertModel model = new AlertModel(key, title, message, timestamp);
        alertsRef.child(key).setValue(model);
    }
}
