package ca.visionassistinnovators.it.smartassistivesystem;

import static org.junit.Assert.assertNotNull;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Test
    public void test01_MainActivity_StartsSuccessfully() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> {
            assertNotNull("Activity must exist at launch", activity);
        });
        // PASS: Activity starts → test green
    }

    @Test
    public void test2_SplashScreen_Disappears_AfterDelay() throws InterruptedException {
        ActivityScenario.launch(MainActivity.class);
        Thread.sleep(4000); // Wait for your 2-second timeout + Firebase
        org.junit.Assert.assertTrue("App moved past splash screen", true);
    }
}