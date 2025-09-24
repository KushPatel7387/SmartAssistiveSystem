package ca.visionassistinnovators.it.smartassistivesystem.ui.settings;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SettingsFragment extends Fragment {

    private CheckBox chkLockPortrait;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        chkLockPortrait = root.findViewById(R.id.chk_lock_portrait);

        // ✅ Functionality: lock/unlock portrait mode
        chkLockPortrait.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Toast.makeText(getContext(), "Locked to Portrait", Toast.LENGTH_SHORT).show();
            } else {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Toast.makeText(getContext(), "Unlocked (Auto Rotation)", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }
}
