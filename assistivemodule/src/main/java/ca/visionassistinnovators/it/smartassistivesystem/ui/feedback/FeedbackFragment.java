/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.feedback;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class FeedbackFragment extends Fragment {

    private EditText etName, etPhone, etEmail, etComment;
    private RatingBar ratingBar;
    private Button btnSubmit;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_feedback, container, false);

        // Initialize Firebase database reference
        dbRef = FirebaseDatabase.getInstance().getReference("Feedback");

        // Link UI components
        etName = root.findViewById(R.id.et_name);
        etPhone = root.findViewById(R.id.et_phone);
        etEmail = root.findViewById(R.id.et_email);
        etComment = root.findViewById(R.id.et_comment);
        ratingBar = root.findViewById(R.id.ratingBar);
        btnSubmit = root.findViewById(R.id.btn_submit_feedback);

        // Set button click listener
        btnSubmit.setOnClickListener(v -> saveFeedback());

        return root;
    }

    private void saveFeedback() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String comment = etComment.getText().toString().trim();
        float rating = ratingBar.getRating();

        // Get device model (hidden)
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Please enter your name and email.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create data map
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("name", name);
        feedbackData.put("phone", phone);
        feedbackData.put("email", email);
        feedbackData.put("comment", comment);
        feedbackData.put("rating", rating);
        feedbackData.put("deviceModel", deviceModel);
        feedbackData.put("timestamp", System.currentTimeMillis());

        // Push data to Firebase Realtime DB
        dbRef.push().setValue(feedbackData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "✅ Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "❌ Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        etName.setText("");
        etPhone.setText("");
        etEmail.setText("");
        etComment.setText("");
        ratingBar.setRating(0);
    }
}
