/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.gallery;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ca.visionassistinnovators.it.smartassistivesystem.R;

// 1. Extend AndroidViewModel instead of ViewModel
public class GalleryViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mText;

    // 2. Add a constructor that takes an Application object
    public GalleryViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();

        // 3. Use the application context to get the string resource
        String data = application.getString(R.string.sensors_data_light_color_distance_spectrum);
        mText.setValue(data);
    }

    public LiveData<String> getText() {
        return mText;
    }
}
