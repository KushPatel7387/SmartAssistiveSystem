/**
 * Course Section: OCA
 * Team Members:
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 * Sarang Prajapati – N01662036
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.settings;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchLockPortrait;
    private SwitchCompat switchNotifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Get UI controls
        switchLockPortrait = root.findViewById(R.id.switch_lock_portrait);
        switchNotifications = root.findViewById(R.id.switch_notifications);

        // Lock to portrait
        switchLockPortrait.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isAdded()) return;

            if (isChecked) {
                requireActivity()
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                Toast.makeText(getContext(),
                        R.string.screen_locked_portrait,
                        Toast.LENGTH_SHORT).show();
            } else {
                requireActivity()
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                Toast.makeText(getContext(),
                        R.string.auto_rotation_enabled,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Notifications toggle
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isAdded()) return;

            if (isChecked) {
                Toast.makeText(getContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }
}
