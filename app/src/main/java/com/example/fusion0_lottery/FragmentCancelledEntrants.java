package com.example.fusion0_lottery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment to display cancelled/declined entrants
 * Includes entrants who declined invitations and those cancelled by organizer
 * Allows organizer to restore or permanently remove entrants
 */
public class FragmentCancelledEntrants extends Fragment {

    private ListView cancelledListView;
    private TextView emptyText;
    private Button backButton, sendNotificationButton;
    private ArrayList<CancelledEntrant> cancelledList;

    private ArrayAdapter<String> cancelledAdapter;
    private Spinner sortFilter;

    private FirebaseFirestore db;
    private String eventId;

    public FragmentCancelledEntrants() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cancelled_entrants, container, false);

        cancelledListView = view.findViewById(R.id.winnerListView);
        emptyText = view.findViewById(R.id.emptyText);
        backButton = view.findViewById(R.id.backButton);
        sendNotificationButton = view.findViewById(R.id.sendNotificationButton);
        sortFilter = view.findViewById(R.id.sortFilter);

        db = FirebaseFirestore.getInstance();
        cancelledList = new ArrayList<>();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadCancelledEntrants();
        }

        sendNotificationButton.setOnClickListener(v -> showNotificationDialog());
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Click to show restore options
        cancelledListView.setOnItemClickListener((parent, v, position, id) -> {
            if (position < cancelledList.size()) {
                CancelledEntrant entrant = cancelledList.get(position);
                showRestoreDialog(entrant, position);
            }
        });

        sortFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCancelledListDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        return view;
    }

    /**
     * Load all cancelled entrants from both winnersList (Declined) and cancelledUsers list
     */
    private void loadCancelledEntrants() {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cancelledList.clear();
                    Map<String, String> userReasons = new HashMap<>(); // userId -> reason

                    // Get declined users from winnersList
                    List<Map<String, Object>> winnersListEntrants = (List<Map<String, Object>>) snapshot.get("winnersList");
                    if (winnersListEntrants != null) {
                        for (Map<String, Object> entrant : winnersListEntrants) {
                            String status = (String) entrant.get("status");
                            if ("Declined".equals(status)) {
                                String userId = (String) entrant.get("userId");
                                if (userId != null) {
                                    userReasons.put(userId, "Declined invitation");
                                }
                            }
                        }
                    }

                    // Get cancelled users list
                    List<String> cancelledUsers = (List<String>) snapshot.get("cancelledUsers");
                    if (cancelledUsers != null) {
                        for (String userId : cancelledUsers) {
                            if (userId != null && !userReasons.containsKey(userId)) {
                                userReasons.put(userId, "Cancelled by organizer");
                            }
                        }
                    }

                    if (userReasons.isEmpty()) {
                        cancelledListView.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText("No cancelled entrants");
                        return;
                    }

                    loadUserDetails(new ArrayList<>(userReasons.keySet()), userReasons);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Load user details for all cancelled entrants
     */
    private void loadUserDetails(List<String> userIds, Map<String, String> reasons) {
        final int[] loadedCount = {0};

        for (String userId : userIds) {
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(userSnap -> {
                        loadedCount[0]++;

                        if (userSnap.exists()) {
                            String name = userSnap.getString("name");
                            String email = userSnap.getString("email");
                            String reason = reasons.get(userId);

                            cancelledList.add(new CancelledEntrant(
                                    userId,
                                    name != null ? name : "Unknown",
                                    email != null ? email : "No email",
                                    reason != null ? reason : "Unknown reason"
                            ));
                        }

                        if (loadedCount[0] == userIds.size()) {
                            cancelledListView.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.GONE);
                            updateCancelledListDisplay();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == userIds.size()) {
                            if (cancelledList.isEmpty()) {
                                cancelledListView.setVisibility(View.GONE);
                                emptyText.setVisibility(View.VISIBLE);
                            } else {
                                updateCancelledListDisplay();
                            }
                        }
                    });
        }
    }

    /**
     * Update ListView display with name, email, and cancellation reason
     */
    private void updateCancelledListDisplay() {
        if (sortFilter.getSelectedItemPosition() == 0) {
            // Sort by name
            Collections.sort(cancelledList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        }

        ArrayList<String> displayList = new ArrayList<>();

        for (CancelledEntrant entrant : cancelledList) {
            displayList.add(
                    "Name: " + entrant.getName() + "\n" +
                    "Email: " + entrant.getEmail() + "\n" +
                    "Reason: " + entrant.getReason()
            );
        }

        cancelledAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayList);
        cancelledListView.setAdapter(cancelledAdapter);
    }

    /**
     * Show dialog with options to restore or permanently remove entrant
     */
    private void showRestoreDialog(CancelledEntrant entrant, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Manage Cancelled Entrant")
                .setMessage(entrant.getName() + "\n" + entrant.getEmail() + "\n\n" +
                        "Reason: " + entrant.getReason())
                .setPositiveButton("Restore to Waiting List", (dialog, which) -> restoreToWaitingList(entrant, position))
                .setNeutralButton("Remove Permanently", (dialog, which) -> removePermanently(entrant, position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Restore entrant to waiting list
     */
    private void restoreToWaitingList(CancelledEntrant entrant, int position) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get current lists
                    List<Object> waitingList = (List<Object>) snapshot.get("waitingList");
                    List<Map<String, Object>> winnersList = (List<Map<String, Object>>) snapshot.get("winnersList");
                    List<String> cancelledUsers = (List<String>) snapshot.get("cancelledUsers");

                    if (waitingList == null) waitingList = new ArrayList<>();
                    if (winnersList == null) winnersList = new ArrayList<>();
                    if (cancelledUsers == null) cancelledUsers = new ArrayList<>();

                    // Remove from declined/cancelled
                    winnersList.removeIf(entry -> entrant.getUserId().equals(entry.get("userId")));
                    cancelledUsers.remove(entrant.getUserId());

                    // Add to waiting list
                    Map<String, Object> waitingEntry = new HashMap<>();
                    waitingEntry.put("userId", entrant.getUserId());
                    waitingEntry.put("joinedAt", System.currentTimeMillis());
                    waitingList.add(waitingEntry);

                    // Update Firebase
                    db.collection("Events").document(eventId)
                            .update(
                                    "waitingList", waitingList,
                                    "winnersList", winnersList,
                                    "cancelledUsers", cancelledUsers
                            )
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), entrant.getName() + " restored to waiting list", Toast.LENGTH_SHORT).show();
                                loadCancelledEntrants(); // Refresh
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to restore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Permanently remove entrant from all lists
     */
    private void removePermanently(CancelledEntrant entrant, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Permanent Removal")
                .setMessage("Are you sure you want to permanently remove " + entrant.getName() + "? This cannot be undone.")
                .setPositiveButton("Remove", (dialog, which) -> {
                    db.collection("Events").document(eventId).get()
                            .addOnSuccessListener(snapshot -> {
                                if (!snapshot.exists()) return;

                                List<Map<String, Object>> winnersList = (List<Map<String, Object>>) snapshot.get("winnersList");
                                List<String> cancelledUsers = (List<String>) snapshot.get("cancelledUsers");

                                if (winnersList == null) winnersList = new ArrayList<>();
                                if (cancelledUsers == null) cancelledUsers = new ArrayList<>();

                                // Remove from all lists
                                winnersList.removeIf(entry -> entrant.getUserId().equals(entry.get("userId")));
                                cancelledUsers.remove(entrant.getUserId());

                                db.collection("Events").document(eventId)
                                        .update(
                                                "winnersList", winnersList,
                                                "cancelledUsers", cancelledUsers
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), entrant.getName() + " removed permanently", Toast.LENGTH_SHORT).show();
                                            loadCancelledEntrants();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Show dialog to send notification to all cancelled entrants
     */
    private void showNotificationDialog() {
        if (cancelledList.isEmpty()) {
            Toast.makeText(getContext(), "No cancelled entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText messageInput = new EditText(getContext());
        messageInput.setHint("Enter your message here...");
        messageInput.setMinLines(4);
        messageInput.setMaxLines(10);
        messageInput.setVerticalScrollBarEnabled(true);
        messageInput.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(getContext())
                .setTitle("Notify Cancelled Entrants")
                .setMessage("Send a notification to all " + cancelledList.size() + " cancelled entrants:")
                .setView(messageInput)
                .setPositiveButton("Send Notification", (dialog, which) -> {
                    String message = messageInput.getText().toString().trim();
                    if (message.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                    } else {
                        sendNotificationToCancelledEntrants(message);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Send notification to all cancelled entrants
     */
    private void sendNotificationToCancelledEntrants(String message) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    String eventName = snapshot.getString("eventName");
                    if (eventName == null) {
                        eventName = "Event";
                    }
                    final String finalEventName = eventName;

                    // Create notification data
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("title", "Update from Organizer - " + finalEventName);
                    notification.put("body", message);
                    notification.put("eventId", eventId);
                    notification.put("eventName", finalEventName);
                    notification.put("timestamp", System.currentTimeMillis());
                    notification.put("type", "cancelled_entrant_notification");

                    // Send to all cancelled entrants
                    int totalUsers = cancelledList.size();
                    final int[] successCount = {0};
                    final int[] failCount = {0};
                    for (CancelledEntrant entrant : cancelledList) {
                        String userId = entrant.getUserId();
                        String recipientName = entrant.getName() != null ? entrant.getName() : "Unknown User";

                        db.collection("Users").document(userId)
                                .collection("Notifications").add(notification)
                                .addOnSuccessListener(docRef -> {
                                    successCount[0]++;

                                    // Log to centralized NotificationLogs for admin
                                    NotificationLogger.logNotification(
                                            userId,
                                            recipientName,
                                            eventId,
                                            finalEventName,
                                            "cancelled_entrant_notification",
                                            message,
                                            "Update from Organizer - " + finalEventName,
                                            docRef.getId()
                                    );

                                    if (successCount[0] + failCount[0] == totalUsers) {
                                        showNotificationResult(successCount[0], failCount[0]);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    failCount[0]++;
                                    if (successCount[0] + failCount[0] == totalUsers) {
                                        showNotificationResult(successCount[0], failCount[0]);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Show result of notification sending
     */
    private void showNotificationResult(int successCount, int failCount) {
        String resultMessage;
        if (failCount == 0) {
            resultMessage = "Successfully sent notification to all " + successCount + " cancelled entrant(s)";
        } else {
            resultMessage = "Sent to " + successCount + " entrant(s), failed for " + failCount + " entrant(s)";
        }
        Toast.makeText(getContext(), resultMessage, Toast.LENGTH_LONG).show();
    }
}
