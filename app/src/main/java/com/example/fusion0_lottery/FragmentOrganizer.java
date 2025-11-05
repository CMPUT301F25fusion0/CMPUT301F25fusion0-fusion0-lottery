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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A fragment for the Organizer screen
 * - Code from lines 89 to 91 was inspired
 *      by Joao Marcos and Gowthaman M from
 *      StackOverflow, https://stackoverflow.com/a/24555520,
 *      published July 3 2014 and edited February 27 2018
 */
public class FragmentOrganizer extends Fragment {
    private Button createNewEvent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer, container, false);

        ListView eventsOrg = view.findViewById(R.id.eventsOrg);

        ArrayList<Event> eventsArray = new ArrayList<>();

        // Listview Adapter
        EventArrayAdapter eventsAdapter = new EventArrayAdapter(getContext(), eventsArray);
        eventsOrg.setAdapter(eventsAdapter);

        //Firebase attributes
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("Events");

        // Adds events from firestore into listview
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                eventsAdapter.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String eventId = snapshot.getId();
                    String eventName = snapshot.getString("eventName");
                    String description = snapshot.getString("description");
                    String startDate = snapshot.getString("startDate");
                    String endDate = snapshot.getString("endDate");
                    String time = snapshot.getString("time");
                    Double price = snapshot.getDouble("price");
                    String location = snapshot.getString("location");
                    String regStart = snapshot.getString("registrationStart");
                    String regEnd = snapshot.getString("registrationEnd");
                    Long maxEntrants = snapshot.getLong("maxEntrants");
                    if (maxEntrants == null) {
                        maxEntrants = -1L; // placeholder; "No Limit" if -1
                    }
                    Event event = new Event(eventName, description,
                            startDate, endDate, time, price,
                            location, regStart, regEnd, maxEntrants.intValue());
                    event.setEventId(eventId);
                    eventsAdapter.add(event);
                }
                eventsAdapter.notifyDataSetChanged();
            }
        });

        eventsOrg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventFragmentOrganizer eventFragmentOrganizer = new EventFragmentOrganizer();
                Bundle args = new Bundle();
                args.putString("eventId", eventsAdapter.getItem(position).getEventId());
                eventFragmentOrganizer.setArguments(args);
                getParentFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container, eventFragmentOrganizer)
                        .commit();
            }
        });

        createNewEvent = view.findViewById(R.id.createEventButton);

        createNewEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EventCreationActivity.class);
            startActivity(intent);
        });

        return view;
    }

}