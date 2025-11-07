package com.example.fusion0_lottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    private final ArrayList<Event> events;
    private final Context context;
    private final OnManageClickListener listener;

    public interface OnManageClickListener {
        void onManageClicked(Event event);
    }

    public EventArrayAdapter(Context context, ArrayList<Event> events, OnManageClickListener listener) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.event_layout, parent, false);
        }

        Event event = getItem(position);

        TextView eventName = convertView.findViewById(R.id.eventName);
        TextView status = convertView.findViewById(R.id.status);
        TextView waitingList = convertView.findViewById(R.id.waitingList);
        TextView drawDate = convertView.findViewById(R.id.drawDate);
        Button manageButton = convertView.findViewById(R.id.manageButton);

        if (event != null) {
            eventName.setText(event.getEventName());
            status.setText("Status: " + (event.getQrCodeUrl() != null ? "lottery open" : "pending"));
            drawDate.setText("Draw Date: " + (event.getEndDate() != null ? event.getEndDate() : "N/A"));

            // load waiting list count from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String eventId = event.getEventId();

            db.collection("Events")
                    .document(eventId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            ArrayList<String> waitingListArray = (ArrayList<String>) snapshot.get("waitingList");
                            int waitingCount = (waitingListArray != null) ? waitingListArray.size() : 0;
                            int maxEntrants = (event.getMaxEntrants() != null) ? event.getMaxEntrants() : 0;
                            int displayedWaitingCount = Math.min(waitingCount, maxEntrants);
                            waitingList.setText("Waiting List: " + displayedWaitingCount + "/" + maxEntrants);
                        } else {
                            waitingList.setText("Waiting List: N/A");
                        }
                    })
                    .addOnFailureListener(e -> waitingList.setText("Waiting List: N/A"));

            manageButton.setOnClickListener(v -> {
                Log.d("EventArrayAdapter", "Manage clicked for " + event.getEventName());
                if (listener != null) {
                    listener.onManageClicked(event);
                }
            });
        }

        return convertView;
    }
}
