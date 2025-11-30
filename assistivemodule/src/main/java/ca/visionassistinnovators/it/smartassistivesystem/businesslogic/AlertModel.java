package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 *
 * Represents a single alert shown in the AlertsFragment.
 */
public class AlertModel {

    public String id;          // Firebase key
    public String title;       // Alert title
    public String message;     // Alert description
    public long timestamp;     // Unix time

    // Required by Firebase
    public AlertModel() {}

    public AlertModel(String id, String title, String message, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Helper (useful for adapters)
    public String getFormattedTimestamp() {
        return android.text.format.DateFormat
                .format("dd MMM, hh:mm a", timestamp)
                .toString();
    }
}
