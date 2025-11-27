package com.example.fusion0_lottery;

/**
 * Model class representing a notification log entry for admin viewing
 */
public class NotificationLog {
    private String logId;
    private String organizerId;
    private String organizerName;
    private String recipientId;
    private String recipientName;
    private String eventId;
    private String eventName;
    private String messageType;
    private String messageBody;
    private long timestamp;
    private String title;
    private String userNotificationId; // ID of the notification in Users/{userId}/Notifications

    public NotificationLog() {
        // Required empty constructor for Firestore
    }

    public NotificationLog(String logId, String organizerId, String organizerName,
                          String recipientId, String recipientName, String eventId,
                          String eventName, String messageType, String messageBody,
                          long timestamp, String title) {
        this.logId = logId;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.messageType = messageType;
        this.messageBody = messageBody;
        this.timestamp = timestamp;
        this.title = title;
    }

    // Getters
    public String getLogId() { return logId; }
    public String getOrganizerId() { return organizerId; }
    public String getOrganizerName() { return organizerName; }
    public String getRecipientId() { return recipientId; }
    public String getRecipientName() { return recipientName; }
    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getMessageType() { return messageType; }
    public String getMessageBody() { return messageBody; }
    public long getTimestamp() { return timestamp; }
    public String getTitle() { return title; }
    public String getUserNotificationId() { return userNotificationId; }

    // Setters
    public void setLogId(String logId) { this.logId = logId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setTitle(String title) { this.title = title; }
    public void setUserNotificationId(String userNotificationId) { this.userNotificationId = userNotificationId; }
}
