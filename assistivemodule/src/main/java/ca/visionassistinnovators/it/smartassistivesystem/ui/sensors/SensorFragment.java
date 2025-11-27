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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SensorFragment extends Fragment {

    private TextView textDistance, textLight, textColorName;
    private View viewIndicator, viewColorBox;
    private Button buttonVibrate;

    private DatabaseReference dbRef;

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

        readDistanceSensor();
        readLightSensor();
        readColorSensor();
        setupVibrationButton();

        return view;
    }

    private void readDistanceSensor() {
        dbRef.child("distance").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer distance = snapshot.getValue(Integer.class);
                if (distance == null) return;

                textDistance.setText(new StringBuilder().append(getString(R.string.distance1)).append(distance).append(" cm").toString());

                if (distance > 100) {
                    viewIndicator.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                } else if (distance >= 50) {
                    viewIndicator.setBackgroundColor(Color.parseColor("#FFC107")); // Yellow
                } else {
                    viewIndicator.setBackgroundColor(Color.parseColor("#F44336")); // Red
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void readLightSensor() {
        dbRef.child("light").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer lux = snapshot.getValue(Integer.class);
                if (lux == null) return;

                textLight.setText(String.format("%s%d lux", getString(R.string.light1), lux));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void readColorSensor() {
        dbRef.child("color").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
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
                    textColorName.setText(String.format(getString(R.string.color_s), name));
                } else
                    textColorName.setText(R.string.color_unknown);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupVibrationButton() {
        buttonVibrate.setOnClickListener(v -> {
            dbRef.child("vibration").child("trigger").setValue(true);
        });
    }
}
