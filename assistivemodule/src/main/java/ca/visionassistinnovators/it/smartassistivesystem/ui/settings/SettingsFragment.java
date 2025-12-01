/**
 * Course Section: OCA
 * Team Members:
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 * Sarang Prajapati – N01662036
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.settings;

import android.content.SharedPreferences;
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
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.EventLogger;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "sas_settings";
    private static final String KEY_LOCK_PORTRAIT = "lock_portrait";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_NOTIFICATIONS = "notifications";

    private SwitchCompat switchLockPortrait;
    private SwitchCompat switchNotifications;

    private RadioGroup rgTheme;
    private RadioButton rbLightTheme;
    private RadioButton rbDarkTheme;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Get UI controls
        switchLockPortrait = root.findViewById(R.id.switch_lock_portrait);
        switchNotifications = root.findViewById(R.id.switch_notifications);

        rgTheme = root.findViewById(R.id.rg_theme);
        rbLightTheme = root.findViewById(R.id.rb_light_theme);
        rbDarkTheme = root.findViewById(R.id.rb_dark_theme);

        // If you want per-user settings, you can build the name like:
        // String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // prefs = requireContext().getSharedPreferences(PREFS_NAME + "_" + uid, Context.MODE_PRIVATE);
        prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        // ---- RESTORE SAVED VALUES ----
        boolean lockPortrait = prefs.getBoolean(KEY_LOCK_PORTRAIT, false);
        boolean darkTheme = prefs.getBoolean(KEY_DARK_THEME, false);
        boolean notifications = prefs.getBoolean(KEY_NOTIFICATIONS, true);

        // Apply switch states (this will trigger listeners ONLY after we attach them)
        switchLockPortrait.setChecked(lockPortrait);
        switchNotifications.setChecked(notifications);

        // Apply theme + radio buttons
        if (darkTheme) {
            rbDarkTheme.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            rbLightTheme.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Apply orientation based on saved setting
        if (lockPortrait) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        // ---- LISTENERS (now we save to prefs whenever user changes something) ----

        // Theme toggle (Light / Dark) – applies to whole app
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isAdded()) return;

            SharedPreferences.Editor editor = prefs.edit();

            if (checkedId == R.id.rb_light_theme) {
                editor.putBoolean(KEY_DARK_THEME, false).apply();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.rb_dark_theme) {
                editor.putBoolean(KEY_DARK_THEME, true).apply();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        // Lock to portrait
        switchLockPortrait.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isAdded()) return;

            prefs.edit().putBoolean(KEY_LOCK_PORTRAIT, isChecked).apply();

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

            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();

            if (isChecked) {
                Toast.makeText(getContext(), R.string.notifications_enabled, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.notifications_disabled, Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventLogger.logScreenView(requireContext(), "Settings");
    }
}
