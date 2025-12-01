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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment to display entrant locations on a map view
 * Shows both a map visualization and a list of locations
 */
public class FragmentMapView extends Fragment implements OnMapReadyCallback {

    private TextView eventNameText, emptyLocationText;
    private ListView locationListView;
    private Button backButton;
    private MapView mapView;
    private GoogleMap googleMap;

    private ArrayList<String> locationDisplayList;
    private ArrayList<LatLng> locationCoordinates;
    private ArrayList<String> locationNames;
    private ArrayAdapter<String> locationAdapter;
    private FirebaseFirestore db;
    private String eventId;

    /** Default constructor */
    public FragmentMapView() {}

    /**
     * Inflate fragment layout, initialize UI and MapView, and load event data.
     *
     * @param inflater LayoutInflater to inflate XML
     * @param container Parent view group
     * @param savedInstanceState Saved state bundle
     * @return The root view of the fragment
     */
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
        locationCoordinates = new ArrayList<>();
        locationNames = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // Initialize MapView
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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
     * Callback when the Google Map is ready.
     * Configures map UI settings and adds markers if locations are loaded.
     *
     * @param map The GoogleMap instance
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Configure map settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);

        // Add markers if locations are already loaded
        if (!locationCoordinates.isEmpty()) {
            addMarkersToMap();
        }
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
        locationCoordinates.clear();
        locationNames.clear();

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
                                    String userName = name != null ? name : "Unknown User";
                                    String locationStr = String.format("%s\nLat: %.6f, Lon: %.6f",
                                            userName, latitude, longitude);

                                    locationDisplayList.add(locationStr);
                                    locationCoordinates.add(new LatLng(latitude, longitude));
                                    locationNames.add(userName);
                                }
                            }
                        }

                        // Check if all users have been loaded
                        if (loadedCount[0] == userIds.size()) {
                            if (usersWithLocation[0] == 0) {
                                showEmptyState("No location data available from entrants");
                            } else {
                                updateLocationList();
                                addMarkersToMap();
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
                                addMarkersToMap();
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
     * Show empty state message if no locations are available
     *
     * @param message Message to display
     */
    private void showEmptyState(String message) {
        locationListView.setVisibility(View.GONE);
        emptyLocationText.setVisibility(View.VISIBLE);
        emptyLocationText.setText(message);
    }

    /**
     * Add markers to the Google Map for all loaded locations
     */
    private void addMarkersToMap() {
        if (googleMap == null || locationCoordinates.isEmpty()) {
            return;
        }

        googleMap.clear();

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (int i = 0; i < locationCoordinates.size(); i++) {
            LatLng position = locationCoordinates.get(i);
            String name = locationNames.get(i);

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(name));

            boundsBuilder.include(position);
        }

        // Adjust camera to show all markers
        try {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 100;
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (IllegalStateException e) {
            // In case of a single marker or other bounds issues, just zoom to the first marker
            if (!locationCoordinates.isEmpty()) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationCoordinates.get(0), 10));
            }
        }
    }

    // MapView lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }
}
