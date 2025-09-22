package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Welcome to Smart Assistive System Dashboard");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
