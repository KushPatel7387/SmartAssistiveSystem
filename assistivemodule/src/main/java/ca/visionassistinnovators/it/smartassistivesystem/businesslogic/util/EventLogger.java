/**
 * Simple analytics / logging helper for Smart Assistive System.
 *
 * Logs important events to:
 *  - Logcat (tag = "SAS-Analytics")
 *  - Firebase Realtime Database under /analytics/events/{autoId}
 *
 * Course Section: OCA
 * Team Members:
 *  - Sarang Prajapati – N01662036
 *  - Krish Patel – N01666556
 *  - Kush Patel – N01657387
 *  - Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import ca.visionassistinnovators.it.smartassistivesystem.R;

/**
 * Central place to log analytics events.
 *
 * INTERNAL ONLY – users never see analytics UI.
 * Events are stored for ALL USERS and later aggregated.
 */
public class EventLogger {

    private static final String TAG = "SAS-Analytics";

    private EventLogger() {
        // Utility class – no instances
    }

    // Get reference to /analytics/events in Firebase
    private static DatabaseReference getAnalyticsRef(@NonNull Context context) {
        // No hardcoding of URL; we reuse firebase_db_url from strings.xml
        String url = context.getString(R.string.firebase_db_url);
        return FirebaseDatabase.getInstance(url)
                .getReference("analytics")
                .child("events");
    }

    /**
     * Log a screen view event.
     *
     * Example:
     *   EventLogger.logScreenView(ctx, "SensorFragment");
     *   EventLogger.logScreenView(ctx, "Home");
     */
    public static void logScreenView(@NonNull Context context,
                                     @NonNull String screenName) {
        logEventInternal(context, "screen_view", screenName, null);
    }

    /**
     * Log a custom event with details (e.g. sensor update).
     *
     * Example:
     *   EventLogger.logEvent(ctx, "sensor_distance_update", "distance_cm=120");
     */
    public static void logEvent(@NonNull Context context,
                                @NonNull String type,
                                @NonNull String details) {
        logEventInternal(context, type, null, details);
    }

    // ---------------------------------------------------------------------
    // INTERNAL IMPLEMENTATION
    // ---------------------------------------------------------------------

    private static void logEventInternal(@NonNull Context context,
                                         @NonNull String type,
                                         String screenName,
                                         String details) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : "anonymous";

        long ts = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", uid);
        data.put("type", type);       // e.g. "screen_view", "sensor_distance_update"
        data.put("timestamp", ts);

        if (screenName != null && !screenName.isEmpty()) {
            data.put("screen", screenName);  // e.g. "Home", "SensorFragment", "Settings"
        }
        if (details != null && !details.isEmpty()) {
            data.put("details", details);
        }

        try {
            DatabaseReference ref = getAnalyticsRef(context).push(); // autoId per event

            // For you in Android Studio (not for users)
            Log.d(TAG, "logEventInternal: " + data);

            // Asynchronous, non-blocking; Firebase will queue if offline and sync later
            ref.setValue(data);

        } catch (Exception e) {
            // NEVER break app if analytics fails
            Log.e(TAG, "Failed to log analytics event", e);
        }
    }
}
