package com.example.fusion0_lottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * An array adapter helper class
 * Code for setting EventArrayAdapter was heavily taken inspiration from Lab 5 CityArrayAdapters
 * Code for lines 52 - 59 was taken inspiration from GeeksForGeeks,
 * https://www.geeksforgeeks.org/java/compare-dates-in-java/, Last Updated July 11 2025,
 * Accessed Nov 4 2025
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {
    private ArrayList<Event> events;
    private Context context;

    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.event_layout, parent, false);
        }

        Event event = events.get(position);
        TextView eventName = view.findViewById(R.id.eventName);
        TextView eventInterests = view.findViewById(R.id.eventInterests);
        TextView eventStatus = view.findViewById(R.id.status);
        TextView eventDrawDate = view.findViewById(R.id.drawDate);

        eventName.setText(event.getEventName());
        eventInterests.setText("Interests: " + event.getInterests());

        // ---- New: safe handling for registrationEnd ----
        String regEndStr = event.getRegistrationEnd();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (regEndStr == null || regEndStr.trim().isEmpty()) {
            // No registration end date -> treat as closed + no draw date
            eventStatus.setText("Status: Closed");
            eventDrawDate.setText("Draw Date: TBA");
        } else {
            try {
                // Strip time from "today" and compare by date only
                Date today = sdf.parse(sdf.format(new Date()));      // e.g. "2025-11-07"
                Date eventRegEnd = sdf.parse(regEndStr);

                if (today != null && eventRegEnd != null && !today.after(eventRegEnd)) {
                    // today <= eventRegEnd  -> Open
                    eventStatus.setText("Status: Open");
                } else {
                    // today > eventRegEnd   -> Closed
                    eventStatus.setText("Status: Closed");
                }

                eventDrawDate.setText("Draw Date: " + regEndStr);
            } catch (ParseException e) {
                // If the stored date is malformed, don’t crash the app
                e.printStackTrace();
                eventStatus.setText("Status: Closed");
                eventDrawDate.setText("Draw Date: Invalid");
            }
        }

        return view;
    }

}
