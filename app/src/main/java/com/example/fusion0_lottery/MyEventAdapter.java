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

    public MyEventAdapter(List<Event> events, String type){
        this.events = events;
        this.type = type;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout_res;
        if (type.equals("waiting")) {
            layout_res = R.layout.waiting;
        } else {
            layout_res = R.layout.selected;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layout_res, parent, false);
        return new MyViewHolder(view, type);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Event event = events.get(position);

        String eventName = event.getEventName();
        String drawDate = event.getRegistrationEnd();

        if (type.equals("waiting")){
            holder.eventName.setText(eventName);
            holder.eventStatus.setText("Status: on waiting list");
            holder.drawDate.setText("Draw: "+ drawDate);
        } else if (type.equals("selected")) {
            holder.selectedName.setText(eventName);
            holder.accept.setOnClickListener(v->{

            });
            holder.decline.setOnClickListener(v->{

            });
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventStatus, drawDate, selectedName;
        Button accept, decline;

        public MyViewHolder(@NonNull View itemView, String type) {
            super(itemView);

            if (type.equals("waiting")) {
                eventName = itemView.findViewById(R.id.event_name);
                eventStatus = itemView.findViewById(R.id.event_status);
                drawDate = itemView.findViewById(R.id.draw_date);
            } else if (type.equals("selected")) {
                selectedName = itemView.findViewById(R.id.selected_event_name);
                accept = itemView.findViewById(R.id.accept);
                decline = itemView.findViewById(R.id.decline);
            }
        }
    }
}
