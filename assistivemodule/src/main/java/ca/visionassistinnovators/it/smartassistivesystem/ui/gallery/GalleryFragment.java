/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */

package ca.visionassistinnovators.it.smartassistivesystem.ui.gallery;

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

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        final TextView textView = root.findViewById(R.id.text_light);
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }
}
