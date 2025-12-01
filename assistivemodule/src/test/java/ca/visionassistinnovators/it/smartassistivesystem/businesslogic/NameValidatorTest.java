package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NameValidatorTest {

    private NameValidator validator;

    @Before
    public void setUp() {
        validator = new NameValidator();
    }

    @Test
    public void validSimpleName_returnsTrue() {
        assertTrue(validator.isValidName("John"));
    }

    @Test
    public void validNameWithSpace_returnsTrue() {
        assertTrue(validator.isValidName("John Doe"));
    }

    @Test
    public void validNameWithHyphen_returnsTrue() {
        assertTrue(validator.isValidName("Mary-Anne"));
    }

    @Test
    public void validNameWithApostrophe_returnsTrue() {
        assertTrue(validator.isValidName("O'Connor"));
    }

    @Test
    public void nullName_returnsFalse() {
        assertFalse(validator.isValidName(null));
    }

    @Test
    public void emptyName_returnsFalse() {
        assertFalse(validator.isValidName(""));
    }

    @Test
    public void spacesOnly_returnsFalse() {
        assertFalse(validator.isValidName("    "));
    }

    @Test
    public void nameWithDigits_returnsFalse() {
        assertFalse(validator.isValidName("John123"));
    }

    @Test
    public void nameWithSymbols_returnsFalse() {
        assertFalse(validator.isValidName("John@Doe"));
    }

    @Test
    public void formatSingleName_capitalizesCorrectly() {
        String formatted = validator.formatSingleName("  sarang  ");
        assertEquals("Sarang", formatted);
        assertNotEquals("sarang", formatted);
    }
}
