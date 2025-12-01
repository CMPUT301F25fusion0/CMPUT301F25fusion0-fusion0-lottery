package com.example.fusion0_lottery;

/**
 * Data class representing an entrant in the final accepted list
 * Includes all contact and registration information
 */
public class FinalListEntrant {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String status;

    public FinalListEntrant(String userId, String name, String email, String phone, String status) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }
}
