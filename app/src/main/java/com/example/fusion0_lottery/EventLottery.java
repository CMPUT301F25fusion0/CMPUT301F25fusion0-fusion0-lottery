package com.example.fusion0_lottery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventLottery extends Fragment {

    private LinearLayout eventsContainer;
    private Button buttonBack;
    private FirebaseFirestore db;
    private String userEmail;

    public EventLottery() {}

    public static EventLottery newInstance(String userEmail) {
        EventLottery fragment = new EventLottery();
        Bundle args = new Bundle();
        args.putString("userEmail", userEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_lottery, container, false);

        eventsContainer = view.findViewById(R.id.eventsContainer);
        buttonBack = view.findViewById(R.id.buttonBack);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        }

        loadEvents();
        return view;
    }

    private void loadEvents() {
        db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("EventLottery", "Number of events fetched: " + queryDocumentSnapshots.size());
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    displayEvents(queryDocumentSnapshots.getDocuments());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EventLottery", "Firestore fetch error", e);
                });
    }

    private void displayEvents(List<DocumentSnapshot> events) {
        if (eventsContainer == null) return;
        eventsContainer.removeAllViews();

        for (DocumentSnapshot eventDoc : events) {
            String eventName = eventDoc.getString("eventName");
            String eventStartDate = eventDoc.getString("startDate");
            String eventLocation = eventDoc.getString("location");
            Double price = eventDoc.getDouble("price");
            List<String> waitingList = (List<String>) eventDoc.get("waitingList");
            int waitlistCount = waitingList != null ? waitingList.size() : 0;
            String eventId = eventDoc.getId();

            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            card.setPadding(24, 24, 24, 24);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 24);
            card.setLayoutParams(cardParams);

            TextView nameText = new TextView(getContext());
            nameText.setText("Event: " + eventName);
            nameText.setTextSize(20f);
            nameText.setTextColor(getResources().getColor(android.R.color.black));
            nameText.setPadding(0, 0, 0, 8);

            TextView dateText = new TextView(getContext());
            dateText.setText("Start Date: " + eventStartDate);
            dateText.setPadding(0, 0, 0, 8);

            TextView locationText = new TextView(getContext());
            locationText.setText("Location: " + eventLocation);
            locationText.setPadding(0, 0, 0, 8);

            TextView waitlistText = new TextView(getContext());
            waitlistText.setText("Current Waitlist: " + waitlistCount);
            waitlistText.setPadding(0, 0, 0, 16);

            Button viewDetailsBtn = new Button(getContext());
            viewDetailsBtn.setText("View Details");
            viewDetailsBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
            viewDetailsBtn.setTextColor(getResources().getColor(android.R.color.white));

            viewDetailsBtn.setOnClickListener(v -> {
                db.collection("Events").document(eventId)
                        .get()
                        .addOnSuccessListener(eventSnapshot -> {
                            if (eventSnapshot.exists()) {

                                String eventNameStr = eventSnapshot.getString("eventName");
                                String eventDescStr = eventSnapshot.getString("description");
                                String eventStartDateStr = eventSnapshot.getString("startDate");
                                String eventLocationStr = eventSnapshot.getString("location");

                                String regStart = eventSnapshot.getString("registrationStart");
                                String regEnd = eventSnapshot.getString("registrationEnd");

                                Long maxEntrantsVal = eventSnapshot.getLong("maxEntrants");
                                Double priceVal = eventSnapshot.getDouble("price");

                                List<String> fullWaitlist = (List<String>) eventSnapshot.get("waitingList");
                                boolean isOnWaitlist = fullWaitlist != null && fullWaitlist.contains(userEmail);

                                boolean waitingListClosed = eventSnapshot.getBoolean("waitingListClosed") != null
                                        ? eventSnapshot.getBoolean("waitingListClosed") : false;

                                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                getParentFragmentManager()
                                        .beginTransaction()
                                        .replace(
                                                R.id.fragment_container,
                                                EventActivityEntrant.newInstance(
                                                        eventId,
                                                        currentUserId,
                                                        eventNameStr != null ? eventNameStr : "No Name",
                                                        eventDescStr != null ? eventDescStr : "No Description",
                                                        eventStartDateStr != null ? eventStartDateStr : "No Date",
                                                        eventLocationStr != null ? eventLocationStr : "No Location",
                                                        isOnWaitlist,
                                                        regStart != null ? regStart : "",
                                                        regEnd != null ? regEnd : "",
                                                        maxEntrantsVal != null ? maxEntrantsVal : 0L,
                                                        priceVal != null ? priceVal : 0.0,
                                                        waitingListClosed
                                                )
                                        )
                                        .addToBackStack(null)
                                        .commit();

                            } else {
                                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            card.addView(nameText);
            card.addView(dateText);
            card.addView(locationText);
            card.addView(waitlistText);
            card.addView(viewDetailsBtn);

            eventsContainer.addView(card);
        }
    }
}
