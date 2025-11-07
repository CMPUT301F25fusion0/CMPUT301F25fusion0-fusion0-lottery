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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

/**
 * EventFragmentEntrant
 *
 * Entrant-facing event details. Lets a user join/leave the waiting list, shows QR when available,
 * keeps a per-user history doc (Users/{uid}/Registrations/{eventId}) and cleans up stale users
 * from the Event.waitingList.
 */
public class EventFragmentEntrant extends Fragment {

    private TextView eventNameText, eventDescriptionText, eventDateText, eventLocationText;
    private TextView registrationText, maxEntrantsText, eventPriceText, qrCodeLabel;
    private ImageView qrCodeImage;
    private Button joinWaitingListButton;

    private String eventId;
    private String currentUserId;
    private boolean isInWaitingList;
    private boolean waitingListClosed;

    private FirebaseFirestore db;

    public EventFragmentEntrant() { }

    public static EventFragmentEntrant newInstance(
            String eventId,
            String currentUserId,
            String eventName,
            String eventDescription,
            String startDate,
            String location,
            boolean isInWaitingList,
            String registrationStart,
            String registrationEnd,
            Long maxEntrants,
            Double price,
            boolean waitingListClosed
    ) {
        EventFragmentEntrant f = new EventFragmentEntrant();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("currentUserId", currentUserId);
        args.putString("eventName", eventName);
        args.putString("eventDescription", eventDescription);
        args.putString("startDate", startDate);
        args.putString("eventLocation", location);
        args.putBoolean("isInWaitingList", isInWaitingList);
        args.putString("registrationStart", registrationStart);
        args.putString("registrationEnd", registrationEnd);
        args.putLong("maxEntrants", maxEntrants != null ? maxEntrants : 0);
        args.putDouble("price", price != null ? price : 0);
        args.putBoolean("waitingListClosed", waitingListClosed);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_activity_entrant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // UI
        eventNameText         = view.findViewById(R.id.eventName);
        eventDescriptionText  = view.findViewById(R.id.eventDescription);
        eventDateText         = view.findViewById(R.id.eventDate);
        eventLocationText     = view.findViewById(R.id.eventLocation);
        registrationText      = view.findViewById(R.id.eventEndDate);
        maxEntrantsText       = view.findViewById(R.id.eventEntrants);
        eventPriceText        = view.findViewById(R.id.eventPrice);
        joinWaitingListButton = view.findViewById(R.id.buttonJoinWaitingList);
        qrCodeImage           = view.findViewById(R.id.eventQrCode);
        qrCodeLabel           = view.findViewById(R.id.qrCodeLabel);
        joinWaitingListButton.setVisibility(View.INVISIBLE);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Event Details");
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            toolbar.setNavigationIcon(android.R.drawable.ic_media_previous);
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Argss
        if (getArguments() != null) {
            eventId           = getArguments().getString("eventId");
            currentUserId     = getArguments().getString("currentUserId");
            waitingListClosed = getArguments().getBoolean("waitingListClosed", false);
            isInWaitingList   = getArguments().getBoolean("isInWaitingList", false);

            eventNameText.setText("Event Name: " + getArguments().getString("eventName"));
            eventDescriptionText.setText("Description: " + getArguments().getString("eventDescription"));
            eventDateText.setText("Start Date: " + getArguments().getString("startDate"));
            eventLocationText.setText("Location: " + getArguments().getString("eventLocation"));
            registrationText.setText("Registration: " +
                    getArguments().getString("registrationStart") + " to " +
                    getArguments().getString("registrationEnd"));
            maxEntrantsText.setText("Max Entrants: " + getArguments().getLong("maxEntrants"));
            eventPriceText.setText("Price: $" + getArguments().getDouble("price"));
        }

        // Refresh from Firestore (QR + clean list + button state)
        if (eventId != null) {
            db.collection("Events").document(eventId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) {
                            joinWaitingListButton.setVisibility(View.GONE);
                            return;
                        }

                        // Show QR if enabled
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

                        // Clean waitingList of deleted users
                        @SuppressWarnings("unchecked")
                        ArrayList<String> tempList = (ArrayList<String>) snapshot.get("waitingList");
                        final ArrayList<String> waitingList = (tempList != null) ? tempList : new ArrayList<>();

                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String uid : new ArrayList<>(waitingList)) {
                            tasks.add(db.collection("Users").document(uid).get());
                        }

                        final ArrayList<String> waitingListCopy = new ArrayList<>(waitingList);
                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(results -> {
                                    ArrayList<String> cleanList = new ArrayList<>();
                                    for (int i = 0; i < results.size(); i++) {
                                        DocumentSnapshot userSnap = (DocumentSnapshot) results.get(i);
                                        if (userSnap.exists()) cleanList.add(waitingListCopy.get(i));
                                    }

                                    isInWaitingList = cleanList.contains(currentUserId);
                                    joinWaitingListButton.setText(
                                            isInWaitingList ? "Leave Waiting List" : "Join Waiting List");

                                    snapshot.getReference().update("waitingList", cleanList,
                                            "waitingListCount", cleanList.size());
                                    joinWaitingListButton.setVisibility(View.VISIBLE);
                                });

                    });
        }

        joinWaitingListButton.setOnClickListener(v -> toggleWaitingList());
    }

    private void toggleWaitingList() {
        if (waitingListClosed) {
            Toast.makeText(getContext(),
                    "The waiting list is closed. You cannot join this event.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUserId == null || eventId == null) {
            Toast.makeText(getContext(), "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }

            @SuppressWarnings("unchecked")
            ArrayList<String> tempList = (ArrayList<String>) snapshot.get("waitingList");
            final ArrayList<String> waitingList = (tempList != null) ? new ArrayList<>(tempList) : new ArrayList<>();

            Long maxEntrants = snapshot.getLong("maxEntrants");
            String registrationEndStr = snapshot.getString("registrationEnd");

            // Registration closed by date (inclusive of the end day)
            boolean isClosedByDate = false;
            if (registrationEndStr != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date regEnd = sdf.parse(registrationEndStr);
                    if (regEnd != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(regEnd);
                        cal.add(Calendar.DATE, 1);
                        Date regEndInclusive = cal.getTime();
                        Date today = new Date();
                        if (today.after(regEndInclusive)) isClosedByDate = true;
                    }
                } catch (Exception ignored) {}
            }

            boolean isFull = maxEntrants != null && waitingList.size() >= maxEntrants;
            if (!isInWaitingList && (isClosedByDate || isFull)) {
                String msg = isClosedByDate
                        ? "The waiting list is closed because registration has ended."
                        : "The waiting list is full. You cannot join this event.";
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                return;
            }

            // Remove deleted users before toggling (safety)
            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (String uid : new ArrayList<>(waitingList)) {
                tasks.add(db.collection("Users").document(uid).get());
            }

            Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        Iterator<String> iter = waitingList.iterator();
                        for (Object obj : results) {
                            DocumentSnapshot userSnap = (DocumentSnapshot) obj;
                            if (!userSnap.exists() && iter.hasNext()) iter.next();
                        }

                        boolean joining;
                        if (isInWaitingList) {
                            waitingList.remove(currentUserId);
                            joining = false;
                            isInWaitingList = false;
                            joinWaitingListButton.setText("Join Waiting List");
                            Toast.makeText(getContext(), "You left the waiting list", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!waitingList.contains(currentUserId)) waitingList.add(currentUserId);
                            joining = true;
                            isInWaitingList = true;
                            joinWaitingListButton.setText("Leave Waiting List");
                            Toast.makeText(getContext(), "You joined the waiting list", Toast.LENGTH_SHORT).show();
                        }

                        // Update event doc
                        eventRef.update("waitingList", waitingList,
                                        "waitingListCount", waitingList.size())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Error updating waiting list: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());

                        // Mirror into per-user History (keep cancelled instead of deleting)
                        DocumentReference regRef = db.collection("Users")
                                .document(currentUserId)
                                .collection("Registrations")
                                .document(eventId);

                        if (joining) {
                            Map<String, Object> reg = new HashMap<>();
                            reg.put("eventId", eventId);
                            reg.put("status", "Pending"); // organizer updates later
                            reg.put("registeredAt", FieldValue.serverTimestamp());
                            reg.put("eventName", snapshot.getString("eventName"));
                            reg.put("startDate", snapshot.getString("startDate"));
                            reg.put("location", snapshot.getString("location"));
                            reg.put("description", snapshot.getString("description"));
                            regRef.set(reg, SetOptions.merge());
                        } else {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("eventId", eventId);
                            updates.put("status", "Cancelled");
                            updates.put("cancelledAt", FieldValue.serverTimestamp());
                            regRef.set(updates, SetOptions.merge());
                        }
                    });
        });
    }

    // --- QR helper ---
    private Bitmap generateQRCode(String eventId) throws WriterException {
        String content = "event://" + eventId;
        int size = 500;

        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                content, BarcodeFormat.QR_CODE, size, size);

        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bmp;
    }
}
