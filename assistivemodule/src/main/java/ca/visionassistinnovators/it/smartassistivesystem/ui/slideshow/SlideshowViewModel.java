/**
        * SlideshowViewModel
        * Displays alerts and warnings for the user.
        *
        * Team Members:
        * - Sarang Prajapati – N01662036
        * - Krish Patel – N01666556
        * - Kush Patel – N01657387
        * - Daksh Rana – N01664095
        * Section: 3DTues
        */
package ca.visionassistinnovators.it.smartassistivesystem.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SlideshowViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SlideshowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Alerts & Notifications:\n"
                + "⚠️ Obstacle detected!\n"
                + "💡 Low brightness detected – switch to safe mode.\n"
                + "✅ All clear.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
