package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment to display and manage all events.
 * <p>
 * Users can view event details, filter events by status (active/inactive),
 * and remove events. Supports bottom navigation to other app sections.
 */
public class BrowseEventsFragment extends Fragment {

    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private BrowseEventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private Button buttonFilter;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Inflates the fragment layout, initializes RecyclerView, bottom navigation, filter button,
     * and loads all events from Firestore.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_events, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.eventsContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BrowseEventAdapter(eventList, this::showEventDetailsDialog);
        recyclerView.setAdapter(adapter);

        buttonFilter = view.findViewById(R.id.button_filter);
        buttonFilter.setOnClickListener(v -> showFilterDialog());

        bottomNavigation = view.findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_events);
        setupBottomNavigation();

        loadEvents();

        return view;
    }

    /** Loads all events from Firestore and updates the RecyclerView. */
    private void loadEvents() {
        db.collection("Events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            eventList.add(event);
                        }
                    }
                    adapter.updateList(eventList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Shows an AlertDialog with full details of a selected event.
     *
     * @param event Event whose details are displayed.
     */
    private void showEventDetailsDialog(Event event) {
        String message = "Description: " + (event.getDescription() != null ? event.getDescription() : "N/A") +
                "\n\nLocation: " + (event.getLocation() != null ? event.getLocation() : "N/A") +
                "\n\nStart Date: " + event.getStartDate() +
                "\nEnd Date: " + event.getEndDate() +
                "\n\nRegistration: " + event.getRegistrationStart() + " - " + event.getRegistrationEnd() +
                "\n\nMax Entrants: " + (event.getMaxEntrants() != null ? event.getMaxEntrants() : 0) +
                "\nNumber of Winners: " + (event.getNumberOfWinners() != null ? event.getNumberOfWinners() : 0) +
                "\n\nPrice: $" + event.getPrice() +
                "\nInterests: " + (event.getInterests() != null ? event.getInterests() : "N/A");

        new AlertDialog.Builder(requireContext())
                .setTitle(event.getEventName())
                .setMessage(message)
                .setPositiveButton("Remove", (dialogue, which) -> confirmDeleteEvent(event))
                .setNegativeButton("Exit", null)
                .show();
    }

    /** Shows a confirmation dialog before removing an event. */
    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Event?")
                .setMessage("Are you sure you want to remove this event?")
                .setPositiveButton("Remove", (dialog, which) -> removeEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the selected event from Firestore and updates the RecyclerView.
     *
     * @param event Event to remove.
     */
    private void removeEvent(Event event) {
        db.collection("Events").document(event.getEventId()).delete()
                .addOnSuccessListener(aVoid -> {
                    eventList.remove(event);
                    adapter.updateList(eventList);
                    Toast.makeText(requireContext(), "Event removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Shows a filter dialog for displaying all, active, or inactive events. */
    private void showFilterDialog() {
        String[] options = {"All Events", "Active Events", "Inactive Events"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter Events")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // All Events
                        adapter.updateList(eventList);
                    } else if (which == 1) { // Active
                        adapter.updateList(getFilteredList(true));
                    } else if (which == 2) { // Inactive
                        adapter.updateList(getFilteredList(false));
                    }
                })
                .show();
    }

    /**
     * Returns a filtered list of events based on active/inactive status.
     *
     * @param active true for active events, false for inactive
     * @return List of filtered events
     */
    private List<Event> getFilteredList(boolean active) {
        List<Event> filtered = new ArrayList<>();
        Date today = new Date();

        for (Event e : eventList) {
            try {
                Date endDate = dateFormat.parse(e.getEndDate());
                if (endDate != null) {
                    boolean isActive = !today.after(endDate); // active if today <= endDate
                    if (isActive == active) filtered.add(e);
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        return filtered;
    }

    /** Sets up bottom navigation and handles fragment switching. */
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profiles) {
                navigateToFragment(new BrowseProfileFragment());
                return true;
            } else if (itemId == R.id.nav_events) {
                return true;
            } else if (itemId == R.id.nav_images) {
                navigateToFragment(new BrowseImagesFragment());
                return true;
            } else if (itemId == R.id.nav_logs) {
                navigateToFragment(new BrowseNotificationsFragment());
                return true;
            }
            return false;
        });
    }

    /** Replaces the current fragment with the given fragment. */
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