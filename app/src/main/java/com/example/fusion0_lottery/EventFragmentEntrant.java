package com.example.fusion0_lottery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Fragment representing the detailed view of an event for entrants.
 * Users can view event details, check lottery criteria, see total entrants,
 * and join or leave the waiting list.
 */
public class EventFragmentEntrant extends Fragment {

    private TextView eventNameText, eventDescriptionText, eventInterestsText, eventDateText, eventLocationText, totalEntrantsText, lotteryCriteriaText;
    private TextView registrationText, maxEntrantsText, eventPriceText, qrCodeLabel;
    private Button joinWaitingListButton;
    private ImageView qrCodeImage;
    String eventId;
    boolean isInWaitingList;

    boolean waitingListClosed;

    FirebaseFirestore db;
    String currentUserId; // <-- Using UID now

    // Location permission request code
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean pendingJoinWithLocation = false;

    public EventFragmentEntrant() {}

    public static EventFragmentEntrant newInstance(
            String eventId,
            String currentUserId,   // <-- pass UID here
            String eventName,
            String eventDescription,
            String interests,
            String startDate,
            String location,
            boolean isInWaitingList,
            String registrationStart,
            String registrationEnd,
            Long maxEntrants,
            Double price,
            boolean waitingListClosed
    ) {
        EventFragmentEntrant fragment = new EventFragmentEntrant();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("currentUserId", currentUserId); // <-- store UID
        args.putString("eventName", eventName);
        args.putString("eventDescription", eventDescription);
        args.putString("interests", interests);
        args.putString("startDate", startDate);
        args.putString("eventLocation", location);
        args.putBoolean("isInWaitingList", isInWaitingList);
        args.putString("registrationStart", registrationStart);
        args.putString("registrationEnd", registrationEnd);
        args.putLong("maxEntrants", maxEntrants != null ? maxEntrants : 0);
        args.putDouble("price", price != null ? price : 0);
        args.putBoolean("waitingListClosed", waitingListClosed);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the layout, initializes views, displays event details,
     * and sets up listeners for back navigation and waiting list actions.
     *
     * @param inflater LayoutInflater
     * @param container ViewGroup container
     * @param savedInstanceState Bundle of saved state
     * @return Inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_activity_entrant, container, false);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        eventNameText = view.findViewById(R.id.eventName);
        eventDescriptionText = view.findViewById(R.id.eventDescription);
        eventInterestsText = view.findViewById(R.id.eventInterests);
        eventDateText = view.findViewById(R.id.eventDate);
        eventLocationText = view.findViewById(R.id.eventLocation);
        registrationText = view.findViewById(R.id.eventEndDate);
        maxEntrantsText = view.findViewById(R.id.eventEntrants);
        eventPriceText = view.findViewById(R.id.eventPrice);
        totalEntrantsText = view.findViewById(R.id.textTotalEntrants);
        lotteryCriteriaText = view.findViewById(R.id.textLotteryCriteria);
        joinWaitingListButton = view.findViewById(R.id.buttonJoinWaitingList);
        qrCodeImage = view.findViewById(R.id.eventQrCode);
        qrCodeLabel = view.findViewById(R.id.qrCodeLabel);
        joinWaitingListButton.setVisibility(View.INVISIBLE);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            currentUserId = getArguments().getString("currentUserId");
            waitingListClosed = getArguments().getBoolean("waitingListClosed", false);

            // Display all fields
            eventNameText.setText("Event Name: " + getArguments().getString("eventName"));
            eventDescriptionText.setText("Description: " + getArguments().getString("eventDescription"));
            eventInterestsText.setText("Interests: " + getArguments().getString("interests"));
            eventDateText.setText("Start Date: " + getArguments().getString("startDate"));
            eventLocationText.setText("Location: " + getArguments().getString("eventLocation"));

            String regStart = getArguments().getString("registrationStart");
            String regEnd = getArguments().getString("registrationEnd");
            registrationText.setText("Registration: " + regStart + " to " + regEnd);

            maxEntrantsText.setText("Max Entrants: " + getArguments().getLong("maxEntrants"));
            eventPriceText.setText("Price: $" + getArguments().getDouble("price"));

            // Listen for live updates of total entrants
            db.collection("Events").document(eventId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (snapshot != null && snapshot.exists()) {
                            List<String> waitingList = (List<String>) snapshot.get("waitingList");
                            int total = waitingList != null ? waitingList.size() : 0;
                            long maxEntrantsVal = getArguments().getLong("maxEntrants");
                            totalEntrantsText.setText("Total Entrants: " + total + " / " + maxEntrantsVal);
                        }
                    });


            // Fetch and display lottery selection criteria
            db.collection("Events").document(eventId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot != null && snapshot.exists()) {
                            String criteria = snapshot.getString("lotteryCriteria");
                            if (criteria != null && !criteria.isEmpty()) {
                                lotteryCriteriaText.setText("Lottery Criteria: " + criteria);
                            } else {
                                lotteryCriteriaText.setText("Lottery Criteria: Random selection after registration closes.");
                            }

                        }
                    })
                    .addOnFailureListener(e -> {
                        lotteryCriteriaText.setText("Lottery Criteria: (Unavailable)");
                    });

            // Fetch latest waiting list and remove deleted users before showing button
            db.collection("Events").document(eventId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) return;

                        List<Object> waitingListData = (List<Object>) snapshot.get("waitingList");
                        if (waitingListData == null) waitingListData = new ArrayList<>();

                        // Extract user IDs from waiting list (handles both String and Map formats)
                        ArrayList<String> userIds = new ArrayList<>();
                        for (Object item : waitingListData) {
                            if (item == null) continue;

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

                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String uid : userIds) {
                            tasks.add(db.collection("Users").document(uid).get());
                        }

                        List<Object> finalWaitingListData = waitingListData;
                        ArrayList<String> finalUserIds = userIds;
                        com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(results -> {
                                    // Build cleaned list maintaining the original format
                                    List<Object> cleanList = new ArrayList<>();
                                    for (int i = 0; i < results.size(); i++) {
                                        DocumentSnapshot userSnap = (DocumentSnapshot) results.get(i);
                                        if (userSnap.exists()) {
                                            // Find the original entry in waitingListData
                                            String validUserId = finalUserIds.get(i);
                                            for (Object item : finalWaitingListData) {
                                                if (item instanceof String && item.equals(validUserId)) {
                                                    cleanList.add(item);
                                                    break;
                                                } else if (item instanceof Map) {
                                                    Map<String, Object> entry = (Map<String, Object>) item;
                                                    if (validUserId.equals(entry.get("userId"))) {
                                                        cleanList.add(item);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Update isInWaitingList
                                    isInWaitingList = finalUserIds.contains(currentUserId);
                                    joinWaitingListButton.setText(isInWaitingList ? "Leave Waiting List" : "Join Waiting List");

                                    // Update Firestore with cleaned list
                                    snapshot.getReference().update("waitingList", cleanList);
                                    joinWaitingListButton.setVisibility(View.VISIBLE);
                                });
                    });
        }

        // Toolbar back arrow
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Event Details");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationIcon(android.R.drawable.ic_media_previous);
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Join/Leave waiting list
        joinWaitingListButton.setOnClickListener(v -> toggleWaitingList());

        return view;
    }

    /**
     * Toggles the current user's membership in the waiting list.
     * Handles old/new waiting list formats, checks max entrants, and updates Firestore.
     */
    void toggleWaitingList() {
        if (waitingListClosed) {
            Toast.makeText(getContext(), "The waiting list is closed. You cannot join this event.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if event requires geolocation
            Boolean requiresGeolocation = snapshot.getBoolean("requiresGeolocation");

            // If not already in waiting list and event requires location, check permissions
            if (!isInWaitingList && requiresGeolocation != null && requiresGeolocation) {
                if (!hasLocationPermission()) {
                    // Request permission and set flag to join after permission granted
                    pendingJoinWithLocation = true;
                    requestLocationPermission();
                    return;
                } else {
                    // Has permission, save location and proceed with join
                    saveUserLocationAndJoinWaitingList(snapshot);
                    return;
                }
            }

            // If leaving or doesn't require geolocation, proceed normally
            proceedWithToggleWaitingList(snapshot);
        });
    }

    /**
     * Proceeds with joining/leaving the waiting list without geolocation requirements
     */
    private void proceedWithToggleWaitingList(DocumentSnapshot snapshot) {
        // Fetch and check maxEntrants
            Long maxEntrants = snapshot.getLong("maxEntrants");
            if (maxEntrants == null || maxEntrants <= 0) {
                Toast.makeText(getContext(), "This event cannot accept entrants (max entrants is 0).", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get waiting list (handles both String and Map formats)
            List<Object> waitingListData = (List<Object>) snapshot.get("waitingList");
            if (waitingListData == null) waitingListData = new ArrayList<>();

            // Normalize to Map format and build mutable list
            List<Map<String, Object>> mutableWaitingList = new ArrayList<>();
            for (Object item : waitingListData) {
                if (item == null) continue;

                Map<String, Object> entry;
                if (item instanceof String) {
                    // Old format: convert String to Map
                    entry = new HashMap<>();
                    entry.put("userId", item);
                    entry.put("joinedAt", null);
                } else if (item instanceof Map) {
                    // New format: already a map
                    entry = new HashMap<>((Map<String, Object>) item);
                } else {
                    continue;
                }

                // Only add valid entries
                if (entry.containsKey("userId") && entry.get("userId") != null) {
                    mutableWaitingList.add(entry);
                }
            }

            // Check if current user is already in list
            Map<String, Object> existingEntry = null;
            for (Map<String, Object> entry : mutableWaitingList) {
                if (currentUserId.equals(entry.get("userId"))) {
                    existingEntry = entry;
                    break;
                }
            }

            if (existingEntry != null) {
                // Leave waiting list
                mutableWaitingList.remove(existingEntry);
                isInWaitingList = false;
                joinWaitingListButton.setText("Join Waiting List");
                Toast.makeText(getContext(), "You left the waiting list", Toast.LENGTH_SHORT).show();
            } else {
                // Join waiting list
                Map<String, Object> newEntrant = new HashMap<>();
                newEntrant.put("userId", currentUserId);
                newEntrant.put("joinedAt", com.google.firebase.Timestamp.now());
                mutableWaitingList.add(newEntrant);

                isInWaitingList = true;
                joinWaitingListButton.setText("Leave Waiting List");
                Toast.makeText(getContext(), "You joined the waiting list", Toast.LENGTH_SHORT).show();
            }

            // Update Firestore with event reference from snapshot
            DocumentReference eventRef = snapshot.getReference();
            eventRef.update("waitingList", mutableWaitingList)
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error updating waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Check if location permission is granted
     */
    private boolean hasLocationPermission() {
        if (getContext() == null) return false;
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location permission from user
     */
    private void requestLocationPermission() {
        if (getActivity() == null) return;
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Handle permission request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with joining if pending
                if (pendingJoinWithLocation) {
                    pendingJoinWithLocation = false;
                    // Re-trigger the join process
                    toggleWaitingList();
                }
            } else {
                // Permission denied
                Toast.makeText(getContext(),
                        "Location permission is required to join this event",
                        Toast.LENGTH_LONG).show();
                pendingJoinWithLocation = false;
            }
        }
    }

    /**
     * Save user's current location to Firebase and join waiting list
     * For simplicity, we'll use a mock location or last known location
     * In production, you'd use FusedLocationProviderClient for accurate location
     */
    private void saveUserLocationAndJoinWaitingList(DocumentSnapshot snapshot) {
        if (getContext() == null) return;

        try {
            // Try to get location using LocationManager
            android.location.LocationManager locationManager =
                    (android.location.LocationManager) getContext().getSystemService(android.content.Context.LOCATION_SERVICE);

            if (locationManager != null && hasLocationPermission()) {
                // Check if we have permission (double check)
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    // Try to get last known location
                    Location lastLocation = locationManager.getLastKnownLocation(
                            android.location.LocationManager.GPS_PROVIDER);

                    if (lastLocation == null) {
                        lastLocation = locationManager.getLastKnownLocation(
                                android.location.LocationManager.NETWORK_PROVIDER);
                    }

                    if (lastLocation != null) {
                        // Save location to user document
                        Map<String, Object> locationData = new HashMap<>();
                        locationData.put("latitude", lastLocation.getLatitude());
                        locationData.put("longitude", lastLocation.getLongitude());
                        locationData.put("timestamp", com.google.firebase.Timestamp.now());

                        db.collection("Users").document(currentUserId)
                                .update("location", locationData)
                                .addOnSuccessListener(aVoid -> {
                                    // Location saved, now proceed with joining waiting list
                                    proceedWithToggleWaitingList(snapshot);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(),
                                            "Error saving location: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // No location available, but still allow joining
                        Toast.makeText(getContext(),
                                "Could not get current location. Joining without location data.",
                                Toast.LENGTH_SHORT).show();
                        proceedWithToggleWaitingList(snapshot);
                    }
                }
            } else {
                // LocationManager not available
                Toast.makeText(getContext(),
                        "Location services not available. Joining without location data.",
                        Toast.LENGTH_SHORT).show();
                proceedWithToggleWaitingList(snapshot);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(),
                    "Error accessing location: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            // Still allow joining even if location fails
            proceedWithToggleWaitingList(snapshot);
        }
    }

}