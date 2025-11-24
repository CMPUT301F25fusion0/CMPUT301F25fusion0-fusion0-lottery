package com.example.fusion0_lottery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

/**
 * Adapter for displaying event images in RecyclerView with selection capability
 */
public class ImageBrowserAdapter extends RecyclerView.Adapter<ImageBrowserAdapter.ImageViewHolder> {

    private List<Event> events;
    private List<String> selectedEventIds;
    private Context context;

    /**
     * Constructor for ImageBrowseAdapter
     * @param events list of events with poster images
     * @param selectedEventIds list of selected event IDs for deletion
     * @param context context for loading images
     */
    public ImageBrowserAdapter(List<Event> events, List<String> selectedEventIds, Context context) {
        this.events = events;
        this.selectedEventIds = selectedEventIds;
        this.context = context;
    }

    /**
     * Creates new ViewHolder for RecyclerView
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return a new ImageViewHolder
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_browse, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder
     * @param holder The ViewHolder to update
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Event event = events.get(position);


        holder.eventName.setText(event.getEventName() != null ? event.getEventName() : "No Name");

        String organizerText = "Organizer: " +
                (event.getOrganizerName() != null ? event.getOrganizerName() : "Unknown");
        holder.organizer_name.setText(organizerText);

        if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.poster.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.poster.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.poster.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.checkbox.setChecked(selectedEventIds.contains(event.getEventId()));

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedEventIds.contains(event.getEventId())) {
                    selectedEventIds.add(event.getEventId());
                }
            } else {
                selectedEventIds.remove(event.getEventId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            holder.checkbox.setChecked(!holder.checkbox.isChecked());
        });
    }

    /**
     * Returns the total number of items in the data set
     * @return the total number of items
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder for image browse items
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView eventName;
        TextView organizer_name;
        CheckBox checkbox;
        /**
         * Constructor for ImageViewHolder
         * @param itemView the view of the image item
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            eventName = itemView.findViewById(R.id.event_name);
            organizer_name = itemView.findViewById(R.id.organizer_name);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}