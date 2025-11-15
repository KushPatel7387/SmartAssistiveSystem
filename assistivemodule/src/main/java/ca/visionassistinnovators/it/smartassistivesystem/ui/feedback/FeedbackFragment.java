package ca.visionassistinnovators.it.smartassistivesystem.ui.feedback;

import android.content.Context;
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
import ca.visionassistinnovators.it.smartassistivesystem.ui.util.Prefs;

public class FeedbackFragment extends Fragment {

    private EditText etFirstName, etLastName, etComment;
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
        etComment   = root.findViewById(R.id.et_comment);
        ratingBar   = root.findViewById(R.id.ratingBar);
        Button btnSubmit = root.findViewById(R.id.btn_submit_feedback);

        btnSubmit.setOnClickListener(v -> submitFeedback());

        return root;
    }

    private void submitFeedback() {
        Context ctx = requireContext();

        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String comment   = etComment.getText().toString().trim();
        float rating     = ratingBar.getRating();

        // Email from currently logged-in Firebase user (not shown on UI)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = (user != null && user.getEmail() != null)
                ? user.getEmail()
                : "";

        // Phone from SharedPreferences (saved at login from DB)
        String phone = Prefs.getString(ctx, Prefs.KEY_USER_PHONE, "");

        feedbackManager.submitFeedback(
                ctx,
                firstName,   // ✅ firstName
                lastName,    // ✅ lastName
                phone,       // ✅ phone
                email,       // email
                comment,     // comment
                rating,      // rating
                new FeedbackManager.FeedbackCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ctx,
                                R.string.feedback_submitted_successfully,
                                Toast.LENGTH_SHORT).show();
                        clearFields();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ctx,
                                getString(R.string.failed) + " " + error,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onValidationError(String message) {
                        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void clearFields() {
        etFirstName.setText("");
        etLastName.setText("");
        etComment.setText("");
        ratingBar.setRating(0f);
    }
}
