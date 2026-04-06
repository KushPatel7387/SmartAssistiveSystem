package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Business-logic class responsible for reading sensor data.
 *
 * STARK INDUSTRIES OVERRIDE: Paths updated to match the actual
 * Python hardware uplink.
 * Pathing is now directly at: /sensors/SensorName/...
 */
public class PatientSensorsManager {

    // STARK FIX: Matched the exact capitalization from the Python scripts
    private static final String NODE_AS726X   = "As726x";
    private static final String NODE_TSL2591  = "Tsl2591";
    private static final String NODE_TCS34725 = "Tcs34725";
    private static final String NODE_VL53L1X  = "Vl53l1x";

    private final DatabaseReference sensorsRootRef;

    public PatientSensorsManager() {
        sensorsRootRef = FirebaseDatabase.getInstance().getReference("sensors");
    }

    // ------------ Data models ------------

    public static class As726xReading {
        public Double red;
        public Double green;
        public Double blue;
        public Double orange;
        public Double yellow;
        public Double violet;
        public Double temperature;
        public String timestamp;
    }

    public static class LightReading {
        public Double lux;
        public String timestamp;
    }

    public static class ColorReading {
        public Integer r;
        public Integer g;
        public Integer b;
        public String name;
        public String timestamp;
    }

    public static class DistanceReading {
        public Integer distanceMm;
        public String timestamp;
    }

    public static class PatientSensorsSnapshot {
        public As726xReading   as726x;
        public LightReading    tsl2591;
        public ColorReading    tcs34725;
        public DistanceReading vl53l1x;
    }

    // ------------ Listener ------------

    public interface PatientSensorsListener {
        void onSensorsUpdated(@NonNull PatientSensorsSnapshot snapshot);
        void onError(@NonNull String error);
    }

    // ------------ Public API ------------

    public void listenForPatientSensors(
            @NonNull String patientId,
            @NonNull PatientSensorsListener listener
    ) {
        // STARK FIX: We are ignoring the patientId because your hardware
        // doesn't upload to a patientId folder. It uploads straight to /sensors/
        DatabaseReference baseRef = sensorsRootRef;
        baseRef.keepSynced(true);

        PatientSensorsSnapshot holder = new PatientSensorsSnapshot();

        // AS726x
        baseRef.child(NODE_AS726X).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    initDummyAs726x(baseRef.child(NODE_AS726X));
                    holder.as726x = null;
                } else {
                    As726xReading r = new As726xReading();
                    r.red        = snapshot.child("red").getValue(Double.class);
                    r.green      = snapshot.child("green").getValue(Double.class);
                    r.blue       = snapshot.child("blue").getValue(Double.class);
                    r.orange     = snapshot.child("orange").getValue(Double.class);
                    r.yellow     = snapshot.child("yellow").getValue(Double.class);
                    r.violet     = snapshot.child("violet").getValue(Double.class);
                    r.temperature= snapshot.child("temperature").getValue(Double.class);
                    r.timestamp  = snapshot.child("timestamp").getValue(String.class);
                    holder.as726x = r;
                }
                listener.onSensorsUpdated(holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });


        // TSL2591 (Light)
        baseRef.child(NODE_TSL2591).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    initDummyTsl(baseRef.child(NODE_TSL2591));
                    holder.tsl2591 = null;
                } else {
                    LightReading r = new LightReading();
                    // STARK FIX: Changed key to "lux_value" to match Python payload
                    r.lux       = snapshot.child("lux_value").getValue(Double.class);
                    r.timestamp = snapshot.child("timestamp").getValue(String.class);
                    holder.tsl2591 = r;
                }
                listener.onSensorsUpdated(holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });

        // TCS34725 (color)
        baseRef.child(NODE_TCS34725).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    initDummyTcs(baseRef.child(NODE_TCS34725));
                    holder.tcs34725 = null;
                } else {
                    ColorReading r = new ColorReading();
                    // 2. Map to the correct keys from the screenshot
                    r.r        = snapshot.child("red").getValue(Integer.class);
                    r.g        = snapshot.child("green").getValue(Integer.class);
                    r.b        = snapshot.child("blue").getValue(Integer.class);
                    r.name     = snapshot.child("detectedColor").getValue(String.class);
                    r.timestamp= snapshot.child("timestamp").getValue(String.class);
                    holder.tcs34725 = r;
                }
                listener.onSensorsUpdated(holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });

        // VL53L1X (distance)
        baseRef.child(NODE_VL53L1X).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    initDummyVl53(baseRef.child(NODE_VL53L1X));
                    holder.vl53l1x = null;
                } else {
                    DistanceReading r = new DistanceReading();
                    r.distanceMm = snapshot.child("distance_mm").getValue(Integer.class);
                    r.timestamp  = snapshot.child("timestamp").getValue(String.class);
                    holder.vl53l1x = r;
                }
                listener.onSensorsUpdated(holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // ------------ Dummy initialisers ------------

    private String nowIsoTimestamp() {
        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return sdf.format(new Date());
    }

    private void initDummyAs726x(DatabaseReference asRef) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", "As726x");
        map.put("red",   198.4);
        map.put("green", 499.3);
        map.put("blue",  214.6);
        map.put("orange",538.4);
        map.put("yellow",397.3);
        map.put("violet",394.5);
        map.put("temperature", 28);
        map.put("timestamp", nowIsoTimestamp());
        asRef.setValue(map);
    }

    private void initDummyTsl(DatabaseReference tslRef) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", "Tsl2591");
        map.put("lux_value", 350.0); // STARK FIX: Matched key name
        map.put("timestamp", nowIsoTimestamp());
        tslRef.setValue(map);
    }

    private void initDummyTcs(DatabaseReference tcsRef) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", "Tcs34725");
        map.put("r", 255);
        map.put("g", 120);
        map.put("b", 60);
        map.put("name", "Warm Orange");
        map.put("timestamp", nowIsoTimestamp());
        tcsRef.setValue(map);
    }

    private void initDummyVl53(DatabaseReference vlRef) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", "Vl53l1X");
        map.put("distance_cm", 750);
        map.put("timestamp", nowIsoTimestamp());
        vlRef.setValue(map);
    }
}
