package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LoginValidatorTest {

    private LoginValidator validator;

    @Before
    public void setUp() {
        validator = new LoginValidator();
    }

    @Test
    public void validEmailAndStrongPassword_returnsValid() {
        LoginValidator.ValidationResult result =
                validator.validate("aaa@bbb.com", "Admin101!");
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    public void missingAtSymbol_returnsInvalid() {
        LoginValidator.ValidationResult result =
                validator.validate("aaabbb.com", "Admin101!");
        assertFalse(result.isValid());
        assertNotNull(result.getMessage());
    }

    @Test
    public void missingDomain_returnsInvalid() {
        LoginValidator.ValidationResult result =
                validator.validate("aaa@", "Admin101!");
        assertFalse(result.isValid());
        assertNotNull(result.getMessage());
    }

    @Test
    public void emptyPassword_returnsInvalid() {
        LoginValidator.ValidationResult result =
                validator.validate("aaa@bbb.com", "");
        assertFalse(result.isValid());
        assertNotNull(result.getMessage());
    }

    @Test
    public void shortPassword_returnsInvalid() {
        LoginValidator.ValidationResult result =
                validator.validate("aaa@bbb.com", "Ab1!");
        assertFalse(result.isValid());
    }

    @Test
    public void passwordWithoutDigit_returnsInvalid() {
        LoginValidator.ValidationResult result =
                validator.validate("aaa@bbb.com", "Admin!!!!");
        assertFalse(result.isValid());
    }

    @Test
    public void passwordWithoutSpecialChar_returnsInvalid() {
        LoginValidator.ValidationResult result =
                validator.validate("aaa@bbb.com", "Admin1234");
        assertFalse(result.isValid());
    }

    @Test
    public void invalidEmailAndPassword_returnsInvalid() {
        LoginValidator.ValidationResult result =
                validator.validate("wrong", "bad");
        assertFalse(result.isValid());
        assertNotEquals("", result.getMessage());
    }

    @Test
    public void minimalValidPasswordBoundary_returnsValid() {
        // adjust if your password rules are stricter
        LoginValidator.ValidationResult result =
                validator.validate("aaa@bbb.com", "Abc12!");
        assertTrue(result.isValid());
    }

    @Test
    public void uppercaseEmailStillValid_returnsValid() {
        LoginValidator.ValidationResult result =
                validator.validate("AAA@BBB.COM", "Admin101!");
        assertTrue(result.isValid());
    }
}
