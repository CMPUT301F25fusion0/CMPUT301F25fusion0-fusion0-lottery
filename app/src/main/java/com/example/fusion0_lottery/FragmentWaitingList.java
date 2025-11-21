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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FragmentWaitingList extends Fragment {

    private ListView waitingListView;
    private TextView emptyText;
    private Button backButton, refreshButton, notifyWaitListButton;
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

        waitingList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadWaitingList(eventId);
        }

        // go back to the previous event manage screen
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // refresh button to reload the waiting list information after applying sorting changes
        refreshButton.setOnClickListener(v -> loadWaitingList(eventId));

        // notify waiting list button to send a message to all users in the waiting list
        notifyWaitListButton.setOnClickListener(v -> showNotificationDialog());

        sortFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // sort by name
                    Collections.sort(waitingList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                }
                else {
                    // sort by join date using a lambda
                    Collections.sort(waitingList, (a, b) -> a.getJoinDate().compareTo(b.getJoinDate()));
                }
                updateListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        return view;
    }

    /**
     *  function to display the waiting list for an event
     *  lists all entrants in the waiting list
     *  allow sorting by name and join date
    */
    private void loadWaitingList(String eventId) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<String> userIds = (ArrayList<String>) snapshot.get("waitingList");
                    /// check if waiting list exists, if not then create one
                    if (userIds == null) {
                        userIds = new ArrayList<>();
                    }

                    // check if there's anyone in the waiting list
                    if (userIds.isEmpty()) {
                        waitingListView.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }

                    // find the number of entrants in the waiting list by ID
                    int totalEntrantsWaiting = userIds.size();

                    // clear the waiting list, then add entrants to the waiting list by ID
                    waitingList.clear();
                    // for every ID that's in the waiting list, get their name, joinDate, and status
                    for (String uid : userIds) {
                        db.collection("Users").document(uid).get()
                                .addOnSuccessListener(userSnap -> {
                                    if (userSnap.exists()) {
                                        String name = userSnap.getString("name");
                                        String joinDate = userSnap.getString("joinDate") != null ? userSnap.getString("joinDate") : "Unknown";
                                        String status = userSnap.getString("status") != null ? userSnap.getString("status") : "Pending";

                                        // add the entrant to the waiting list
                                        waitingList.add(new WaitingListEntrants(name, joinDate, status));

                                        // check if everyone is added yet, if yes then sort, if not keep adding
                                        if (waitingList.size() == totalEntrantsWaiting) {
                                            if (sortFilter.getSelectedItemPosition() == 0) {
                                                Collections.sort(waitingList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                                            }
                                            else {
                                                Collections.sort(waitingList, (a, b) -> a.getJoinDate().compareTo(b.getJoinDate()));
                                            }
                                            updateListView();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(),
                                        "Failed to load user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
     */
    private void sendNotificationToWaitingList(String message) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<String> userIds = (ArrayList<String>) snapshot.get("waitingList");
                    if (userIds == null || userIds.isEmpty()) {
                        Toast.makeText(getContext(), "No users in waiting list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get event name for the notification
                    String eventName = snapshot.getString("eventName");
                    if (eventName == null) {
                        eventName = "Event";
                    }

                    // Create notification data
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("message", message);
                    notification.put("eventId", eventId);
                    notification.put("eventName", eventName);
                    notification.put("timestamp", System.currentTimeMillis());
                    notification.put("type", "waiting_list_update");

                    // Send notification to each user in the waiting list
                    int totalUsers = userIds.size();
                    final int[] successCount = {0};
                    final int[] failCount = {0};

                    for (String userId : userIds) {
                        db.collection("Users").document(userId)
                                .collection("notifications").add(notification)
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
}
