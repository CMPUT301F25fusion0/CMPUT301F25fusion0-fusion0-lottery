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
 */
public class FragmentCancelledEntrants extends Fragment {

    private ListView winnersListView;
    private TextView emptyText;
    private Button backButton, sendNotificationButton;
    private ArrayList<LotteryWinners> cancelledList;

    private ArrayAdapter<String> winnersAdapter;
    private Spinner sortFilter;

    private FirebaseFirestore db;
    private String eventId;

    public FragmentCancelledEntrants() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_selected_entrants, container, false);

        winnersListView = view.findViewById(R.id.winnerListView);
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
                    ArrayList<String> allCancelledUserIds = new ArrayList<>();

                    // Get declined users from winnersList
                    List<Map<String, Object>> winnersListEntrants = (List<Map<String, Object>>) snapshot.get("winnersList");
                    if (winnersListEntrants != null) {
                        for (Map<String, Object> entrant : winnersListEntrants) {
                            String status = (String) entrant.get("status");
                            if ("Declined".equals(status)) {
                                String userId = (String) entrant.get("userId");
                                if (userId != null && !allCancelledUserIds.contains(userId)) {
                                    allCancelledUserIds.add(userId);
                                }
                            }
                        }
                    }

                    // Get cancelled users list
                    List<String> cancelledUsers = (List<String>) snapshot.get("cancelledUsers");
                    if (cancelledUsers != null) {
                        for (String userId : cancelledUsers) {
                            if (userId != null && !allCancelledUserIds.contains(userId)) {
                                allCancelledUserIds.add(userId);
                            }
                        }
                    }

                    if (allCancelledUserIds.isEmpty()) {
                        winnersListView.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText("No cancelled entrants");
                        return;
                    }

                    loadUserDetails(allCancelledUserIds);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Load user details for all cancelled entrants
     */
    private void loadUserDetails(List<String> userIds) {
        final int[] loadedCount = {0};

        for (String userId : userIds) {
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(userSnap -> {
                        loadedCount[0]++;

                        if (userSnap.exists()) {
                            String name = userSnap.getString("name");
                            cancelledList.add(new LotteryWinners(
                                    name != null ? name : "Unknown",
                                    "Cancelled"
                            ));
                        }

                        if (loadedCount[0] == userIds.size()) {
                            winnersListView.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.GONE);
                            updateCancelledListDisplay();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == userIds.size()) {
                            if (cancelledList.isEmpty()) {
                                winnersListView.setVisibility(View.GONE);
                                emptyText.setVisibility(View.VISIBLE);
                            } else {
                                updateCancelledListDisplay();
                            }
                        }
                    });
        }
    }

    /**
     * Update ListView display
     */
    private void updateCancelledListDisplay() {
        if (sortFilter.getSelectedItemPosition() == 0) {
            // Sort by name
            Collections.sort(cancelledList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        }

        ArrayList<String> displayList = new ArrayList<>();

        for (LotteryWinners entry : cancelledList) {
            displayList.add("Name: " + entry.getName() + "\n" +
                    "Status: " + entry.getStatus()
            );
        }

        winnersAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayList);
        winnersListView.setAdapter(winnersAdapter);
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

                    // Collect all cancelled user IDs
                    ArrayList<String> allCancelledUserIds = new ArrayList<>();

                    // From winnersList (Declined)
                    List<Map<String, Object>> winnersListEntrants = (List<Map<String, Object>>) snapshot.get("winnersList");
                    if (winnersListEntrants != null) {
                        for (Map<String, Object> entrant : winnersListEntrants) {
                            String status = (String) entrant.get("status");
                            if ("Declined".equals(status)) {
                                String userId = (String) entrant.get("userId");
                                if (userId != null && !allCancelledUserIds.contains(userId)) {
                                    allCancelledUserIds.add(userId);
                                }
                            }
                        }
                    }

                    // From cancelledUsers list
                    List<String> cancelledUsers = (List<String>) snapshot.get("cancelledUsers");
                    if (cancelledUsers != null) {
                        for (String userId : cancelledUsers) {
                            if (userId != null && !allCancelledUserIds.contains(userId)) {
                                allCancelledUserIds.add(userId);
                            }
                        }
                    }

                    if (allCancelledUserIds.isEmpty()) {
                        Toast.makeText(getContext(), "No cancelled entrants to notify", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create notification data
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("title", "Update from Organizer - " + eventName);
                    notification.put("body", message);
                    notification.put("eventId", eventId);
                    notification.put("eventName", eventName);
                    notification.put("timestamp", System.currentTimeMillis());
                    notification.put("type", "cancelled_entrant_notification");

                    // Send to all cancelled entrants
                    int totalUsers = allCancelledUserIds.size();
                    final int[] successCount = {0};
                    final int[] failCount = {0};

                    for (String userId : allCancelledUserIds) {
                        db.collection("Users").document(userId)
                                .collection("Notifications").add(notification)
                                .addOnSuccessListener(docRef -> {
                                    successCount[0]++;
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