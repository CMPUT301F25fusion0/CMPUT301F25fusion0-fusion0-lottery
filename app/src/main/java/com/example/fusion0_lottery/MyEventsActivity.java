package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        db = FirebaseFirestore.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser();

        recycler_waiting = findViewById(R.id.recycler_waiting);
        recycler_selected = findViewById(R.id.recycler_selected);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

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
            }
        });
    }

    private void horizontal_scroll(RecyclerView recycler) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(layoutManager);
    }

    private void load_events() {
        waiting_events.clear();
        selected_event.clear();
        updateAdapters();

        db.collection("Events").whereArrayContains("waitingList", current_user.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Event event = snapshot.toObject(Event.class);
                        event.setEventId(snapshot.getId());
                        waiting_events.add(event);
                    }
                    updateAdapters();
                });

        db.collection("Events").whereArrayContains("selectedUser", current_user.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId());
                        selected_event.add(event);
                    }
                    updateAdapters();
                });
    }

    private void updateAdapters() {
        if(waitingAdapter != null){
            waitingAdapter.notifyDataSetChanged();
        }
        if (selectedAdapter != null){
            selectedAdapter.notifyDataSetChanged();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(MyEventsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_my_events) {
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Intent intent = new Intent(MyEventsActivity.this, ProfileActivity.class);
                // startActivity(intent);
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_my_events);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onViewEvent(String eventId) {
        openEventDetails(eventId);
    }

    @Override
    public void onLeaveWaitingList(String eventId, int position) {
        leaveWaitingList(eventId, position);
    }

    @Override
    public void onAcceptInvitation(String eventId, int position) {
        acceptInvitation(eventId, position);
    }

    @Override
    public void onDeclineInvitation(String eventId, int position) {
        declineInvitation(eventId, position);
    }

    private void declineInvitation(String eventId, int position) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    if (eventSnapshot.exists()) {
                        List<String> selectedUsers = (List<String>) eventSnapshot.get("selectedUser");
                        List<String> cancelledUsers = (List<String>) eventSnapshot.get("cancelledUser");
                        List<String> waitingList = (List<String>) eventSnapshot.get("waitingList");

                        if (waitingList == null) {
                            waitingList = new ArrayList<>();
                        }
                        if (selectedUsers == null) {
                            selectedUsers = new ArrayList<>();
                        }
                        if (cancelledUsers == null) {
                            cancelledUsers = new ArrayList<>();
                        }

                        if (selectedUsers.contains(currentUserId)) {
                            selectedUsers.remove(currentUserId);
                            cancelledUsers.add(currentUserId);

                            if (!waitingList.isEmpty()) {
                                int randomIndex = new Random().nextInt(waitingList.size());
                                String newSelectedUser = waitingList.get(randomIndex);

                                waitingList.remove(randomIndex);
                                selectedUsers.add(newSelectedUser);

                                db.collection("Events").document(eventId)
                                        .update(
                                                "selectedUser", selectedUsers,
                                                "cancelledUser", cancelledUsers,
                                                "waitingList", waitingList
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            selected_event.remove(position);
                                            selectedAdapter.notifyItemRemoved(position);
                                            Toast.makeText(this, "Invitation declined.", Toast.LENGTH_SHORT).show();
                                            sendNotificationToUser(newSelectedUser, eventId, eventSnapshot.getString("eventName"));
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                db.collection("Events").document(eventId)
                                        .update(
                                                "selectedUser", selectedUsers,
                                                "cancelledUser", cancelledUsers
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            selected_event.remove(position);
                                            selectedAdapter.notifyItemRemoved(position);
                                            Toast.makeText(this, "Invitation declined.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    }
                });
    }

    private void acceptInvitation(String eventId, int position) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    if (eventSnapshot.exists()) {
                        List<String> selectedUsers = (List<String>) eventSnapshot.get("selectedUser");
                        List<String> enrolledUsers = (List<String>) eventSnapshot.get("enrolledUser");

                        if (enrolledUsers == null) {
                            enrolledUsers = new ArrayList<>();
                        }

                        if (selectedUsers != null && selectedUsers.contains(currentUserId)) {
                            // Move from selected to enrolled
                            selectedUsers.remove(currentUserId);
                            enrolledUsers.add(currentUserId);

                            db.collection("Events").document(eventId)
                                    .update(
                                            "selectedUser", selectedUsers,
                                            "enrolledUser", enrolledUsers
                                    )
                                    .addOnSuccessListener(aVoid -> {
                                        selected_event.remove(position);
                                        selectedAdapter.notifyItemRemoved(position);
                                        Toast.makeText(this, "You're enrolled!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });
    }

    private void sendNotificationToUser(String userId, String eventId, String eventName) {

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("eventId", eventId);
        notification.put("type", "selected");
        notification.put("message", "Congratulations! You've been selected for: " + (eventName != null ? eventName : "an event"));
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("Notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    android.util.Log.d("Notification", "Notification sent to user: " + userId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("Notification", "Error sending notification: " + e.getMessage());
                });
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

                        List<String> fullWaitlist = (List<String>) eventSnapshot.get("waitingList");
                        boolean isOnWaitlist = fullWaitlist != null && fullWaitlist.contains(currentUserId);

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
                                    List<String> waitingList = (List<String>) eventSnapshot.get("waitingList");

                                    if (waitingList != null && waitingList.contains(currentUserId)) {
                                        waitingList.remove(currentUserId);

                                        db.collection("Events").document(eventId)
                                                .update("waitingList", waitingList)
                                                .addOnSuccessListener(aVoid -> {
                                                    waiting_events.remove(position);
                                                    waitingAdapter.notifyItemRemoved(position);
                                                    waitingAdapter.notifyItemRangeChanged(position, waiting_events.size());
                                                    Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Error leaving waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(this, "You are not on this waiting list", Toast.LENGTH_SHORT).show();
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
}