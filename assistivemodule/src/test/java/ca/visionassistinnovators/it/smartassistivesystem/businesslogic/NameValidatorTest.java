package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for NameValidator (pure business logic).
 * Uses: assertTrue, assertFalse, assertEquals, assertNotEquals, assertNotNull.
 */
public class NameValidatorTest {

    private NameValidator validator;

    @Before
    public void setUp() {
        validator = new NameValidator();
    }

    @Test
    public void validSimpleName_returnsTrue() {
        assertTrue(validator.isValidName("Sarang"));
    }

    @Test
    public void validNameWithSpace_returnsTrue() {
        assertTrue(validator.isValidName("Krish Patel"));
    }

    @Test
    public void emptyName_returnsFalse() {
        assertFalse(validator.isValidName(""));
    }

    @Test
    public void nullName_returnsFalse() {
        assertFalse(validator.isValidName(null));
    }

    @Test
    public void nameWithDigits_returnsFalse() {
        assertFalse(validator.isValidName("Daksh123"));
    }

    @Test
    public void nameWithSymbols_returnsFalse() {
        assertFalse(validator.isValidName("Kush@Patel"));
    }

    @Test
    public void buildFullName_basic() {
        String full = validator.buildFullName("Sarang", "Prajapati");
        assertEquals("Sarang Prajapati", full);
        assertNotNull(full);
    }

    @Test
    public void buildFullName_trimsExtraSpaces() {
        String full = validator.buildFullName("  Krish  ", "  Patel ");
        assertEquals("Krish Patel", full);
    }

    @Test
    public void buildFullName_notEqualsDifferentOrder() {
        String full = validator.buildFullName("Daksh", "Rana");
        assertNotEquals("Rana Daksh", full);
    }

    @Test
    public void longButValidName_returnsTrue() {
        assertTrue(validator.isValidName("Very Long Name With Spaces"));
    }
}
