package com.example.fusion0_lottery;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to log notifications to the centralized NotificationLogs collection
 * for admin monitoring and auditing purposes
 */
public class NotificationLogger {

    private static final String TAG = "NotificationLogger";

    /**
     * Logs a notification to the centralized NotificationLogs collection
     *
     * @param recipientId The user ID of the notification recipient
     * @param recipientName The name of the recipient
     * @param eventId The event ID
     * @param eventName The event name
     * @param messageType The type of notification (e.g., "winner_notification")
     * @param messageBody The notification message content
     * @param title The notification title
     * @param userNotificationId The document ID of the notification in Users/{userId}/Notifications
     */
    public static void logNotification(String recipientId, String recipientName,
                                      String eventId, String eventName,
                                      String messageType, String messageBody,
                                      String title, String userNotificationId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "Cannot log notification: No current user");
            return;
        }

        String organizerId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Logging notification for recipient: " + recipientId + ", organizer: " + organizerId);

        // Fetch organizer name from Firestore
        db.collection("Users").document(organizerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String organizerName = documentSnapshot.getString("name");
                    if (organizerName == null) {
                        organizerName = "Unknown Organizer";
                    }

                    // Create log entry
                    Map<String, Object> logEntry = new HashMap<>();
                    logEntry.put("organizerId", organizerId);
                    logEntry.put("organizerName", organizerName);
                    logEntry.put("recipientId", recipientId);
                    logEntry.put("recipientName", recipientName);
                    logEntry.put("eventId", eventId);
                    logEntry.put("eventName", eventName);
                    logEntry.put("type", messageType);
                    logEntry.put("body", messageBody);
                    logEntry.put("title", title);
                    logEntry.put("timestamp", System.currentTimeMillis());
                    logEntry.put("userNotificationId", userNotificationId);

                    // Add to NotificationLogs collection
                    db.collection("NotificationLogs").add(logEntry)
                            .addOnSuccessListener(docRef -> {
                                Log.d(TAG, "Successfully logged notification: " + docRef.getId());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to log notification to Firestore", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch organizer name", e);
                });
    }

    /**
     * Logs a notification with organizer name already known
     */
    public static void logNotificationWithOrganizerName(String organizerId, String organizerName,
                                                       String recipientId, String recipientName,
                                                       String eventId, String eventName,
                                                       String messageType, String messageBody,
                                                       String title) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Logging notification (with organizer name) for recipient: " + recipientId);

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("organizerId", organizerId);
        logEntry.put("organizerName", organizerName != null ? organizerName : "Unknown Organizer");
        logEntry.put("recipientId", recipientId);
        logEntry.put("recipientName", recipientName != null ? recipientName : "Unknown Recipient");
        logEntry.put("eventId", eventId);
        logEntry.put("eventName", eventName);
        logEntry.put("type", messageType);
        logEntry.put("body", messageBody);
        logEntry.put("title", title);
        logEntry.put("timestamp", System.currentTimeMillis());

        db.collection("NotificationLogs").add(logEntry)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Successfully logged notification: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to log notification to Firestore", e);
                });
    }
}
