package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * A fragment for the Organizer screen
 */
public class FragmentOrganizer extends Fragment {

    private Button createNewEvent;

    // Firebase attributes
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private Button backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer, container, false);

        ListView eventsOrg = view.findViewById(R.id.eventsOrg);
        ArrayList<Event> eventsArray = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        EventArrayAdapter eventsAdapter = new EventArrayAdapter(requireContext(), eventsArray, event -> {
            ManageEvents manageEventsFragment = new ManageEvents();
            Bundle args = new Bundle();
            args.putString("eventId", event.getEventId());
            args.putString("eventName", event.getEventName());
            manageEventsFragment.setArguments(args);
            ((MainActivity) requireActivity()).replaceFragment(manageEventsFragment);
        });

        eventsOrg.setAdapter(eventsAdapter);

        // load events from Firestore
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }
            // check if there are any events
            if (value != null && !value.isEmpty()) {
                eventsAdapter.clear();

                // go through through all the documents
                for (QueryDocumentSnapshot snapshot : value) {
                    // convert the event into an object to get data from
                    Event event = snapshot.toObject(Event.class);
                    event.setEventId(snapshot.getId());
                    // get size of list from Firestore database arrays
                    ArrayList<String> waitingList = (ArrayList<String>) snapshot.get("waitingList");
                    ArrayList<String> selectedList = (ArrayList<String>) snapshot.get("selectedList");
                    ArrayList<String> enrolledList = (ArrayList<String>) snapshot.get("enrolledList");

                    // set the waiting list count, selected count, and enrolled count based on what size value was in the array
                    if (waitingList != null) {
                        event.setWaitingListCount(waitingList.size());
                    }
                    else {
                        event.setWaitingListCount(0);
                    }

                    if (selectedList != null) {
                        event.setUserSelectedCount(selectedList.size());
                    }
                    else {
                        event.setUserSelectedCount(0);
                    }

                    if (enrolledList != null) {
                        event.setUserEnrolledCount(enrolledList.size());
                    }
                    else {
                        event.setUserEnrolledCount(0);
                    }
                    eventsAdapter.add(event);
                }
                eventsAdapter.notifyDataSetChanged();
            }
        });

        createNewEvent = view.findViewById(R.id.createEventButton);
        createNewEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EventCreationActivity.class);
            startActivity(intent);
        });

        // go back to role selection (for testing purposes; don't include in final submission)
        backButton = view.findViewById(R.id.backToRoleButton);
        backButton.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).replaceFragment(new FragmentRoleSelection());
        });

        return view;
    }
}
