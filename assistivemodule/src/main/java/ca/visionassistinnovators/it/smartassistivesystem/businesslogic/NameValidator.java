package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.text.TextUtils;

/**
 * Shared business rules for names (first, last, or full):
 *  - Not empty
 *  - Only letters, spaces, apostrophes, and hyphens
 *  - Can be auto-formatted to Capitalized form
 */
public class NameValidator {

    /**
     * Check if a given name is syntactically valid.
     * Allows A–Z, a–z, spaces, apostrophes, and hyphens.
     */
    public boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String trimmed = name.trim();
        // At least 1 letter, no digits or weird symbols
        return trimmed.matches("[A-Za-z][A-Za-z '\\-]*");
    }

    /**
     * Capitalize a single name segment: "sarang" -> "Sarang"
     */
    public String formatSingleName(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim().toLowerCase();
        if (trimmed.isEmpty()) return "";
        return Character.toUpperCase(trimmed.charAt(0)) +
                (trimmed.length() > 1 ? trimmed.substring(1) : "");
    }

    /**
     * Build a full name from first and last, formatted.
     */
    public String buildFullName(String firstName, String lastName) {
        return formatSingleName(firstName) + " " + formatSingleName(lastName);
    }
}
