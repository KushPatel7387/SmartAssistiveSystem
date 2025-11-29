/**
 * AnalyticsAggregator
 *
 * Reads events from Firebase and calculates, for each screen:
 *  - number of accesses in the last 24 hours
 *  - number of accesses in the last 7 days
 *
 * INTERNAL ONLY – results are written to Logcat, not shown to users.
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class AnalyticsAggregator {

    private static final String TAG = "SAS-Analytics-Aggregator";

    private AnalyticsAggregator() {
        // Utility class
    }

    private static DatabaseReference getAnalyticsRef(@NonNull Context context) {
        String url = context.getString(R.string.firebase_db_url);
        return FirebaseDatabase.getInstance(url)
                .getReference("analytics")
                .child("events");
    }

    /**
     * Compute and LOG stats to Logcat:
     *  - For each screen: count in last 24h, count in last 7d
     *
     * Usage (debug only, e.g. from HomeFragment):
     *   AnalyticsAggregator.debugLogScreenUsage(requireContext());
     */
    public static void debugLogScreenUsage(@NonNull Context context) {
        long now = System.currentTimeMillis();
        final long cutoff24h = now - 24L * 60L * 60L * 1000L;
        final long cutoff7d  = now - 7L  * 24L * 60L * 60L * 1000L;

        getAnalyticsRef(context)
                .orderByChild("timestamp")
                .startAt(cutoff7d) // we only care about last 7 days
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Map<String, Integer> counts24h = new HashMap<>();
                        Map<String, Integer> counts7d  = new HashMap<>();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            String type   = child.child("type").getValue(String.class);
                            String screen = child.child("screen").getValue(String.class);
                            Long tsObj    = child.child("timestamp").getValue(Long.class);

                            if (type == null || !"screen_view".equals(type)) {
                                continue; // ignore non screen-view events
                            }
                            if (screen == null || tsObj == null) {
                                continue;
                            }

                            long ts = tsObj;

                            // Count last 7 days
                            counts7d.put(screen,
                                    counts7d.getOrDefault(screen, 0) + 1);

                            // Count last 24 hours
                            if (ts >= cutoff24h) {
                                counts24h.put(screen,
                                        counts24h.getOrDefault(screen, 0) + 1);
                            }
                        }

                        // Log nicely so you can copy into the PDF
                        Log.d(TAG, "===== SCREEN USAGE (Last 24 hours) =====");
                        for (Map.Entry<String, Integer> e : counts24h.entrySet()) {
                            Log.d(TAG, "Screen: " + e.getKey() + " -> " + e.getValue());
                        }

                        Log.d(TAG, "===== SCREEN USAGE (Last 7 days) =====");
                        for (Map.Entry<String, Integer> e : counts7d.entrySet()) {
                            Log.d(TAG, "Screen: " + e.getKey() + " -> " + e.getValue());
                        }

                        // Extra line to help you extract table later
                        Log.d(TAG, "===== END OF ANALYTICS REPORT =====");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Analytics aggregation failed: " + error.getMessage());
                    }
                });
    }
}
