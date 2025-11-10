package com.example.fusion0_lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyEventAdapter extends RecyclerView.Adapter<MyEventAdapter.MyViewHolder> {
    private List<Event> events;
    private String type;
    private OnEventActionListener listener;

    public interface OnEventActionListener {
        void onViewEvent(String eventId);
        void onLeaveWaitingList(String eventId, int position);
        void onAcceptInvitation(String eventId, int position);
        void onDeclineInvitation(String eventId, int position);
    }

    public MyEventAdapter(List<Event> events, String type, OnEventActionListener listener) {
        this.events = events;
        this.type = type;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout_res = type.equals("waiting") ? R.layout.waiting : R.layout.selected;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout_res, parent, false);
        return new MyViewHolder(view, type);
    }

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

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventStatus, drawDate, selectedName;
        Button accept, decline, view, leave;

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