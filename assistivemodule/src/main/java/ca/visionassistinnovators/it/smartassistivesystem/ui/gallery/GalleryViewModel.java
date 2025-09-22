/**
 * GalleryViewModel
 * Displays sensor readings (light, color, distance, spectrum).
 *
 * Team Members:
 * - Sarang Prajapati – N01662036
 * - Krish Patel – N01666556
 * - Kush Patel – N01657387
 * - Daksh Rana – N01664095
 * Section: 3DTues
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Sensor Data:\n"
                + "- Light Sensor (TSL2591)\n"
                + "- Color Sensor (TCS34725)\n"
                + "- Distance Sensor (VL53L1X)\n"
                + "- Spectrum Sensor (AS7262)");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
