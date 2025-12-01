/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.patients;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.GuardianPatientManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientAdapter;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientModel;

public class SosFragment extends Fragment {

    private TextView tvPatientCount;
    private TextView tvEmptyState;
    private PatientAdapter adapter;
    private GuardianPatientManager patientManager;
    private String guardianUid;

    private ActivityResultLauncher<String> callPermissionLauncher;
    private String pendingPhoneToCall;

    // Notification permission launcher
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_sos, container, false);

        RecyclerView rvPatients = root.findViewById(R.id.rv_patients);
        tvPatientCount = root.findViewById(R.id.tv_patient_count);
        tvEmptyState = root.findViewById(R.id.tv_empty_state);
        FloatingActionButton fabAdd = root.findViewById(R.id.fab_add_patient);

        rvPatients.setLayoutManager(new LinearLayoutManager(getContext()));

        // ADAPTER WITH CLICK LISTENER
        adapter = new PatientAdapter(new PatientAdapter.OnPatientActionListener() {
            @Override
            public void onPatientClicked(PatientModel patient) {
                if (!isAdded()) return;

                if (patient == null || patient.id == null) {
                    Toast.makeText(requireContext(),
                            getString(R.string.err_invalid_patient),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString("patientId", patient.id);

                NavController navController =
                        Navigation.findNavController(requireActivity(),
                                R.id.nav_host_fragment_content_main);

                navController.navigate(R.id.navigation_location, bundle);
            }

            @Override
            public void onCallClicked(PatientModel patient) {
                callPatient(patient);
            }

            @Override
            public void onDeleteClicked(PatientModel patient) {
                confirmDeletePatient(patient);
            }
        });

        rvPatients.setAdapter(adapter);

        patientManager = new GuardianPatientManager();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        guardianUid = (user != null) ? user.getUid() : null;

        if (guardianUid != null) {
            listenForPatients();
        } else if (isAdded()) {
            Toast.makeText(getContext(),
                    getString(R.string.err_login_again),
                    Toast.LENGTH_SHORT).show();
        }

        // PHONE PERMISSION HANDLER
        callPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isAdded()) return;
                    if (isGranted && pendingPhoneToCall != null) {
                        actuallyCallPhone(pendingPhoneToCall);
                    } else {
                        Toast.makeText(requireContext(),
                                getString(R.string.err_call_permission_denied),
                                Toast.LENGTH_SHORT).show();
                    }
                    pendingPhoneToCall = null;
                });

        // Notification permission handler
        setupNotificationPermissionLauncher();

        fabAdd.setOnClickListener(v -> showAddPatientDialog());

        // Ask notification permission when SOS screen opens (if needed)
        requestNotificationPermissionIfNeeded();

        return root;
    }

    private void listenForPatients() {
        // requireContext() is safe here because we call from onCreateView/onViewCreated
        patientManager.listenForPatients(requireContext(), guardianUid,
                new GuardianPatientManager.PatientListListener() {
                    @Override
                    public void onPatientsChanged(List<PatientModel> patients) {
                        // 🔒 Guard: fragment might be detached when Firebase callback fires
                        if (!isAdded()) return;

                        adapter.setPatients(patients);
                        updatePatientCount(patients == null ? 0 : patients.size());
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;

                        Toast.makeText(requireContext(),
                                getString(R.string.err_generic_with_reason, error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void updatePatientCount(int count) {
        if (!isAdded()) return;

        // You can switch this to a string resource if you want:
        // tvPatientCount.setText(getString(R.string.patients_count_label, count));
        tvPatientCount.setText("Patients: " + count);
        tvEmptyState.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void showAddPatientDialog() {
        if (!isAdded()) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_patient, null, false);

        TextInputEditText etFirstName = dialogView.findViewById(R.id.et_patient_first_name);
        TextInputEditText etLastName = dialogView.findViewById(R.id.et_patient_last_name);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_patient_email);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_patient_phone);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_patient_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {

                    String fName = etFirstName.getText() != null ? etFirstName.getText().toString().trim() : "";
                    String lName = etLastName.getText() != null ? etLastName.getText().toString().trim() : "";
                    String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                    String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

                    patientManager.addPatient(requireContext(), guardianUid,
                            fName, lName, email, phone,
                            new GuardianPatientManager.AddPatientCallback() {
                                @Override
                                public void onSuccess() {
                                    if (!isAdded()) return;

                                    Toast.makeText(requireContext(),
                                            getString(R.string.patient_added),
                                            Toast.LENGTH_SHORT).show();

                                    // Ask for notification permission right after adding a patient
                                    requestNotificationPermissionIfNeeded();
                                }

                                @Override
                                public void onValidationError(String message) {
                                    if (!isAdded()) return;
                                    Toast.makeText(requireContext(), message,
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String error) {
                                    if (!isAdded()) return;
                                    Toast.makeText(requireContext(),
                                            getString(R.string.err_generic_with_reason, error),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmDeletePatient(PatientModel patient) {
        if (patient == null || patient.id == null || !isAdded()) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_patient_title)
                .setMessage(R.string.delete_patient_confirm)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {

                    patientManager.deletePatient(requireContext(), guardianUid, patient.id,
                            new GuardianPatientManager.DeletePatientCallback() {
                                @Override
                                public void onSuccess() {
                                    if (!isAdded()) return;
                                    Toast.makeText(requireContext(),
                                            getString(R.string.patient_deleted),
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String error) {
                                    if (!isAdded()) return;
                                    Toast.makeText(requireContext(),
                                            getString(R.string.err_generic_with_reason, error),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void callPatient(PatientModel patient) {
        if (!isAdded()) return;

        if (patient.phone == null || patient.phone.isEmpty()) {
            Toast.makeText(requireContext(),
                    getString(R.string.err_no_phone_number),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            actuallyCallPhone(patient.phone);
        } else {
            pendingPhoneToCall = patient.phone;
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void actuallyCallPhone(String phone) {
        if (!isAdded()) return;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    // ---------------- Notification permission helpers ----------------

    private void setupNotificationPermissionLauncher() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return; // No runtime notification permission before Android 13
        }

        notificationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (!isAdded()) return;
                            if (!isGranted) {
                                Toast.makeText(
                                        requireContext(),
                                        getString(R.string.notification_permission_denied),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void requestNotificationPermissionIfNeeded() {
        if (!isAdded()) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            if (notificationPermissionLauncher != null) {
                notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            }
        }
    }
}
