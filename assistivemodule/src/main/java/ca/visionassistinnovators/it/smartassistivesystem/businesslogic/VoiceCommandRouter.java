/**
 * Simple router that maps recognized speech to a destination tab.
 *
 * Examples:
 *  - "open home"      → HOME
 *  - "go to sensors"  → SENSORS
 *  - "open feedback"  → FEEDBACK
 *  - "open settings"  → SETTINGS
 *  - "open patients" / "open sos" → PATIENTS
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import java.util.Locale;

public class VoiceCommandRouter {

    public enum Destination {
        HOME,
        SENSORS,
        FEEDBACK,
        SETTINGS,
        PATIENTS,
        UNKNOWN
    }

    public Destination resolve(String spokenRaw) {
        if (spokenRaw == null) return Destination.UNKNOWN;

        String spoken = spokenRaw.toLowerCase(Locale.getDefault());

        // Home / dashboard
        if (spoken.contains("home") || spoken.contains("dashboard")) {
            return Destination.HOME;
        }

        // Sensors
        if (spoken.contains("sensor") || spoken.contains("sensors")) {
            return Destination.SENSORS;
        }

        // Feedback
        if (spoken.contains("feedback") || spoken.contains("review")) {
            return Destination.FEEDBACK;
        }

        // Settings
        if (spoken.contains("setting") || spoken.contains("settings") ||
                spoken.contains("preference") || spoken.contains("preferences")) {
            return Destination.SETTINGS;
        }

        // Patients / SOS
        if (spoken.contains("patient") || spoken.contains("patients") ||
                spoken.contains("sos") || spoken.contains("emergency")) {
            return Destination.PATIENTS;
        }

        return Destination.UNKNOWN;
    }
}
