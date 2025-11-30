package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 *
 * Patient model representing one linked patient under a guardian.
 * Fully Firebase-compatible (public fields + empty constructor).
 */
public class PatientModel {

    // ----------------------------
    // Patient Identifiers
    // ----------------------------
    public String id;            // Firebase key (auto-generated)
    public String guardianId;    // UID of the logged-in guardian
    public String fullName;
    public String phone;
    public String email;

    // ----------------------------
    // Sensor Architecture Fields
    // ----------------------------
    public Float sensor1;
    public Float sensor2;
    public Float sensor3;
    public Float sensor4;

    // ----------------------------
    // ⭐ NEW — GPS Tracking Fields
    // ----------------------------
    public Double latitude;      // Last known latitude
    public Double longitude;     // Last known longitude
    public Long lastUpdated;     // Timestamp (ms)

    // Required empty constructor for Firebase
    public PatientModel() {}

    public PatientModel(
            String id,
            String guardianId,
            String fullName,
            String phone,
            String email,
            Float sensor1,
            Float sensor2,
            Float sensor3,
            Float sensor4,
            Double latitude,
            Double longitude,
            Long lastUpdated
    ) {
        this.id = id;
        this.guardianId = guardianId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;

        this.sensor1 = sensor1;
        this.sensor2 = sensor2;
        this.sensor3 = sensor3;
        this.sensor4 = sensor4;

        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdated = lastUpdated;
    }

    // Convenience method (optional, does NOT break Firebase)
    public boolean hasAnySensorData() {
        return sensor1 != null || sensor2 != null || sensor3 != null || sensor4 != null;
    }

    // OPTIONAL helper for tracking
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}
