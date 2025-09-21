/**
 * GalleryViewModel
 * Manages live sensor data for the SensorsFragment.
 *
 * Team: Vision Assist Innovators
 * Members:
 *  - Sarang Prajapati (N01662036)
 *  - Krish Patel (N01666556)
 *  - Kush Patel (N01657387)
 *  - Daksh Rana (N01664095)
 * Section: [Your Section Here]
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> lightSensor;
    private final MutableLiveData<String> colorSensor;
    private final MutableLiveData<String> distanceSensor;
    private final MutableLiveData<String> spectrumSensor;

    public GalleryViewModel() {
        lightSensor = new MutableLiveData<>();
        colorSensor = new MutableLiveData<>();
        distanceSensor = new MutableLiveData<>();
        spectrumSensor = new MutableLiveData<>();

        // Default placeholders until real data comes from hardware
        lightSensor.setValue("Light: Waiting for data...");
        colorSensor.setValue("Color: Waiting for data...");
        distanceSensor.setValue("Distance: Waiting for data...");
        spectrumSensor.setValue("Spectrum: Waiting for data...");
    }

    public LiveData<String> getLightSensor() {
        return lightSensor;
    }

    public LiveData<String> getColorSensor() {
        return colorSensor;
    }

    public LiveData<String> getDistanceSensor() {
        return distanceSensor;
    }

    public LiveData<String> getSpectrumSensor() {
        return spectrumSensor;
    }

    // Methods to update sensor values (will be called when hardware sends data)
    public void updateLightSensor(String value) {
        lightSensor.setValue("Light: " + value);
    }

    public void updateColorSensor(String value) {
        colorSensor.setValue("Color: " + value);
    }

    public void updateDistanceSensor(String value) {
        distanceSensor.setValue("Distance: " + value);
    }

    public void updateSpectrumSensor(String value) {
        spectrumSensor.setValue("Spectrum: " + value);
    }
}
