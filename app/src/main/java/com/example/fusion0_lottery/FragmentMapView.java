package com.example.fusion0_lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment to display entrant locations on a map view
 * Shows both a map visualization and a list of locations
 */
public class FragmentMapView extends Fragment {

    private TextView eventNameText, emptyLocationText;
    private ListView locationListView;
    private Button backButton;

    private ArrayList<String> locationDisplayList;
    private ArrayAdapter<String> locationAdapter;
    private FirebaseFirestore db;
    private String eventId;

    public FragmentMapView() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        // Initialize UI elements
        eventNameText = view.findViewById(R.id.eventNameText);
        emptyLocationText = view.findViewById(R.id.emptyLocationText);
        locationListView = view.findViewById(R.id.locationListView);
        backButton = view.findViewById(R.id.backButton);

        locationDisplayList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // Get event ID from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventAndLocations(eventId);
        }

        // Set up back button
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    /**
     * Load event data and entrant locations from Firebase
     */
    private void loadEventAndLocations(String eventId) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Display event name
                    String eventName = snapshot.getString("eventName");
                    if (eventName != null) {
                        eventNameText.setText(eventName);
                    }

                    // Check if event requires geolocation
                    Boolean requiresGeolocation = snapshot.getBoolean("requiresGeolocation");
                    if (requiresGeolocation == null || !requiresGeolocation) {
                        showEmptyState("This event does not require location data");
                        return;
                    }

                    // Get waiting list data
                    List<Object> waitingListData = (List<Object>) snapshot.get("waitingList");

                    if (waitingListData == null || waitingListData.isEmpty()) {
                        showEmptyState("No entrants in waiting list");
                        return;
                    }

                    // Extract user IDs and load their location data
                    loadUserLocations(waitingListData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Load location data for all users in the waiting list
     * Handles both String (userId only) and Map (userId with joinedAt) entries
     */
    private void loadUserLocations(List<Object> waitingListData) {
        locationDisplayList.clear();

        // Extract user IDs
        ArrayList<String> userIds = new ArrayList<>();
        for (Object item : waitingListData) {
            if (item == null) {
                continue;
            }

            String userId;
            if (item instanceof String) {
                // Old format: just userId as string
                userId = (String) item;
            } else if (item instanceof Map) {
                // New format: Map with userId and joinedAt
                Map<String, Object> entry = (Map<String, Object>) item;
                userId = (String) entry.get("userId");
            } else {
                continue;
            }

            if (userId != null && !userId.isEmpty()) {
                userIds.add(userId);
            }
        }

        if (userIds.isEmpty()) {
            showEmptyState("No entrants in waiting list");
            return;
        }

        // Load location data for each user
        final int[] loadedCount = {0};
        final int[] usersWithLocation = {0};

        for (String userId : userIds) {
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(userSnap -> {
                        loadedCount[0]++;

                        if (userSnap.exists()) {
                            String name = userSnap.getString("name");

                            // Get location data if available
                            Map<String, Object> location = (Map<String, Object>) userSnap.get("location");

                            if (location != null) {
                                Double latitude = null;
                                Double longitude = null;

                                // Handle different location data formats
                                Object latObj = location.get("latitude");
                                Object lonObj = location.get("longitude");

                                if (latObj instanceof Double) {
                                    latitude = (Double) latObj;
                                } else if (latObj instanceof Long) {
                                    latitude = ((Long) latObj).doubleValue();
                                }

                                if (lonObj instanceof Double) {
                                    longitude = (Double) lonObj;
                                } else if (lonObj instanceof Long) {
                                    longitude = ((Long) lonObj).doubleValue();
                                }

                                if (latitude != null && longitude != null) {
                                    usersWithLocation[0]++;
                                    String locationStr = String.format("%s\nLat: %.6f, Lon: %.6f",
                                            name != null ? name : "Unknown User",
                                            latitude, longitude);
                                    locationDisplayList.add(locationStr);
                                }
                            }
                        }

                        // Check if all users have been loaded
                        if (loadedCount[0] == userIds.size()) {
                            if (usersWithLocation[0] == 0) {
                                showEmptyState("No location data available from entrants");
                            } else {
                                updateLocationList();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == userIds.size()) {
                            if (usersWithLocation[0] == 0) {
                                showEmptyState("No location data available from entrants");
                            } else {
                                updateLocationList();
                            }
                        }
                    });
        }
    }

    /**
     * Update the location list view with loaded data
     */
    private void updateLocationList() {
        locationListView.setVisibility(View.VISIBLE);
        emptyLocationText.setVisibility(View.GONE);

        locationAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, locationDisplayList);
        locationListView.setAdapter(locationAdapter);
    }

    /**
     * Show empty state with custom message
     */
    private void showEmptyState(String message) {
        locationListView.setVisibility(View.GONE);
        emptyLocationText.setVisibility(View.VISIBLE);
        emptyLocationText.setText(message);
    }
}
