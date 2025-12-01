/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 *
 * Business logic for guardian → patients relationship.
 * Patients are stored under:
 *   /users/{uid}/patients/{patientId}
 * Alerts under:
 *   /users/{uid}/alerts/{alertId}
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.NotificationHelper;

public class GuardianPatientManager {

    private static final String USERS_NODE     = "users";
    private static final String PATIENTS_CHILD = "patients";
    private static final String SENSORS_NODE   = "sensors";
    private static final String ALERTS_CHILD   = "alerts";

    private final NameValidator nameValidator = new NameValidator();

    // -------------------- Callbacks --------------------
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

    // -------------------- Helpers --------------------

    private DatabaseReference getGuardianPatientsRef(Context ctx, String guardianUid) {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                ctx.getString(R.string.firebase_db_url)
        );
        return db.getReference(USERS_NODE)
                .child(guardianUid)
                .child(PATIENTS_CHILD);
    }

    private DatabaseReference getGuardianAlertsRef(Context ctx, String guardianUid) {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                ctx.getString(R.string.firebase_db_url)
        );
        return db.getReference(USERS_NODE)
                .child(guardianUid)
                .child(ALERTS_CHILD);
    }

    private DatabaseReference getSensorsRootRef(Context ctx) {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                ctx.getString(R.string.firebase_db_url)
        );
        return db.getReference(SENSORS_NODE);
    }

    // -------------------------------------------------
    // Listen for patients
    // -------------------------------------------------
    public void listenForPatients(Context ctx,
                                  String guardianUid,
                                  PatientListListener listener) {

        if (TextUtils.isEmpty(guardianUid)) {
            if (listener != null) {
                listener.onError(ctx.getString(R.string.err_no_guardian_user_id));
            }
            return;
        }

        DatabaseReference patientsRef = getGuardianPatientsRef(ctx, guardianUid);
        patientsRef.keepSynced(true);

        patientsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PatientModel> result = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PatientModel model = child.getValue(PatientModel.class);
                    if (model != null) {
                        model.id = child.getKey();
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

    // -------------------------------------------------
    // Add patient
    // -------------------------------------------------
    public void addPatient(Context ctx,
                           String guardianUid,
                           String firstName,
                           String lastName,
                           String email,
                           String phone,
                           AddPatientCallback callback) {

        if (TextUtils.isEmpty(guardianUid)) {
            if (callback != null) {
                callback.onFailure(ctx.getString(R.string.err_no_guardian_user_id));
            }
            return;
        }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            if (TextUtils.isEmpty(email) ||
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (callback != null) {
                    callback.onValidationError(
                            ctx.getString(R.string.please_enter_a_valid_email_address)
                    );
                }
                return;
            }
        }

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
                    // DB alert + local notification as business logic
                    createNewPatientAlert(ctx, guardianUid, model);

                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        String msg = (e != null && e.getMessage() != null)
                                ? e.getMessage()
                                : ctx.getString(R.string.err_unknown);
                        callback.onFailure(msg);
                    }
                });
    }

    // -------------------------------------------------
    // Delete patient + sensors/{patientId}
    // -------------------------------------------------
    public void deletePatient(Context ctx,
                              String guardianUid,
                              String patientId,
                              DeletePatientCallback callback) {

        if (TextUtils.isEmpty(guardianUid) || TextUtils.isEmpty(patientId)) {
            if (callback != null) {
                callback.onFailure(ctx.getString(R.string.err_missing_ids_for_delete));
            }
            return;
        }

        DatabaseReference patientRef =
                getGuardianPatientsRef(ctx, guardianUid).child(patientId);
        DatabaseReference sensorRef =
                getSensorsRootRef(ctx).child(patientId);

        patientRef.removeValue()
                .addOnSuccessListener(unused -> {
                    sensorRef.removeValue()
                            .addOnSuccessListener(unused2 -> {
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    String msg = (e != null && e.getMessage() != null)
                                            ? e.getMessage()
                                            : ctx.getString(R.string.err_failed_delete_sensor_data);
                                    callback.onFailure(msg);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        String msg = (e != null && e.getMessage() != null)
                                ? e.getMessage()
                                : ctx.getString(R.string.err_unknown);
                        callback.onFailure(msg);
                    }
                });
    }

    // -------------------------------------------------
    // Phone normalization
    // -------------------------------------------------
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            return null;
        }
        return digits;
    }

    // -------------------------------------------------
    // DB alert + local notification for new patient
    // -------------------------------------------------
    private void createNewPatientAlert(Context ctx,
                                       String guardianUid,
                                       PatientModel patient) {

        if (TextUtils.isEmpty(guardianUid) || patient == null) return;

        DatabaseReference alertsRef = getGuardianAlertsRef(ctx, guardianUid);
        String key = alertsRef.push().getKey();
        if (key == null) return;

        String title = ctx.getString(R.string.new_patient_added_title);
        String message = ctx.getString(
                R.string.new_patient_added_message_with_name,
                patient.fullName   // ✅ use fullName field from PatientModel
        );

        long now = System.currentTimeMillis();
        AlertModel alert = new AlertModel(key, title, message, now);

        alertsRef.child(key).setValue(alert);

        // Local notification (checks permission inside helper)
        NotificationHelper.showNewPatientNotification(
                ctx.getApplicationContext(),
                patient.fullName   // ✅ pass fullName to NotificationHelper
        );
    }
}
