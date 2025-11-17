package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

/**
 * Represents one patient of a guardian.
 * Sensor fields are placeholders for your 4 sensors.
 */
public class PatientModel {

    public String id;          // Firebase key
    public String guardianId;  // UID of logged-in guardian
    public String fullName;
    public String phone;
    public String email;

    // Optional sensor readings (can be filled from DB)
    public Float sensor1;
    public Float sensor2;
    public Float sensor3;
    public Float sensor4;

    public PatientModel() {
        // required for Firebase
    }

    public PatientModel(String id,
                        String guardianId,
                        String fullName,
                        String phone,
                        String email,
                        Float sensor1,
                        Float sensor2,
                        Float sensor3,
                        Float sensor4) {
        this.id = id;
        this.guardianId = guardianId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.sensor1 = sensor1;
        this.sensor2 = sensor2;
        this.sensor3 = sensor3;
        this.sensor4 = sensor4;
    }
}
