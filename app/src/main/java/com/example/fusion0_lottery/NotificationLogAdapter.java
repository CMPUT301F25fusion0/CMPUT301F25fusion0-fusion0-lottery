package com.example.fusion0_lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying notification logs in admin panel
 */
public class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.NotificationViewHolder> {

    private List<NotificationLog> notificationLogs;
    private List<NotificationLog> notificationLogsFiltered;
    private OnNotificationLogActionListener listener;

    public interface OnNotificationLogActionListener {
        void onNotificationClicked(NotificationLog log);
        void onDeleteClicked(NotificationLog log);
    }

    public NotificationLogAdapter(List<NotificationLog> notificationLogs, OnNotificationLogActionListener listener) {
        this.notificationLogs = notificationLogs;
        this.notificationLogsFiltered = new ArrayList<>(notificationLogs);
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationLog log = notificationLogsFiltered.get(position);

        // Format date/time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateTime = sdf.format(new Date(log.getTimestamp()));
        holder.dateTextView.setText("Date: " + dateTime);

        // Set organizer name
        String organizerName = log.getOrganizerName() != null ? log.getOrganizerName() : "Unknown";
        holder.organizerTextView.setText("Organizer: " + organizerName);

        // Set recipient name
        String recipientName = log.getRecipientName() != null ? log.getRecipientName() : "Unknown";
        holder.recipientTextView.setText("Recipient: " + recipientName);

        // Set event name
        String eventName = log.getEventName() != null ? log.getEventName() : "Unknown Event";
        holder.eventNameTextView.setText("Event: " + eventName);

        // Set message type
        String messageType = log.getMessageType() != null ? log.getMessageType() : "notification";
        holder.messageTypeTextView.setText("Type: " + messageType);

        // Set message preview
        String messageBody = log.getMessageBody() != null ? log.getMessageBody() : "";
        holder.messagePreviewTextView.setText("Message: " + messageBody);

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClicked(log);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(log);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationLogsFiltered.size();
    }

    public void updateList(List<NotificationLog> newLogs) {
        this.notificationLogs = newLogs;
        this.notificationLogsFiltered = new ArrayList<>(newLogs);
        notifyDataSetChanged();
    }

    /**
     * Filter the notification logs based on search query
     * @param query Search query (searches in organizer name, recipient name, event name, message)
     */
    public void filter(String query) {
        notificationLogsFiltered.clear();
        if (query == null || query.isEmpty()) {
            notificationLogsFiltered.addAll(notificationLogs);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (NotificationLog log : notificationLogs) {
                if ((log.getOrganizerName() != null && log.getOrganizerName().toLowerCase().contains(lowerCaseQuery)) ||
                    (log.getRecipientName() != null && log.getRecipientName().toLowerCase().contains(lowerCaseQuery)) ||
                    (log.getEventName() != null && log.getEventName().toLowerCase().contains(lowerCaseQuery)) ||
                    (log.getMessageBody() != null && log.getMessageBody().toLowerCase().contains(lowerCaseQuery)) ||
                    (log.getMessageType() != null && log.getMessageType().toLowerCase().contains(lowerCaseQuery))) {
                    notificationLogsFiltered.add(log);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Filter by organizer name
     */
    public void filterByOrganizer(String organizerName) {
        notificationLogsFiltered.clear();
        if (organizerName == null || organizerName.isEmpty() || organizerName.equals("All Organizers")) {
            notificationLogsFiltered.addAll(notificationLogs);
        } else {
            for (NotificationLog log : notificationLogs) {
                if (log.getOrganizerName() != null && log.getOrganizerName().equals(organizerName)) {
                    notificationLogsFiltered.add(log);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Filter by event name
     */
    public void filterByEvent(String eventName) {
        notificationLogsFiltered.clear();
        if (eventName == null || eventName.isEmpty() || eventName.equals("All Events")) {
            notificationLogsFiltered.addAll(notificationLogs);
        } else {
            for (NotificationLog log : notificationLogs) {
                if (log.getEventName() != null && log.getEventName().equals(eventName)) {
                    notificationLogsFiltered.add(log);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Filter by date range
     */
    public void filterByDateRange(long startDate, long endDate) {
        notificationLogsFiltered.clear();
        for (NotificationLog log : notificationLogs) {
            if (log.getTimestamp() >= startDate && log.getTimestamp() <= endDate) {
                notificationLogsFiltered.add(log);
            }
        }
        notifyDataSetChanged();
    }

    public List<NotificationLog> getFilteredLogs() {
        return new ArrayList<>(notificationLogsFiltered);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView organizerTextView;
        TextView recipientTextView;
        TextView eventNameTextView;
        TextView messageTypeTextView;
        TextView messagePreviewTextView;
        ImageButton deleteButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.notification_date);
            organizerTextView = itemView.findViewById(R.id.organizer_name);
            recipientTextView = itemView.findViewById(R.id.recipient_name);
            eventNameTextView = itemView.findViewById(R.id.event_name);
            messageTypeTextView = itemView.findViewById(R.id.message_type);
            messagePreviewTextView = itemView.findViewById(R.id.message_preview);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
