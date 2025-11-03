package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class HomeFragment extends Fragment {

    private TextView tvCaption;
    private ImageView imgSlideshow;
    private final Handler handler = new Handler();
    private int index = 0;

    public HomeFragment() { /* required empty constructor */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgSlideshow = view.findViewById(R.id.imgSlideshow);
        tvCaption = view.findViewById(R.id.tv_caption);
        startSlideShow();
    }

    private void startSlideShow() {
        // Build arrays AFTER fragment is attached
        final int[] images = {
                R.drawable.img_sensor,
                R.drawable.img_alert,
                R.drawable.img_microphone
        };
        final String[] captions = {
                getString(R.string.smart_assistive_system_in_action),
                getString(R.string.sensors_active),
                getString(R.string.voice_assistance)
        };

        handler.post(new Runnable() {
            @Override public void run() {
                if (getView() == null) return;               // fragment not visible
                imgSlideshow.setImageResource(images[index % images.length]);
                tvCaption.setText(captions[index % captions.length]);
                index++;
                handler.postDelayed(this, 3500);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null); // prevent leaks
        imgSlideshow = null;
        tvCaption = null;
    }
}
