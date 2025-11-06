package com.example.fusion0_lottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.fusion0_lottery.R;


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
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.event_layout, parent, false);
        }

        Event event = events.get(position);
        TextView eventName = view.findViewById(R.id.eventName);
        TextView eventStatus = view.findViewById(R.id.status);
        TextView eventDrawDate = view.findViewById(R.id.drawDate);

        eventName.setText(event.getEventName());


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date today = sdf.parse(sdf.format(new Date()));
            Date eventRegEnd = sdf.parse(event.getRegistrationEnd());
            assert today != null;
            if (today.before(eventRegEnd)) {
                eventStatus.setText("Status: Open");
            }
            else {
                eventStatus.setText("Status: Closed");
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        eventDrawDate.setText("Draw Date: " + event.getRegistrationEnd());




        return view;
    }
}
