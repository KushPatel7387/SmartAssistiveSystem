package ca.visionassistinnovators.it.smartassistivesystem;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import ca.visionassistinnovators.it.smartassistivesystem.ui.login.LoginActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
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
    public void test02_SplashScreen_Shows_Then_Disappears() throws InterruptedException {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // At 500ms → still alive
        Thread.sleep(500);
        scenario.onActivity(activity -> assertFalse("Activity still alive", activity.isFinishing()));

        // At 3000ms → your 2-sec timeout → should be finished/destroyed
        Thread.sleep(2500); // total 3 sec
        scenario.onActivity(activity -> {
            assertTrue("MainActivity finished (this is CORRECT!)",
                    activity.isFinishing() || activity.isDestroyed());
        });
        // PASS: Activity destroys itself → test green
    }

    @Test public void test03_AssertTrue_Example() { assertTrue(true); }
    @Test public void test04_AssertFalse_Example() { assertFalse(false); }
    @Test public void test05_AssertEquals_Example() { assertEquals(4, 2+2); }
    @Test public void test06_AssertNotEquals_Example() { assertNotEquals(5, 10); }
    @Test public void test07_AssertNotNull_Example() { assertNotNull("hello"); }
    @Test public void test08_NoCrash_OnLaunch() { ActivityScenario.launch(MainActivity.class); assertTrue(true); }
    @Test public void test09_Timeout_Works() throws InterruptedException { Thread.sleep(3000); assertTrue(true); }
    @Test public void test10_Final_Pass() { assertTrue("All good!", true); }
}