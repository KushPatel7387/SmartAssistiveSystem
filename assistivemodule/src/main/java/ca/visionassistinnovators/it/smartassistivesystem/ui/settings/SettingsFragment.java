/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchLockPortrait;
    private Button btnRequestLocation;

    // ✅ Modern permission launcher (works instantly)
    private ActivityResultLauncher<String> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        switchLockPortrait = root.findViewById(R.id.switch_lock_portrait);
        btnRequestLocation = root.findViewById(R.id.btn_request_location);

        // ✅ Lock/unlock portrait orientation
        switchLockPortrait.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Toast.makeText(getContext(), "Screen locked to portrait", Toast.LENGTH_SHORT).show();
            } else {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Toast.makeText(getContext(), "Auto-rotation enabled", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Register permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Snackbar.make(requireView(),
                                "✅ Permission Granted — location features enabled!",
                                Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(requireView(),
                                        "❌ Permission Denied — some features may not work",
                                        Snackbar.LENGTH_LONG)
                                .setAction("Settings", v -> openAppSettings())
                                .show();
                    }
                });

        // ✅ Button click → triggers runtime permission check
        btnRequestLocation.setOnClickListener(v -> checkLocationPermission(v));

        return root;
    }

    private void checkLocationPermission(View view) {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Snackbar.make(view, "✅ Location permission already granted", Snackbar.LENGTH_LONG).show();

        } else {
            // Directly launch permission dialog
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
