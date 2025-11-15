package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import ca.visionassistinnovators.it.smartassistivesystem.R;

/**
 * Business rules for registration:
 *  - First + last name required + valid characters
 *  - Phone must be 10 digits
 *  - Email valid
 *  - Password: min 8 chars, 1 upper, 1 lower, 1 special
 *  - Exposes password strength (weak/medium/strong)
 */
public class RegistrationBusinessLogic {

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    public enum PasswordStrength {
        TOO_SHORT,
        WEAK,
        MEDIUM,
        STRONG
    }

    private final NameValidator nameValidator = new NameValidator();

    public ValidationResult validate(Context ctx,
                                     String firstName,
                                     String lastName,
                                     String phone,
                                     String email,
                                     String pass,
                                     String conf) {

        // First & last name required
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            return new ValidationResult(false,
                    ctx.getString(R.string.err_first_and_last_name_required));
        }

        // Same rules for names everywhere
        if (!nameValidator.isValidName(firstName) || !nameValidator.isValidName(lastName)) {
            return new ValidationResult(false,
                    ctx.getString(R.string.err_invalid_name_characters));
        }

        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            return new ValidationResult(false,
                    ctx.getString(R.string.err_phone_10_digits));
        }

        if (TextUtils.isEmpty(email)) {
            return new ValidationResult(false,
                    ctx.getString(R.string.all_fields_required));
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResult(false,
                    ctx.getString(R.string.invalid_email_format));
        }

        if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(conf)) {
            return new ValidationResult(false,
                    ctx.getString(R.string.all_fields_required));
        }

        // Min length 8
        if (pass.length() < 8) {
            return new ValidationResult(false,
                    ctx.getString(R.string.password_too_short));
        }

        // Complexity: 1 upper, 1 lower, 1 special
        if (!isPasswordComplexEnough(pass)) {
            return new ValidationResult(false,
                    ctx.getString(R.string.err_password_complexity));
        }

        if (!pass.equals(conf)) {
            return new ValidationResult(false,
                    ctx.getString(R.string.passwords_do_not_match));
        }

        return new ValidationResult(true, null);
    }

    public String buildFullName(String firstName, String lastName) {
        return nameValidator.buildFullName(firstName, lastName);
    }

    /**
     * Keep only digits, require exactly 10. Returns null if invalid.
     */
    public String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            return null;
        }
        return digits;
    }

    // ─────────────────────────────────────────
    // Password helpers
    // ─────────────────────────────────────────

    /**
     * At least 8 chars, 1 upper, 1 lower, 1 special char.
     */
    public boolean isPasswordComplexEnough(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper   = password.matches(".*[A-Z].*");
        boolean hasLower   = password.matches(".*[a-z].*");
        boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");

        return hasUpper && hasLower && hasSpecial;
    }

    /**
     * Strength based on length ONLY:
     *  <8        -> TOO_SHORT
     *  8-9       -> WEAK
     *  10-11     -> MEDIUM
     *  12 or more-> STRONG
     */
    public PasswordStrength getPasswordStrength(String password) {
        if (password == null) return PasswordStrength.TOO_SHORT;
        int len = password.length();

        if (len < 8) {
            return PasswordStrength.TOO_SHORT;
        } else if (len < 10) {
            return PasswordStrength.WEAK;
        } else if (len < 12) {
            return PasswordStrength.MEDIUM;
        } else {
            return PasswordStrength.STRONG;
        }
    }
}
