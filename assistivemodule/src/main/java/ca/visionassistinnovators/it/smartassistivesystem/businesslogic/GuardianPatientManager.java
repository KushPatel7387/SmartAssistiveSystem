package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;

/**
 * Business logic for guardian → patients relationship.
 * Patients are stored under the SAME tree as registration:
 *   /users/{uid}/patients/{patientId}
 * This reuses the existing "users" node like RegisterActivity
 * and does NOT overwrite user profiles.
 */
public class GuardianPatientManager {

    private static final String USERS_NODE     = "users";
    private static final String PATIENTS_CHILD = "patients";

    private final NameValidator nameValidator = new NameValidator();

    public interface PatientListListener {
        void onPatientsChanged(List<PatientModel> patients);
        void onError(String error);
    }

    public interface AddPatientCallback {
        void onSuccess();
        void onValidationError(String message);
        void onFailure(String error);
    }

    public interface DeletePatientCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /** Reuse SAME DB URL + "users" root as RegisterActivity */
    private DatabaseReference getGuardianPatientsRef(Context ctx, String guardianUid) {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                ctx.getString(R.string.firebase_db_url)
        );
        // Path: /users/{uid}/patients
        return db.getReference(USERS_NODE)
                .child(guardianUid)
                .child(PATIENTS_CHILD);
    }

    /**
     * Listen for all patients under /users/{uid}/patients
     */
    public void listenForPatients(Context ctx,
                                  String guardianUid,
                                  PatientListListener listener) {

        if (TextUtils.isEmpty(guardianUid)) {
            if (listener != null) {
                listener.onError("No guardian/user ID.");
            }
            return;
        }

        getGuardianPatientsRef(ctx, guardianUid)
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<PatientModel> result = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            PatientModel model = child.getValue(PatientModel.class);
                            if (model != null) {
                                model.id = child.getKey();
                                // if you don't want to use guardianId, just ignore it in UI
                                model.guardianId = guardianUid;
                                result.add(model);
                            }
                        }
                        if (listener != null) {
                            listener.onPatientsChanged(result);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (listener != null) {
                            listener.onError(error.getMessage());
                        }
                    }
                });
    }

    /**
     * Add a new patient under /users/{uid}/patients.
     */
    public void addPatient(Context ctx,
                           String guardianUid,
                           String firstName,
                           String lastName,
                           String email,
                           String phone,
                           AddPatientCallback callback) {

        if (TextUtils.isEmpty(guardianUid)) {
            if (callback != null) {
                callback.onFailure(ctx.getString(R.string.no_guardian_user_id));
            }
            return;
        }

        // Validate names
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            if (callback != null) {
                callback.onValidationError(
                        ctx.getString(R.string.err_first_and_last_name_required)
                );
            }
            return;
        }

        if (nameValidator.isValidName(firstName) || nameValidator.isValidName(lastName)) {
            if (callback != null) {
                callback.onValidationError(
                        ctx.getString(R.string.err_invalid_name_characters)
                );
            }
            return;
        }

        // Validate patient email
        if (TextUtils.isEmpty(email) ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (callback != null) {
                callback.onValidationError(ctx.getString(R.string.please_enter_a_valid_email_address));
            }
            return;
        }

        // Validate phone (10 digits, same as registration)
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            if (callback != null) {
                callback.onValidationError(
                        ctx.getString(R.string.err_phone_10_digits)
                );
            }
            return;
        }

        String fullName = nameValidator.buildFullName(firstName, lastName);

        // Sensor readings start as null (to be filled later)
        PatientModel model = new PatientModel(
                null,
                guardianUid,
                fullName,
                normalizedPhone,
                email,
                null, null, null, null
        );

        DatabaseReference ref = getGuardianPatientsRef(ctx, guardianUid).push();
        model.id = ref.getKey();

        ref.setValue(model)
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        String msg = e.getMessage() != null
                                ? e.getMessage()
                                : "Unknown error";
                        callback.onFailure(msg);
                    }
                });
    }

    /**
     * Delete a patient: /users/{uid}/patients/{patientId}
     */
    public void deletePatient(Context ctx,
                              String guardianUid,
                              String patientId,
                              DeletePatientCallback callback) {

        if (TextUtils.isEmpty(guardianUid) || TextUtils.isEmpty(patientId)) {
            if (callback != null) {
                callback.onFailure("Missing IDs.");
            }
            return;
        }

        getGuardianPatientsRef(ctx, guardianUid)
                .child(patientId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        String msg = e.getMessage() != null
                                ? e.getMessage()
                                : "Unknown error";
                        callback.onFailure(msg);
                    }
                });
    }

    /**
     * Same phone rule as registration: 10 digits only.
     */
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            return null;
        }
        return digits;
    }
}
