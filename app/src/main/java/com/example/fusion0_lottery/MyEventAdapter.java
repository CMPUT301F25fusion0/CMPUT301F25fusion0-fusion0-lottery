package com.example.fusion0_lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * This class ia a RecyclerView adapter class for displaying user's lottery events.
 * It shows waiting list and selected list in horizontal scroll view
 */

public class MyEventAdapter extends RecyclerView.Adapter<MyEventAdapter.MyViewHolder> {
    private List<Event> events;
    private String type;
    private OnEventActionListener listener;

    /**
     * Interface for handling user actions on events in the RecyclerView.
     */

    public interface OnEventActionListener {
        void onViewEvent(String eventId);
        void onLeaveWaitingList(String eventId, int position);
        void onAcceptInvitation(String eventId, int position);
        void onDeclineInvitation(String eventId, int position);
    }

    /**
     * constructs a new MyEventAdapter
     * @param events the list of events to display
     * @param type  the display type: waiting or selected
     * @param listener the listener for handling event actions
     */
    public MyEventAdapter(List<Event> events, String type, OnEventActionListener listener) {
        this.events = events;
        this.type = type;
        this.listener = listener;
    }

    /**
     * creates new viewHolder for recyclerView based on the view type
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return a new ViewHolder that holds a view of the given view type
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout_res = type.equals("waiting") ? R.layout.waiting : R.layout.selected;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout_res, parent, false);
        return new MyViewHolder(view, type);
    }

    /**
     * binds event data to the viewHolder
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Event event = events.get(position);
        String eventName = event.getEventName();
        String drawDate = event.getRegistrationEnd();

        if (type.equals("waiting")) {
            holder.eventName.setText(eventName);
            holder.eventStatus.setText("Status: on waiting list");
            holder.drawDate.setText("Draw: " + drawDate);

            holder.view.setOnClickListener(v -> {
                listener.onViewEvent(event.getEventId());
            });

            holder.leave.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onLeaveWaitingList(event.getEventId(), currentPosition);
                }
            });
        } else if (type.equals("selected")) {
            holder.selectedName.setText(eventName);

            holder.accept.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onAcceptInvitation(event.getEventId(), currentPosition);
                }
            });

            holder.decline.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onDeclineInvitation(event.getEventId(), currentPosition);
                }
            });
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter
     * @return the total number of items in this adapter
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventStatus, drawDate, selectedName;
        Button accept, decline, view, leave;

        /**
         * constructs a ViewHolder for the event item view based on the type
         * @param itemView the view of the event item
         * @param type the type of the event display (waiting or selected)
         */
        public MyViewHolder(@NonNull View itemView, String type) {
            super(itemView);

            if (type.equals("waiting")) {
                eventName = itemView.findViewById(R.id.event_name);
                eventStatus = itemView.findViewById(R.id.event_status);
                drawDate = itemView.findViewById(R.id.draw_date);
                view = itemView.findViewById(R.id.view);
                leave = itemView.findViewById(R.id.leave);
            } else if (type.equals("selected")) {
                selectedName = itemView.findViewById(R.id.selected_event_name);
                accept = itemView.findViewById(R.id.accept);
                decline = itemView.findViewById(R.id.decline);
            }
        }
    }
}