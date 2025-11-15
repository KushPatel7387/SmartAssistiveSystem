package ca.visionassistinnovators.it.smartassistivesystem.ui.feedback;

import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.FeedbackManager;

public class FeedbackFragment extends Fragment {

    private EditText etFirstName, etLastName, etPhone, etEmail, etComment;
    private RatingBar ratingBar;
    private FeedbackManager feedbackManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_feedback, container, false);

        feedbackManager = new FeedbackManager();

        etFirstName = root.findViewById(R.id.et_first_name);
        etLastName  = root.findViewById(R.id.et_last_name);
        etPhone     = root.findViewById(R.id.et_phone);
        etEmail     = root.findViewById(R.id.et_email);
        etComment   = root.findViewById(R.id.et_comment);
        ratingBar   = root.findViewById(R.id.ratingBar);
        Button btnSubmit = root.findViewById(R.id.btn_submit_feedback);

        // Auto-fill email from logged-in Firebase user and lock it
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            etEmail.setText(currentUser.getEmail());
            etEmail.setEnabled(false);
            etEmail.setFocusable(false);
            etEmail.setFocusableInTouchMode(false);
        }

        btnSubmit.setOnClickListener(v -> submitFeedback());

        return root;
    }

    private void submitFeedback() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String comment   = etComment.getText().toString().trim();
        float rating     = ratingBar.getRating();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(getContext(),
                    R.string.err_not_logged_in_for_feedback,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String email = currentUser.getEmail();

        feedbackManager.submitFeedback(
                requireContext(),
                firstName,
                lastName,
                phone,
                email,
                comment,
                rating,
                new FeedbackManager.FeedbackCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(
                                getContext(),
                                R.string.feedback_submitted_successfully,
                                Toast.LENGTH_SHORT
                        ).show();
                        clearFields();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(
                                getContext(),
                                getString(R.string.failed) + error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onValidationError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void clearFields() {
        etFirstName.setText("");
        etLastName.setText("");
        etPhone.setText("");
        etComment.setText("");
        ratingBar.setRating(0);
        // Email stays – tied to logged-in user
    }
}
