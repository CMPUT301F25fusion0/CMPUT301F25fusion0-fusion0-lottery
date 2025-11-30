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

/**
 * RecyclerView Adapter for displaying a list of events.
 * <p>
 * Shows event name, start/end dates, and status (Active/Inactive based on today's date).
 */
public class BrowseEventAdapter extends RecyclerView.Adapter<BrowseEventAdapter.ViewHolder> {

    private List<Event> events;
    private final OnEventClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Interface to handle click events on a single event item.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event is clicked.
         *
         * @param event The clicked event object.
         */
        void onEventClicked(Event event);
    }

    /**
     * Constructor for the adapter.
     *
     * @param events   List of events to display.
     * @param listener Listener for event click interactions.
     */
    public BrowseEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Updates the adapter's event list and refreshes the RecyclerView.
     *
     * @param filteredList The new filtered list of events.
     */
    public void updateList(List<Event> filteredList) {
        this.events = filteredList;
        notifyDataSetChanged();
    }

    /**
     * Inflates the view for a single RecyclerView item.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The type of view (unused).
     * @return A new ViewHolder containing the inflated view.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds event data to a ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position Position of the event in the list.
     */
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
     * Determines if an event is active or inactive.
     * <p>
     * An event is considered active if today's date is before or equal to its end date.
     *
     * @param event The event to check.
     * @return True if active, false if inactive.
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


    /**
     * Returns the total number of events in the adapter.
     *
     * @return Number of events.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder class for a single event item.
     * <p>
     * Holds references to TextViews for event name, date, and status.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDate, eventStatus;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The item view representing a single event.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDate = itemView.findViewById(R.id.event_date);
            eventStatus = itemView.findViewById(R.id.event_status);
        }
    }
}
