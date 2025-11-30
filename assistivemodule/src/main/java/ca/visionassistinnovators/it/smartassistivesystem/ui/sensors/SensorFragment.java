/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.sensors;

import android.content.Context;
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

        // Analytics – screen view (safe here, fragment is attached)
        EventLogger.logScreenView(requireContext(), "Sensors");

        loadPatientsAndBindSensors();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Analytics again on resume
        if (isAdded()) {
            EventLogger.logScreenView(requireContext(), "Sensors");
        }
    }

    // -------------------------------------------------------
    // Load patients and create one card per patient
    // -------------------------------------------------------
    private void loadPatientsAndBindSensors() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            if (isAdded()) {
                showEmpty(getString(R.string.not_logged_in));
            }
            return;
        }

        Context ctx = getContext();
        if (ctx == null) {
            // Fragment not attached yet / already detached
            return;
        }

        patientManager.listenForPatients(
                ctx,
                current.getUid(),
                new GuardianPatientManager.PatientListListener() {
                    @Override
                    public void onPatientsChanged(List<PatientModel> patients) {
                        // 🔒 Guard: fragment must still be attached
                        if (!isAdded()) {
                            return;
                        }

                        if (patients == null || patients.isEmpty()) {
                            showEmpty(getString(R.string.no_sensor_data));
                            return;
                        }

                        emptyText.setVisibility(View.GONE);
                        patientListLayout.removeAllViews();

                        Context context = getContext();
                        if (context == null) return;

                        for (PatientModel patient : patients) {
                            addPatientCard(context, patient);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        showEmpty(getString(R.string.db_read_error_fmt, error));
                    }
                }
        );
    }

    private void showEmpty(String message) {
        if (!isAdded()) return;
        patientListLayout.removeAllViews();
        emptyText.setText(message);
        emptyText.setVisibility(View.VISIBLE);
    }

    // -------------------------------------------------------
    // Create one card and attach sensor listeners
    // -------------------------------------------------------
    private void addPatientCard(@NonNull Context context,
                                @NonNull PatientModel patient) {
        if (!isAdded()) {
            // Safety: fragment already detached
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View cardView = inflater.inflate(
                R.layout.item_patient_sensors,
                patientListLayout,
                false
        );

        TextView tvPatientName     = cardView.findViewById(R.id.tv_patient_name);
        TextView tvPatientSubtitle = cardView.findViewById(R.id.tv_patient_subtitle);
        TextView tvAs726x          = cardView.findViewById(R.id.tv_as726x_value);
        TextView tvTsl             = cardView.findViewById(R.id.tv_tsl_value);
        TextView tvTcs             = cardView.findViewById(R.id.tv_tcs_value);
        TextView tvVl53            = cardView.findViewById(R.id.tv_vl53_value);
        TextView tvLastUpdated     = cardView.findViewById(R.id.tv_last_updated);

        ImageView ivAs726xStatus   = cardView.findViewById(R.id.iv_as726x_status);
        ImageView ivTslStatus      = cardView.findViewById(R.id.iv_tsl_status);
        ImageView ivTcsStatus      = cardView.findViewById(R.id.iv_tcs_status);
        ImageView ivVl53Status     = cardView.findViewById(R.id.iv_vl53_status);

        final String patientName = (patient.fullName != null) ? patient.fullName : "";
        final String patientId   = patient.id; // set in GuardianPatientManager

        tvPatientName.setText(patientName);
        tvPatientSubtitle.setText(
                getString(R.string.sensor_patient_subtitle)
        );

        // default: all sensors "no data yet"
        setSensorStatus(ivAs726xStatus, false);
        setSensorStatus(ivTslStatus, false);
        setSensorStatus(ivTcsStatus, false);
        setSensorStatus(ivVl53Status, false);

        tvAs726x.setText(getString(R.string.sensor_initializing_as726x));
        tvTsl.setText(getString(R.string.sensor_initializing_tsl2591));
        tvTcs.setText(getString(R.string.sensor_initializing_tcs34725));
        tvVl53.setText(getString(R.string.sensor_initializing_vl53l1x));
        tvLastUpdated.setText(getString(R.string.sensor_last_updated_unknown));

        patientListLayout.addView(cardView);

        if (patientId == null || patientId.isEmpty()) {
            // No sensors without an ID
            return;
        }

        sensorsManager.listenForPatientSensors(patientId, new PatientSensorsListener() {
            @Override
            public void onSensorsUpdated(@NonNull PatientSensorsSnapshot snapshot) {
                if (!isAdded()) return;

                tvAs726x.setText(formatAs726x(snapshot.as726x));
                tvTsl.setText(formatLight(snapshot.tsl2591));
                tvTcs.setText(formatColor(snapshot.tcs34725));
                tvVl53.setText(formatDistance(snapshot.vl53l1x));

                // Status icons: tick if we have meaningful data
                setSensorStatus(
                        ivAs726xStatus,
                        snapshot.as726x != null && snapshot.as726x.red != null
                );
                setSensorStatus(
                        ivTslStatus,
                        snapshot.tsl2591 != null && snapshot.tsl2591.lux != null
                );
                setSensorStatus(
                        ivTcsStatus,
                        snapshot.tcs34725 != null
                                && snapshot.tcs34725.r != null
                                && snapshot.tcs34725.g != null
                                && snapshot.tcs34725.b != null
                );
                setSensorStatus(
                        ivVl53Status,
                        snapshot.vl53l1x != null && snapshot.vl53l1x.distanceMm != null
                );

                // Last updated (pick first non-null timestamp)
                String ts = null;
                if (snapshot.as726x != null && snapshot.as726x.timestamp != null) {
                    ts = snapshot.as726x.timestamp;
                } else if (snapshot.tsl2591 != null && snapshot.tsl2591.timestamp != null) {
                    ts = snapshot.tsl2591.timestamp;
                } else if (snapshot.tcs34725 != null && snapshot.tcs34725.timestamp != null) {
                    ts = snapshot.tcs34725.timestamp;
                } else if (snapshot.vl53l1x != null && snapshot.vl53l1x.timestamp != null) {
                    ts = snapshot.vl53l1x.timestamp;
                }

                if (ts != null && !ts.isEmpty()) {
                    tvLastUpdated.setText(
                            getString(R.string.sensor_last_updated, ts)
                    );
                } else {
                    tvLastUpdated.setText(
                            getString(R.string.sensor_last_updated_unknown)
                    );
                }
            }

            @Override
            public void onError(@NonNull String error) {
                if (!isAdded()) return;
                tvAs726x.setText(
                        getString(R.string.db_read_error_fmt, error)
                );
            }
        });
    }

    // -------------------------------------------------------
    // Formatting helpers – all UI text via resources
    // -------------------------------------------------------

    private String formatAs726x(@Nullable As726xReading r) {
        if (r == null) {
            return getString(R.string.sensor_initializing_as726x);
        }

        StringBuilder sb = new StringBuilder();
        if (r.red != null) {
            sb.append("R: ")
                    .append(formatDouble(r.red))
                    .append("  ");
        }
        if (r.green != null) {
            sb.append("G: ")
                    .append(formatDouble(r.green))
                    .append("  ");
        }
        if (r.blue != null) {
            sb.append("B: ")
                    .append(formatDouble(r.blue))
                    .append("  ");
        }
        if (r.temperature != null) {
            sb.append(
                    getString(R.string.sensor_temp_suffix, r.temperature.intValue())
            );
        }

        if (sb.length() == 0) {
            return getString(R.string.sensor_initializing_as726x);
        }
        return sb.toString();
    }

    private String formatLight(@Nullable LightReading r) {
        if (r == null || r.lux == null) {
            return getString(R.string.sensor_initializing_tsl2591);
        }
        return getString(
                R.string.sensor_light_label,
                r.lux
        );
    }

    private String formatColor(@Nullable ColorReading r) {
        if (r == null || r.r == null || r.g == null || r.b == null) {
            return getString(R.string.sensor_initializing_tcs34725);
        }

        String label = (r.name != null && !r.name.isEmpty())
                ? r.name
                : getString(R.string.color_unknown);

        return getString(
                R.string.sensor_color_label,
                label,
                r.r, r.g, r.b
        );
    }

    private String formatDistance(@Nullable DistanceReading r) {
        if (r == null || r.distanceMm == null) {
            return getString(R.string.sensor_initializing_vl53l1x);
        }
        return getString(
                R.string.sensor_distance_label,
                r.distanceMm
        );
    }

    private String formatDouble(@NonNull Double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    // -------------------------------------------------------
    // Status icon helper
    // -------------------------------------------------------
    private void setSensorStatus(@NonNull ImageView icon, boolean ok) {
        icon.setImageResource(
                ok
                        ? android.R.drawable.presence_online   // green dot
                        : android.R.drawable.presence_busy     // red dot
        );
    }
}
