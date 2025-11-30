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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
                if (patient == null || patient.id == null) {
                    Toast.makeText(requireContext(), "Invalid patient", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString("patientId", patient.id);

                NavController navController =
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);

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

        if (guardianUid != null) listenForPatients();
        else Toast.makeText(getContext(), "Login again.", Toast.LENGTH_SHORT).show();

        // PHONE PERMISSION HANDLER
        callPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isAdded()) return;
                    if (isGranted && pendingPhoneToCall != null) {
                        actuallyCallPhone(pendingPhoneToCall);
                    } else {
                        Toast.makeText(requireContext(),
                                "Call permission denied.", Toast.LENGTH_SHORT).show();
                    }
                    pendingPhoneToCall = null;
                });

        fabAdd.setOnClickListener(v -> showAddPatientDialog());

        return root;
    }

    private void listenForPatients() {
        patientManager.listenForPatients(requireContext(), guardianUid,
                new GuardianPatientManager.PatientListListener() {
                    @Override
                    public void onPatientsChanged(List<PatientModel> patients) {
                        adapter.setPatients(patients);
                        updatePatientCount(patients == null ? 0 : patients.size());
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(),
                                "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void updatePatientCount(int count) {
        tvPatientCount.setText("Patients: " + count);
        tvEmptyState.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void showAddPatientDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_patient, null, false);

        TextInputEditText etFirstName = dialogView.findViewById(R.id.et_patient_first_name);
        TextInputEditText etLastName = dialogView.findViewById(R.id.et_patient_last_name);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_patient_email);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_patient_phone);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Patient")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {

                    String fName = etFirstName.getText() != null ? etFirstName.getText().toString().trim() : "";
                    String lName = etLastName.getText() != null ? etLastName.getText().toString().trim() : "";
                    String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                    String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

                    patientManager.addPatient(requireContext(), guardianUid,
                            fName, lName, email, phone,
                            new GuardianPatientManager.AddPatientCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(requireContext(), "Patient added.",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onValidationError(String message) {
                                    Toast.makeText(requireContext(), message,
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(requireContext(),
                                            "Failed: " + error,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmDeletePatient(PatientModel patient) {
        if (patient == null || patient.id == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Patient")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    patientManager.deletePatient(requireContext(), guardianUid, patient.id,
                            new GuardianPatientManager.DeletePatientCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(requireContext(),
                                            "Deleted.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(requireContext(),
                                            "Failed: " + error,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void callPatient(PatientModel patient) {
        if (patient.phone == null || patient.phone.isEmpty()) {
            Toast.makeText(requireContext(), "No phone number.", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }
}
