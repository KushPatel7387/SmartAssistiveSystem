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

    // Node name EXACTLY as in your DB: "Feedback"
    private static final String FEEDBACK_NODE = "Feedback";

    public FeedbackManager() {
        // no-op
    }

    public void submitFeedback(Context context,
                               String name,
                               String phone,
                               String email,
                               String comment,
                               float rating,
                               FeedbackCallback callback) {

        // ─────────────────────────────────────────
        // 1. Business validation (NOT in Fragment)
        // ─────────────────────────────────────────
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            if (callback != null) {
                callback.onValidationError(
                        context.getString(R.string.please_enter_your_name_and_email)
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

        // ─────────────────────────────────────────
        // 2. Build data object
        // ─────────────────────────────────────────
        long timestamp = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("email", email);
        data.put("comment", comment);
        data.put("rating", rating);
        data.put("timestamp", timestamp);

        // ─────────────────────────────────────────
        // 3. Use SAME DB URL style as RegisterActivity
        //    and SAME node name: "Feedback"
        // ─────────────────────────────────────────
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
}
