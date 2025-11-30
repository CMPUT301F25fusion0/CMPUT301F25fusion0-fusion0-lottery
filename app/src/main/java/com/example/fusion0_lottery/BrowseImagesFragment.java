package com.example.fusion0_lottery;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for browsing event posters.
 * Only events with a real poster image (not null, not empty, not "default_poster") are displayed.
 * Admins can select events and remove their posters. After removal, events with removed posters
 * disappear from this view.
 */
public class BrowseImagesFragment extends Fragment {
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private List<String> selected_event_id;
    private Button remove_btn;
    private ImageBrowserAdapter adapter;

    /**
     * Inflates the fragment's layout and initializes UI components.
     * Sets up the RecyclerView and its adapter, loads only events with actual poster images,
     * configures bottom navigation, and sets the click listener for the remove button.
     *
     * @param inflater           The LayoutInflater used to inflate views in the fragment
     * @param container          The parent view that the fragment's UI should attach to
     * @param savedInstanceState Saved state to restore, if any
     * @return The root View of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();
        selected_event_id = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_browse_images, container, false);
        bottomNavigation = view.findViewById(R.id.bottom_navigation);
        recyclerView = view.findViewById(R.id.recycler_images);
        remove_btn = view.findViewById(R.id.remove_btn);
        bottomNavigation.setSelectedItemId(R.id.nav_images);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ImageBrowserAdapter(eventList, selected_event_id, getContext());
        recyclerView.setAdapter(adapter);

        loadOnlyEventsWithPosters();
        setupBottomNavigation();

        remove_btn.setOnClickListener(v -> removeSelectedImages());
        return view;
    }

    /**
     * Loads events from Firestore that have actual poster images.
     * Events with null, empty, or default posters are ignored.
     * Updates the RecyclerView adapter after loading.
     */
    private void loadOnlyEventsWithPosters() {
        eventList.clear();

        db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());

                        if (event.getPosterImage() != null &&
                                !event.getPosterImage().trim().isEmpty() &&
                                !event.getPosterImage().equals("default_poster")) {
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (eventList.isEmpty()) {
                        Toast.makeText(getContext(), "No events with posters found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void removeSelectedImages() {
        if (selected_event_id.isEmpty()) {
            Toast.makeText(getContext(), "No images selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Images")
                .setMessage("Do you want to remove " + selected_event_id.size() + " selected image(s)?")
                .setPositiveButton("Remove", (dialog, which) -> replaceWithDefaultPoster())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Replaces selected poster images in Firestore with "default_poster".
     * Refreshes the event list after completion to remove updated events from view.
     */
    private void replaceWithDefaultPoster() {
        int totalToUpdate = selected_event_id.size();
        int[] updatedCount = {0};

        for (String eventId : selected_event_id) {
            db.collection("Events").document(eventId)
                    .update("posterImage", "default_poster")
                    .addOnSuccessListener(aVoid -> {
                        updatedCount[0]++;
                        if (updatedCount[0] == totalToUpdate) {
                            Toast.makeText(getContext(),
                                    "Removed selected posters successfully",
                                    Toast.LENGTH_SHORT).show();
                            selected_event_id.clear();
                            loadOnlyEventsWithPosters(); // refresh visible list
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Error updating image: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Sets up the bottom navigation view for the fragment.
     * Handles navigation to Profiles, Events, Images, and Logs fragments.
     */
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profiles) {
                navigateToFragment(new BrowseProfileFragment());
                return true;
            } else if (itemId == R.id.nav_events) {
                navigateToFragment(new BrowseEventsFragment());
                return true;
            } else if (itemId == R.id.nav_images) {
                return true;
            } else if (itemId == R.id.nav_logs) {
                navigateToFragment(new BrowseNotificationsFragment());
                return true;
            }
            return false;
        });
    }

    /**
     * Replaces the current fragment with the given fragment.
     *
     * @param fragment The fragment to navigate to.
     */
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