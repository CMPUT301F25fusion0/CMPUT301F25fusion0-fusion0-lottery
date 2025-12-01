package com.example.fusion0_lottery;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BrowseNotificationsFragment extends Fragment implements NotificationLogAdapter.OnNotificationLogActionListener {

    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private NotificationLogAdapter adapter;
    private List<NotificationLog> notificationLogs;
    private EditText searchBar;
    private Button filterOrganizerButton;
    private Button filterEventButton;
    private Button exportCsvButton;
    private Button clearAllButton;
    private TextView emptyStateText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_notifications, container, false);

        // Initialize views
        bottomNavigation = view.findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_logs);
        recyclerView = view.findViewById(R.id.notification_logs_recycler);
        searchBar = view.findViewById(R.id.search_bar);
        filterOrganizerButton = view.findViewById(R.id.filter_organizer_button);
        filterEventButton = view.findViewById(R.id.filter_event_button);
        exportCsvButton = view.findViewById(R.id.export_csv_button);
        clearAllButton = view.findViewById(R.id.clear_all_button);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        notificationLogs = new ArrayList<>();
        adapter = new NotificationLogAdapter(notificationLogs, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup listeners
        setupSearchBar();
        setupFilterButtons();
        setupExportButton();
        setupClearAllButton();
        bottom_navigation();

        // Load notification logs
        loadNotificationLogs();

        return view;
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterButtons() {
        filterOrganizerButton.setOnClickListener(v -> showOrganizerFilterDialog());
        filterEventButton.setOnClickListener(v -> showEventFilterDialog());
    }

    private void setupExportButton() {
        exportCsvButton.setOnClickListener(v -> exportToCsv());
    }

    private void setupClearAllButton() {
        clearAllButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Clear All Notification Logs")
                    .setMessage("Are you sure you want to delete all notification logs? This action cannot be undone.")
                    .setPositiveButton("Yes, Clear All", (dialog, which) -> clearAllLogs())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadNotificationLogs() {
        android.util.Log.d("BrowseNotifications", "Loading notification logs from Firestore...");
        db.collection("NotificationLogs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("BrowseNotifications", "Successfully loaded " + queryDocumentSnapshots.size() + " notification logs");
                    notificationLogs.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        NotificationLog log = new NotificationLog();
                        log.setLogId(doc.getId());
                        log.setOrganizerId(doc.getString("organizerId"));
                        log.setOrganizerName(doc.getString("organizerName"));
                        log.setRecipientId(doc.getString("recipientId"));
                        log.setRecipientName(doc.getString("recipientName"));
                        log.setEventId(doc.getString("eventId"));
                        log.setEventName(doc.getString("eventName"));
                        log.setMessageType(doc.getString("type"));
                        log.setMessageBody(doc.getString("body"));
                        log.setTitle(doc.getString("title"));

                        Long timestamp = doc.getLong("timestamp");
                        if (timestamp != null) {
                            log.setTimestamp(timestamp);
                        }

                        notificationLogs.add(log);
                        android.util.Log.d("BrowseNotifications", "Added log: " + log.getOrganizerName() + " -> " + log.getRecipientName());
                    }

                    // Sort by timestamp in descending order (newest first)
                    notificationLogs.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                    adapter.updateList(notificationLogs);
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("BrowseNotifications", "Failed to load notification logs", e);
                    Toast.makeText(getContext(), "Failed to load notification logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void showOrganizerFilterDialog() {
        // Get unique organizer names
        Set<String> organizerNames = new HashSet<>();
        organizerNames.add("All Organizers");
        for (NotificationLog log : notificationLogs) {
            if (log.getOrganizerName() != null) {
                organizerNames.add(log.getOrganizerName());
            }
        }

        String[] organizerArray = organizerNames.toArray(new String[0]);

        new AlertDialog.Builder(getContext())
                .setTitle("Filter by Organizer")
                .setItems(organizerArray, (dialog, which) -> {
                    String selectedOrganizer = organizerArray[which];
                    adapter.filterByOrganizer(selectedOrganizer);
                    updateEmptyState();
                    Toast.makeText(getContext(), "Filtered by: " + selectedOrganizer, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showEventFilterDialog() {
        // Get unique event names
        Set<String> eventNames = new HashSet<>();
        eventNames.add("All Events");
        for (NotificationLog log : notificationLogs) {
            if (log.getEventName() != null) {
                eventNames.add(log.getEventName());
            }
        }

        String[] eventArray = eventNames.toArray(new String[0]);

        new AlertDialog.Builder(getContext())
                .setTitle("Filter by Event")
                .setItems(eventArray, (dialog, which) -> {
                    String selectedEvent = eventArray[which];
                    adapter.filterByEvent(selectedEvent);
                    updateEmptyState();
                    Toast.makeText(getContext(), "Filtered by: " + selectedEvent, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void exportToCsv() {
        try {
            List<NotificationLog> logsToExport = adapter.getFilteredLogs();

            if (logsToExport.isEmpty()) {
                Toast.makeText(getContext(), "No logs to export", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create CSV file
            String fileName = "notification_logs_" + System.currentTimeMillis() + ".csv";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);

            FileWriter writer = new FileWriter(csvFile);

            // Write CSV header
            writer.append("Date/Time,Organizer Name,Recipient Name,Event Name,Message Type,Message Body\n");

            // Write data
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (NotificationLog log : logsToExport) {
                writer.append(escapeCsv(sdf.format(new Date(log.getTimestamp())))).append(",");
                writer.append(escapeCsv(log.getOrganizerName())).append(",");
                writer.append(escapeCsv(log.getRecipientName())).append(",");
                writer.append(escapeCsv(log.getEventName())).append(",");
                writer.append(escapeCsv(log.getMessageType())).append(",");
                writer.append(escapeCsv(log.getMessageBody())).append("\n");
            }

            writer.flush();
            writer.close();

            // Show success message and offer to open file
            new AlertDialog.Builder(getContext())
                    .setTitle("Export Successful")
                    .setMessage("Notification logs exported to:\n" + csvFile.getAbsolutePath())
                    .setPositiveButton("Open", (dialog, which) -> openCsvFile(csvFile))
                    .setNegativeButton("OK", null)
                    .show();

        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to export CSV: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void openCsvFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(getContext(),
                    getContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/csv");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open CSV with"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No app found to open CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAllLogs() {
        db.collection("NotificationLogs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    notificationLogs.clear();
                    adapter.updateList(notificationLogs);
                    updateEmptyState();
                    Toast.makeText(getContext(), "All notification logs cleared", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to clear logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNotificationClicked(NotificationLog log) {
        // Show detailed notification info in dialog
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String details = "Date/Time: " + sdf.format(new Date(log.getTimestamp())) + "\n\n" +
                "Organizer: " + (log.getOrganizerName() != null ? log.getOrganizerName() : "Unknown") + "\n" +
                "Recipient: " + (log.getRecipientName() != null ? log.getRecipientName() : "Unknown") + "\n" +
                "Event: " + (log.getEventName() != null ? log.getEventName() : "Unknown") + "\n" +
                "Type: " + (log.getMessageType() != null ? log.getMessageType() : "notification") + "\n\n" +
                "Message:\n" + (log.getMessageBody() != null ? log.getMessageBody() : "");

        new AlertDialog.Builder(getContext())
                .setTitle("Notification Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .setNegativeButton("Delete", (dialog, which) -> onDeleteClicked(log))
                .show();
    }

    @Override
    public void onDeleteClicked(NotificationLog log) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Notification Log")
                .setMessage("Are you sure you want to delete this notification log?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("NotificationLogs").document(log.getLogId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                notificationLogs.remove(log);
                                adapter.updateList(notificationLogs);
                                updateEmptyState();
                                Toast.makeText(getContext(), "Notification log deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void bottom_navigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profiles) {
                navigateToFragment(new BrowseProfileFragment());
                return true;
            } else if (itemId == R.id.nav_events) {
                navigateToFragment(new BrowseEventsFragment());
                return true;
            } else if (itemId == R.id.nav_images) {
                navigateToFragment(new BrowseImagesFragment());
                return true;
            } else if (itemId == R.id.nav_logs) {
                return true;
            }
            return false;
        });
    }

    private void navigateToFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}