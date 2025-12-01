package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Fragment representing the main Organizer screen in the lottery application.
 *
 * <p>This fragment serves as the central hub for event organizers to view and manage
 * their events. It displays a list of all events in the system with real-time updates
 * from Firestore, showing event details including waiting list counts, selected entrants,
 * and enrolled participants.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Display all events from Firestore in real-time using snapshot listeners</li>
 *   <li>Navigate to event management screens when an event is selected</li>
 *   <li>Provide access to event creation functionality</li>
 *   <li>Show event statistics (waiting list, selected, and enrolled counts)</li>
 * </ul>
 *
 * <p><b>Design Pattern:</b> This fragment follows the MVC (Model-View-Controller) pattern
 * where the fragment acts as the controller, Firestore as the model, and the XML layout
 * as the view.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently identified.</p>
 *
 * @see ManageEvents
 * @see EventCreationActivity
 * @see Event
 *
 * @version 1.0
 * @since 2024-11-30
 *
 * Code Attribution:
 * - Code from lines 89 to 91 was inspired by Joao Marcos and Gowthaman M from
 *   StackOverflow, https://stackoverflow.com/a/24555520,
 *   published July 3 2014 and edited February 27 2018
 */
public class FragmentOrganizer extends Fragment {
    private Button createNewEvent;

    // Firebase attributes
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private Button backButton;
    private FirebaseAuth auth;
    private FirebaseUser user;


    /**
     * Creates and initializes the organizer fragment view.
     *
     * <p>This method sets up the event list view with real-time Firestore synchronization,
     * configures click listeners for event selection and navigation, and initializes
     * the back button for role selection.</p>
     *
     * @param inflater The LayoutInflater object to inflate views in the fragment
     * @param container The parent view that this fragment's UI will be attached to
     * @param savedInstanceState Previously saved state of the fragment, if any
     * @return The root View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer, container, false);

        ListView eventsOrg = view.findViewById(R.id.eventsOrg);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            return view;
        }
        String userId = user.getUid();
        ArrayList<Event> eventsArray = new ArrayList<>();

        //Firebase attributes
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        EventArrayAdapter eventsAdapter = new EventArrayAdapter(requireContext(), eventsArray);
        eventsOrg.setAdapter(eventsAdapter);

        // load events from Firestore
        eventsRef.whereEqualTo("organizerId", userId)
                .addSnapshotListener((value, error) ->{
            if (value != null && !value.isEmpty()) {
                eventsAdapter.clear();

                // go through through all the documents
                for (QueryDocumentSnapshot snapshot : value) {
                    String eventId = snapshot.getId();
                    String eventName = snapshot.getString("eventName");
                    String interests = snapshot.getString("interests");
                    String description = snapshot.getString("description");
                    String startDate = snapshot.getString("startDate");
                    String endDate = snapshot.getString("endDate");
                    String time = snapshot.getString("time");
                    Double priceDouble = snapshot.getDouble("price");
                    double price = (priceDouble != null) ? priceDouble : 0.0;
                    String location = snapshot.getString("location");
                    String regStart = snapshot.getString("registrationStart");
                    String regEnd = snapshot.getString("registrationEnd");
                    Long maxEntrants = snapshot.getLong("maxEntrants");
                    if (maxEntrants == null) {
                        maxEntrants = -1L; // placeholder; "No Limit" if -1
                    }
                    Long winnersLong = snapshot.getLong("numberOfWinners");
                    int numberOfWinners = (winnersLong != null) ? winnersLong.intValue() : 0;

                    // convert the event into an object to get data from
                    Event event = snapshot.toObject(Event.class);
                    event.setEventId(snapshot.getId());
                    // get size of list from Firestore database arrays
                    ArrayList<String> waitingList = (ArrayList<String>) snapshot.get("waitingList");
                    ArrayList<String> selectedList = (ArrayList<String>) snapshot.get("selectedList");
                    ArrayList<String> enrolledList = (ArrayList<String>) snapshot.get("enrolledList");
                    // set counts
                    event.setWaitingListCount(waitingList != null ? waitingList.size() : 0);
                    event.setUserSelectedCount(selectedList != null ? selectedList.size() : 0);
                    event.setUserEnrolledCount(enrolledList != null ? enrolledList.size() : 0);
                    // add to adapter
                    eventsAdapter.add(event);
                }
                eventsAdapter.notifyDataSetChanged();
            }
        });

        eventsOrg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ManageEvents manageEventsFragment = new ManageEvents();
                Bundle args = new Bundle();
                args.putString("eventId", eventsAdapter.getItem(position).getEventId());
                manageEventsFragment.setArguments(args);
                getParentFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container, manageEventsFragment)
                        .commit();
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
