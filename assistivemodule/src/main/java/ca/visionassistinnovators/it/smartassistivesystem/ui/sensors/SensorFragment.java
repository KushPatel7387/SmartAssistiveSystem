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

    private DatabaseReference sensorRef;
    private ValueEventListener listener;

    public SensorFragment() { /* empty */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sensors, container, false);

        tvLightValue = root.findViewById(R.id.txtLightValue);      // show a friendly label
        tvCloudValue = root.findViewById(R.id.txtFromFirebase);    // “Cloud Value: …”
        tvUpdatedAt  = root.findViewById(R.id.txtUpdatedAt);       // add this TextView in layout if missing
        progress     = root.findViewById(R.id.progress);           // add a small ProgressBar in layout if missing

        // Point to your RTDB path: SensorData/LightSensor { latest: <float>, timestamp: <string> }
        sensorRef = FirebaseDatabase
                .getInstance("https://smartassistivesystem-39072-default-rtdb.firebaseio.com/")
                .getReference("SensorData")
                .child("LightSensor");

        // Initial UI
        if (progress != null) progress.setVisibility(View.VISIBLE);
        tvLightValue.setText("Light Level (from cloud)");
        tvCloudValue.setText("Cloud Value: —");
        if (tvUpdatedAt != null) tvUpdatedAt.setText("Updated: —");

        // Realtime listener — READ ONLY
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!isAdded()) return;
                if (progress != null) progress.setVisibility(View.GONE);

                if (snap.exists()) {
                    Object latest = snap.child("latest").getValue();
                    Object ts     = snap.child("timestamp").getValue();

                    String latestText = (latest == null) ? "—" : String.valueOf(latest);
                    String timeText   = (ts == null) ? "—" : String.valueOf(ts);

                    tvCloudValue.setText("Cloud Value: " + latestText + " lx");
                    if (tvUpdatedAt != null) tvUpdatedAt.setText("Updated: " + timeText);
                } else {
                    tvCloudValue.setText("Cloud Value: —");
                    if (tvUpdatedAt != null) tvUpdatedAt.setText("Updated: —");
                    Toast.makeText(requireContext(), "No sensor data found in DB", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                if (progress != null) progress.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        "DB read error: " + error.getMessage(),
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
}
