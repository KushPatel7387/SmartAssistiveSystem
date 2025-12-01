/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */

package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.GuardianPatientManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientModel;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.EventLogger;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.AnalyticsAggregator;
import ca.visionassistinnovators.it.smartassistivesystem.ui.services.WalkingAssistService;

public class HomeFragment extends Fragment {

    private TextView tvCaption, tvPatientCount, tvPatientLabel;
    private ImageView imgSlideshow;
    private Button btnManagePatients, btnViewSensors, btnViewAlerts;
    private View cardSensors, cardAlerts;
    private SwitchMaterial walkingSwitch;

    private PieChart pieChartPatients;
    private BarChart barChartSensors;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int index = 0;

    private GuardianPatientManager patientManager;

    private ActivityResultLauncher<String[]> permissionLauncher;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgSlideshow = view.findViewById(R.id.imgSlideshow);
        tvCaption = view.findViewById(R.id.tv_caption);

        tvPatientCount = view.findViewById(R.id.tv_home_patient_count);
        tvPatientLabel = view.findViewById(R.id.tv_home_patient_label);
        btnManagePatients = view.findViewById(R.id.btn_home_manage_patients);

        btnViewSensors = view.findViewById(R.id.btn_home_view_sensors);
        btnViewAlerts = view.findViewById(R.id.btn_home_view_alerts);
        cardSensors = view.findViewById(R.id.card_home_sensors);
        cardAlerts = view.findViewById(R.id.card_home_alerts);

        walkingSwitch = view.findViewById(R.id.switchWalkingAssist);

        pieChartPatients = view.findViewById(R.id.pieChartPatients);
        barChartSensors = view.findViewById(R.id.barChartSensors);

        FloatingActionButton fabHelp = view.findViewById(R.id.fab_help);

        patientManager = new GuardianPatientManager();
        NavController navController = NavHostFragment.findNavController(this);

        // Navigation
        View cardPatients = view.findViewById(R.id.card_home_patients);

        View.OnClickListener openPatients = v -> navController.navigate(R.id.nav_sos);

        btnManagePatients.setOnClickListener(openPatients);
        cardPatients.setOnClickListener(openPatients);

        cardSensors.setOnClickListener(v -> navController.navigate(R.id.nav_sensors));
        btnViewSensors.setOnClickListener(v -> navController.navigate(R.id.nav_sensors));

        cardAlerts.setOnClickListener(v -> navController.navigate(R.id.nav_alerts));
        btnViewAlerts.setOnClickListener(v -> navController.navigate(R.id.nav_alerts));

        fabHelp.setOnClickListener(v ->
                Snackbar.make(v, "Tip: Check Sensors or Alerts for real-time assistance.", Snackbar.LENGTH_LONG)
                        .setAction("Open Sensors", a ->
                                navController.navigate(R.id.nav_sensors))
                        .show()
        );

        // Permissions
        setupPermissionLauncher();

        walkingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestWalkingPermissions();
            } else {
                stopWalkingAssistService();
            }
        });

        startSlideShow();
        setupPieChart();
        setupBarChart();
        setupPatientSummary();
    }

    // ------------------------------------------------------------------------
    // Permission Handling
    // ------------------------------------------------------------------------
    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {

                    boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    boolean fgServiceLocation =
                            result.getOrDefault(Manifest.permission.FOREGROUND_SERVICE_LOCATION, false);

                    // Android 14+ requires FOREGROUND_SERVICE_LOCATION
                    if (Build.VERSION.SDK_INT >= 34 && !fgServiceLocation) {
                        walkingSwitch.setChecked(false);
                        Toast.makeText(requireContext(),
                                "Foreground service location permission is required",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (fine || coarse) {
                        startWalkingAssistService();
                    } else {
                        walkingSwitch.setChecked(false);
                        Toast.makeText(requireContext(),
                                "Permissions required for Walking Assistance",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void requestWalkingPermissions() {
        List<String> perms = new ArrayList<>();

        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (Build.VERSION.SDK_INT >= 34) {
            perms.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION);
        }

        permissionLauncher.launch(perms.toArray(new String[0]));
    }

    // ------------------------------------------------------------------------
    // Foreground Service Start / Stop
    // ------------------------------------------------------------------------
    private void startWalkingAssistService() {

        if (!isGPSEnabled()) {
            walkingSwitch.setChecked(false);
            Snackbar.make(requireView(),
                            "GPS is required to enable Walking Assistance.",
                            Snackbar.LENGTH_LONG)
                    .setAction("Enable", v ->
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .show();
            return;
        }

        Intent intent = new Intent(requireContext(), WalkingAssistService.class);

        ContextCompat.startForegroundService(requireContext(), intent);

        Toast.makeText(requireContext(), "Walking Assistance Enabled", Toast.LENGTH_SHORT).show();
    }

    private void stopWalkingAssistService() {
        Intent intent = new Intent(requireContext(), WalkingAssistService.class);
        requireContext().stopService(intent);
        Toast.makeText(requireContext(), "Walking Assistance Disabled", Toast.LENGTH_SHORT).show();
    }

    private boolean isGPSEnabled() {
        LocationManager lm = (LocationManager)
                requireContext().getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // ------------------------------------------------------------------------
    // Patient Summary
    // ------------------------------------------------------------------------
    private void setupPatientSummary() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        if (current == null) {
            tvPatientCount.setText("--");
            tvPatientLabel.setText("Sign in to manage patients");
            btnManagePatients.setVisibility(View.GONE);
            return;
        }

        patientManager.listenForPatients(
                requireContext(),
                current.getUid(),
                new GuardianPatientManager.PatientListListener() {
                    @Override
                    public void onPatientsChanged(List<PatientModel> patients) {
                        int count = (patients != null) ? patients.size() : 0;

                        tvPatientCount.setText(String.valueOf(count));

                        if (count == 0) {
                            tvPatientLabel.setText("No patients added yet");
                            btnManagePatients.setText("Add Patient");
                        } else {
                            tvPatientLabel.setText("Patients linked to your account");
                            btnManagePatients.setText("View Patients");
                        }

                        btnManagePatients.setVisibility(View.VISIBLE);
                        updatePieChartWithPatientCount(count);
                    }

                    @Override
                    public void onError(String error) {
                        tvPatientCount.setText("--");
                        tvPatientLabel.setText("Unable to load patients");
                    }
                }
        );
    }

    // ------------------------------------------------------------------------
    // Slideshow
    // ------------------------------------------------------------------------
    private void startSlideShow() {
        final int[] images = {
                R.drawable.img_sensor,
                R.drawable.img_alert,
                R.drawable.img_microphone
        };

        final String[] captions = {
                getString(R.string.smart_assistive_system_in_action),
                getString(R.string.sensors_active),
                getString(R.string.voice_assistance_activated)
        };

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;

                imgSlideshow.setImageResource(images[index % images.length]);
                tvCaption.setText(captions[index % captions.length]);
                index++;

                handler.postDelayed(this, 3500);
            }
        });
    }

    // ------------------------------------------------------------------------
    // Pie Chart
    // ------------------------------------------------------------------------
    private void setupPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(1, getString(R.string.active)));
        entries.add(new PieEntry(0, getString(R.string.idle)));

        PieDataSet dataSet = new PieDataSet(entries, "Patient Overview");
        dataSet.setColors(
                getResources().getColor(R.color.teal_700),
                getResources().getColor(R.color.purple_500)
        );

        PieData data = new PieData(dataSet);

        pieChartPatients.setData(data);
        pieChartPatients.setUsePercentValues(true);
        pieChartPatients.getDescription().setEnabled(false);
        pieChartPatients.invalidate();
    }

    private void updatePieChartWithPatientCount(int totalPatients) {
        if (pieChartPatients == null) return;

        int active = Math.max(totalPatients - 1, 0);
        int idle = totalPatients - active;

        if (totalPatients == 0) {
            active = 0;
            idle = 1;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(active, getString(R.string.active)));
        entries.add(new PieEntry(idle, getString(R.string.idle)));

        PieDataSet dataSet = new PieDataSet(entries, "Patient Overview");
        dataSet.setColors(
                getResources().getColor(R.color.teal_700),
                getResources().getColor(R.color.purple_500)
        );

        PieData data = new PieData(dataSet);

        pieChartPatients.setData(data);
        pieChartPatients.invalidate();
    }

    // ------------------------------------------------------------------------
    // Bar Chart
    // ------------------------------------------------------------------------
    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 95));
        entries.add(new BarEntry(2, 88));
        entries.add(new BarEntry(3, 91));
        entries.add(new BarEntry(4, 76));

        BarDataSet dataSet = new BarDataSet(entries, "Sensor Health (%)");
        dataSet.setColor(getResources().getColor(R.color.purple_500));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChartSensors.setData(data);
        barChartSensors.getDescription().setEnabled(false);
        barChartSensors.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventLogger.logScreenView(requireContext(), "Home");
        AnalyticsAggregator.debugLogScreenUsage(requireContext());
    }
}
