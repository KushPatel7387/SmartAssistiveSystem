package ca.visionassistinnovators.it.smartassistivesystem.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * SlideshowViewModel
 * Manages alert messages and user preferences for alerts.
 */
public class SlideshowViewModel extends ViewModel {

    private final MutableLiveData<String> alertMessage;
    private boolean voiceEnabled = true;
    private boolean vibrationEnabled = true;
    private boolean soundEnabled = true;

    public SlideshowViewModel() {
        alertMessage = new MutableLiveData<>();
        alertMessage.setValue("No alerts yet.");
    }

    public LiveData<String> getAlertMessage() {
        return alertMessage;
    }

    // Methods to simulate sensor warnings
    public void sendAlert(String message) {
        alertMessage.setValue("⚠️ " + message);
    }

    // Preferences
    public void setVoiceEnabled(boolean enabled) { this.voiceEnabled = enabled; }
    public void setVibrationEnabled(boolean enabled) { this.vibrationEnabled = enabled; }
    public void setSoundEnabled(boolean enabled) { this.soundEnabled = enabled; }

    public boolean isVoiceEnabled() { return voiceEnabled; }
    public boolean isVibrationEnabled() { return vibrationEnabled; }
    public boolean isSoundEnabled() { return soundEnabled; }
}
