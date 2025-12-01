package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ca.visionassistinnovators.it.smartassistivesystem.R;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class RegistrationBusinessLogicRobolectricTest {

    private Context context;
    private RegistrationBusinessLogic logic;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        logic = new RegistrationBusinessLogic();
    }

    @Test
    public void validInputs_returnsOk() {
        RegistrationBusinessLogic.ValidationResult result =
                logic.validate(
                        context,
                        "John",
                        "Doe",
                        "1234567890",
                        "aaa@bbb.com",
                        "Admin101!",
                        "Admin101!"
                );

        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    public void missingFirstName_returnsError() {
        RegistrationBusinessLogic.ValidationResult result =
                logic.validate(
                        context,
                        "",
                        "Doe",
                        "1234567890",
                        "aaa@bbb.com",
                        "Admin101!",
                        "Admin101!"
                );

        assertFalse(result.isValid());
        assertEquals(context.getString(R.string.err_first_and_last_name_required),
                result.getMessage());
    }

    @Test
    public void invalidPhone_returnsError() {
        RegistrationBusinessLogic.ValidationResult result =
                logic.validate(
                        context,
                        "John",
                        "Doe",
                        "12345",  // too short
                        "aaa@bbb.com",
                        "Admin101!",
                        "Admin101!"
                );

        assertFalse(result.isValid());
        assertEquals(context.getString(R.string.err_phone_10_digits),
                result.getMessage());
    }

    @Test
    public void mismatchedPasswords_returnsError() {
        RegistrationBusinessLogic.ValidationResult result =
                logic.validate(
                        context,
                        "John",
                        "Doe",
                        "1234567890",
                        "aaa@bbb.com",
                        "Admin101!",
                        "Admin101?"
                );

        assertFalse(result.isValid());
        assertEquals(context.getString(R.string.passwords_do_not_match_),
                result.getMessage());
    }

    @Test
    public void invalidEmailFormat_returnsError() {
        RegistrationBusinessLogic.ValidationResult result =
                logic.validate(
                        context,
                        "John",
                        "Doe",
                        "1234567890",
                        "aaa@",           // invalid email
                        "Admin101!",
                        "Admin101!"
                );

        assertFalse(result.isValid());
        assertNotNull(result.getMessage());
    }
}
