package ca.visionassistinnovators.it.smartassistivesystem.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;   // ⭐ IMPORTANT FIX — You forgot this import!

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class ProfileFragment extends Fragment {

    private EditText etName, etEmail, etPhone;
    private Button btnSave;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        // UI Components
        etName  = v.findViewById(R.id.et_profile_name);
        etEmail = v.findViewById(R.id.et_profile_email);
        etPhone = v.findViewById(R.id.et_profile_phone);
        btnSave = v.findViewById(R.id.btn_update_profile);

        // Firebase
        auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();

        if (uid == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return v;
        }

        userRef = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference("users")
                .child(uid);

        // Load profile into fields
        loadProfileData();

        // Save updated profile
        btnSave.setOnClickListener(view -> updateProfile());

        return v;
    }

    // Load user profile data into EditTexts
    private void loadProfileData() {
        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(), "Failed to load profile!", Toast.LENGTH_SHORT).show();
                return;
            }

            DataSnapshot ds = task.getResult();

            if (!ds.exists()) {
                Toast.makeText(getContext(), "Profile not found!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ⭐ Set edit text values
            etName.setText(ds.child("name").getValue(String.class));
            etEmail.setText(ds.child("email").getValue(String.class));
            etPhone.setText(ds.child("phone").getValue(String.class));
        });
    }

    // Update edited profile data in Firebase
    private void updateProfile() {
        String name  = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter name");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Enter phone");
            etPhone.requestFocus();
            return;
        }

        // ⭐ Firebase update map
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);

        userRef.updateChildren(map)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
