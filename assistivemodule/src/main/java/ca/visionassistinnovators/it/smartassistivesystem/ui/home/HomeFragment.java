/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */

package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ImageView imgSlideshow;
    private TextView tvCaption;
    private Handler handler;
    private int currentImageIndex = 0;

    // 🔹 6 Images (place your actual drawable names here)
    private final int[] imageList = {
            R.drawable.bell,
            R.drawable.gps,
            R.drawable.record,
            R.drawable.blind,
            R.drawable.sos,
            R.drawable.sound
    };

    // 🔹 Optional captions (one per image)
    private final String[] captions = {
            "Smart Assistive System: Helping visually impaired users", // bell
            "Voice assistance activated",                               // gps
            "Sensor module detecting nearby obstacles",                // record
            "System providing safe navigation",                        // blind
            "Emergency alert mode active",                              // sos
            "All systems operating normally"                            // sound
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // ✅ Initialize slideshow components
        imgSlideshow = root.findViewById(R.id.imgSlideshow);
        tvCaption = root.findViewById(R.id.tv_caption);

        // ✅ Initialize TextView from ViewModel
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // ✅ Start slideshow
        startImageSlideshow();

        return root;
    }

    private void startImageSlideshow() {
        handler = new Handler(Looper.getMainLooper()); // ✅ Non-deprecated

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Fade animation
                AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
                fadeOut.setDuration(500);
                fadeOut.setFillAfter(true);

                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(800);
                fadeIn.setFillAfter(true);

                imgSlideshow.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(android.view.animation.Animation animation) {}

                    @Override
                    public void onAnimationEnd(android.view.animation.Animation animation) {
                        // Change image and caption
                        imgSlideshow.setImageResource(imageList[currentImageIndex]);
                        tvCaption.setText(captions[currentImageIndex]);
                        imgSlideshow.startAnimation(fadeIn);
                    }

                    @Override
                    public void onAnimationRepeat(android.view.animation.Animation animation) {}
                });

                // Move to next image
                currentImageIndex = (currentImageIndex + 1) % imageList.length;

                // Repeat every 3 seconds
                handler.postDelayed(this, 3000);
            }
        }, 0);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop handler to avoid memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
