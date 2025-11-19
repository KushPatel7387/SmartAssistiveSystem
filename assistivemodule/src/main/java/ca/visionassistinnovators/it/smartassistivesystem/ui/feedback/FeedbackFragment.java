package ca.visionassistinnovators.it.smartassistivesystem.ui.feedback;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.FeedbackManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.Prefs;

public class FeedbackFragment extends Fragment {

    private EditText etFirstName, etLastName, etComment;
    private RatingBar ratingBar;
    private FeedbackManager feedbackManager;

    private Button btnSubmit;
    private TextView tvCooldown;

    // Progress dialog (centered progress bar + dim background)
    private AlertDialog progressDialog;

    // 24h timer
    private CountDownTimer cooldownTimer;

    // current user email (unique ID)
    private String currentEmail = "";

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
        btnSubmit   = root.findViewById(R.id.btn_submit_feedback);
        tvCooldown  = root.findViewById(R.id.tv_feedback_cooldown);

        // Get current user email (used for per-email cooldown)
        Context ctx = requireContext();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentEmail = (user != null && user.getEmail() != null) ? user.getEmail() : "";

        // Check if this user is already in the 24h cooldown window
        long remaining = feedbackManager.getRemainingCooldownMs(ctx, currentEmail);
        if (remaining > 0) {
            startCooldown(remaining);
        } else {
            setSubmitButtonEnabled(true);
            tvCooldown.setVisibility(View.GONE);
        }

        btnSubmit.setOnClickListener(v -> submitFeedback());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cooldownTimer != null) {
            cooldownTimer.cancel();
            cooldownTimer = null;
        }
        hideProgressDialog();
    }

    private void submitFeedback() {
        Context ctx = requireContext();

        // Read inputs
        final String firstName = etFirstName.getText().toString().trim();
        final String lastName  = etLastName.getText().toString().trim();
        final String comment   = etComment.getText().toString().trim();
        final float rating     = ratingBar.getRating();

        final String email = currentEmail;
        final String phone = Prefs.getString(ctx, Prefs.KEY_USER_PHONE, "");

        // ✅ FIRST: validate everything (including 24h rule) BEFORE showing progress
        String validationError = feedbackManager.validateFeedback(
                ctx,
                firstName,
                lastName,
                phone,
                email,
                comment,
                rating
        );

        if (validationError != null) {
            Toast.makeText(ctx, validationError, Toast.LENGTH_SHORT).show();
            return; // do NOT show progress bar
        }

        // From here, we know inputs are valid & user is not in cooldown

        // Show centered progress bar & dim background, disable button
        showProgressDialog();
        setSubmitButtonEnabled(false);

        // Wait 5 seconds before actually sending to DB (assignment requirement)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            feedbackManager.submitFeedback(
                    ctx,
                    firstName,
                    lastName,
                    phone,
                    email,
                    comment,
                    rating,
                    new FeedbackManager.FeedbackCallback() {
                        @Override
                        public void onSuccess() {
                            hideProgressDialog();

                            Toast.makeText(ctx,
                                    R.string.feedback_submitted_successfully,
                                    Toast.LENGTH_SHORT).show();
                            clearFields();

                            // Start a full 24h cooldown from now
                            startCooldown(FeedbackManager.FEEDBACK_COOLDOWN_MS);

                            // AlertDialog with OK after DB confirmation (requirement 45)
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.customer_feedback)
                                    .setMessage(R.string.feedback_submitted_successfully)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        }

                        @Override
                        public void onFailure(String error) {
                            hideProgressDialog();
                            // Re-enable button if not in cooldown
                            setSubmitButtonEnabled(true);
                            Toast.makeText(ctx,
                                    getString(R.string.failed) + " " + error,
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onValidationError(String message) {
                            // This should rarely happen now (we already validated),
                            // but we still handle it.
                            hideProgressDialog();
                            setSubmitButtonEnabled(true);
                            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        }, 5000); // 5 seconds
    }

    // ===== Progress dialog helpers =====

    private void showProgressDialog() {
        if (progressDialog == null) {
            ProgressBar bar = new ProgressBar(requireContext());
            progressDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setView(bar)
                    .setCancelable(false)
                    .create();
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // ===== Cooldown UI: greyed button + timer =====

    private void startCooldown(long remainingMs) {
        setSubmitButtonEnabled(false);
        tvCooldown.setVisibility(View.VISIBLE);

        if (cooldownTimer != null) {
            cooldownTimer.cancel();
        }

        cooldownTimer = new CountDownTimer(remainingMs, 60_000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalMinutes = millisUntilFinished / 60_000L;
                long hours = totalMinutes / 60L;
                long minutes = totalMinutes % 60L;

                String text = getString(
                        R.string.feedback_cooldown_format,
                        hours,
                        minutes
                );
                tvCooldown.setText(text);
            }

            @Override
            public void onFinish() {
                tvCooldown.setText("");
                tvCooldown.setVisibility(View.GONE);
                setSubmitButtonEnabled(true);
            }
        }.start();
    }

    private void setSubmitButtonEnabled(boolean enabled) {
        btnSubmit.setEnabled(enabled);
        btnSubmit.setAlpha(enabled ? 1f : 0.5f);
    }

    private void clearFields() {
        etFirstName.setText("");
        etLastName.setText("");
        etComment.setText("");
        ratingBar.setRating(0f);
    }
}
