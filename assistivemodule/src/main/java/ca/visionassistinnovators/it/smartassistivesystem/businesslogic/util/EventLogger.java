/**
 * Simple analytics / logging helper for Smart Assistive System.
 *
 * Logs important events to:
 *  - Logcat (tag = "SAS-Analytics")
 *  - Firebase Realtime Database under /analytics/{uid or anonymous}/{timestamp}
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

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EventLogger {

    private static final String TAG = "SAS-Analytics";

    private static String getCurrentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getUid() : "anonymous";
    }

    /**
     * Convenience method for logging a screen view.
     */
    public static void logScreenView(Context context, String screenName) {
        logEventInternal("screen_view", screenName, null);
    }

    /**
     * Convenience method for logging a named event with optional details.
     */
    public static void logEvent(Context context, String eventName, @Nullable String details) {
        logEventInternal(eventName, null, details);
    }

    private static void logEventInternal(String type,
                                         @Nullable String screenName,
                                         @Nullable String details) {

        String uid = getCurrentUid();
        long ts = System.currentTimeMillis();

        // 1) Logcat
        StringBuilder sb = new StringBuilder();
        sb.append("uid=").append(uid)
                .append(" type=").append(type);
        if (screenName != null) {
            sb.append(" screen=").append(screenName);
        }
        if (details != null && !details.isEmpty()) {
            sb.append(" details=").append(details);
        }
        Log.d(TAG, sb.toString());

        // 2) Realtime Database
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("analytics")
                .child(uid)
                .child(String.valueOf(ts));

        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("timestamp", ts);

        if (screenName != null) {
            data.put("screen", screenName);
        }
        if (details != null && !details.isEmpty()) {
            data.put("details", details);
        }

        // Fire-and-forget (no UI blocking, no callbacks needed)
        ref.setValue(data);
    }
}
