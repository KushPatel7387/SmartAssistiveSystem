/**     Team: Vision Assist Innovators
 * Members:
 *  - Sarang Prajapati (N01662036)
 *  - Krish Patel (N01666556)
 *  - Kush Patel (N01657387)
 *  - Daksh Rana (N01664095)
 * Section: [CENG-323 OCA]
 **/
package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(
                "Welcome to Smart Assistive System!\n\n" +
                        "This dashboard will give you:\n" +
                        "• Light sensor data (brightness)\n" +
                        "• Color sensor data (object & signal recognition)\n" +
                        "• Distance sensor data (obstacle detection)\n" +
                        "• Spectrum sensor data (traffic lights, safety signs)\n\n" +
                        "Navigate using the menu to explore details."
        );
    }

    public LiveData<String> getText() {
        return mText;
    }
}
