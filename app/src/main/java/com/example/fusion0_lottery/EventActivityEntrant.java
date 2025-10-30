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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EventActivityEntrant extends Fragment {

    private TextView eventNameText, eventDescriptionText, eventDateText, eventLocationText;
    private TextView registrationText, maxEntrantsText, eventPriceText;
    private Button joinWaitingListButton;
    private String eventId, userEmail;
    private boolean isInWaitingList;
    private boolean waitingListClosed;

    private FirebaseFirestore db;

    public EventActivityEntrant() {}

    public static EventActivityEntrant newInstance(
            String eventId,
            String userEmail,
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
        EventActivityEntrant fragment = new EventActivityEntrant();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("userEmail", userEmail);
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

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            userEmail = getArguments().getString("userEmail");
            isInWaitingList = getArguments().getBoolean("isInWaitingList", false);
            waitingListClosed = getArguments().getBoolean("waitingListClosed", false);

            // Display all fields with labels
            eventNameText.setText("Event Name: " + getArguments().getString("eventName"));
            eventDescriptionText.setText("Description: " + getArguments().getString("eventDescription"));
            eventDateText.setText("Start Date: " + getArguments().getString("startDate"));
            eventLocationText.setText("Location: " + getArguments().getString("eventLocation"));

            String regStart = getArguments().getString("registrationStart");
            String regEnd = getArguments().getString("registrationEnd");
            registrationText.setText("Registration: " + regStart + " to " + regEnd);

            long maxEntrants = getArguments().getLong("maxEntrants");
            maxEntrantsText.setText("Max Entrants: " + maxEntrants);

            double price = getArguments().getDouble("price");
            eventPriceText.setText("Price: $" + price);

            joinWaitingListButton.setText(isInWaitingList ? "Leave Waiting List" : "Join Waiting List");
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

            if (isInWaitingList) {
                waitingList.remove(userEmail);
                isInWaitingList = false;
                joinWaitingListButton.setText("Join Waiting List");
                Toast.makeText(getContext(), "You left the waiting list", Toast.LENGTH_SHORT).show();
            } else {
                waitingList.add(userEmail);
                isInWaitingList = true;
                joinWaitingListButton.setText("Leave Waiting List");
                Toast.makeText(getContext(), "You joined the waiting list", Toast.LENGTH_SHORT).show();
            }

            eventRef.update("waitingList", waitingList)
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error updating waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
