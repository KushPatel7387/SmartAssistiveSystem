/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

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

        etName  = v.findViewById(R.id.et_profile_name);
        etEmail = v.findViewById(R.id.et_profile_email);
        etPhone = v.findViewById(R.id.et_profile_phone);
        btnSave = v.findViewById(R.id.btn_update_profile);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "Not logged in!", Toast.LENGTH_SHORT).show();
            return v;
        }

        String uid = user.getUid();

        userRef = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference("users")
                .child(uid);

        // Load data
        loadProfileData(user);

        btnSave.setOnClickListener(view -> updateProfile(uid));

        return v;
    }

    private void loadProfileData(FirebaseUser authUser) {

        userRef.get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                Toast.makeText(getContext(), "Failed to load profile!", Toast.LENGTH_SHORT).show();
                return;
            }

            DataSnapshot ds = task.getResult();

            // Case 1: User profile exists in database
            if (ds.exists()) {
                etName.setText(ds.child("name").getValue(String.class));
                etEmail.setText(ds.child("email").getValue(String.class));
                etPhone.setText(ds.child("phone").getValue(String.class));
            }
            else {
                // Case 2: Profile does NOT exist → fallback to Authentication
                etEmail.setText(authUser.getEmail());
                etName.setText(authUser.getDisplayName() != null ? authUser.getDisplayName() : "");
                etPhone.setText(authUser.getPhoneNumber() != null ? authUser.getPhoneNumber() : "");

                Toast.makeText(getContext(),
                        "No DB profile found — loaded from Firebase Authentication",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile(String uid) {

        String name  = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Required"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Required"); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Required"); return; }

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);

        userRef.updateChildren(map)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
