package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import java.util.Locale;

/**
 * Shared business rules for names (first, last, or full):
 *  - Not null or empty
 *  - Only letters, spaces, apostrophes, and hyphens
 *  - Can be auto-formatted to Capitalized form
 */
public class NameValidator {

    /**
     * Returns true only when the name is syntactically valid.
     * Allows A–Z, a–z, spaces, apostrophes (') and hyphens (-).
     */
    public boolean isValidName(String name) {
        if (name == null) return false;

        String trimmed = name.trim();
        if (trimmed.isEmpty()) return false;

        // Must start with a letter, rest can be letters/space/'/-
        return trimmed.matches("[A-Za-z][A-Za-z '\\-]*");
    }

    /**
     * Capitalize a single name part ("sarang" -> "Sarang").
     */
    public String formatSingleName(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";

        String lower = trimmed.toLowerCase(Locale.getDefault());
        return Character.toUpperCase(lower.charAt(0)) +
                (lower.length() > 1 ? lower.substring(1) : "");
    }

    /**
     * Build a full name from first and last, formatted.
     */
    public String buildFullName(String firstName, String lastName) {
        String first = formatSingleName(firstName);
        String last  = formatSingleName(lastName);
        return (first + " " + last).trim();
    }
}
