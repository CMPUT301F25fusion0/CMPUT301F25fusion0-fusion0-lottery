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
 * Adapter for displaying a list of events with their poster images in a RecyclerView.
 * <p>
 * Shows event details including name, start date, time, description, and a poster image.
 * Users can select events using a checkbox. Default posters disable checkbox selection.
 */
public class ImageBrowserAdapter extends RecyclerView.Adapter<ImageBrowserAdapter.ImageViewHolder> {

    private List<Event> events;
    private List<String> selectedEventIds;
    private Context context;

    /**
     * Constructor for the adapter.
     *
     * @param events           List of events to display
     * @param selectedEventIds List of event IDs that are currently selected
     * @param context          Context for inflating views
     */
    public ImageBrowserAdapter(List<Event> events, List<String> selectedEventIds, Context context) {
        this.events = events;
        this.selectedEventIds = selectedEventIds;
        this.context = context;
    }

    /**
     * Inflates the view for each RecyclerView item.
     *
     * @param parent   The parent ViewGroup
     * @param viewType Type of the view
     * @return A new ImageViewHolder
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_browse, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds event data to the item view.
     * <p>
     * Displays the poster, event name, start date, time, description, and checkbox selection.
     *
     * @param holder   The ViewHolder for the item
     * @param position Position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Event event = events.get(position);

        // Set event details
        holder.eventName.setText(event.getEventName() != null ? event.getEventName() : "No Name");
        holder.startDate.setText("Start: " + (event.getStartDate() != null ? event.getStartDate() : "Unknown"));
        holder.time.setText("Time: " + (event.getTime() != null ? event.getTime() : "Unknown"));
        holder.description.setText(event.getDescription() != null ? event.getDescription() : "");

        // Determine if default poster should be displayed
        boolean isDefaultPoster = false;
        if (event.getPosterImage() == null || event.getPosterImage().isEmpty() ||
                event.getPosterImage().equals("default_poster")) {
            holder.poster.setImageResource(R.drawable.default_poster);
            isDefaultPoster = true;
        } else {
            try {
                byte[] decodedBytes = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.poster.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.poster.setImageResource(R.drawable.default_poster);
                isDefaultPoster = true;
            }
        }

        // Checkbox handling
        holder.checkbox.setChecked(selectedEventIds.contains(event.getEventId()));
        holder.checkbox.setEnabled(!isDefaultPoster); // Disable if default poster

        final boolean finalIsDefaultPoster = isDefaultPoster; // Needed for lambda
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!finalIsDefaultPoster) {
                if (isChecked) {
                    if (!selectedEventIds.contains(event.getEventId())) {
                        selectedEventIds.add(event.getEventId());
                    }
                } else {
                    selectedEventIds.remove(event.getEventId());
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (!finalIsDefaultPoster) {
                holder.checkbox.setChecked(!holder.checkbox.isChecked());
            }
        });
    }

    /**
     * Returns the number of events in the list.
     *
     * @return Total item count
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder class for event items.
     * <p>
     * Holds references to views for the poster image, event name, date, time, description, and checkbox.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView eventName, startDate, time, description;
        CheckBox checkbox;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            eventName = itemView.findViewById(R.id.event_name);
            startDate = itemView.findViewById(R.id.start_date);
            time = itemView.findViewById(R.id.time);
            description = itemView.findViewById(R.id.description);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}
