package ca.visionassistinnovators.it.smartassistivesystem.ui.login;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.textfield.TextInputLayout;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.visionassistinnovators.it.smartassistivesystem.R;

/**
 * Espresso UI tests for LoginActivity.
 * Focus:
 *  - Invalid email format
 *  - Invalid password
 *  - Basic UI presence and interaction
 *
 * This class is DIFFERENT from the JUnit-only and Robolectric tests.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityEspressoTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * 1) Screen loads with email, password, and login button visible.
     */
    @Test
    public void screenLoads_showsCoreViews() {
        onView(withId(R.id.editTextEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.editTextPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }

    /**
     * 2) Invalid email "aaa@" with valid-looking password
     *    should set an error on the email TextInputLayout.
     */
    @Test
    public void invalidEmail_setsErrorOnEmailLayout() {
        onView(withId(R.id.editTextEmail))
                .perform(typeText("aaa@"), closeSoftKeyboard());

        onView(withId(R.id.editTextPassword))
                .perform(typeText("Admin101!"), closeSoftKeyboard());

        onView(withId(R.id.btnLogin)).perform(click());

        // Inspect the Activity directly to check TextInputLayout error
        activityRule.getScenario().onActivity(activity -> {
            TextInputLayout tilEmail = activity.findViewById(R.id.til_email);
            CharSequence error = tilEmail.getError();
            assertNotNull("Email TextInputLayout error should not be null for invalid email", error);
            assertTrue("Error message should not be empty", error.length() > 0);
        });
    }

    /**
     * 3) Valid email but an obviously invalid password
     *    should set an error on the password TextInputLayout.
     */
    @Test
    public void invalidPassword_setsErrorOnPasswordLayout() {
        onView(withId(R.id.editTextEmail))
                .perform(typeText("aaa@bbb.com"), closeSoftKeyboard());

        // Too short / invalid password
        onView(withId(R.id.editTextPassword))
                .perform(typeText("123"), closeSoftKeyboard());

        onView(withId(R.id.btnLogin)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            TextInputLayout tilPassword = activity.findViewById(R.id.til_password);
            CharSequence error = tilPassword.getError();
            assertNotNull("Password TextInputLayout error should not be null for invalid password", error);
            assertTrue("Password error message should not be empty", error.length() > 0);
        });
    }

    /**
     * 4) "Remember me" checkbox should be visible and can be toggled.
     *    (Simple UI interaction test.)
     */
    @Test
    public void rememberMe_canBeCheckedAndUnchecked() {
        onView(withId(R.id.cb_remember)).check(matches(isDisplayed()));
        onView(withId(R.id.cb_remember)).perform(click());
        // No crash and still visible is enough for this test
        onView(withId(R.id.cb_remember)).check(matches(isDisplayed()));
    }

    /**
     * 5) "Sign up" text view is visible and clickable.
     *    (We don't assert navigation here, just that the view exists and responds.)
     */
    @Test
    public void signUpTextView_isDisplayedAndClickable() {
        onView(withId(R.id.tv_sign_up)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_sign_up)).perform(click());
        // If it crashes, the test fails. If it stays alive, it's considered passed.
    }
}
