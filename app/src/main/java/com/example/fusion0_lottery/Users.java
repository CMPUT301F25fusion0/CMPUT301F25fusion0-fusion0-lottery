package com.example.fusion0_lottery;


public class Users {

    private String username;
    private String email;
    private String phone;
    private String password;
    private String userRole; // Entrant or Organizer
    private String deviceId; // Device-based tracking

    public Users() {
        // Default constructor required for Firebase
    }

    public Users(String username, String email, String phone, String password, String userRole, String deviceId) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.userRole = userRole;
        this.deviceId = deviceId;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
