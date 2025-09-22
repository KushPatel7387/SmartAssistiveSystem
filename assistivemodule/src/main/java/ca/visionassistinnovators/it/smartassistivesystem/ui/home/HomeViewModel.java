/**
 * HomeViewModel
 * Displays dashboard/overview text for the app.
 *
 * Team Members:
 * - Sarang Prajapati – N01662036
 * - Krish Patel – N01666556
 * - Kush Patel – N01657387
 * - Daksh Rana – N01664095
 * Section: 3DTues
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Welcome to the Smart Assistive System.\n"
                + "This app helps visually impaired individuals navigate safely "
                + "using real-time sensor data.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
