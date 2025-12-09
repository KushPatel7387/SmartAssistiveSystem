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
import android.Manifest.permission;
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
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

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
    private Button btnManagePatients;
    private SwitchMaterial walkingSwitch;

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


        Button btnViewSensors = view.findViewById(R.id.btn_home_view_sensors);
        Button btnViewAlerts = view.findViewById(R.id.btn_home_view_alerts);
        View cardSensors = view.findViewById(R.id.card_home_sensors);
        View cardAlerts = view.findViewById(R.id.card_home_alerts);

        walkingSwitch = view.findViewById(R.id.switchWalkingAssist);

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
                Snackbar.make(v, getString(R.string.tip_check_sensors_alerts), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.open_sensors), a ->
                                navController.navigate(R.id.nav_sensors))
                        .show()
        );

        // Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setupPermissionLauncher();
        }

        walkingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestWalkingPermissions();
            } else {
                stopWalkingAssistService();
            }
        });

        startSlideShow();
        setupPatientSummary();
    }

    // ------------------------------------------------------------------------
    // Permission Handling
    // ------------------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {

                    if (!isAdded()) return;

                    boolean fine = Boolean.TRUE.equals(result.getOrDefault(permission.ACCESS_FINE_LOCATION, false));
                    boolean coarse = Boolean.TRUE.equals(result.getOrDefault(permission.ACCESS_COARSE_LOCATION, false));

                    boolean fgServiceLocation =
                            Boolean.TRUE.equals(result.getOrDefault(permission.FOREGROUND_SERVICE_LOCATION, false));

                    // Android 14+ requires FOREGROUND_SERVICE_LOCATION
                    if (!fgServiceLocation) {
                        walkingSwitch.setChecked(false);
                        Toast.makeText(requireContext(),
                                getString(R.string.foreground_service_location_required),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (fine || coarse) {
                        startWalkingAssistService();
                    } else {
                        walkingSwitch.setChecked(false);
                        Toast.makeText(requireContext(),
                                getString(R.string.permissions_required_for_walking_assistance),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void requestWalkingPermissions() {
        List<String> perms = new ArrayList<>();

        perms.add(permission.ACCESS_FINE_LOCATION);
        perms.add(permission.ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= 33) {
            perms.add(permission.POST_NOTIFICATIONS);
        }
        if (Build.VERSION.SDK_INT >= 34) {
            perms.add(permission.FOREGROUND_SERVICE_LOCATION);
        }

        permissionLauncher.launch(perms.toArray(new String[0]));
    }

    // ------------------------------------------------------------------------
    // Foreground Service Start / Stop
    // ------------------------------------------------------------------------
    private void startWalkingAssistService() {

        if (!isAdded()) return;

        if (!isGPSEnabled()) {
            walkingSwitch.setChecked(false);
            Snackbar.make(requireView(),
                            getString(R.string.gps_required_for_walking_assistance),
                            Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.enable), v ->
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .show();
            return;
        }

        Intent intent = new Intent(requireContext(), WalkingAssistService.class);

        ContextCompat.startForegroundService(requireContext(), intent);

        Toast.makeText(requireContext(),
                getString(R.string.walking_assistance_enabled),
                Toast.LENGTH_SHORT).show();
    }

    private void stopWalkingAssistService() {
        if (!isAdded()) return;
        Intent intent = new Intent(requireContext(), WalkingAssistService.class);
        requireContext().stopService(intent);
        Toast.makeText(requireContext(),
                getString(R.string.walking_assistance_disabled),
                Toast.LENGTH_SHORT).show();
    }

    private boolean isGPSEnabled() {
        if (!isAdded()) return false;
        LocationManager lm = (LocationManager)
                requireContext().getSystemService(Context.LOCATION_SERVICE);
        return lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // ------------------------------------------------------------------------
    // Patient Summary
    // ------------------------------------------------------------------------
    private void setupPatientSummary() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        if (current == null) {
            tvPatientCount.setText(getString(R.string.home_patient_count_placeholder)); // e.g. "--"
            tvPatientLabel.setText(getString(R.string.sign_in_to_manage_patients));
            btnManagePatients.setVisibility(View.GONE);
            return;
        }

        patientManager.listenForPatients(
                requireContext(),
                current.getUid(),
                new GuardianPatientManager.PatientListListener() {
                    @Override
                    public void onPatientsChanged(List<PatientModel> patients) {
                        if (!isAdded()) return;

                        int count = (patients != null) ? patients.size() : 0;

                        tvPatientCount.setText(String.valueOf(count));

                        if (count == 0) {
                            tvPatientLabel.setText(getString(R.string.no_patients_added_yet));
                            btnManagePatients.setText(getString(R.string.add_patient));
                        } else {
                            tvPatientLabel.setText(getString(R.string.patients_linked_to_account));
                            btnManagePatients.setText(getString(R.string.view_patients));
                        }

                        btnManagePatients.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        tvPatientCount.setText(getString(R.string.home_patient_count_placeholder));
                        tvPatientLabel.setText(getString(R.string.unable_to_load_patients));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAdded()) return;
        EventLogger.logScreenView(requireContext(), "Home");
        AnalyticsAggregator.debugLogScreenUsage(requireContext());
    }
}
