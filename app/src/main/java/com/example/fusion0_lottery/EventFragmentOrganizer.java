package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Displays the event details for Organizers
 * - Code for lines 49 to 52 was inspired by
 *      Dhara Bhavsar on StackOverflow at
 *      https://stackoverflow.com/a/49822393,
 *      published April 13 2018
 */
public class EventFragmentOrganizer extends Fragment {

    public EventFragmentOrganizer() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_organizer, container, false);
        super.onCreate(savedInstanceState);

        // Views Setup
        TextView eventName = view.findViewById(R.id.eventName);
        TextView eventDesc = view.findViewById(R.id.eventDescription);
        TextView eventDate = view.findViewById(R.id.eventDate);
        TextView eventLoc = view.findViewById(R.id.eventLocation);
        TextView eventRegDate = view.findViewById(R.id.eventEndDate);
        TextView eventMaxEntrant = view.findViewById(R.id.eventEntrants);
        TextView eventPrice = view.findViewById(R.id.eventPrice);
        ImageView qrCodeImage = view.findViewById(R.id.eventQrCode);
        TextView qrCodeLabel = view.findViewById(R.id.qrCodeLabel);

        // Retrieve eventId
        String eventId = getArguments().getString("eventId");

        // Database Setup
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Event event = documentSnapshot.toObject(Event.class);

                eventName.setText("Event Name: " + event.getEventName());
                eventDesc.setText("Description: " + event.getDescription());
                eventDate.setText("Start Date: " + event.getStartDate());
                eventLoc.setText("Location: " + event.getLocation());

                String regStart = event.getRegistrationStart();
                String regEnd = event.getRegistrationEnd();
                eventRegDate.setText("Registration: " + regStart + " to " + regEnd);

                if (event.getMaxEntrants() < 0) {
                    eventMaxEntrant.setText("Max Entrants: " + "No Limit");
                }
                else {
                    eventMaxEntrant.setText("Max Entrants: " + event.getMaxEntrants());
                }
                eventPrice.setText("Price: $" + event.getPrice());

                // Load and display QR code if available
                String qrCodeUrl = event.getQrCodeUrl();
                if (qrCodeUrl != null && !qrCodeUrl.isEmpty()) {
                    qrCodeLabel.setVisibility(View.VISIBLE);
                    qrCodeImage.setVisibility(View.VISIBLE);
                    Glide.with(EventFragmentOrganizer.this)
                            .load(qrCodeUrl)
                            .into(qrCodeImage);
                } else {
                    qrCodeLabel.setVisibility(View.GONE);
                    qrCodeImage.setVisibility(View.GONE);
                }

            }

        });

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Event Details");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationIcon(android.R.drawable.ic_media_previous);
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }
}