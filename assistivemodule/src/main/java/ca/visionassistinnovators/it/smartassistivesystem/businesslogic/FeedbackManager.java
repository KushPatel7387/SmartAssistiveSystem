package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
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

    // 24 hours in ms
    public static final long FEEDBACK_COOLDOWN_MS = 24L * 60L * 60L * 1000L;

    // Local prefs for per-email cooldown
    private static final String PREFS_NAME = "feedback_prefs";
    private static final String KEY_PREFIX_LAST = "last_feedback_";

    private final NameValidator nameValidator = new NameValidator();

    public FeedbackManager() {
        // no-op
    }

    /**
     * Validate feedback + 24h rule.
     *
     * @return null if everything is valid, otherwise a localized error message.
     */
    public String validateFeedback(Context context,
                                   String firstName,
                                   String lastName,
                                   String phone,
                                   String email,
                                   String comment,
                                   float rating) {

        // ----- 24h per-email cooldown (also considered "validation") -----
        String userKey = buildUserKey(email);
        long now = System.currentTimeMillis();
        long remainingMs = getRemainingMs(context, userKey, now);
        if (remainingMs > 0) {
            return context.getString(R.string.feedback_already_submitted_24h);
        }

        // Names required
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            return context.getString(R.string.err_first_and_last_name_required);
        }

        // Same rules as registration: if NOT valid → error
        if (!nameValidator.isValidName(firstName) || !nameValidator.isValidName(lastName)) {
            return context.getString(R.string.err_invalid_name_characters);
        }

        // Phone rule same style (10 digits)
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            return context.getString(R.string.err_phone_10_digits);
        }

        if (TextUtils.isEmpty(comment)) {
            return context.getString(R.string.please_enter_your_message);
        }

        // (Optional) rating rule: if you want to enforce rating > 0, add here.

        return null; // everything OK
    }

    public void submitFeedback(Context context,
                               String firstName,
                               String lastName,
                               String phone,
                               String email,
                               String comment,
                               float rating,
                               FeedbackCallback callback) {

        // Reuse same validation logic inside manager (so it is safe to call directly)
        String validationError = validateFeedback(context, firstName, lastName, phone, email, comment, rating);
        if (validationError != null) {
            if (callback != null) {
                callback.onValidationError(validationError);
            }
            return;
        }

        // At this point, we know:
        // - Not in 24h cooldown
        // - Names, phone, and comment are valid

        String normalizedPhone = normalizePhone(phone);
        // normalizedPhone cannot be null here if validateFeedback ran

        String fullName = nameValidator.buildFullName(firstName, lastName);
        long timestamp = System.currentTimeMillis();

        // Device model (requirement 46)
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;

        Map<String, Object> data = new HashMap<>();
        data.put("name", fullName);
        data.put("phone", normalizedPhone);
        data.put("email", email);
        data.put("comment", comment);
        data.put("rating", rating);
        data.put("timestamp", timestamp);
        data.put("deviceModel", deviceModel);

        FirebaseDatabase db = FirebaseDatabase.getInstance(
                context.getString(R.string.firebase_db_url)
        );
        DatabaseReference feedbackRef = db.getReference(FEEDBACK_NODE);

        // Save to DB
        feedbackRef.push()
                .setValue(data)
                .addOnSuccessListener(unused -> {
                    // Save last submission time for this email (for 24h rule)
                    String userKey = buildUserKey(email);
                    saveLastSubmissionTime(context, userKey, timestamp);

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

    /**
     * Used by the fragment to know if current email is already in cooldown.
     */
    public long getRemainingCooldownMs(Context context, String email) {
        String userKey = buildUserKey(email);
        long now = System.currentTimeMillis();
        return getRemainingMs(context, userKey, now);
    }

    // ----- helpers -----

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            return null;
        }
        return digits;
    }

    /**
     * Email is the unique identifier (requirement 27).
     */
    private String buildUserKey(String email) {
        if (!TextUtils.isEmpty(email)) {
            return KEY_PREFIX_LAST + email.toLowerCase();
        }
        return KEY_PREFIX_LAST + "anonymous";
    }

    private SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void saveLastSubmissionTime(Context context, String userKey, long timestamp) {
        getPrefs(context)
                .edit()
                .putLong(userKey, timestamp)
                .apply();
    }

    private long getRemainingMs(Context context, String userKey, long now) {
        long last = getPrefs(context).getLong(userKey, 0L);
        if (last == 0L) return 0L;
        long diff = now - last;
        if (diff >= FEEDBACK_COOLDOWN_MS) {
            return 0L;
        }
        return FEEDBACK_COOLDOWN_MS - diff;
    }
}
