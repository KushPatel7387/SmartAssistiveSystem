/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.login;

public class UserModel {
    public String name;
    public String phone;
    public String email;
    public String role;

    public UserModel() { } // required for Firebase

    public UserModel(String name, String phone, String email, String role) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
    }
}
