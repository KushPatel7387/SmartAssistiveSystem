package ca.visionassistinnovators.it.smartassistivesystem.ui.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class PatientLocationFragment extends Fragment implements LocationListener {

    private GoogleMap map;
    private DatabaseReference dbRef;
    private LocationManager locationManager;

    private String patientId; // REAL patient ID

    public PatientLocationFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ----------------------------------
        // 1️⃣ DETERMINE WHICH PATIENT TO LOAD
        // ----------------------------------
        if (getArguments() != null && getArguments().containsKey("patientId")) {
            // Caretaker selected a patient
            patientId = getArguments().getString("patientId");
        } else {
            // Patient is viewing their own location
            patientId = FirebaseAuth.getInstance().getUid();
        }

        // ----------------------------------
        // 2️⃣ DATABASE REFERENCE
        // ----------------------------------
        dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(patientId)
                .child("location");

        // ----------------------------------
        // 3️⃣ MAP INITIALIZE
        // ----------------------------------
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map_patient_location);

        mapFragment.getMapAsync(callback);

        // ----------------------------------
        // 4️⃣ START UPLOADING LOCATION ONLY IF:
        // This device belongs to the patient
        // ----------------------------------
        if (FirebaseAuth.getInstance().getUid().equals(patientId)) {
            startSendingLocation();  // patient uploads location
        }
        // Caretaker will NOT upload
    }

    private final OnMapReadyCallback callback = googleMap -> {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        listenForLiveLocation();
    };

    @SuppressLint("MissingPermission")
    private void startSendingLocation() {

        locationManager = (LocationManager)
                requireContext().getSystemService(requireContext().LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);

            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1500,   // every 1.5 sec
                1,      // 1 meter change
                this
        );
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        // Only PATIENT executes this (caretaker does NOT call this)
        if (!FirebaseAuth.getInstance().getUid().equals(patientId)) return;

        HashMap<String, Object> map = new HashMap<>();
        map.put("latitude", location.getLatitude());
        map.put("longitude", location.getLongitude());
        map.put("updatedAt", System.currentTimeMillis());

        dbRef.setValue(map);
    }

    // ----------------------------------
    // 🔴 CARETAKER / PATIENT → READ LOCATION
    // ----------------------------------
    private void listenForLiveLocation() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Double lat = snapshot.child("latitude").getValue(Double.class);
                Double lng = snapshot.child("longitude").getValue(Double.class);

                if (lat == null || lng == null) return;

                LatLng loc = new LatLng(lat, lng);

                map.clear();

                map.addMarker(new MarkerOptions()
                        .position(loc)
                        .title("Patient Location"));

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17));

                map.addCircle(new CircleOptions()
                        .center(loc)
                        .radius(100)
                        .strokeWidth(4)
                        .strokeColor(0xFF6200EE)
                        .fillColor(0x226200EE));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }
}
