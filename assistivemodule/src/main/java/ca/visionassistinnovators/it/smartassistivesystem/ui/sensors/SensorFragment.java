/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Daksh Rana – N01664095
 * Kush Patel – N01657387
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.GuardianPatientManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientModel;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientSensorsManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientSensorsManager.As726xReading;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientSensorsManager.ColorReading;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientSensorsManager.DistanceReading;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientSensorsManager.LightReading;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientSensorsManager.PatientSensorsListener;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientSensorsManager.PatientSensorsSnapshot;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.EventLogger;

public class SensorFragment extends Fragment {

    private LinearLayout patientListLayout;
    private TextView emptyText;

    private GuardianPatientManager patientManager;
    private PatientSensorsManager sensorsManager;

    // -------- OFFLINE SENSOR CACHE --------
    private static final String PREF_SENSOR_CACHE = "sas_sensor_cache";

    public SensorFragment() { }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_sensors, container, false);

        patientListLayout = view.findViewById(R.id.layout_patient_sensor_list);
        emptyText         = view.findViewById(R.id.tv_sensors_empty);

        patientManager = new GuardianPatientManager();
        sensorsManager = new PatientSensorsManager();

        EventLogger.logScreenView(requireContext(), "Sensors");

        loadPatientsAndBindSensors();
        return view;
    }

    // -------------------------------------------------------
    // Load patients and create one card per patient
    // -------------------------------------------------------
    private void loadPatientsAndBindSensors() {

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            showEmpty("Not Logged In");
            return;
        }

        Context ctx = getContext();
        if (ctx == null) return;

        patientManager.listenForPatients(
                ctx,
                current.getUid(),
                new GuardianPatientManager.PatientListListener() {
                    @Override
                    public void onPatientsChanged(List<PatientModel> patients) {

                        if (!isAdded()) return;

                        if (patients == null || patients.isEmpty()) {
                            showEmpty("No Patients Found");
                            return;
                        }

                        emptyText.setVisibility(View.GONE);
                        patientListLayout.removeAllViews();

                        for (PatientModel patient : patients) {
                            addPatientCard(ctx, patient);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        showEmpty("DB Error: " + error);
                    }
                }
        );
    }

    private void showEmpty(String message) {
        patientListLayout.removeAllViews();
        emptyText.setText(message);
        emptyText.setVisibility(View.VISIBLE);
    }

    // -------------------------------------------------------
    // Create one card and attach sensor listeners
    // -------------------------------------------------------
    private void addPatientCard(@NonNull Context context,
                                @NonNull PatientModel patient) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View cardView = inflater.inflate(
                R.layout.item_patient_sensors,
                patientListLayout,
                false
        );

        TextView tvPatientName     = cardView.findViewById(R.id.tv_patient_name);
        TextView tvAs726x          = cardView.findViewById(R.id.tv_as726x_value);
        TextView tvTsl             = cardView.findViewById(R.id.tv_tsl_value);
        TextView tvTcs             = cardView.findViewById(R.id.tv_tcs_value);
        TextView tvVl53            = cardView.findViewById(R.id.tv_vl53_value);
        TextView tvLastUpdated     = cardView.findViewById(R.id.tv_last_updated);

        ImageView ivAs726xStatus   = cardView.findViewById(R.id.iv_as726x_status);
        ImageView ivTslStatus      = cardView.findViewById(R.id.iv_tsl_status);
        ImageView ivTcsStatus      = cardView.findViewById(R.id.iv_tcs_status);
        ImageView ivVl53Status     = cardView.findViewById(R.id.iv_vl53_status);

        final String patientName = patient.fullName != null ? patient.fullName : "Patient";
        final String patientId   = patient.id;

        tvPatientName.setText(patientName);

        // ✅ LOAD LAST SAVED SENSOR VALUES FIRST (OFFLINE SUPPORT)
        loadLastSensorValues(context, patientId,
                tvAs726x, tvTsl, tvTcs, tvVl53, tvLastUpdated,
                ivAs726xStatus, ivTslStatus, ivTcsStatus, ivVl53Status
        );

        patientListLayout.addView(cardView);

        if (patientId == null || patientId.isEmpty()) return;

        sensorsManager.listenForPatientSensors(patientId, new PatientSensorsListener() {
            @Override
            public void onSensorsUpdated(@NonNull PatientSensorsSnapshot snapshot) {

                String as726x = formatAs726x(snapshot.as726x);
                String tsl    = formatLight(snapshot.tsl2591);
                String tcs    = formatColor(snapshot.tcs34725);
                String vl53   = formatDistance(snapshot.vl53l1x);

                tvAs726x.setText(as726x);
                tvTsl.setText(tsl);
                tvTcs.setText(tcs);
                tvVl53.setText(vl53);

                setSensorStatus(ivAs726xStatus, snapshot.as726x != null);
                setSensorStatus(ivTslStatus, snapshot.tsl2591 != null);
                setSensorStatus(ivTcsStatus, snapshot.tcs34725 != null);
                setSensorStatus(ivVl53Status, snapshot.vl53l1x != null);

                String ts = String.valueOf(System.currentTimeMillis());
                tvLastUpdated.setText("Last Updated: " + ts);

                // ✅ SAVE LAST KNOWN VALUES
                saveLastSensorValues(context, patientId,
                        as726x, tsl, tcs, vl53, ts);
            }

            @Override
            public void onError(@NonNull String error) { }
        });
    }

    // -------------------------------------------------------
    // ✅ OFFLINE SAVE + LOAD (PER PATIENT)
    // -------------------------------------------------------

    private void saveLastSensorValues(Context ctx, String patientId,
                                      String as726x, String tsl,
                                      String tcs, String vl53, String ts) {

        SharedPreferences prefs =
                ctx.getSharedPreferences(PREF_SENSOR_CACHE, Context.MODE_PRIVATE);

        prefs.edit()
                .putString(patientId + "_as726x", as726x)
                .putString(patientId + "_tsl", tsl)
                .putString(patientId + "_tcs", tcs)
                .putString(patientId + "_vl53", vl53)
                .putString(patientId + "_ts", ts)
                .apply();
    }

    private void loadLastSensorValues(Context ctx, String patientId,
                                      TextView as726x, TextView tsl,
                                      TextView tcs, TextView vl53,
                                      TextView ts,
                                      ImageView i1, ImageView i2,
                                      ImageView i3, ImageView i4) {

        SharedPreferences prefs =
                ctx.getSharedPreferences(PREF_SENSOR_CACHE, Context.MODE_PRIVATE);

        as726x.setText(prefs.getString(patientId + "_as726x", "No Data"));
        tsl.setText(prefs.getString(patientId + "_tsl", "No Data"));
        tcs.setText(prefs.getString(patientId + "_tcs", "No Data"));
        vl53.setText(prefs.getString(patientId + "_vl53", "No Data"));

        String lastTs = prefs.getString(patientId + "_ts", null);
        if (lastTs != null) ts.setText("Last Updated: " + lastTs);

        setSensorStatus(i1, true);
        setSensorStatus(i2, true);
        setSensorStatus(i3, true);
        setSensorStatus(i4, true);
    }

    // -------------------------------------------------------
    // Formatting helpers
    // -------------------------------------------------------

    private String formatAs726x(@Nullable As726xReading r) {
        if (r == null || r.red == null) return "No Data";
        return "R:" + r.red + " G:" + r.green + " B:" + r.blue;
    }

    private String formatLight(@Nullable LightReading r) {
        if (r == null || r.lux == null) return "No Data";
        return "Lux: " + r.lux;
    }

    private String formatColor(@Nullable ColorReading r) {
        if (r == null || r.r == null) return "No Data";
        return r.name + " (" + r.r + "," + r.g + "," + r.b + ")";
    }

    private String formatDistance(@Nullable DistanceReading r) {
        if (r == null || r.distanceMm == null) return "No Data";
        return "Distance: " + r.distanceMm + " mm";
    }

    private void setSensorStatus(@NonNull ImageView icon, boolean ok) {
        icon.setImageResource(
                ok ? android.R.drawable.presence_online
                        : android.R.drawable.presence_busy
        );
    }
}
