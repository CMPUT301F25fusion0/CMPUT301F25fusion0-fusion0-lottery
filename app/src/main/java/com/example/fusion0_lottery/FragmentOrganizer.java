package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * A fragment for the Organizer screen
 */
public class FragmentOrganizer extends Fragment {

    private Button createNewEvent;

    //Firebase attributes
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer, container, false);

        ListView eventsOrg = view.findViewById(R.id.eventsOrg);

        // Dummy event
        ArrayList<Event> eventsArray = new ArrayList<>();
        Event fortnite = new Event("fortnite",
                "gaming event", "march",
                "april", "6:07",
                6.7, "Tilted Towers",
                "feb", "march",
                2);

        // Listview Adapter
        EventArrayAdapter eventsAdapter = new EventArrayAdapter(getContext(), eventsArray);
        eventsOrg.setAdapter(eventsAdapter);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        // Adds events from firestore into listview
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                eventsAdapter.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String eventName = snapshot.getString("eventName");
                    String description = snapshot.getString("description");
                    String startDate = snapshot.getString("startDate");
                    String endDate = snapshot.getString("endDate");
                    String time = snapshot.getString("time");
                    Double price = snapshot.getDouble("price");
                    String location = snapshot.getString("location");
                    String regStart = snapshot.getString("registrationStart");
                    String regEnd = snapshot.getString("registrationEnd");
                    Integer maxEntrants = snapshot.getLong("maxEntrants").intValue();

                    eventsAdapter.add(new Event(eventName, description,
                            startDate, endDate, time, price,
                            location, regStart, regEnd, maxEntrants));
                }
                eventsAdapter.notifyDataSetChanged();
            }
        });

        createNewEvent = view.findViewById(R.id.createEventButton);

        createNewEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EventCreationActivity.class);
            startActivity(intent);
        });


        // Adds event to listview and database
        eventsArray.add(fortnite);
        eventsAdapter.notifyDataSetChanged();

        DocumentReference eventsDocRef = eventsRef.document(fortnite.getEventName());
        eventsDocRef.set(fortnite);

        return view;
    }

}