/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
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

    // ---- OFFLINE PROFILE CACHE ----
    private static final String PREF_OFFLINE_PROFILE = "sas_offline_profile";
    private static final String KEY_PROFILE_NAME  = "offline_profile_name";
    private static final String KEY_PROFILE_EMAIL = "offline_profile_email";
    private static final String KEY_PROFILE_PHONE = "offline_profile_phone";

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
            Toast.makeText(getContext(), R.string.not_logged_in, Toast.LENGTH_SHORT).show();
            return v;
        }

        String uid = user.getUid();

        userRef = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference(getString(R.string.users1))
                .child(uid);

        // 1) Load last saved profile from local cache (works in Airplane mode)
        loadOfflineProfile(requireContext());

        // 2) Try to refresh from Firebase when online
        loadProfileData(user);

        btnSave.setOnClickListener(view -> updateProfile(uid));

        return v;
    }

    // ---------------- OFFLINE HELPERS ----------------

    private void loadOfflineProfile(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_OFFLINE_PROFILE, Context.MODE_PRIVATE);

        String cachedName  = prefs.getString(KEY_PROFILE_NAME, "");
        String cachedEmail = prefs.getString(KEY_PROFILE_EMAIL, "");
        String cachedPhone = prefs.getString(KEY_PROFILE_PHONE, "");

        boolean anyLoaded = false;

        if (!TextUtils.isEmpty(cachedName)) {
            etName.setText(cachedName);
            anyLoaded = true;
        }
        if (!TextUtils.isEmpty(cachedEmail)) {
            etEmail.setText(cachedEmail);
            anyLoaded = true;
        }
        if (!TextUtils.isEmpty(cachedPhone)) {
            etPhone.setText(cachedPhone);
            anyLoaded = true;
        }

        // Optional: you can remove this toast if you find it noisy
        if (anyLoaded) {
            Toast.makeText(
                    getContext(),
                    "Profile loaded from last offline copy.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void saveOfflineProfile(Context context,
                                    String name,
                                    String email,
                                    String phone) {
        context.getSharedPreferences(PREF_OFFLINE_PROFILE, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PROFILE_NAME,  name != null ? name.trim() : "")
                .putString(KEY_PROFILE_EMAIL, email != null ? email.trim() : "")
                .putString(KEY_PROFILE_PHONE, phone != null ? phone.trim() : "")
                .apply();
    }

    // ---------------- ONLINE LOAD ----------------

    private void loadProfileData(FirebaseUser authUser) {

        userRef.get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                // If this fails (e.g., Airplane mode), we keep whatever offline data we already showed.
                Toast.makeText(getContext(), "Failed to load profile!", Toast.LENGTH_SHORT).show();
                return;
            }

            DataSnapshot ds = task.getResult();

            if (ds.exists()) {
                // Case 1: Profile exists in DB
                String name  = ds.child(getString(R.string.name1)).getValue(String.class);
                String email = ds.child(getString(R.string.email1)).getValue(String.class);
                String phone = ds.child(getString(R.string.phone1)).getValue(String.class);

                if (name != null) {
                    etName.setText(name);
                }
                if (email != null) {
                    etEmail.setText(email);
                }
                if (phone != null) {
                    etPhone.setText(phone);
                }

                if (getContext() != null) {
                    saveOfflineProfile(getContext(), name, email, phone);
                }
            } else {
                // Case 2: No DB profile → fallback to Firebase Authentication fields
                String email = authUser.getEmail();
                String name  = authUser.getDisplayName() != null ? authUser.getDisplayName() : "";
                String phone = authUser.getPhoneNumber() != null ? authUser.getPhoneNumber() : "";

                etEmail.setText(email);
                etName.setText(name);
                etPhone.setText(phone);

                if (getContext() != null) {
                    saveOfflineProfile(getContext(), name, email, phone);
                }

                Toast.makeText(
                        getContext(),
                        R.string.no_db_profile_found_loaded_from_firebase_authentication,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // ---------------- UPDATE PROFILE ----------------

    private void updateProfile(String uid) {

        String name  = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name))  { etName.setError("Required");  return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Required"); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Required"); return; }

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);

        userRef.updateChildren(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                    if (getContext() != null) {
                        // Also refresh offline cache with the new values
                        saveOfflineProfile(getContext(), name, email, phone);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                getContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());
    }
}
