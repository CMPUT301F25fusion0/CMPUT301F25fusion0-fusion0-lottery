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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FragmentWaitingList extends Fragment {

    private ListView waitingListView;
    private TextView emptyText;
    private Button backButton, refreshButton, notifyWaitListButton, drawWinnersButton, mapViewButton;

    private Spinner sortFilter;

    private ArrayList<WaitingListEntrants> waitingList;
    private ArrayAdapter<String> waitingListAdapter;
    private FirebaseFirestore db;
    private String eventId;

    public FragmentWaitingList() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waiting_list, container, false);

        notifyWaitListButton = view.findViewById(R.id.notifyWaitListButton);
        waitingListView = view.findViewById(R.id.waitingListView);
        emptyText = view.findViewById(R.id.emptyText);
        backButton = view.findViewById(R.id.backButton);
        refreshButton = view.findViewById(R.id.refreshButton);
        sortFilter = view.findViewById(R.id.sortFilter);
        drawWinnersButton = view.findViewById(R.id.drawWinnersButton);
        mapViewButton = view.findViewById(R.id.mapViewButton);

        waitingList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadWaitingList(eventId);
        }

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        refreshButton.setOnClickListener(v -> loadWaitingList(eventId));
        notifyWaitListButton.setOnClickListener(v -> showNotificationDialog());
        sortFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortAndUpdate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        drawWinnersButton.setOnClickListener(v -> drawRandomWinners());
        mapViewButton.setOnClickListener(v -> openMapView());
        return view;
    }

    /**
     *  function to display the waiting list for an event
     *  lists all entrants in the waiting list
     *  allow sorting by name and join date
     *  Handles both String (userId only) and Map (userId with joinedAt) entries
     */
    private void loadWaitingList(String eventId) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Object> waitingListData = (List<Object>) snapshot.get("waitingList");

                    if (waitingListData == null || waitingListData.isEmpty()) {
                        waitingListView.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }
                    else {
                        waitingListView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }

                    waitingList.clear();
                    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

                    for (Object item : waitingListData) {
                        if (item == null) {
                            continue;
                        }

                        String userId;
                        String joinDate;

                        // Handle both String (just userId) and Map (userId with joinedAt) types
                        if (item instanceof String) {
                            // Old format: just userId as string
                            userId = (String) item;
                            joinDate = "Unknown";
                        } else if (item instanceof Map) {
                            // New format: Map with userId and joinedAt
                            Map<String, Object> entry = (Map<String, Object>) item;
                            userId = (String) entry.get("userId");
                            Timestamp joinedAt = (Timestamp) entry.get("joinedAt");

                            if (joinedAt != null) {
                                joinDate = formatDate.format(joinedAt.toDate());
                            } else {
                                joinDate = "Unknown";
                            }
                        } else {
                            // Unknown format, skip this entry
                            continue;
                        }

                        if (userId == null) {
                            continue;
                        }

                        db.collection("Users").document(userId).get()
                                .addOnSuccessListener(userSnap -> {
                                    if (userSnap.exists()) {
                                        String name = userSnap.getString("name");
                                        String status = userSnap.getString("status") != null ? userSnap.getString("status") : "Pending";
                                        waitingList.add(new WaitingListEntrants(name, joinDate, status));
                                        if (waitingList.size() == waitingListData.size()) {
                                            sortAndUpdate();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * function to randomly select entrants in the waiting list for an event
     * randomly select up to 'numberOfWinners' amount from the waiting list
     * once entrant is selected, remove them from waiting list and add to winners list
     * Handles both String (userId only) and Map (userId with joinedAt) entries
     */
    private void drawRandomWinners() {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long numWinners = snapshot.getLong("numberOfWinners");
                    if (numWinners == null) {
                        Toast.makeText(getContext(), "Event missing number of winners field", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int numberOfWinners = numWinners.intValue();
                    List<Object> waitingListData = (List<Object>) snapshot.get("waitingList");

                    if (waitingListData == null || waitingListData.isEmpty()) {
                        Toast.makeText(getContext(), "Waiting list is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Normalize the waiting list to Map format
                    List<Map<String, Object>> normalizedWaitingList = new ArrayList<>();
                    for (Object item : waitingListData) {
                        if (item == null) {
                            continue;
                        }

                        Map<String, Object> entry = new HashMap<>();
                        if (item instanceof String) {
                            // Old format: just userId as string
                            entry.put("userId", item);
                            entry.put("joinedAt", null);
                        } else if (item instanceof Map) {
                            // New format: already a map
                            entry = (Map<String, Object>) item;
                        } else {
                            continue;
                        }
                        normalizedWaitingList.add(entry);
                    }

                    if (normalizedWaitingList.isEmpty()) {
                        Toast.makeText(getContext(), "Waiting list is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // randomly select numberOfWinners amount or until waiting list is empty
                    int numberRandomWinners = Math.min(numberOfWinners, normalizedWaitingList.size());

                    List<Map<String, Object>> tempList = new ArrayList<>(normalizedWaitingList);
                    List<Map<String, Object>> chosenWinners = new ArrayList<>();
                    Random random = new Random();
                    for (int i = 0; i < numberRandomWinners; i++) {
                        int index = random.nextInt(tempList.size());
                        Map<String, Object> winner = tempList.get(index);
                        winner.put("status", "Pending");
                        chosenWinners.add(winner);
                        tempList.remove(index);
                    }

                    String eventName = snapshot.getString("eventName");

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("waitingList", tempList);
                    updates.put("winnersList", chosenWinners);

                    db.collection("Events").document(eventId)
                            .update(updates)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(getContext(), "Selected " + chosenWinners.size() + " winners", Toast.LENGTH_LONG).show();

                                // Send notifications to selected winners
                                sendNotificationsToWinners(chosenWinners, eventName != null ? eventName : "Event");

                                loadWaitingList(eventId);
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update winners: " + e.getMessage(), Toast.LENGTH_LONG).show());

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    /**
     * function to sort the waiting list viewing based on name or join date
     * if sort filter is on "Name", then display waiting list sorted by name alphabetically
     * if sort filter is on "Date", then display waiting list sorted by join date from earliest to latest
     */
    private void sortAndUpdate() {
        if (sortFilter.getSelectedItemPosition() == 0) {
            // sort by name
            Collections.sort(waitingList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        }
        else {
            // sort by join date
            Collections.sort(waitingList, (a, b) -> a.getJoinDate().compareTo(b.getJoinDate()));
        }
        updateListView();
    }


    // converts the waiting list data into display strings (for testing)
    ArrayList<String> getDisplayStrings(ArrayList<WaitingListEntrants> waitingList) {
        ArrayList<String> displayList = new ArrayList<>();
        for (WaitingListEntrants entry : waitingList) {
            displayList.add(
                    "Name: " + entry.getName() + "\n" +
                            "Joined: " + entry.getJoinDate() + "\n" +
                            "Status: " + entry.getStatus()
            );
        }
        return displayList;
    }


    /**
     * function to update the waiting list
     * used when trying to sort entrants by name or join date
     */
    private void updateListView() {
        ArrayList<String> displayList = getDisplayStrings(waitingList);
        waitingListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayList);
        waitingListView.setAdapter(waitingListAdapter);
    }

    /**
     * Show dialog to send notification to all users in the waiting list
     * The notifications are stored in Firebase under: Users/{userId}/notifications/ and include the message, event details, and timestamp.
     */
    private void showNotificationDialog() {
        // Create a multiline EditText for the message
        final EditText messageInput = new EditText(getContext());
        messageInput.setHint("Enter your message here...");
        messageInput.setMinLines(4);
        messageInput.setMaxLines(10);
        messageInput.setVerticalScrollBarEnabled(true);
        messageInput.setPadding(50, 40, 50, 40);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Please type the update you'd like to share:");
        builder.setView(messageInput);

        // Add Send Update button
        builder.setPositiveButton("Send Update", (dialog, which) -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            } else {
                sendNotificationToWaitingList(message);
            }
        });

        // Add Cancel Update button
        builder.setNegativeButton("Cancel Update", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }

    /**
     * Send notification to all users in the waiting list
     * Handles both String (userId only) and Map (userId with joinedAt) entries
     */
    private void sendNotificationToWaitingList(String message) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Object> waitingListData = (List<Object>) snapshot.get("waitingList");
                    if (waitingListData == null || waitingListData.isEmpty()) {
                        Toast.makeText(getContext(), "No users in waiting list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Extract user IDs from the waiting list (handles both String and Map formats)
                    ArrayList<String> userIds = new ArrayList<>();
                    for (Object item : waitingListData) {
                        if (item == null) {
                            continue;
                        }

                        String userId;
                        if (item instanceof String) {
                            // Old format: just userId as string
                            userId = (String) item;
                        } else if (item instanceof Map) {
                            // New format: Map with userId and joinedAt
                            Map<String, Object> entry = (Map<String, Object>) item;
                            userId = (String) entry.get("userId");
                        } else {
                            continue;
                        }

                        if (userId != null && !userId.isEmpty()) {
                            userIds.add(userId);
                        }
                    }

                    if (userIds.isEmpty()) {
                        Toast.makeText(getContext(), "No users in waiting list", Toast.LENGTH_SHORT).show();
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
                    notification.put("title", "Update for " + finalEventName);
                    notification.put("body", message);
                    notification.put("eventId", eventId);
                    notification.put("eventName", finalEventName);
                    notification.put("timestamp", System.currentTimeMillis());
                    notification.put("type", "waiting_list_update");

                    // Send notification to each user in the waiting list
                    int totalUsers = userIds.size();
                    final int[] successCount = {0};
                    final int[] failCount = {0};

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
                                                android.util.Log.d("FragmentWaitingList", "About to log notification for user: " + userId);
                                                NotificationLogger.logNotification(
                                                        userId,
                                                        finalRecipientName,
                                                        eventId,
                                                        finalEventName,
                                                        "waiting_list_update",
                                                        message,
                                                        "Update for " + finalEventName,
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
                                })
                                .addOnFailureListener(e -> {
                                    failCount[0]++;
                                    if (successCount[0] + failCount[0] == totalUsers) {
                                        showNotificationResult(successCount[0], failCount[0]);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to send notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Show result of notification sending
     */
    private void showNotificationResult(int successCount, int failCount) {
        String resultMessage;
        if (failCount == 0) {
            resultMessage = "Successfully sent notification to " + successCount + " user(s)";
        } else {
            resultMessage = "Sent to " + successCount + " user(s), failed for " + failCount + " user(s)";
        }
        Toast.makeText(getContext(), resultMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Send notifications to selected winners
     * @param winners List of winners (each containing userId)
     * @param eventName Name of the event
     */
    private void sendNotificationsToWinners(List<Map<String, Object>> winners, String eventName) {
        if (winners == null || winners.isEmpty()) {
            return;
        }

        // Create notification data
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "You've Been Selected!");
        notification.put("body", "Congratulations! You've been selected for " + eventName + ". Please accept or decline your invitation.");
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", "winner_selection");

        // Send notification to each winner
        for (Map<String, Object> winner : winners) {
            String userId = (String) winner.get("userId");
            if (userId != null && !userId.isEmpty()) {
                db.collection("Users").document(userId)
                        .collection("Notifications").add(notification)
                        .addOnFailureListener(e -> {
                            // Silent failure for individual notifications
                        });
            }
        }
    }

    /**
     * Open the map view fragment to display entrant locations
     */
    private void openMapView() {
        // Create new MapViewFragment
        FragmentMapView mapViewFragment = new FragmentMapView();

        // Pass event ID to the fragment
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        mapViewFragment.setArguments(args);

        // Navigate to the map view fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapViewFragment)
                .addToBackStack(null)
                .commit();
    }
}