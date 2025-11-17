package ca.visionassistinnovators.it.smartassistivesystem.ui.detection;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import ca.visionassistinnovators.it.smartassistivesystem.R;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FallDetectionFragment extends Fragment {

    private TextView textFallStatus;
    private ImageView imgStatus;
    private Button buttonSimulate;
    private DatabaseReference fallRef;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        fallRef = FirebaseDatabase.getInstance().getReference("sensors").child("fall");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fall_detection, container, false);

        textFallStatus = view.findViewById(R.id.textFallStatus);
        imgStatus = view.findViewById(R.id.imgStatus);
        buttonSimulate = view.findViewById(R.id.buttonSimulateFall);

        readFallStatus();
        setupSimulationButton();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(accelListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelListener);
    }

    private final SensorEventListener accelListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            double ax = event.values[0];
            double ay = event.values[1];
            double az = event.values[2];

            double magnitude = Math.sqrt(ax * ax + ay * ay + az * az);

            if (magnitude > 25) {  // Sudden impact
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                fallRef.child("detected").setValue(true);
                fallRef.child("timestamp").setValue(timeStamp);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void readFallStatus() {
        fallRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean detected = snapshot.child("detected").getValue(Boolean.class);
                String time = snapshot.child("timestamp").getValue(String.class);

                if (detected != null && detected) {
                    showAlert(time);
                    textFallStatus.setText("Fall Detected at: " + time);
                    imgStatus.setColorFilter(0xFFD32F2F); // red
                } else {
                    textFallStatus.setText("No Fall Detected");
                    imgStatus.setColorFilter(0xFF4CAF50); // green
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSimulationButton() {
        buttonSimulate.setOnClickListener(v -> {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            fallRef.child("detected").setValue(true);
            fallRef.child("timestamp").setValue(timeStamp);
        });
    }

    private void showAlert(String time) {
        new AlertDialog.Builder(getContext())
                .setTitle("⚠️ Emergency Alert")
                .setMessage("Possible fall detected!\nTime: " + time)
                .setPositiveButton("OK", null)
                .show();
    }
}
