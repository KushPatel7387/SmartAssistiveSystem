/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Daksh Rana – N01664095
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.sensors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SensorFragment extends Fragment {

    private TextView tvLightValue, tvCloudValue, tvUpdatedAt;
    private ProgressBar progress;

    // 🔹 NEW: 4 sensor labels + bar-graphs (no external lib)
    private TextView tvSensorLight, tvSensorColor, tvSensorDistance, tvSensorUv;
    private ProgressBar barLight, barColor, barDistance, barUv;

    private DatabaseReference sensorRef;
    private ValueEventListener listener;

    public SensorFragment() { /* empty */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sensors, container, false);

        tvLightValue = root.findViewById(R.id.txtLightValue);
        tvCloudValue = root.findViewById(R.id.txtFromFirebase);
        tvUpdatedAt  = root.findViewById(R.id.txtUpdatedAt);
        progress     = root.findViewById(R.id.progress);

        // 🔹 NEW: findViewById for 4 sensor text + bars (make sure IDs exist in XML)
        tvSensorLight    = root.findViewById(R.id.txtSensorLight);
        tvSensorColor    = root.findViewById(R.id.txtSensorColor);
        tvSensorDistance = root.findViewById(R.id.txtSensorDistance);
        tvSensorUv       = root.findViewById(R.id.txtSensorUv);

        barLight   = root.findViewById(R.id.barLight);
        barColor   = root.findViewById(R.id.barColor);
        barDistance= root.findViewById(R.id.barDistance);
        barUv      = root.findViewById(R.id.barUV);

        // DB path from strings.xml (no hardcoding)
        sensorRef = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference(getString(R.string.rtdb_path_sensor_data))
                .child(getString(R.string.rtdb_node_light_sensor));

        // Initial UI (from strings)
        if (progress != null) progress.setVisibility(View.VISIBLE);
        tvLightValue.setText(R.string.light_level_title);
        tvCloudValue.setText(getString(R.string.cloud_value_fmt, "—"));
        if (tvUpdatedAt != null) tvUpdatedAt.setText(getString(R.string.updated_fmt, "—"));

        // 🔹 Initial dummy sensor values (manual) – so graphs not empty
        applySensorValues(10f, 30f, 50f, 70f, false);

        // Realtime listener — READ ONLY
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!isAdded()) return;
                if (progress != null) progress.setVisibility(View.GONE);

                if (snap.exists()) {
                    Object latest = snap.child(getString(R.string.rtdb_field_latest)).getValue();
                    Object ts     = snap.child(getString(R.string.rtdb_field_timestamp)).getValue();

                    String latestText = (latest == null) ? "—" : String.valueOf(latest);
                    String timeText   = (ts == null) ? "—" : String.valueOf(ts);

                    tvCloudValue.setText(getString(R.string.cloud_value_fmt, latestText));
                    if (tvUpdatedAt != null) {
                        tvUpdatedAt.setText(getString(R.string.updated_fmt, timeText));
                    }

                    // 🔹 Try to parse light value from Firebase
                    float lightValue = 0f;
                    try {
                        if (latest != null) {
                            lightValue = Float.parseFloat(String.valueOf(latest));
                        }
                    } catch (NumberFormatException e) {
                        lightValue = 0f;
                    }

                    // 🔹 Other sensors currently manual/demo (no Firebase)
                    float colorValue    = 40f;
                    float distanceValue = 60f;
                    float uvValue       = 20f;

                    // All 4 sensors update + bar-graph update
                    applySensorValues(lightValue, colorValue, distanceValue, uvValue, true);

                } else {
                    tvCloudValue.setText(getString(R.string.cloud_value_fmt, "—"));
                    if (tvUpdatedAt != null) {
                        tvUpdatedAt.setText(getString(R.string.updated_fmt, "—"));
                    }
                    Toast.makeText(requireContext(),
                            R.string.no_sensor_data, Toast.LENGTH_SHORT).show();

                    // 🔹 If no data in DB, keep manual demo values
                    applySensorValues(10f, 30f, 50f, 70f, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                if (progress != null) progress.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        getString(R.string.db_read_error_fmt, error.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        };

        sensorRef.addValueEventListener(listener);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sensorRef != null && listener != null) {
            sensorRef.removeEventListener(listener);
        }
    }

    // 🔹 Helper: update all 4 sensor labels + progress bars
    private void applySensorValues(float light, float color,
                                   float distance, float uv,
                                   boolean fromDb) {

        String source = fromDb ? "Firebase" : "Manual demo";
        // tvCloudValue already shows latest value; source info add karva hoy to:
        // tvCloudValue.setText(getString(R.string.cloud_value_fmt, source));

        if (tvSensorLight != null) {
            tvSensorLight.setText("Light: " + light);
        }
        if (tvSensorColor != null) {
            tvSensorColor.setText("Color: " + color);
        }
        if (tvSensorDistance != null) {
            tvSensorDistance.setText("Distance: " + distance);
        }
        if (tvSensorUv != null) {
            tvSensorUv.setText("UV: " + uv);
        }

        if (barLight != null) {
            barLight.setProgress(scaleToProgress(light));
        }
        if (barColor != null) {
            barColor.setProgress(scaleToProgress(color));
        }
        if (barDistance != null) {
            barDistance.setProgress(scaleToProgress(distance));
        }
        if (barUv != null) {
            barUv.setProgress(scaleToProgress(uv));
        }
    }

    // 🔹 Scale float 0–100 for horizontal progress bars
    private int scaleToProgress(float value) {
        if (Float.isNaN(value)) return 0;
        if (value < 0f) return 0;
        if (value > 100f) return 100;
        return Math.round(value);
    }
}
