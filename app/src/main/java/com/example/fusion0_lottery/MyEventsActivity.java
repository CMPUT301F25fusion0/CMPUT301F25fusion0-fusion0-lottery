package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This activity is used for managing user events including waiting list and selected events.
 * It allows users to accept or decline events from selected list
 * and view event details and leave waiting list from waiting list
 * user will be replaced by random selection from waiting lists
 */

public class MyEventsActivity extends AppCompatActivity implements MyEventAdapter.OnEventActionListener {

    private RecyclerView recycler_waiting;
    private RecyclerView recycler_selected;

    private FirebaseFirestore db;
    private FirebaseUser current_user;
    private List<Event> waiting_events;
    private List<Event> selected_event;
    private MyEventAdapter waitingAdapter;
    private MyEventAdapter selectedAdapter;

    private BottomNavigationView bottomNavigationView;
    private ListenerRegistration waitingListener;
    private ListenerRegistration selectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        db = FirebaseFirestore.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser();

        recycler_waiting = findViewById(R.id.recycler_waiting);
        recycler_selected = findViewById(R.id.recycler_selected);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Bottom_navigation();

        waiting_events = new ArrayList<>();
        selected_event = new ArrayList<>();

        horizontal_scroll(recycler_waiting);
        horizontal_scroll(recycler_selected);

        waitingAdapter = new MyEventAdapter(waiting_events,"waiting", this);
        selectedAdapter = new MyEventAdapter(selected_event,"selected", this);

        recycler_waiting.setAdapter(waitingAdapter);
        recycler_selected.setAdapter(selectedAdapter);
        load_events();

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {

                findViewById(R.id.main).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_container).setVisibility(View.GONE);
                bottomNavigationView.setSelectedItemId(R.id.navigation_my_events);
            }
        });
    }

    private void horizontal_scroll(RecyclerView recycler) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(layoutManager);
    }

    /**
     * Loads events from firebase where the current user is either on waiting list or selected
     */
    private void load_events() {
        String currentUserId = current_user.getUid();
        waiting_events.clear();
        selected_event.clear();
        updateAdapters();

        if (waitingListener != null) {
            waitingListener.remove();
        }
        if (selectedListener != null) {
            selectedListener.remove();
        }

        waitingListener = db.collection("Events")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading waiting events", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    waiting_events.clear();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            List<Object> waitingListData = (List<Object>) snapshot.get("waitingList");

                            if (waitingListData != null) {
                                boolean isInWaitingList = false;
                                for (Object item : waitingListData) {
                                    if (item instanceof String) {
                                        if (currentUserId.equals(item)) {
                                            isInWaitingList = true;
                                            break;
                                        }
                                    } else if (item instanceof Map) {
                                        Map<String, Object> entry = (Map<String, Object>) item;
                                        String userId = (String) entry.get("userId");
                                        if (currentUserId.equals(userId)) {
                                            isInWaitingList = true;
                                            break;
                                        }
                                    }
                                }

                                if (isInWaitingList) {
                                    Event event = snapshot.toObject(Event.class);
                                    event.setEventId(snapshot.getId());
                                    waiting_events.add(event);
                                }
                            }
                        }
                    }
                    updateAdapters();
                });

        selectedListener = db.collection("Events")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading selected events", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selected_event.clear();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            List<Map<String, Object>> winnersList = (List<Map<String, Object>>) snapshot.get("winnersList");

                            if (winnersList != null) {
                                for (Map<String, Object> winner : winnersList) {
                                    String winnerUserId = (String) winner.get("userId");
                                    String status = (String) winner.get("status");

                                    // Only show events where user is selected and status is "Pending"
                                    if (currentUserId.equals(winnerUserId) && "Pending".equals(status)) {
                                        Event event = snapshot.toObject(Event.class);
                                        event.setEventId(snapshot.getId());
                                        selected_event.add(event);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    updateAdapters();
                });
    }

    /**
     * notifies both waiting and selected adapters that their data set have changed
     */
    private void updateAdapters() {
        if(waitingAdapter != null){
            waitingAdapter.notifyDataSetChanged();
        }
        if (selectedAdapter != null){
            selectedAdapter.notifyDataSetChanged();
        }
    }

    private void Bottom_navigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(MyEventsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_my_events) {
                return true;
            } else if (itemId == R.id.navigation_profile) {
                findViewById(R.id.main).setVisibility(View.GONE);
                findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new UpdateProfileFragment())
                        .addToBackStack("Profile")
                        .commit();
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_my_events);
    }

    /**
     * handles back button press
     */
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Opens detailed view for a specific event
     * @param eventId unique id of the event to view
     */
    @Override
    public void onViewEvent(String eventId) {
        openEventDetails(eventId);
    }

    /**
     * handles user request to leave waiting list
     * @param eventId unique id of the event
     * @param position the position of the event in the waiting list adapter
     */
    @Override
    public void onLeaveWaitingList(String eventId, int position) {
        leaveWaitingList(eventId, position);
    }

    /**
     * handles user acceptance of an event invitation
     * @param eventId unique id of the event
     * @param position the position of the event in the selected events adapter
     */
    @Override
    public void onAcceptInvitation(String eventId, int position) {
        acceptInvitation(eventId, position);
    }

    /**
     * handles user decline of an event invitation
     * @param eventId unique id of the event
     * @param position the position of the event in the selected events adapter
     */
    @Override
    public void onDeclineInvitation(String eventId, int position) {
        declineInvitation(eventId, position);
    }

    private void declineInvitation(String eventId, int position) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String eventName = selected_event.get(position).getEventName();

        new AlertDialog.Builder(this)
                .setTitle("Decline Invitation")
                .setMessage("Are you sure you want to decline the invitation for \"" + eventName + "\"?")
                .setPositiveButton("Yes, Decline", (dialog, which) -> {
                    db.collection("Events").document(eventId)
                            .get()
                            .addOnSuccessListener(eventSnapshot -> {
                                if (eventSnapshot.exists()) {
                                    List<Map<String, Object>> winnersList = (List<Map<String, Object>>) eventSnapshot.get("winnersList");
                                    List<String> waitingList = (List<String>) eventSnapshot.get("waitingList");
                                    List<String> cancelledUsers = (List<String>) eventSnapshot.get("cancelledUsers");

                                    if (cancelledUsers == null) {
                                        cancelledUsers = new ArrayList<>();
                                    }

                                    if (winnersList != null) {
                                        for (int i = 0; i < winnersList.size(); i++) {
                                            Map<String, Object> winner = winnersList.get(i);
                                            String winnerUserId = (String) winner.get("userId");
                                            if (currentUserId.equals(winnerUserId)) {
                                                winnersList.remove(i);
                                                break;
                                            }
                                        }

                                        cancelledUsers.add(currentUserId);


                                        if (waitingList != null && !waitingList.isEmpty()) {
                                            int randomIndex = new Random().nextInt(waitingList.size());
                                            String newWinnerId = waitingList.get(randomIndex);
                                            waitingList.remove(randomIndex);

                                            Map<String, Object> newWinner = new HashMap<>();
                                            newWinner.put("userId", newWinnerId);
                                            newWinner.put("status", "Pending");
                                            winnersList.add(newWinner);

                                            String eventNameForNotif = eventName;

                                            db.collection("Events").document(eventId)
                                                    .update(
                                                            "winnersList", winnersList,
                                                            "waitingList", waitingList,
                                                            "cancelledUsers", cancelledUsers
                                                    )
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Send notification to new winner
                                                        sendWinnerNotification(newWinnerId, eventId, eventNameForNotif);

                                                        // Don't remove locally - the snapshot listener will update automatically
                                                        Toast.makeText(this, "Invitation declined. A new entrant has been selected.", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            db.collection("Events").document(eventId)
                                                    .update(
                                                            "winnersList", winnersList,
                                                            "cancelledUsers", cancelledUsers
                                                    )
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Don't remove locally - the snapshot listener will update automatically
                                                        Toast.makeText(this, "Invitation declined.", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    }
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    private void acceptInvitation(String eventId, int position) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String eventName = selected_event.get(position).getEventName();

        new AlertDialog.Builder(this)
                .setTitle("Accept Invitation")
                .setMessage("Are you sure you want to accept the invitation for \"" + eventName + "\"?")
                .setPositiveButton("Yes, Accept", (dialog, which) -> {
                    db.collection("Events").document(eventId)
                            .get()
                            .addOnSuccessListener(eventSnapshot -> {
                                if (eventSnapshot.exists()) {
                                    List<Map<String, Object>> winnersList = (List<Map<String, Object>>) eventSnapshot.get("winnersList");
                                    List<String> enrolledUsers = (List<String>) eventSnapshot.get("enrolledUsers");

                                    if (enrolledUsers == null) {
                                        enrolledUsers = new ArrayList<>();
                                    }

                                    if (winnersList != null) {
                                        for (Map<String, Object> winner : winnersList) {
                                            String winnerUserId = (String) winner.get("userId");
                                            if (currentUserId.equals(winnerUserId)) {
                                                winner.put("status", "Accepted");
                                                break;
                                            }
                                        }

                                        enrolledUsers.add(currentUserId);

                                        db.collection("Events").document(eventId)
                                                .update(
                                                        "winnersList", winnersList,
                                                        "enrolledUsers", enrolledUsers
                                                )
                                                .addOnSuccessListener(aVoid -> {
                                                    // Don't remove locally - the snapshot listener will update automatically
                                                    Toast.makeText(this, "You're enrolled!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void openEventDetails(String eventId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    if (eventSnapshot.exists()) {
                        String eventNameStr = eventSnapshot.getString("eventName");
                        String eventDescStr = eventSnapshot.getString("description");
                        String eventInterests = eventSnapshot.getString("interests");
                        String eventStartDateStr = eventSnapshot.getString("startDate");
                        String eventLocationStr = eventSnapshot.getString("location");

                        String regStart = eventSnapshot.getString("registrationStart");
                        String regEnd = eventSnapshot.getString("registrationEnd");

                        Long maxEntrantsVal = eventSnapshot.getLong("maxEntrants");
                        Double priceVal = eventSnapshot.getDouble("price");

                        List<Object> waitingListData = (List<Object>) eventSnapshot.get("waitingList");
                        boolean isOnWaitlist = false;

                        if (waitingListData != null) {
                            for (Object item : waitingListData) {
                                if (item instanceof String) {
                                    if (currentUserId.equals(item)) {
                                        isOnWaitlist = true;
                                        break;
                                    }
                                } else if (item instanceof Map) {
                                    Map<String, Object> entry = (Map<String, Object>) item;
                                    String userId = (String) entry.get("userId");
                                    if (currentUserId.equals(userId)) {
                                        isOnWaitlist = true;
                                        break;
                                    }
                                }
                            }
                        }

                        boolean waitingListClosed = eventSnapshot.getBoolean("waitingListClosed") != null
                                ? eventSnapshot.getBoolean("waitingListClosed") : false;

                        findViewById(R.id.main).setVisibility(View.GONE);
                        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, EventFragmentEntrant.newInstance(
                                        eventId,
                                        currentUserId,
                                        eventNameStr != null ? eventNameStr : "No Name",
                                        eventDescStr != null ? eventDescStr : "No Description",
                                        eventInterests != null ? eventInterests : "None",
                                        eventStartDateStr != null ? eventStartDateStr : "No Date",
                                        eventLocationStr != null ? eventLocationStr : "No Location",
                                        isOnWaitlist,
                                        regStart != null ? regStart : "",
                                        regEnd != null ? regEnd : "",
                                        maxEntrantsVal != null ? maxEntrantsVal : 0L,
                                        priceVal != null ? priceVal : 0.0,
                                        waitingListClosed
                                ))
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void leaveWaitingList(String eventId, int position) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        new AlertDialog.Builder(this)
                .setTitle("Leave Waiting List")
                .setMessage("Are you sure you want to leave?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    db.collection("Events").document(eventId)
                            .get()
                            .addOnSuccessListener(eventSnapshot -> {
                                if (eventSnapshot.exists()) {
                                    List<Object> waitingListData = (List<Object>) eventSnapshot.get("waitingList");

                                    if (waitingListData != null) {
                                        List<Object> updatedWaitingList = new ArrayList<>();
                                        boolean found = false;

                                        for (Object item : waitingListData) {
                                            if (item instanceof String) {
                                                if (!currentUserId.equals(item)) {
                                                    updatedWaitingList.add(item);
                                                } else {
                                                    found = true;
                                                }
                                            } else if (item instanceof Map) {
                                                Map<String, Object> entry = (Map<String, Object>) item;
                                                String userId = (String) entry.get("userId");
                                                if (!currentUserId.equals(userId)) {
                                                    updatedWaitingList.add(item);
                                                } else {
                                                    found = true;
                                                }
                                            } else {
                                                updatedWaitingList.add(item);
                                            }
                                        }

                                        if (found) {
                                            db.collection("Events").document(eventId)
                                                    .update("waitingList", updatedWaitingList)
                                                    .addOnSuccessListener(aVoid -> {

                                                        int actualPosition = -1;
                                                        for (int i = 0; i < waiting_events.size(); i++) {
                                                            if (eventId.equals(waiting_events.get(i).getEventId())) {
                                                                actualPosition = i;
                                                                break;
                                                            }
                                                        }
                                                        if (actualPosition != -1) {
                                                            waiting_events.remove(actualPosition);
                                                            waitingAdapter.notifyItemRemoved(actualPosition);
                                                            Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            load_events();
                                                            Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Error leaving waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Toast.makeText(this, "You are not on this waiting list", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    protected void onDestroy() {
        super.onDestroy();
        if (waitingListener != null) {
            waitingListener.remove();
        }
        if (selectedListener != null) {
            selectedListener.remove();
        }
    }

    /**
     * Send notification to a newly selected winner
     * @param userId The user ID of the winner
     * @param eventId The event ID
     * @param eventName The event name
     */
    private void sendWinnerNotification(String userId, String eventId, String eventName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "You've Been Selected!");
        notification.put("body", "Congratulations! You've been selected for " + eventName + ". Please accept or decline your invitation.");
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", "winner_selection");

        db.collection("Users").document(userId)
                .collection("Notifications").add(notification)
                .addOnFailureListener(e -> {
                    Log.e("MyEventsActivity", "Failed to send notification to winner: " + e.getMessage());
                });
    }
}