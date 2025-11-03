/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Daksh Rana – N01664095
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 */

package ca.visionassistinnovators.it.smartassistivesystem.ui.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SensorFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView lightValueText;
    private DatabaseReference sensorRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sensors, container, false);

        lightValueText = root.findViewById(R.id.txtLightValue);

        // ✅ Initialize the Sensor Manager and Light Sensor
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(requireContext(), "Light sensor not available on this device", Toast.LENGTH_SHORT).show();
        }

        // ✅ Firebase reference (SensorData → LightSensor)
        sensorRef = FirebaseDatabase.getInstance()
                .getReference("SensorData")
                .child("LightSensor");

        // ✅ Listen for value changes from Firebase
        sensorRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object value = snapshot.child("latest").getValue();
                    TextView cloudText = root.findViewById(R.id.txtFromFirebase);
                    cloudText.setText("Cloud Value: " + value + " lx");
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to read from DB", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float lux = event.values[0];
        lightValueText.setText("Light Level: " + lux + " lx");

        // ✅ Push sensor data to Firebase with timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        sensorRef.child("latest").setValue(lux);
        sensorRef.child("timestamp").setValue(timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
