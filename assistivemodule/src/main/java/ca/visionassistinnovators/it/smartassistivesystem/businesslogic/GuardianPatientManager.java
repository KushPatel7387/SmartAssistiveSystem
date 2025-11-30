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
 *
 * Patients are stored under:
 *      /users/{guardianUid}/patients/{patientId}
 */
public class GuardianPatientManager {

    private static final String USERS_NODE     = "users";
    private static final String PATIENTS_CHILD = "patients";

    private final NameValidator nameValidator = new NameValidator();

    // -------------------------------------------------------------------------
    // Listener interfaces
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Get Firebase Reference: /users/{uid}/patients
    // -------------------------------------------------------------------------
    private DatabaseReference getGuardianPatientsRef(Context ctx, String guardianUid) {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                ctx.getString(R.string.firebase_db_url)
        );

        return db.getReference(USERS_NODE)
                .child(guardianUid)
                .child(PATIENTS_CHILD);
    }

    // -------------------------------------------------------------------------
    // LISTEN FOR PATIENTS
    // -------------------------------------------------------------------------
    public void listenForPatients(Context ctx,
                                  String guardianUid,
                                  PatientListListener listener) {

        if (TextUtils.isEmpty(guardianUid)) {
            if (listener != null) listener.onError("No guardian/user ID.");
            return;
        }

        getGuardianPatientsRef(ctx, guardianUid)
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<PatientModel> list = new ArrayList<>();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            PatientModel model = child.getValue(PatientModel.class);

                            if (model != null) {
                                model.id = child.getKey();      // Ensure ID is set
                                model.guardianId = guardianUid; // Keep guardian link
                                list.add(model);
                            }
                        }

                        if (listener != null) listener.onPatientsChanged(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (listener != null) listener.onError(error.getMessage());
                    }
                });
    }

    // -------------------------------------------------------------------------
    // ADD PATIENT
    // -------------------------------------------------------------------------
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

        // ---------- Validation ----------
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            if (callback != null) {
                callback.onValidationError(
                        ctx.getString(R.string.err_first_and_last_name_required)
                );
            }
            return;
        }

        if (!nameValidator.isValidName(firstName) || !nameValidator.isValidName(lastName)) {
            if (callback != null) {
                callback.onValidationError(
                        ctx.getString(R.string.err_invalid_name_characters)
                );
            }
            return;
        }

        if (TextUtils.isEmpty(email) ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (callback != null)
                callback.onValidationError(ctx.getString(R.string.please_enter_a_valid_email_address));
            return;
        }

        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            if (callback != null)
                callback.onValidationError(ctx.getString(R.string.err_phone_10_digits));
            return;
        }

        // ---------- Build Patient ----------------
        String fullName = nameValidator.buildFullName(firstName, lastName);

        PatientModel model = new PatientModel(
                null,                // ID (assigned below)
                guardianUid,
                fullName,
                normalizedPhone,
                email,
                null, null, null, null,       // Sensors
                null, null, null              // GPS fields
        );

        DatabaseReference ref = getGuardianPatientsRef(ctx, guardianUid).push();
        model.id = ref.getKey();

        ref.setValue(model)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage() != null ? e.getMessage() : "Unknown error");
                    }
                });
    }

    // -------------------------------------------------------------------------
    // DELETE PATIENT
    // -------------------------------------------------------------------------
    public void deletePatient(Context ctx,
                              String guardianUid,
                              String patientId,
                              DeletePatientCallback callback) {

        if (TextUtils.isEmpty(guardianUid) || TextUtils.isEmpty(patientId)) {
            if (callback != null) callback.onFailure("Missing IDs.");
            return;
        }

        getGuardianPatientsRef(ctx, guardianUid)
                .child(patientId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null)
                        callback.onFailure(e.getMessage() != null ? e.getMessage() : "Unknown error");
                });
    }

    // -------------------------------------------------------------------------
    // ⭐ NEW — UPDATE PATIENT LOCATION (for real-time tracking)
    // -------------------------------------------------------------------------
    public void updatePatientLocation(Context ctx,
                                      String guardianUid,
                                      String patientId,
                                      double latitude,
                                      double longitude) {

        if (TextUtils.isEmpty(guardianUid) || TextUtils.isEmpty(patientId)) {
            return;
        }

        DatabaseReference ref = getGuardianPatientsRef(ctx, guardianUid)
                .child(patientId);

        ref.child("latitude").setValue(latitude);
        ref.child("longitude").setValue(longitude);
        ref.child("lastUpdated").setValue(System.currentTimeMillis());
    }

    // -------------------------------------------------------------------------
    // Normalizes phone to 10 digits
    // -------------------------------------------------------------------------
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        return digits.length() == 10 ? digits : null;
    }
}
