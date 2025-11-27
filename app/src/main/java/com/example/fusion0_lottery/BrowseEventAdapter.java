package com.example.fusion0_lottery;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BrowseEventAdapter extends RecyclerView.Adapter<BrowseEventAdapter.ViewHolder> {

    private List<Event> events;
    private final OnEventClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface OnEventClickListener {
        void onEventClicked(Event event);
    }

    public BrowseEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public void updateList(List<Event> filteredList) {
        this.events = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText("Date: " + event.getStartDate() + " - " + event.getEndDate());

        // Determine active or inactive based on current date vs endDate
        if (isActive(event)) {
            holder.eventStatus.setText("Status: Active");
            holder.eventStatus.setTextColor(Color.parseColor("#2E7D32")); // green
        } else {
            holder.eventStatus.setText("Status: Inactive");
            holder.eventStatus.setTextColor(Color.parseColor("#C62828")); // red
        }

        holder.itemView.setOnClickListener(v -> listener.onEventClicked(event));
    }

    /**
     * Event is "Active" if today's date is <= endDate, otherwise "Inactive"
     */
    private boolean isActive(Event event) {
        try {
            Date today = new Date();
            Date endDate = dateFormat.parse(event.getEndDate());
            return endDate != null && !today.after(endDate);  // active if today <= endDate
        } catch (ParseException e) {
            e.printStackTrace();
            return true; // default to active if parsing fails
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDate, eventStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDate = itemView.findViewById(R.id.event_date);
            eventStatus = itemView.findViewById(R.id.event_status);
        }
    }
}
