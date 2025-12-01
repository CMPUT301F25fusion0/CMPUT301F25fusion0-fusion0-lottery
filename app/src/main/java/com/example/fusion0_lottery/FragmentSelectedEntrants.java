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
import java.util.Random;

public class FragmentSelectedEntrants extends Fragment {

    private ListView winnersListView;
    private TextView emptyText;
    private Button backButton, sendNotificationButton, redrawButton;
    private ArrayList<LotteryWinners> winnersList;
    private ArrayAdapter<String> winnersAdapter;
    private Spinner sortFilter;

    private FirebaseFirestore db;
    private String eventId;

    public FragmentSelectedEntrants() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_selected_entrants, container, false);

        winnersListView = view.findViewById(R.id.winnerListView);
        emptyText = view.findViewById(R.id.emptyText);
        backButton = view.findViewById(R.id.backButton);
        sendNotificationButton = view.findViewById(R.id.sendNotificationButton);
        redrawButton = view.findViewById(R.id.redrawButton);
        sortFilter = view.findViewById(R.id.sortFilter);

        db = FirebaseFirestore.getInstance();
        winnersList = new ArrayList<>();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadWinnersList();
        }

        sendNotificationButton.setOnClickListener(v -> showConfirmDialog());
        redrawButton.setOnClickListener(v -> redrawWinners());
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        sortFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateWinnersListDisplay(winnersList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        return view;
    }

    /**
     *  function to get all entrants the winners list for an event
     *  find all entrants in the winners list (selected) and display them
     *  same functionality as loadWaitingList function in FragmentWaitingList but for winners
     */
    private void loadWinnersList() {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<Map<String, Object>> winnersListEntrants = (List<Map<String, Object>>) snapshot.get("winnersList");

                    if (winnersListEntrants == null || winnersListEntrants.isEmpty()) {
                        winnersListView.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }
                    else {
                        winnersListView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }

                    winnersList.clear();

                    List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();

                    for (Map<String, Object> entrant : winnersListEntrants) {
                        String userId = (String) entrant.get("userId");
                        tasks.add(db.collection("Users").document(userId).get());
                    }

                    com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(results -> {
                                for (Object obj : results) {
                                    DocumentSnapshot userSnap = (DocumentSnapshot) obj;
                                    String name = userSnap.getString("name");
                                    String status = userSnap.getString("status");
                                    if (status == null) {
                                        status = "Pending";
                                    }
                                    winnersList.add(new LotteryWinners(name, status));
                                }
                                updateWinnersListDisplay(winnersList);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    /**
     * function to redraw and select entrants from waiting list if there are entrants who declined
     * go through all selected entrants and see if there are any status = Declined
     * for every decline, remove them from winners list redraw in the waiting list and update winners list
     */
    private void redrawWinners() {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Map<String, Object>> waitingList = (List<Map<String, Object>>) snapshot.get("waitingList");
                    List<Map<String, Object>> entrantWinners = (List<Map<String, Object>>) snapshot.get("winnersList");

                    if (waitingList == null || waitingList.isEmpty()) {
                        Toast.makeText(getContext(), "Waiting List is empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (entrantWinners == null || entrantWinners.isEmpty()) {
                        Toast.makeText(getContext(), "No Entrants in winners list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (Map<String, Object> entry : entrantWinners) {
                        String userId = (String) entry.get("userId");
                        tasks.add(db.collection("Users").document(userId).get());
                    }

                    com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(results -> {
                                List<Map<String, Object>> nonDeclinedWinners = new ArrayList<>();
                                List<Map<String, Object>> declinedWinners = new ArrayList<>();
                                int currentEntrantIndex = 0;
                                for (Object obj : results) {
                                    DocumentSnapshot userSnap = (DocumentSnapshot) obj;
                                    Map<String, Object> currentEntrant = entrantWinners.get(currentEntrantIndex);
                                    if (userSnap.exists()) {
                                        String status = userSnap.getString("status");
                                        // if status = Declined, add to declined winners,
                                        if (status == null) {
                                            status = "Pending";
                                        }
                                        if (status.equals("Declined")) {
                                            declinedWinners.add(currentEntrant);
                                        }
                                        else {
                                            nonDeclinedWinners.add(currentEntrant);
                                        }
                                        currentEntrantIndex++;
                                    }
                                }

                                // if there is no declined winners, no redraws take place
                                if (declinedWinners.isEmpty()) {
                                    Toast.makeText(getContext(), "No redraws can be made", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // redraw until waiting list is empty or until all declined winners are replaced
                                int numberOfRedraws = 0;
                                if (declinedWinners.size() > waitingList.size()) {
                                    numberOfRedraws = waitingList.size();
                                }
                                else {
                                    numberOfRedraws = declinedWinners.size();
                                }
                                List<Map<String, Object>> tempList = new ArrayList<>(waitingList);
                                List<Map<String, Object>> redrawWinners = new ArrayList<>();
                                Random random = new Random();
                                // redraw from waiting list, add to winners, and remove that winner from waiting list
                                for (int i = 0; i < numberOfRedraws; i++) {
                                    int index = random.nextInt(tempList.size());
                                    redrawWinners.add(tempList.get(index));
                                    tempList.remove(index);
                                }

                                // update winners list to include only Accepted/Pending winner entrants
                                List<Map<String, Object>> updatedWinnersList = new ArrayList<>();
                                updatedWinnersList.addAll(nonDeclinedWinners);
                                updatedWinnersList.addAll(redrawWinners);

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("waitingList", tempList);
                                updates.put("winnersList", updatedWinnersList);

                                db.collection("Events").document(eventId)
                                        .update(updates)
                                        .addOnSuccessListener(v -> {
                                            Toast.makeText(getContext(), "Selected " + updatedWinnersList.size() + " winners", Toast.LENGTH_LONG).show();
                                            loadWinnersList();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update winners: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load user statuses: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * displays winners in the ListView
     * includes sort by name or status
     */
    private void updateWinnersListDisplay(ArrayList<LotteryWinners> winnersList) {

        if (sortFilter.getSelectedItemPosition() == 0) {
            Collections.sort(winnersList, (a, b) -> {
                String nameA = a.getName();
                String nameB = b.getName();

                if (nameA == null && nameB == null) {
                    return 0;
                }
                if (nameA == null) {
                    return 1;
                }
                if (nameB == null) {
                    return -1;
                }
                return nameA.compareToIgnoreCase(nameB);
            });
        }
        else {
            Collections.sort(winnersList, (a, b) -> {
                String statusA = a.getStatus();
                String statusB = b.getStatus();

                if (statusA == null && statusB == null) {
                    return 0;
                }
                if (statusA == null) {
                    return 1;
                }
                if (statusB == null) {
                    return -1;
                }
                return statusA.compareToIgnoreCase(statusB);
            });
        }

        ArrayList<String> displayList = new ArrayList<>();
        for (LotteryWinners w : winnersList) {
            displayList.add("Name: " + w.getName() + "\n" +
                    "Status: " + w.getStatus()
            );
        }
        winnersAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayList);
        winnersListView.setAdapter(winnersAdapter);
    }

    /**
     * Show dialog to send notification to all selected entrants (winners)
     * Collects notification message from organizer and sends to all winners
     * Similar to sendNotificationToWaitingList in FragmentWaitingList
     */
    private void showConfirmDialog() {
        // Create a multiline EditText for the message
        final EditText messageInput = new EditText(getContext());
        messageInput.setHint("Enter your notification message here...");
        messageInput.setMinLines(4);
        messageInput.setMaxLines(10);
        messageInput.setVerticalScrollBarEnabled(true);
        messageInput.setPadding(50, 40, 50, 40);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notify Selected Entrants");
        builder.setMessage("Send a notification to all selected entrants:");
        builder.setView(messageInput);

        // Add Send button
        builder.setPositiveButton("Send Notification", (dialog, which) -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            } else {
                sendNotificationToWinners(message);
            }
        });

        // Add Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }

    /**
     * Send notification to all users in the winners list
     * Sends winner notifications to selected entrants for event registration
     * Stores notifications in Users/{userId}/notifications/ collection
     */
    private void sendNotificationToWinners(String message) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Map<String, Object>> winnersListData = (List<Map<String, Object>>) snapshot.get("winnersList");
                    if (winnersListData == null || winnersListData.isEmpty()) {
                        Toast.makeText(getContext(), "No selected entrants to notify", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Extract user IDs from the winners list
                    ArrayList<String> userIds = new ArrayList<>();
                    for (Map<String, Object> entry : winnersListData) {
                        String userId = (String) entry.get("userId");
                        if (userId != null && !userId.isEmpty()) {
                            userIds.add(userId);
                        }
                    }

                    if (userIds.isEmpty()) {
                        Toast.makeText(getContext(), "No selected entrants to notify", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get event name for the notification
                    String eventName = snapshot.getString("eventName");
                    if (eventName == null) {
                        eventName = "Event";
                    }
                    final String finalEventName = eventName;

                    // Create notification data
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("title", "Message from Organizer - " + finalEventName);
                    notification.put("body", message);
                    notification.put("eventId", eventId);
                    notification.put("eventName", finalEventName);
                    notification.put("timestamp", System.currentTimeMillis());
                    notification.put("type", "winner_notification");

                    // Send notification to each user in the winners list
                    int totalUsers = userIds.size();
                    final int[] successCount = {0};
                    final int[] failCount = {0};
                    final ArrayList<String> failedUserIds = new ArrayList<>();

                    for (String userId : userIds) {
                        // Fetch user data first to get recipient name
                        db.collection("Users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String recipientName = userDoc.getString("name");
                                    if (recipientName == null) {
                                        recipientName = "Unknown User";
                                    }

                                    String finalRecipientName = recipientName;

                                    // Send notification to user
                                    db.collection("Users").document(userId)
                                            .collection("Notifications").add(notification)
                                            .addOnSuccessListener(docRef -> {
                                                successCount[0]++;

                                                // Log to centralized NotificationLogs for admin
                                                NotificationLogger.logNotification(
                                                        userId,
                                                        finalRecipientName,
                                                        eventId,
                                                        finalEventName,
                                                        "winner_notification",
                                                        message,
                                                        "Message from Organizer - " + finalEventName,
                                                        docRef.getId()

                                                );

                                                if (successCount[0] + failCount[0] == totalUsers) {
                                                    showNotificationResult(successCount[0], failCount[0], failedUserIds);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                failCount[0]++;
                                                failedUserIds.add(userId);
                                                if (successCount[0] + failCount[0] == totalUsers) {
                                                    showNotificationResult(successCount[0], failCount[0], failedUserIds);
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    failCount[0]++;
                                    failedUserIds.add(userId);
                                    if (successCount[0] + failCount[0] == totalUsers) {
                                        showNotificationResult(successCount[0], failCount[0], failedUserIds);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showErrorDialog("Failed to load event: " + e.getMessage(), message);
                });
    }

    /**
     * Show result of notification sending with success/failure counts
     * If there are failures, show retry option
     */
    private void showNotificationResult(int successCount, int failCount, ArrayList<String> failedUserIds) {
        if (failCount == 0) {
            // All notifications sent successfully
            new AlertDialog.Builder(getContext())
                    .setTitle("Success")
                    .setMessage("Successfully sent notification to all " + successCount + " selected entrant(s)")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        } else {
            // Some notifications failed - show retry option
            String resultMessage = "Sent to " + successCount + " entrant(s)\n" +
                    "Failed for " + failCount + " entrant(s)";

            new AlertDialog.Builder(getContext())
                    .setTitle("Partial Success")
                    .setMessage(resultMessage + "\n\nWould you like to retry sending to failed recipients?")
                    .setPositiveButton("Retry", (dialog, which) -> {
                        retryFailedNotifications(failedUserIds);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Toast.makeText(getContext(), resultMessage, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }
    }

    /**
     * Retry sending notifications to users who failed to receive them
     */
    private void retryFailedNotifications(ArrayList<String> failedUserIds) {
        if (failedUserIds.isEmpty()) {
            Toast.makeText(getContext(), "No failed notifications to retry", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prompt for message again
        final EditText messageInput = new EditText(getContext());
        messageInput.setHint("Enter your notification message here...");
        messageInput.setMinLines(4);
        messageInput.setMaxLines(10);
        messageInput.setVerticalScrollBarEnabled(true);
        messageInput.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(getContext())
                .setTitle("Retry Notification")
                .setMessage("Retrying for " + failedUserIds.size() + " recipient(s).\nEnter message:")
                .setView(messageInput)
                .setPositiveButton("Send", (dialog, which) -> {
                    String message = messageInput.getText().toString().trim();
                    if (message.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                    } else {
                        sendNotificationToSpecificUsers(message, failedUserIds);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Send notification to specific list of users (used for retry)
     */
    private void sendNotificationToSpecificUsers(String message, ArrayList<String> userIds) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    String eventName = snapshot.getString("eventName");
                    if (eventName == null) {
                        eventName = "Event";
                    }

                    // Create notification data
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("title", "Message from Organizer - " + eventName);
                    notification.put("body", message);
                    notification.put("eventId", eventId);
                    notification.put("eventName", eventName);
                    notification.put("timestamp", System.currentTimeMillis());
                    notification.put("type", "winner_notification_retry");

                    int totalUsers = userIds.size();
                    final int[] successCount = {0};
                    final int[] failCount = {0};

                    for (String userId : userIds) {
                        db.collection("Users").document(userId)
                                .collection("Notifications").add(notification)
                                .addOnSuccessListener(docRef -> {
                                    successCount[0]++;
                                    if (successCount[0] + failCount[0] == totalUsers) {
                                        showFinalRetryResult(successCount[0], failCount[0]);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    failCount[0]++;
                                    if (successCount[0] + failCount[0] == totalUsers) {
                                        showFinalRetryResult(successCount[0], failCount[0]);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showErrorDialog("Failed to load event: " + e.getMessage(), message);
                });
    }

    /**
     * Show final result after retry attempt
     */
    private void showFinalRetryResult(int successCount, int failCount) {
        String resultMessage;
        if (failCount == 0) {
            resultMessage = "Successfully sent notification to all " + successCount + " recipient(s) on retry";
        } else {
            resultMessage = "Retry results:\n" +
                    "Sent to " + successCount + " recipient(s)\n" +
                    "Failed for " + failCount + " recipient(s)";
        }

        new AlertDialog.Builder(getContext())
                .setTitle(failCount == 0 ? "Success" : "Retry Complete")
                .setMessage(resultMessage)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Show error dialog with retry option
     */
    private void showErrorDialog(String errorMessage, String originalMessage) {
        new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(errorMessage + "\n\nWould you like to try again?")
                .setPositiveButton("Retry", (dialog, which) -> {
                    sendNotificationToWinners(originalMessage);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(getContext(), "Notification sending cancelled", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .create()
                .show();
    }
}
