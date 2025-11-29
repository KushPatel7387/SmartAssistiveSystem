/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;   // ✅ NEW
import com.google.android.material.snackbar.Snackbar;                          // ✅ NEW
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.GuardianPatientManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientModel;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.EventLogger;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.AnalyticsAggregator;

public class HomeFragment extends Fragment {

    private TextView tvCaption;
    private ImageView imgSlideshow;
    private PieChart pieChartPatients;
    private BarChart barChartSensors;

    // Dashboard views
    private TextView tvPatientCount;
    private TextView tvPatientLabel;
    private Button btnManagePatients;

    private Button btnViewSensors;
    private Button btnViewAlerts;
    private View cardSensors;
    private View cardAlerts;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int index = 0;

    private GuardianPatientManager patientManager;

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

        // Slideshow + caption
        imgSlideshow = view.findViewById(R.id.imgSlideshow);
        tvCaption = view.findViewById(R.id.tv_caption);

        // Dashboard views: Patients
        tvPatientCount = view.findViewById(R.id.tv_home_patient_count);
        tvPatientLabel = view.findViewById(R.id.tv_home_patient_label);
        btnManagePatients = view.findViewById(R.id.btn_home_manage_patients);

        // Dashboard views: Sensors & Alerts
        btnViewSensors = view.findViewById(R.id.btn_home_view_sensors);
        btnViewAlerts = view.findViewById(R.id.btn_home_view_alerts);
        cardSensors = view.findViewById(R.id.card_home_sensors);
        cardAlerts = view.findViewById(R.id.card_home_alerts);

        // Charts
        pieChartPatients = view.findViewById(R.id.pieChartPatients);
        barChartSensors = view.findViewById(R.id.barChartSensors);

        // ✅ Floating Action Button (FAB)
        FloatingActionButton fabHelp = view.findViewById(R.id.fab_help);

        patientManager = new GuardianPatientManager();

        // NavController for this fragment
        NavController navController = NavHostFragment.findNavController(this);

        // Patients: button + whole card
        View cardPatients = view.findViewById(R.id.card_home_patients);
        View.OnClickListener openPatients = v ->
                navController.navigate(R.id.nav_sos);

        btnManagePatients.setOnClickListener(openPatients);
        cardPatients.setOnClickListener(openPatients);

        // Sensors: button + whole card
        View.OnClickListener openSensors = v ->
                navController.navigate(R.id.nav_sensors);

        btnViewSensors.setOnClickListener(openSensors);
        cardSensors.setOnClickListener(openSensors);

        // Alerts: button + whole card
        View.OnClickListener openAlerts = v ->
                navController.navigate(R.id.nav_alerts);

        btnViewAlerts.setOnClickListener(openAlerts);
        cardAlerts.setOnClickListener(openAlerts);

        // ✅ FAB behavior: show quick-help Snackbar + shortcut to Sensors
        fabHelp.setOnClickListener(v -> {
            Snackbar.make(
                            v,
                            "Tip: Open Sensors, SOS or Alerts to view real-time assistive data.",
                            Snackbar.LENGTH_LONG
                    )
                    .setAction("Open Sensors", actionView ->
                            navController.navigate(R.id.nav_sensors)
                    )
                    .show();
        });

        startSlideShow();
        setupPieChart();
        setupBarChart();
        setupPatientSummary();
    }

    // ------------------------------
    // PATIENT SUMMARY (live from Firebase)
    // ------------------------------
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
                            btnManagePatients.setVisibility(View.VISIBLE);
                        } else {
                            tvPatientLabel.setText("Patients linked to your account");
                            btnManagePatients.setText("View Patients");
                            btnManagePatients.setVisibility(View.VISIBLE);
                        }

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

    // ------------------------------
    // SLIDESHOW
    // ------------------------------
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

    // ------------------------------
    // PIE CHART - PATIENTS
    // ------------------------------
    private void setupPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(1, getString(R.string.active)));
        entries.add(new PieEntry(0, getString(R.string.idle)));

        PieDataSet dataSet = new PieDataSet(entries, "Patient Overview");
        dataSet.setColors(
                getResources().getColor(R.color.teal_700),
                getResources().getColor(R.color.purple_500)
        );
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);

        pieChartPatients.setData(data);
        pieChartPatients.setUsePercentValues(true);
        pieChartPatients.getDescription().setEnabled(false);
        pieChartPatients.getLegend().setEnabled(true);
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
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChartPatients.setData(data);
        pieChartPatients.invalidate();
    }

    // ------------------------------
    // BAR CHART - SENSOR HEALTH
    // ------------------------------
    private void setupBarChart() {

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 95)); // Distance sensor
        entries.add(new BarEntry(2, 88)); // Light sensor
        entries.add(new BarEntry(3, 91)); // Color sensor
        entries.add(new BarEntry(4, 76)); // Fall-detection / IMU sensor

        BarDataSet dataSet = new BarDataSet(entries, "Sensor Health (%)");
        dataSet.setColor(getResources().getColor(R.color.purple_500));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChartSensors.setData(data);
        barChartSensors.getDescription().setEnabled(false);
        barChartSensors.getLegend().setEnabled(false);
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
        // DEBUG ONLY: run this when you want to generate the stats

        AnalyticsAggregator.debugLogScreenUsage(requireContext());
    }
}