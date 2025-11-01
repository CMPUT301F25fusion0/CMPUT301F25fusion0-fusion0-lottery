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


import java.util.ArrayList;


/**
 * An array adapter helper class
 * Code was heavily taken inspiration from Lab 5 CityArrayAdapters
 * NOT FINISHED
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

        eventName.setText(event.getEventName());

        return view;
    }
}
