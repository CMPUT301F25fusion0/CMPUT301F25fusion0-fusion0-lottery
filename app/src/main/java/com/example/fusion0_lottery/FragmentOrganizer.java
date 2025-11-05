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
                        maxEntrants = -1L; // placeholder; EventArrayAdapter will display "No Limit" if -1
                    }
                    eventsAdapter.add(new Event(eventName, description,
                            startDate, endDate, time, price,
                            location, regStart, regEnd, maxEntrants.intValue()));
                }
                eventsAdapter.notifyDataSetChanged();
            }
        });

        Button createNewEvent = view.findViewById(R.id.createEventButton);

        createNewEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EventCreationActivity.class);
            startActivity(intent);
        });

        return view;
    }

}