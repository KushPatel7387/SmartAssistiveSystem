package ca.visionassistinnovators.it.smartassistivesystem.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.databinding.FragmentSlideshowBinding;

/**
 * SlideshowFragment → Alerts & Settings Screen
 *
 * Displays alert preferences and latest warnings from sensors.
 *
 * Team: Vision Assist Innovators
 * Members:
 *  - Sarang Prajapati (N01662036)
 *  - Krish Patel (N01666556)
 *  - Kush Patel (N01657387)
 *  - Daksh Rana (N01664095)
 * Section: [Your Section Here]
 */
public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private SlideshowViewModel slideshowViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Bind UI elements
        final TextView alertText = binding.textAlerts;
        final CheckBox chkVoice = binding.chkVoice;
        final CheckBox chkVibration = binding.chkVibration;
        final CheckBox chkSound = binding.chkSound;

        // Observe ViewModel data
        slideshowViewModel.getAlertMessage().observe(getViewLifecycleOwner(), alertText::setText);

        // Example: reacting to preferences (later can be saved in SharedPreferences)
        chkVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            slideshowViewModel.setVoiceEnabled(isChecked);
        });

        chkVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            slideshowViewModel.setVibrationEnabled(isChecked);
        });

        chkSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            slideshowViewModel.setSoundEnabled(isChecked);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
