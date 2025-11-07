package com.example.fusion0_lottery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

import java.util.ArrayList;

public class ManageEvents extends Fragment {

    private TabLayout tabLayout;
    private ImageView eventPosterImage, qrCodeImage;
    private TextView manageEventTitle, eventDetailsText, eventDescriptionText;
    private Button editEventButton, updatePosterButton, notifyWaitlistButton, drawWinnersButton, exportCsvButton;

    private Uri newPosterUri;

    private FirebaseFirestore db;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.manage_events, container, false);

        tabLayout = view.findViewById(R.id.manageEventTabs);
        // viewPager = view.findViewById(R.id.viewPager);

        eventPosterImage = view.findViewById(R.id.eventPosterImage);
        qrCodeImage = view.findViewById(R.id.qrCodeImage);
        manageEventTitle = view.findViewById(R.id.manageEventTitle);
        eventDetailsText = view.findViewById(R.id.eventDetailsText);
        eventDescriptionText = view.findViewById(R.id.eventDescriptionText);


        editEventButton = view.findViewById(R.id.editEventButton);
        updatePosterButton = view.findViewById(R.id.updatePosterButton);
        notifyWaitlistButton = view.findViewById(R.id.notifyWaitlistButton);
        drawWinnersButton = view.findViewById(R.id.drawWinnersButton);
        exportCsvButton = view.findViewById(R.id.exportCsvButton);

        Button backToEventsButton = view.findViewById(R.id.backToEventsButton);
        backToEventsButton.setOnClickListener(v -> {
            FragmentOrganizer organizerFragment = new FragmentOrganizer();
            ((MainActivity) requireActivity()).replaceFragment(organizerFragment);
        });

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventDetails();
        }

        setupTabs();
        setupButtonActions();

        return view;
    }

    /**
     * function to set up tabs on the Manage Events screen
     * takes the organizer to a different screen within Manage Events when clicked
     * Details: default screen for managing events
     * Waiting List: view all entrants who are in the waiting list for that event
     * Selected: list of all entrants who have been chosen for the event [incomplete]
     * Manage Participants: allow organizers control on the entrants who have been selected [incomplete]
     * Generate QR code: generate a working QR code for the event that entrants can scan and see the event details [incomplete]
    */
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Details"));
        tabLayout.addTab(tabLayout.newTab().setText("Waiting List"));
        tabLayout.addTab(tabLayout.newTab().setText("Selected"));
        tabLayout.addTab(tabLayout.newTab().setText("Manage Participants"));
        tabLayout.addTab(tabLayout.newTab().setText("Generate QR"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String title = tab.getText().toString();
                if (title.equals("Waiting List")) {
                    FragmentWaitingList fragmentWaitingList = new FragmentWaitingList();
                    Bundle args = new Bundle();
                    args.putString("eventId", eventId);
                    fragmentWaitingList.setArguments(args);
                    ((MainActivity) requireActivity()).replaceFragment(fragmentWaitingList);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     *  function to set up the buttons near the bottom of the screen in the Manage Events screen
     *  after clicking on "Manage" for any of the listed events, display list of buttons for the event
     *  Update Poster: allow organizer to change the event poster
     *  Edit Event: allow the organizer to change the details for the event
     *  Notify Waiting List: send a notification to everyone in the event's waiting list [incomplete]
     *  Draw Winners: allow the organizer to select a winner from the waiting list [incomplete]
     *  Export CSV: download CSV file for all confirmed entrants [incomplete]
    */
    private void setupButtonActions() {
        updatePosterButton.setOnClickListener(v -> {
            if (eventId != null) {
                FragmentUpdatePoster updatePosterImage = new FragmentUpdatePoster();
                Bundle args = new Bundle();
                args.putString("eventId", eventId);
                updatePosterImage.setArguments(args);
                ((MainActivity) requireActivity()).replaceFragment(updatePosterImage);
            }
            else {
                Toast.makeText(requireContext(), "Unable to edit Poster Image", Toast.LENGTH_SHORT).show();
            }
        });

        editEventButton.setOnClickListener(v -> {
            if (eventId != null) {
                FragmentEditEvent editFragment = new FragmentEditEvent();
                Bundle args = new Bundle();
                args.putString("eventId", eventId);
                editFragment.setArguments(args);
                ((MainActivity) requireActivity()).replaceFragment(editFragment);
            }
            else {
                Toast.makeText(requireContext(), "Event ID not available", Toast.LENGTH_SHORT).show();
            }
        });


        /*
        other tabs not started
        notifyWaitlistButton.setOnClickListener(v -> {

        });

        drawWinnersButton.setOnClickListener(v -> {
        });

        exportCsvButton.setOnClickListener(v -> {
        });
        */
    }


    /**
     *  function display the details for the event
     *  gets the reference from Firestore 'Events' based on event ID
     *  if document exists, convert the data into an object and display event information
    */
    private void loadEventDetails() {
        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Event event = snapshot.toObject(Event.class);
                if (event != null) {
                    // get the event's description from the database
                    String description;
                    if (event.getDescription() != null || !event.getDescription().isEmpty()) {
                        description = event.getDescription();
                    }
                    // if no description set empty
                    else {
                        description = "";
                    }

                    eventDescriptionText.setText(description);
                    manageEventTitle.setText(event.getEventName());

                    // get the number of people in the waiting list
                    ArrayList<String> waitingList = (ArrayList<String>) snapshot.get("waitingList");
                    int waitingCount, selectedCount, enrolledCount;

                    // check if waiting list is empty, if empty -> waiting count = 0, otherwise set count = waiting count size
                    if (waitingList != null) {
                        waitingCount = waitingList.size();
                    }
                    else {
                        waitingCount = 0;
                    }

                    // get the number of entrants selected and enrolled (null / 0 for now)
                    // check if users selected is null or not, if null, set = 0
                    if (event.getUserSelectedCount() != null) {
                        selectedCount = event.getUserSelectedCount();
                    }
                    else {
                        selectedCount = 0;
                    }

                    // check if users enrolled  is null or not, if null, set = 0
                    if (event.getUserEnrolledCount() != null) {
                        enrolledCount = event.getUserEnrolledCount();
                    }
                    else {
                        enrolledCount = 0;
                    }

                    // get all of the event's details
                    eventDetailsText.setText(getString(
                            R.string.event_details, event.getRegistrationStart(), event.getRegistrationEnd(),
                            event.getMaxEntrants(), waitingCount, selectedCount, enrolledCount,
                            event.getStartDate(), event.getTime(), event.getPrice()));

                    // load poster from Base64
                    String poster = snapshot.getString("posterImage");
                    if (poster != null && !poster.isEmpty()) {
                        byte[] imageBytes = Base64.decode(poster, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        eventPosterImage.setImageBitmap(bitmap);
                    }
                }
            }
        });
    }
}
