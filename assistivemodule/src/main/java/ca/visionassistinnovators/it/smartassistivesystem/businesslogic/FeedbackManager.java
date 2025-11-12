package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.os.Build;
import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles feedback-related logic: validation, data preparation, and saving to Firebase.
 */
public class FeedbackManager {

    private final DatabaseReference dbRef;

    public interface FeedbackCallback {
        void onSuccess();
        void onFailure(String error);
        void onValidationError(String message);
    }

    public FeedbackManager() {
        dbRef = FirebaseDatabase.getInstance().getReference("Feedback");
    }

    public void submitFeedback(String name,
                               String phone,
                               String email,
                               String comment,
                               float rating,
                               FeedbackCallback callback) {

        // Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            callback.onValidationError("Please enter your name and email.");
            return;
        }

        // Device model
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;

        // Prepare data
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("name", name);
        feedbackData.put("phone", phone);
        feedbackData.put("email", email);
        feedbackData.put("comment", comment);
        feedbackData.put("rating", rating);
        feedbackData.put("deviceModel", deviceModel);
        feedbackData.put("timestamp", System.currentTimeMillis());

        // Write to Firebase
        dbRef.push().setValue(feedbackData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
