package com.example.fusion0_lottery;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Map;

public class EventFragmentEntrant extends Fragment {

    private TextView eventNameText, eventDescriptionText, eventInterestsText, eventDateText, eventLocationText;
    private TextView registrationText, maxEntrantsText, eventPriceText, qrCodeLabel;
    private Button joinWaitingListButton;
    private ImageView qrCodeImage;
    private String eventId;
    private boolean isInWaitingList;
    private boolean waitingListClosed;

    private FirebaseFirestore db;
    private String currentUserId; // <-- Using UID now

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

            // Fetch latest waiting list and remove deleted users before showing button
            db.collection("Events").document(eventId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) return;

                        List<Object> waitingListData = (List<Object>) snapshot.get("waitingList");
                        if (waitingListData == null) waitingListData = new ArrayList<>();

                        // Generate and display QR code if enabled
                        Boolean hasQrCode = snapshot.getBoolean("hasQrCode");
                        String eventIdForQr = snapshot.getString("eventId");

                        if (hasQrCode != null && hasQrCode && eventIdForQr != null) {
                            try {
                                Bitmap qrBitmap = generateQRCode(eventIdForQr);
                                qrCodeLabel.setVisibility(View.VISIBLE);
                                qrCodeImage.setVisibility(View.VISIBLE);
                                qrCodeImage.setImageBitmap(qrBitmap);
                            } catch (WriterException e) {
                                qrCodeLabel.setVisibility(View.GONE);
                                qrCodeImage.setVisibility(View.GONE);
                            }
                        } else {
                            qrCodeLabel.setVisibility(View.GONE);
                            qrCodeImage.setVisibility(View.GONE);
                        }

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

    private void toggleWaitingList() {
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

            // Update Firestore
            eventRef.update("waitingList", mutableWaitingList)
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error updating waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Generate QR code bitmap from event ID
     */
    private Bitmap generateQRCode(String eventId) throws WriterException {
        String qrContent = "event://" + eventId;
        int size = 500;

        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                size,
                size
        );

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }
}