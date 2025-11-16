/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095

 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.settings;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchLockPortrait;
    private SwitchCompat switchNotifications;
    private RadioGroup rgTheme;
    private RadioButton rbLightTheme;
    private RadioButton rbDarkTheme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // ===== Bind views to XML IDs =====
        switchLockPortrait   = root.findViewById(R.id.switch_lock_portrait);
        switchNotifications  = root.findViewById(R.id.switch_notifications);
        rgTheme              = root.findViewById(R.id.rg_theme);
        rbLightTheme         = root.findViewById(R.id.rb_light_theme);
        rbDarkTheme          = root.findViewById(R.id.rb_dark_theme);

        // ===== Lock / unlock screen orientation =====
        switchLockPortrait.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Toast.makeText(getContext(),
                        R.string.screen_locked_portrait,
                        Toast.LENGTH_SHORT).show();
            } else {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Toast.makeText(getContext(),
                        R.string.auto_rotation_enabled,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ===== Notifications toggle (simple feedback) =====
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(getContext(),
                        "Notifications enabled",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(),
                        "Notifications disabled",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ===== Theme selection (Light / Dark) =====
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_light_theme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(getContext(),
                        "Light theme selected",
                        Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.rb_dark_theme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(getContext(),
                        "Dark theme selected",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }
}
