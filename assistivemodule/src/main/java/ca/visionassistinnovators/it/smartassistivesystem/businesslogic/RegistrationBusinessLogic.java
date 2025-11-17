// File: RegistrationBusinessLogic.java
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.content.Context;
import android.text.TextUtils;

import ca.visionassistinnovators.it.smartassistivesystem.R;

/**
 * Handles non-UI registration rules:
 *  - Validates name, phone, email, password, confirm password
 *  - Reuses LoginValidator for email + password rules
 *  - Builds full name
 *  - Normalizes phone
 *  - Calculates password strength (for UI only)
 */
public class RegistrationBusinessLogic {

    private final NameValidator nameValidator = new NameValidator();
    private final LoginValidator loginValidator = new LoginValidator();

    // Used by RegisterActivity to show "Weak/Medium/Strong"
    public enum PasswordStrength {
        TOO_SHORT,
        WEAK,
        MEDIUM,
        STRONG
    }

    // ─────────────────────────────────────────────
    // MAIN VALIDATION (professor’s requirements)
    // ─────────────────────────────────────────────

    public ValidationResult validate(Context ctx,
                                     String firstName,
                                     String lastName,
                                     String phone,
                                     String email,
                                     String password,
                                     String confirmPassword) {

        // 1) Names required
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            return ValidationResult.error(
                    ctx.getString(R.string.err_first_and_last_name_required)
            );
        }

        // 2) Name characters (reuse same rules as elsewhere)
        if (nameValidator.isValidName(firstName) || nameValidator.isValidName(lastName)) {
            return ValidationResult.error(
                    ctx.getString(R.string.err_invalid_name_characters)
            );
        }

        // 3) Phone: must be valid 10 digits
        String normalized = normalizePhone(phone);
        if (normalized == null) {
            return ValidationResult.error(
                    ctx.getString(R.string.err_phone_10_digits)
            );
        }

        // 4) Password / confirm required (no new string resources)
        if (TextUtils.isEmpty(password)) {
            return ValidationResult.error(ctx.getString(R.string.please_enter_password));
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            return ValidationResult.error(ctx.getString(R.string.please_confirm_password));
        }

        if (!password.equals(confirmPassword)) {
            return ValidationResult.error(ctx.getString(R.string.passwords_do_not_match_));
        }

        // 5) Reuse LoginValidator for email + password rules
        LoginValidator.ValidationResult loginResult =
                loginValidator.validate(email, password);

        if (!loginResult.isValid()) {
            // Combined message from LoginValidator, e.g.
            // "Missing: at least 6 characters and 1 special character."
            String msg = loginResult.getMessage();
            if (TextUtils.isEmpty(msg)) {
                msg = ctx.getString(R.string.invalid_email_or_password_format);
            }
            return ValidationResult.error(msg);
        }

        // If we reach here, everything is valid
        return ValidationResult.ok();
    }

    // ─────────────────────────────────────────────
    // Helper methods used by RegisterActivity
    // ─────────────────────────────────────────────

    public String buildFullName(String firstName, String lastName) {
        return nameValidator.buildFullName(firstName, lastName);
    }

    /**
     * Returns 10-digit phone string or null if invalid.
     */
    public String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            return null;
        }
        return digits;
    }

    /**
     * Password strength for the UI indicator.
     * NOTE: This is for visual feedback only and does not affect validation.
     */
    public PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.TOO_SHORT;
        }

        int len = password.length();

        // < 6 → too short (also invalid per rules)
        if (len < 6) {
            return PasswordStrength.TOO_SHORT;
        }

        // 6–7 chars → weak
        if (len < 8) {
            return PasswordStrength.WEAK;
        }

        // exactly 8 chars → medium
        if (len == 8) {
            return PasswordStrength.MEDIUM;
        }

        // 9 or more → strong
        return PasswordStrength.STRONG;
    }


    // Simple result wrapper used by RegisterActivity
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
