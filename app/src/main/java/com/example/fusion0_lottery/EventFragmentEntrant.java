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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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
    private String currentUserId;
    private boolean isInWaitingList;
    private boolean waitingListClosed;

    private FirebaseFirestore db;

    public EventFragmentEntrant() { }

    public static EventFragmentEntrant newInstance(
            String eventId,
            String currentUserId,
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
        EventFragmentEntrant f = new EventFragmentEntrant();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("currentUserId", currentUserId);
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
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_activity_entrant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // UI refs
        eventNameText         = view.findViewById(R.id.eventName);
        eventDescriptionText  = view.findViewById(R.id.eventDescription);
        eventDateText         = view.findViewById(R.id.eventDate);
        eventLocationText     = view.findViewById(R.id.eventLocation);
        registrationText      = view.findViewById(R.id.eventEndDate);
        maxEntrantsText       = view.findViewById(R.id.eventEntrants);
        eventPriceText        = view.findViewById(R.id.eventPrice);
        joinWaitingListButton = view.findViewById(R.id.buttonJoinWaitingList);
        joinWaitingListButton.setVisibility(View.INVISIBLE);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Event Details");
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            toolbar.setNavigationIcon(android.R.drawable.ic_media_previous);
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Arguments
        if (getArguments() != null) {
            eventId           = getArguments().getString("eventId");
            currentUserId     = getArguments().getString("currentUserId");
            waitingListClosed = getArguments().getBoolean("waitingListClosed", false);
            isInWaitingList   = getArguments().getBoolean("isInWaitingList", false);

            // Show cached fields right away
            eventNameText.setText("Event Name: " + getArguments().getString("eventName"));
            eventDescriptionText.setText("Description: " + getArguments().getString("eventDescription"));
            eventDateText.setText("Start Date: " + getArguments().getString("startDate"));
            eventLocationText.setText("Location: " + getArguments().getString("eventLocation"));
            registrationText.setText("Registration: " +
                    getArguments().getString("registrationStart") + " to " +
                    getArguments().getString("registrationEnd"));
            maxEntrantsText.setText("Max Entrants: " + getArguments().getLong("maxEntrants"));
            eventPriceText.setText("Price: $" + getArguments().getDouble("price"));
        }

        // Refresh from Firestore to ensure state is current & clean waitingList of deleted users
        if (eventId != null) {
            db.collection("Events").document(eventId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) {
                            joinWaitingListButton.setVisibility(View.GONE);
                            return;
                        }

                        @SuppressWarnings("unchecked")
                        ArrayList<String> waitingList = (ArrayList<String>) snapshot.get("waitingList");
                        if (waitingList == null) waitingList = new ArrayList<>();

                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String uid : new ArrayList<>(waitingList)) {
                            tasks.add(db.collection("Users").document(uid).get());
                        }

                        ArrayList<String> finalWaitingList = waitingList;
                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(results -> {
                                    ArrayList<String> cleanList = new ArrayList<>();
                                    for (int i = 0; i < results.size(); i++) {
                                        DocumentSnapshot userSnap = (DocumentSnapshot) results.get(i);
                                        if (userSnap.exists()) cleanList.add(finalWaitingList.get(i));
                                    }

                                    isInWaitingList = cleanList.contains(currentUserId);
                                    joinWaitingListButton.setText(
                                            isInWaitingList ? "Leave Waiting List" : "Join Waiting List");

                                    snapshot.getReference().update("waitingList", cleanList);
                                    joinWaitingListButton.setVisibility(View.VISIBLE);
                                });
                    });
        }

        joinWaitingListButton.setOnClickListener(v -> toggleWaitingList());
    }

    private void toggleWaitingList() {
        if (waitingListClosed) {
            Toast.makeText(getContext(), "The waiting list is closed. You cannot join this event.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUserId == null || eventId == null) {
            Toast.makeText(getContext(), "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }

            @SuppressWarnings("unchecked")
            ArrayList<String> waitingList = (ArrayList<String>) snapshot.get("waitingList");
            if (waitingList == null) waitingList = new ArrayList<>();

            ArrayList<String> mutableWaitingList = new ArrayList<>(waitingList);

            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (String uid : new ArrayList<>(mutableWaitingList)) {
                tasks.add(db.collection("Users").document(uid).get());
            }

            Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        // remove deleted users
                        Iterator<String> iter = mutableWaitingList.iterator();
                        for (Object obj : results) {
                            DocumentSnapshot userSnap = (DocumentSnapshot) obj;
                            if (!userSnap.exists()) iter.remove();
                        }

                        boolean joining;
                        if (isInWaitingList) {
                            mutableWaitingList.remove(currentUserId);
                            joining = false;
                            isInWaitingList = false;
                            joinWaitingListButton.setText("Join Waiting List");
                            Toast.makeText(getContext(), "You left the waiting list", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!mutableWaitingList.contains(currentUserId)) {
                                mutableWaitingList.add(currentUserId);
                            }
                            joining = true;
                            isInWaitingList = true;
                            joinWaitingListButton.setText("Leave Waiting List");
                            Toast.makeText(getContext(), "You joined the waiting list", Toast.LENGTH_SHORT).show();
                        }

                        // update event doc list
                        eventRef.update("waitingList", mutableWaitingList)
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Error updating waiting list: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());

                        // mirror into History
                        DocumentReference regRef = db.collection("Users")
                                .document(currentUserId)
                                .collection("Registrations")
                                .document(eventId);

                        if (joining) {
                            Map<String, Object> reg = new HashMap<>();
                            reg.put("eventId", eventId);
                            reg.put("status", "Pending"); // organizer flow will update later
                            reg.put("registeredAt", FieldValue.serverTimestamp());
                            reg.put("eventName", snapshot.getString("eventName"));
                            reg.put("startDate", snapshot.getString("startDate"));
                            reg.put("location", snapshot.getString("location"));
                            reg.put("description", snapshot.getString("description"));

                            regRef.set(reg, SetOptions.merge());
                        } else {
                            // KEEP history: mark as Cancelled instead of deleting
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("eventId", eventId);
                            updates.put("status", "Cancelled");
                            updates.put("cancelledAt", FieldValue.serverTimestamp());
                            regRef.set(updates, SetOptions.merge());
                        }
                    });
        });
    }
}
