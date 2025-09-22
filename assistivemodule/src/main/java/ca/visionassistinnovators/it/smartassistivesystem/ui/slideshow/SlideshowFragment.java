/**
 * SlideshowFragment
 * Displays alerts, warnings, and settings.
 */

package ca.visionassistinnovators.it.smartassistivesystem.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);

        final TextView textView = root.findViewById(R.id.text_slideshow);
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }
}
