/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.sensors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.EventLogger;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SensorFragment extends Fragment {

    private TextView textDistance, textLight, textColorName;
    private View viewIndicator, viewColorBox;
    private Button buttonVibrate;

    private DatabaseReference dbRef;

    // ---- OFFLINE CACHE KEYS ----
    private static final String PREF_OFFLINE_SENSORS = "sas_offline_sensors";
    private static final String KEY_DISTANCE = "offline_distance_cm";
    private static final String KEY_LIGHT = "offline_light_lux";
    private static final String KEY_COLOR_R = "offline_color_r";
    private static final String KEY_COLOR_G = "offline_color_g";
    private static final String KEY_COLOR_B = "offline_color_b";
    private static final String KEY_COLOR_NAME = "offline_color_name";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sensors, container, false);

        textDistance = view.findViewById(R.id.textDistance);
        viewIndicator = view.findViewById(R.id.viewIndicator);
        buttonVibrate = view.findViewById(R.id.buttonVibrate);

        textLight = view.findViewById(R.id.textLight);
        viewColorBox = view.findViewById(R.id.viewColorBox);
        textColorName = view.findViewById(R.id.textColorName);

        dbRef = FirebaseDatabase.getInstance().getReference("sensors");

        // 🔍 Analytics: screen view
        EventLogger.logScreenView(requireContext(), "SensorFragment");

        // ✅ Load last known snapshot for OFFLINE mode
        loadOfflineSnapshot(requireContext());

        // 🔴 Live Firebase listeners (ONLINE mode)
        readDistanceSensor();
        readLightSensor();
        readColorSensor();
        setupVibrationButton();

        return view;
    }

    private void readDistanceSensor() {
        dbRef.child("distance").addValueEventListener(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer distance = snapshot.getValue(Integer.class);
                        if (distance == null) return;

                        textDistance.setText(
                                new StringBuilder()
                                        .append(getString(R.string.distance1))
                                        .append(distance)
                                        .append(" cm")
                                        .toString()
                        );

                        if (distance > 100) {
                            viewIndicator.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                        } else if (distance >= 50) {
                            viewIndicator.setBackgroundColor(Color.parseColor("#FFC107")); // Yellow
                        } else {
                            viewIndicator.setBackgroundColor(Color.parseColor("#F44336")); // Red
                        }

                        // ✅ Save last distance reading for OFFLINE mode
                        saveDistanceOffline(requireContext(), distance);

                        // 🔍 Analytics: distance update
                        EventLogger.logEvent(
                                requireContext(),
                                "sensor_distance_update",
                                "distance_cm=" + distance
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void readLightSensor() {
        dbRef.child("light").addValueEventListener(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer lux = snapshot.getValue(Integer.class);
                        if (lux == null) return;

                        textLight.setText(
                                String.format("%s%d lux", getString(R.string.light1), lux)
                        );

                        // ✅ Save last light reading for OFFLINE mode
                        saveLightOffline(requireContext(), lux);

                        // 🔍 Analytics: light update
                        EventLogger.logEvent(
                                requireContext(),
                                "sensor_light_update",
                                "lux=" + lux
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void readColorSensor() {
        dbRef.child("color").addValueEventListener(
                new com.google.firebase.database.ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Integer r = snapshot.child("r").getValue(Integer.class);
                        Integer g = snapshot.child("g").getValue(Integer.class);
                        Integer b = snapshot.child("b").getValue(Integer.class);
                        String name = snapshot.child("name").getValue(String.class);

                        if (r == null || g == null || b == null) return;

                        viewColorBox.setBackgroundColor(Color.rgb(r, g, b));

                        if (name != null) {
                            textColorName.setText(
                                    String.format(getString(R.string.color_s), name)
                            );
                        } else {
                            textColorName.setText(R.string.color_unknown);
                        }

                        // ✅ Save last color reading for OFFLINE mode
                        saveColorOffline(requireContext(), r, g, b, name);

                        // 🔍 Analytics: color update
                        String detail = "r=" + r + ", g=" + g + ", b=" + b +
                                (name != null ? (", name=" + name) : "");
                        EventLogger.logEvent(
                                requireContext(),
                                "sensor_color_update",
                                detail
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupVibrationButton() {
        buttonVibrate.setOnClickListener(v -> {
            dbRef.child("vibration").child("trigger").setValue(true);

            // 🔍 Analytics: vibration trigger
            EventLogger.logEvent(
                    requireContext(),
                    "sensor_vibration_trigger",
                    "Vibration command sent to Arduino / ESP"
            );
        });
    }

    // ------------------------------
    // OFFLINE CACHE HELPERS
    // ------------------------------
    private void saveDistanceOffline(Context c, int distance) {
        SharedPreferences prefs = c.getSharedPreferences(
                PREF_OFFLINE_SENSORS, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_DISTANCE, distance)
                .apply();
    }

    private void saveLightOffline(Context c, int lux) {
        SharedPreferences prefs = c.getSharedPreferences(
                PREF_OFFLINE_SENSORS, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_LIGHT, lux)
                .apply();
    }

    private void saveColorOffline(Context c, int r, int g, int b, @Nullable String name) {
        SharedPreferences prefs = c.getSharedPreferences(
                PREF_OFFLINE_SENSORS, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_COLOR_R, r)
                .putInt(KEY_COLOR_G, g)
                .putInt(KEY_COLOR_B, b)
                .putString(KEY_COLOR_NAME, name != null ? name : "")
                .apply();
    }

    private void loadOfflineSnapshot(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(
                PREF_OFFLINE_SENSORS, Context.MODE_PRIVATE);

        boolean anyLoaded = false;

        // Distance
        if (prefs.contains(KEY_DISTANCE)) {
            int distance = prefs.getInt(KEY_DISTANCE, 0);
            textDistance.setText(
                    new StringBuilder()
                            .append(getString(R.string.distance1))
                            .append(distance)
                            .append(" cm")
                            .toString()
            );

            if (distance > 100) {
                viewIndicator.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            } else if (distance >= 50) {
                viewIndicator.setBackgroundColor(Color.parseColor("#FFC107")); // Yellow
            } else {
                viewIndicator.setBackgroundColor(Color.parseColor("#F44336")); // Red
            }
            anyLoaded = true;
        }

        // Light
        if (prefs.contains(KEY_LIGHT)) {
            int lux = prefs.getInt(KEY_LIGHT, 0);
            textLight.setText(
                    String.format("%s%d lux", getString(R.string.light1), lux)
            );
            anyLoaded = true;
        }

        // Color
        if (prefs.contains(KEY_COLOR_R)
                && prefs.contains(KEY_COLOR_G)
                && prefs.contains(KEY_COLOR_B)) {

            int r = prefs.getInt(KEY_COLOR_R, 0);
            int g = prefs.getInt(KEY_COLOR_G, 0);
            int b = prefs.getInt(KEY_COLOR_B, 0);
            viewColorBox.setBackgroundColor(Color.rgb(r, g, b));

            String name = prefs.getString(KEY_COLOR_NAME, "");
            if (name != null && !name.isEmpty()) {
                textColorName.setText(
                        String.format(getString(R.string.color_s), name)
                );
            } else {
                textColorName.setText(R.string.color_unknown);
            }
            anyLoaded = true;
        }

        if (anyLoaded) {
            // 🔍 Analytics: offline snapshot used
            EventLogger.logEvent(
                    c,
                    "sensors_offline_snapshot_loaded",
                    "Loaded cached sensor values when screen opened."
            );
        }
    }
}
