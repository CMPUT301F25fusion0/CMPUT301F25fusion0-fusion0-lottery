package com.example.fusion0_lottery;

import android.app.AlertDialog;
import android.content.Intent;
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
 * Fragment to display the final list of entrants who accepted invitations
 * Includes export functionality and ability to send notifications
 */
public class FragmentFinalList extends Fragment {

    private ListView winnersListView;
    private TextView emptyText;
    private Button backButton, exportButton, sendNotificationButton;
    private ArrayList<FinalListEntrant> acceptedWinnersList;

    private ArrayAdapter<String> winnersAdapter;
    private Spinner sortFilter;

    private FirebaseFirestore db;
    private String eventId;

    public FragmentFinalList() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_final_list, container, false);

        winnersListView = view.findViewById(R.id.winnerListView);
        emptyText = view.findViewById(R.id.emptyText);
        backButton = view.findViewById(R.id.backButton);
        exportButton = view.findViewById(R.id.exportButton);
        sendNotificationButton = view.findViewById(R.id.sendNotificationButton);
        sortFilter = view.findViewById(R.id.sortFilter);

        db = FirebaseFirestore.getInstance();
        acceptedWinnersList = new ArrayList<>();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadAcceptedWinners();
        }

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        exportButton.setOnClickListener(v -> exportFinalList());
        sendNotificationButton.setOnClickListener(v -> showNotificationDialog());

        sortFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAcceptedWinnersListDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        return view;
    }

    /**
     * Load all entrants who accepted their invitations
     * Retrieves full user details including email and phone
     */
    private void loadAcceptedWinners() {
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
                        emptyText.setText("No accepted entrants yet");
                        return;
                    }

                    acceptedWinnersList.clear();

                    // Get all winner user IDs who have accepted
                    List<String> acceptedUserIds = new ArrayList<>();
                    for (Map<String, Object> entrant : winnersListEntrants) {
                        String status = (String) entrant.get("status");
                        if ("Accepted".equals(status)) {
                            String userId = (String) entrant.get("userId");
                            if (userId != null) {
                                acceptedUserIds.add(userId);
                            }
                        }
                    }

                    if (acceptedUserIds.isEmpty()) {
                        winnersListView.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText("No accepted entrants yet");
                        return;
                    }

                    // Load full user details for accepted entrants
                    loadUserDetails(acceptedUserIds);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Load detailed user information for accepted entrants
     */
    private void loadUserDetails(List<String> userIds) {
        final int[] loadedCount = {0};

        for (String userId : userIds) {
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(userSnap -> {
                        loadedCount[0]++;

                        if (userSnap.exists()) {
                            String name = userSnap.getString("name");
                            String email = userSnap.getString("email");
                            String phone = userSnap.getString("phoneNumber");

                            acceptedWinnersList.add(new FinalListEntrant(
                                    userId,
                                    name != null ? name : "Unknown",
                                    email != null ? email : "No email",
                                    phone != null ? phone : "No phone",
                                    "Accepted"
                            ));
                        }

                        if (loadedCount[0] == userIds.size()) {
                            winnersListView.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.GONE);
                            updateAcceptedWinnersListDisplay();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == userIds.size()) {
                            if (acceptedWinnersList.isEmpty()) {
                                winnersListView.setVisibility(View.GONE);
                                emptyText.setVisibility(View.VISIBLE);
                            } else {
                                updateAcceptedWinnersListDisplay();
                            }
                        }
                    });
        }
    }

    /**
     * Update ListView display with accepted winners including contact info
     */
    private void updateAcceptedWinnersListDisplay() {
        if (sortFilter.getSelectedItemPosition() == 0) {
            Collections.sort(acceptedWinnersList, (a, b) -> {
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

        ArrayList<String> displayList = new ArrayList<>();

        for (FinalListEntrant entrant : acceptedWinnersList) {
            displayList.add(
                    "Name: " + entrant.getName() + "\n" +
                    "Email: " + entrant.getEmail() + "\n" +
                    "Phone: " + entrant.getPhone() + "\n" +
                    "Status: " + entrant.getStatus()
            );
        }

        winnersAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayList);
        winnersListView.setAdapter(winnersAdapter);
    }

    /**
     * Export final list as plain text via share intent
     */
    private void exportFinalList() {
        if (acceptedWinnersList.isEmpty()) {
            Toast.makeText(getContext(), "No entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build export text
        StringBuilder exportText = new StringBuilder();
        exportText.append("FINAL ENTRANTS LIST\n");
        exportText.append("===================\n\n");

        for (int i = 0; i < acceptedWinnersList.size(); i++) {
            FinalListEntrant entrant = acceptedWinnersList.get(i);
            exportText.append((i + 1)).append(". ").append(entrant.getName()).append("\n");
            exportText.append("   Email: ").append(entrant.getEmail()).append("\n");
            exportText.append("   Phone: ").append(entrant.getPhone()).append("\n");
            exportText.append("   Status: ").append(entrant.getStatus()).append("\n\n");
        }

        exportText.append("Total Accepted Entrants: ").append(acceptedWinnersList.size());

        // Create share intent
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, exportText.toString());
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Final Entrants List");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Export Final List");
        startActivity(shareIntent);

        Toast.makeText(getContext(), "Exporting final list...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Show dialog to send notification to all accepted entrants
     */
    private void showNotificationDialog() {
        if (acceptedWinnersList.isEmpty()) {
            Toast.makeText(getContext(), "No accepted entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText messageInput = new EditText(getContext());
        messageInput.setHint("Enter your message here...");
        messageInput.setMinLines(4);
        messageInput.setMaxLines(10);
        messageInput.setVerticalScrollBarEnabled(true);
        messageInput.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(getContext())
                .setTitle("Notify Final List")
                .setMessage("Send a notification to all " + acceptedWinnersList.size() + " accepted entrants:")
                .setView(messageInput)
                .setPositiveButton("Send Notification", (dialog, which) -> {
                    String message = messageInput.getText().toString().trim();
                    if (message.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                    } else {
                        sendNotificationToFinalList(message);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Send notification to all accepted entrants
     */
    private void sendNotificationToFinalList(String message) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
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
                    notification.put("type", "final_list_notification");

                    // Send to all accepted entrants
                    int totalUsers = acceptedWinnersList.size();
                    final int[] successCount = {0};
                    final int[] failCount = {0};
                    for (FinalListEntrant entrant : acceptedWinnersList) {
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
                                            "final_list_notification",
                                            message,
                                            "Message from Organizer - " + finalEventName,
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
            resultMessage = "Successfully sent notification to all " + successCount + " entrant(s)";
        } else {
            resultMessage = "Sent to " + successCount + " entrant(s), failed for " + failCount + " entrant(s)";
        }
        Toast.makeText(getContext(), resultMessage, Toast.LENGTH_LONG).show();
    }
}