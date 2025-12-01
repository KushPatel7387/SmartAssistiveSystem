package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RegistrationPasswordStrengthTest {

    private RegistrationBusinessLogic logic;

    @Before
    public void setUp() {
        logic = new RegistrationBusinessLogic();
    }

    @Test
    public void emptyPassword_isTooShort() {
        RegistrationBusinessLogic.PasswordStrength strength =
                logic.getPasswordStrength("");
        assertEquals(RegistrationBusinessLogic.PasswordStrength.TOO_SHORT, strength);
        assertNotNull(strength);
    }

    @Test
    public void nullPassword_isTooShort() {
        RegistrationBusinessLogic.PasswordStrength strength =
                logic.getPasswordStrength(null);
        assertEquals(RegistrationBusinessLogic.PasswordStrength.TOO_SHORT, strength);
    }

    @Test
    public void lengthLessThanSix_isTooShort() {
        RegistrationBusinessLogic.PasswordStrength strength =
                logic.getPasswordStrength("Ab1!");
        assertEquals(RegistrationBusinessLogic.PasswordStrength.TOO_SHORT, strength);
    }

    @Test
    public void lengthSixToSeven_isWeak() {
        RegistrationBusinessLogic.PasswordStrength strength =
                logic.getPasswordStrength("Abc12!");
        assertEquals(RegistrationBusinessLogic.PasswordStrength.WEAK, strength);
    }

    @Test
    public void lengthExactlyEight_isMedium() {
        RegistrationBusinessLogic.PasswordStrength strength =
                logic.getPasswordStrength("Admin101");
        assertEquals(RegistrationBusinessLogic.PasswordStrength.MEDIUM, strength);
    }

    @Test
    public void lengthGreaterThanEight_isStrong() {
        RegistrationBusinessLogic.PasswordStrength strength =
                logic.getPasswordStrength("Admin101!");
        assertEquals(RegistrationBusinessLogic.PasswordStrength.STRONG, strength);
    }

    @Test
    public void differentPasswords_haveDifferentStrength() {
        RegistrationBusinessLogic.PasswordStrength shortPw =
                logic.getPasswordStrength("Ab1!");
        RegistrationBusinessLogic.PasswordStrength strongPw =
                logic.getPasswordStrength("Admin101!");

        assertNotEquals(shortPw, strongPw);
    }

    @Test
    public void samePassword_sameStrength() {
        RegistrationBusinessLogic.PasswordStrength s1 =
                logic.getPasswordStrength("Admin101!");
        RegistrationBusinessLogic.PasswordStrength s2 =
                logic.getPasswordStrength("Admin101!");

        assertEquals(s1, s2);
        assertNotNull(s1);
    }

    @Test
    public void boundarySevenChars_isWeak() {
        RegistrationBusinessLogic.PasswordStrength strength =
                logic.getPasswordStrength("Abc123!");
        assertEquals(RegistrationBusinessLogic.PasswordStrength.WEAK, strength);
    }

    @Test
    public void boundaryNineChars_isStrong() {
        RegistrationBusinessLogic.PasswordStrength strength =
                logic.getPasswordStrength("Abc1234!!");
        assertEquals(RegistrationBusinessLogic.PasswordStrength.STRONG, strength);
    }
}
