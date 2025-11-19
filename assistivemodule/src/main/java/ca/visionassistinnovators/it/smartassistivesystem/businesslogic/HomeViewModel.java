/**
 * Course Section: OCA
 * Team Members:
 * Daksh Rana – N01664095
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ca.visionassistinnovators.it.smartassistivesystem.R;

// 1. Extend AndroidViewModel to get an application context
public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mText;

    // 2. Update the constructor to accept an Application instance
    public HomeViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();
        // 3. Use the application context to get the string from strings.xml
        mText.setValue(application.getString(R.string.home_welcome_message));
    }

    public LiveData<String> getText() {
        return mText;
    }
}
