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

        // 1) Validate first/last name
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            if (callback != null) {
                callback.onValidationError(
                        context.getString(R.string.err_first_and_last_name_required)
                );
            }
            return;
        }

        // 2) Validate + normalize phone (digits only, must be 10)
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            if (callback != null) {
                callback.onValidationError(
                        context.getString(R.string.err_phone_10_digits)
                );
            }
            return;
        }

        // 3) Validate comment
        if (TextUtils.isEmpty(comment)) {
            if (callback != null) {
                callback.onValidationError(
                        context.getString(R.string.please_enter_your_message)
                );
            }
            return;
        }

        // 4) Build formatted full name (auto-capitalized)
        String fullName = formatName(firstName) + " " + formatName(lastName);

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

    // ─────────────────────────────────────────
    // Helpers: name & phone formatting
    // ─────────────────────────────────────────

    private String formatName(String raw) {
        if (raw == null) return "";
        raw = raw.trim().toLowerCase();
        if (raw.isEmpty()) return "";

        // first letter upper, rest lower
        return Character.toUpperCase(raw.charAt(0)) +
                (raw.length() > 1 ? raw.substring(1) : "");
    }

    /**
     * Keep only digits, require exactly 10.
     * Returns normalized phone or null if invalid.
     */
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", ""); // strip non-digits
        if (digits.length() != 10) {
            return null;
        }
        return digits;
    }
}
