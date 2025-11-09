/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */

package ca.visionassistinnovators.it.smartassistivesystem.ui.slideshow;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SlideshowViewModel extends AndroidViewModel {
    private final MutableLiveData<String> mText = new MutableLiveData<>();

    public SlideshowViewModel(@NonNull Application application) {
        super(application);
        mText.setValue(application.getString(R.string.alerts_voice_warnings_title));
    }

    public LiveData<String> getText() {
        return mText;
    }
}
