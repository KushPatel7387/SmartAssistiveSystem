package ca.visionassistinnovators.it.smartassistivesystem.ui.feedback;

import android.os.Build;
import android.os.Bundle;
import android.os.Build.VERSION_CODES;
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

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class FeedbackFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_feedback, container, false);

        EditText etName = root.findViewById(R.id.et_name);
        EditText etPhone = root.findViewById(R.id.et_phone);
        EditText etEmail = root.findViewById(R.id.et_email);
        EditText etComment = root.findViewById(R.id.et_comment);
        RatingBar ratingBar = root.findViewById(R.id.ratingBar);
        Button btnSubmit = root.findViewById(R.id.btn_submit_feedback);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String comment = etComment.getText().toString().trim();
            float rating = ratingBar.getRating();

            // device model (required by prof)
            String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Name and email required", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: push to Firebase / RTDB
            // Data to store: name, phone, email, comment, rating, deviceModel

            Toast.makeText(getContext(), "Feedback submitted", Toast.LENGTH_SHORT).show();
        });

        return root;
    }
}
