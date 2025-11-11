package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

public class LoginValidator {

    public boolean isEmailOrPasswordEmpty(String email, String password) {
        return email == null || password == null ||
                email.trim().isEmpty() || password.trim().isEmpty();
    }
}
