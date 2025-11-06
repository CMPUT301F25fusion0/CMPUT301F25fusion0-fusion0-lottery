package com.example.fusion0_lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EventFragmentEntrant extends Fragment {

    private TextView eventNameText, eventDescriptionText, eventDateText, eventLocationText;
    private TextView registrationText, maxEntrantsText, eventPriceText;
    private Button joinWaitingListButton;
    private String eventId;
    private boolean isInWaitingList;
    private boolean waitingListClosed;

    private FirebaseFirestore db;
    private String currentUserId; // <-- Using UID now

    public EventFragmentEntrant() {}

    public static EventFragmentEntrant newInstance(
            String eventId,
            String currentUserId,   // <-- pass UID here
            String eventName,
            String eventDescription,
            String startDate,
            String location,
            boolean isInWaitingList,
            String registrationStart,
            String registrationEnd,
            Long maxEntrants,
            Double price,
            boolean waitingListClosed
    ) {
        EventFragmentEntrant fragment = new EventFragmentEntrant();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("currentUserId", currentUserId); // <-- store UID
        args.putString("eventName", eventName);
        args.putString("eventDescription", eventDescription);
        args.putString("startDate", startDate);
        args.putString("eventLocation", location);
        args.putBoolean("isInWaitingList", isInWaitingList);
        args.putString("registrationStart", registrationStart);
        args.putString("registrationEnd", registrationEnd);
        args.putLong("maxEntrants", maxEntrants != null ? maxEntrants : 0);
        args.putDouble("price", price != null ? price : 0);
        args.putBoolean("waitingListClosed", waitingListClosed);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_activity_entrant, container, false);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        eventNameText = view.findViewById(R.id.eventName);
        eventDescriptionText = view.findViewById(R.id.eventDescription);
        eventDateText = view.findViewById(R.id.eventDate);
        eventLocationText = view.findViewById(R.id.eventLocation);
        registrationText = view.findViewById(R.id.eventEndDate);
        maxEntrantsText = view.findViewById(R.id.eventEntrants);
        eventPriceText = view.findViewById(R.id.eventPrice);
        joinWaitingListButton = view.findViewById(R.id.buttonJoinWaitingList);
        joinWaitingListButton.setVisibility(View.INVISIBLE);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            currentUserId = getArguments().getString("currentUserId");
            waitingListClosed = getArguments().getBoolean("waitingListClosed", false);

            // Display all fields
            eventNameText.setText("Event Name: " + getArguments().getString("eventName"));
            eventDescriptionText.setText("Description: " + getArguments().getString("eventDescription"));
            eventDateText.setText("Start Date: " + getArguments().getString("startDate"));
            eventLocationText.setText("Location: " + getArguments().getString("eventLocation"));

            String regStart = getArguments().getString("registrationStart");
            String regEnd = getArguments().getString("registrationEnd");
            registrationText.setText("Registration: " + regStart + " to " + regEnd);

            maxEntrantsText.setText("Max Entrants: " + getArguments().getLong("maxEntrants"));
            eventPriceText.setText("Price: $" + getArguments().getDouble("price"));

            // Fetch latest waiting list and remove deleted users before showing button
            db.collection("Events").document(eventId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) return;

                        ArrayList<String> waitingList =
                                (ArrayList<String>) snapshot.get("waitingList");
                        if (waitingList == null) waitingList = new ArrayList<>();

                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String uid : new ArrayList<>(waitingList)) {
                            tasks.add(db.collection("Users").document(uid).get());
                        }

                        ArrayList<String> finalWaitingList = waitingList;
                        com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(results -> {
                                    ArrayList<String> cleanList = new ArrayList<>();
                                    for (int i = 0; i < results.size(); i++) {
                                        DocumentSnapshot userSnap =
                                                (DocumentSnapshot) results.get(i);
                                        if (userSnap.exists()) {
                                            cleanList.add(finalWaitingList.get(i));
                                        }
                                    }

                                    // Update isInWaitingList for this user
                                    isInWaitingList = cleanList.contains(currentUserId);
                                    joinWaitingListButton.setText(
                                            isInWaitingList
                                                    ? "Leave Waiting List"
                                                    : "Join Waiting List"
                                    );

                                    // Push cleaned list back to Firestore
                                    snapshot.getReference().update("waitingList", cleanList);

                                    joinWaitingListButton.setVisibility(View.VISIBLE);
                                });
                    });
        }

        // Toolbar back arrow
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Event Details");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationIcon(android.R.drawable.ic_media_previous);
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Join/Leave waiting list button
        joinWaitingListButton.setOnClickListener(v -> toggleWaitingList());

        return view;
    }

    private void toggleWaitingList() {
        if (waitingListClosed) {
            Toast.makeText(
                    getContext(),
                    "The waiting list is closed. You cannot join this event.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> waitingList =
                    (ArrayList<String>) snapshot.get("waitingList");
            if (waitingList == null) waitingList = new ArrayList<>();

            // Copy for safe mutation inside callback
            ArrayList<String> mutableWaitingList = new ArrayList<>(waitingList);

            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (String uid : new ArrayList<>(mutableWaitingList)) {
                tasks.add(db.collection("Users").document(uid).get());
            }

            com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        // 1. Clean up removed accounts
                        Iterator<String> iter = mutableWaitingList.iterator();
                        for (Object obj : results) {
                            DocumentSnapshot userSnap = (DocumentSnapshot) obj;
                            if (!userSnap.exists()) {
                                iter.remove();
                            }
                        }

                        // 2. Decide if the user is joining or leaving
                        boolean joining;
                        if (isInWaitingList) {
                            // user is LEAVING
                            mutableWaitingList.remove(currentUserId);
                            joining = false;
                            isInWaitingList = false;
                            joinWaitingListButton.setText("Join Waiting List");
                            Toast.makeText(getContext(),
                                    "You left the waiting list",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // user is JOINING
                            if (!mutableWaitingList.contains(currentUserId)) {
                                mutableWaitingList.add(currentUserId);
                            }
                            joining = true;
                            isInWaitingList = true;
                            joinWaitingListButton.setText("Leave Waiting List");
                            Toast.makeText(getContext(),
                                    "You joined the waiting list",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // 3. Update the event's waiting list in Firestore
                        eventRef.update("waitingList", mutableWaitingList)
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                getContext(),
                                                "Error updating waiting list: " + e.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show()
                                );

                        // 4. Update this user's Registrations subcollection for History
                        DocumentReference regRef = db.collection("Users")
                                .document(currentUserId)
                                .collection("Registrations")
                                .document(eventId);

                        if (joining) {
                            // Joined -> create or update a registration record
                            Map<String, Object> reg = new HashMap<>();
                            reg.put("eventId", eventId);
                            reg.put("status", "Pending"); // will change later to Selected / Declined etc.
                            reg.put("registeredAt", FieldValue.serverTimestamp());
                            reg.put("eventName", snapshot.getString("eventName"));
                            reg.put("startDate", snapshot.getString("startDate"));
                            reg.put("location", snapshot.getString("location"));
                            reg.put("description", snapshot.getString("description"));

                            regRef.set(reg);
                        } else {
                            // Left -> keep it in history, but mark it Cancelled
                            regRef.update("status", "Cancelled")
                                    .addOnFailureListener(e ->
                                            Toast.makeText(
                                                    getContext(),
                                                    "Error updating registration: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                        }
                    });
        });
    }
}
