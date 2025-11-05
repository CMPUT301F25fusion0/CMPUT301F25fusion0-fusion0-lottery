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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
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

                        ArrayList<String> waitingList = (ArrayList<String>) snapshot.get("waitingList");
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
                                        DocumentSnapshot userSnap = (DocumentSnapshot) results.get(i);
                                        if (userSnap.exists()) cleanList.add(finalWaitingList.get(i));
                                    }

                                    // Update isInWaitingList
                                    isInWaitingList = cleanList.contains(currentUserId);
                                    joinWaitingListButton.setText(isInWaitingList ? "Leave Waiting List" : "Join Waiting List");

                                    // Update Firestore with cleaned list
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

        // Join/Leave waiting list
        joinWaitingListButton.setOnClickListener(v -> toggleWaitingList());

        return view;
    }

    private void toggleWaitingList() {
        if (waitingListClosed) {
            Toast.makeText(getContext(), "The waiting list is closed. You cannot join this event.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> waitingList = (ArrayList<String>) snapshot.get("waitingList");
            if (waitingList == null) waitingList = new ArrayList<>();

            // Create a copy for mutation inside lambda
            ArrayList<String> mutableWaitingList = new ArrayList<>(waitingList);

            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (String uid : new ArrayList<>(mutableWaitingList)) {
                tasks.add(db.collection("Users").document(uid).get());
            }

            com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        // Clean up deleted users
                        Iterator<String> iter = mutableWaitingList.iterator();
                        for (Object obj : results) {
                            com.google.firebase.firestore.DocumentSnapshot userSnap = (com.google.firebase.firestore.DocumentSnapshot) obj;
                            if (!userSnap.exists()) iter.remove();
                        }

                        // Add/remove current user
                        if (isInWaitingList) {
                            mutableWaitingList.remove(currentUserId);
                            isInWaitingList = false;
                            joinWaitingListButton.setText("Join Waiting List");
                            Toast.makeText(getContext(), "You left the waiting list", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!mutableWaitingList.contains(currentUserId)) mutableWaitingList.add(currentUserId);
                            isInWaitingList = true;
                            joinWaitingListButton.setText("Leave Waiting List");
                            Toast.makeText(getContext(), "You joined the waiting list", Toast.LENGTH_SHORT).show();
                        }

                        // Update Firestore
                        eventRef.update("waitingList", mutableWaitingList)
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error updating waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });
        });
    }
}