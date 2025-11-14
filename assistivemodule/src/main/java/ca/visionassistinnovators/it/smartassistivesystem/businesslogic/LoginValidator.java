// File: LoginValidator.java
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LoginValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MIN_LENGTH = 6;

    public boolean isEmailOrPasswordEmpty(String email, String password) {
        return email == null || password == null ||
                email.trim().isEmpty() || password.trim().isEmpty();
    }

    // THIS METHOD MUST EXIST
    public ValidationResult validate(String email, String password) {
        List<String> errors = new ArrayList<>();

        // Email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email address");
        }

        // Password
        if (password.length() < MIN_LENGTH) {
            errors.add("at least " + MIN_LENGTH + " characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.add("1 uppercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            errors.add("1 digit");
        }
        if (!password.matches(".*[@#$%^&+=!].*")) {
            errors.add("1 special char (@#$%^&+=!)");
        }

        return new ValidationResult(errors);
    }

    // NESTED CLASS — MUST BE HERE
    public static class ValidationResult {
        private final List<String> errors;
        private final boolean isValid;

        ValidationResult(List<String> errors) {
            this.errors = errors;
            this.isValid = errors.isEmpty();
        }

        public boolean isValid() { return isValid; }

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
            return errors.stream().anyMatch(e -> e.contains("email"));
        }
    }
}