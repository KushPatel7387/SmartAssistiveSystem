package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class FeedbackManager {

    public interface FeedbackCallback {
        void onSuccess();
        void onFailure(String error);
        void onValidationError(String message);
    }

    // EXACT node name from your DB
    private static final String FEEDBACK_NODE = "Feedback";

    private final NameValidator nameValidator = new NameValidator();

    public FeedbackManager() {
        // no-op
    }

    public void submitFeedback(Context context,
                               String firstName,
                               String lastName,
                               String phone,
                               String email,
                               String comment,
                               float rating,
                               FeedbackCallback callback) {

        // Names required
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            if (callback != null) {
                callback.onValidationError(
                        context.getString(R.string.err_first_and_last_name_required)
                );
            }
            return;
        }

        // Same rules as registration
        if (!nameValidator.isValidName(firstName) || !nameValidator.isValidName(lastName)) {
            if (callback != null) {
                callback.onValidationError(
                        context.getString(R.string.err_invalid_name_characters)
                );
            }
            return;
        }

        // Phone rule same style (10 digits)
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            if (callback != null) {
                callback.onValidationError(
                        context.getString(R.string.err_phone_10_digits)
                );
            }
            return;
        }

        if (TextUtils.isEmpty(comment)) {
            if (callback != null) {
                callback.onValidationError(
                        context.getString(R.string.please_enter_your_message)
                );
            }
            return;
        }

        // Auto-capitalized full name shared with registration style
        String fullName = nameValidator.buildFullName(firstName, lastName);
        long timestamp = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("name", fullName);
        data.put("phone", normalizedPhone);
        data.put("email", email);
        data.put("comment", comment);
        data.put("rating", rating);
        data.put("timestamp", timestamp);

        FirebaseDatabase db = FirebaseDatabase.getInstance(
                context.getString(R.string.firebase_db_url)
        );
        DatabaseReference feedbackRef = db.getReference(FEEDBACK_NODE);

        feedbackRef.push()
                .setValue(data)
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        String msg = (e != null && e.getMessage() != null)
                                ? e.getMessage()
                                : "Unknown error";
                        callback.onFailure(msg);
                    }
                });
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            return null;
        }
        return digits;
    }
}
