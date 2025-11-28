package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates login and registration credentials.
 * Used by LoginActivity + RegistrationBusinessLogic.
 */
public class LoginValidator {

    // Email format validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Minimum password length
    private static final int MIN_LENGTH = 6;

    public boolean isEmailOrPasswordEmpty(String email, String password) {
        return email == null || password == null ||
                email.trim().isEmpty() || password.trim().isEmpty();
    }

    /**
     * Main validation method used throughout the app.
     */
    public ValidationResult validate(String email, String password) {
        List<String> errors = new ArrayList<>();

        // Null safety
        if (email == null) email = "";
        if (password == null) password = "";

        // Email format check
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email address");
        }

        // Minimum length check
        if (password.length() < MIN_LENGTH) {
            errors.add("at least " + MIN_LENGTH + " characters");
        }

        // Password uppercase rule
        if (!password.matches(".*[A-Z].*")) {
            errors.add("1 uppercase letter");
        }

        // Password digit rule
        if (!password.matches(".*[0-9].*")) {
            errors.add("1 digit");
        }

        // Password special character rule
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            errors.add("1 special character");
        }

        return new ValidationResult(errors);
    }

    /**
     * Nested class to wrap validation results.
     */
    public static class ValidationResult {

        private final List<String> errors;
        private final boolean isValid;

        ValidationResult(List<String> errors) {
            this.errors = errors;
            this.isValid = errors.isEmpty();
        }

        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            if (errors.isEmpty()) return null;

            StringBuilder sb = new StringBuilder("Missing: ");
            for (int i = 0; i < errors.size(); i++) {
                if (i > 0) sb.append(i == errors.size() - 1 ? " and " : ", ");
                sb.append(errors.get(i));
            }
            return sb.append(".").toString();
        }

        public boolean isEmailError() {
            for (String e : errors) {
                if (e.toLowerCase().contains("email")) return true;
            }
            return false;
        }
    }
}
