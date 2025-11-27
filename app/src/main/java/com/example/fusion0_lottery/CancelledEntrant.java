package com.example.fusion0_lottery;

/**
 * Data class representing a cancelled entrant
 * Includes user details and cancellation information
 */
public class CancelledEntrant {
    private String userId;
    private String name;
    private String email;
    private String reason;

    public CancelledEntrant(String userId, String name, String email, String reason) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }
}
