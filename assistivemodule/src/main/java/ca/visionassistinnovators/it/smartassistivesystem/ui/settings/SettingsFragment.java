/**
 * Course Section: OCA
 * Team Members:
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 * Sarang Prajapati – N01662036
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

    private ActivityResultLauncher<String> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        switchLockPortrait = root.findViewById(R.id.switch_lock_portrait);
        btnRequestLocation = root.findViewById(R.id.btn_request_location);

        // Lock/unlock portrait orientation
        switchLockPortrait.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Toast.makeText(getContext(), R.string.screen_locked_portrait, Toast.LENGTH_SHORT).show();
            } else {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Toast.makeText(getContext(), R.string.auto_rotation_enabled, Toast.LENGTH_SHORT).show();
            }
        });

        // Register permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isAdded()) return;
                    if (isGranted) {
                        Snackbar.make(root, R.string.permission_granted_location_features_enabled, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(root, R.string.permission_denied_location, Snackbar.LENGTH_LONG)
                                .setAction(R.string.open_settings, v -> openAppSettings())
                                .show();
                    }
                });

        // Request location on click
        btnRequestLocation.setOnClickListener(v -> checkLocationPermission(root));

        return root;
    }

    private void checkLocationPermission(View anchor) {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Snackbar.make(anchor, R.string.location_permission_already, Snackbar.LENGTH_LONG).show();

        } else {
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
