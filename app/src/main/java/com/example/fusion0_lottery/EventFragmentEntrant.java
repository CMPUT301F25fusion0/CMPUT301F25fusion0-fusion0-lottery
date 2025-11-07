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
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

/**
 * EventFragmentEntrant.java
 *
 * Fragment that displays detailed information about a single event for a user.
 * Users can view the event details, registration dates, price, and join or leave the waiting list for the event.
 *
 * This fragment also handles:
 * - Checking whether the event waiting list is full or closed
 * - Updating the waiting list in Firestore
 * - Generating QR codes for events
 *
 * Outstanding issues / considerations:
 * - Firestore operations lack advanced error recovery or retry logic.
 */
public class EventFragmentEntrant extends Fragment {

    private TextView eventNameText, eventDescriptionText, eventDateText, eventLocationText;
    private TextView registrationText, maxEntrantsText, eventPriceText, qrCodeLabel;
    private Button joinWaitingListButton;
    private ImageView qrCodeImage;
    private String eventId;
    private boolean isInWaitingList;
    private boolean waitingListClosed;

    private FirebaseFirestore db;
    private String currentUserId;

    /**
     * Default constructor.
     */
    public EventFragmentEntrant() {}

    /**
     * Factory method to create a new instance of EventFragmentEntrant.
     *
     * @param eventId The Firestore document ID for the event.
     * @param currentUserId ID of the currently logged-in user.
     * @param eventName Name of the event.
     * @param eventDescription Description of the event.
     * @param startDate Start date of the event (yyyy-MM-dd).
     * @param location Event location.
     * @param isInWaitingList Whether the user is already in the waiting list.
     * @param registrationStart Start date of registration.
     * @param registrationEnd End date of registration.
     * @param maxEntrants Maximum number of entrants allowed.
     * @param price Event price.
     * @param waitingListClosed True if waiting list is closed.
     * @return A new instance of EventFragmentEntrant.
     */
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
        EventFragmentEntrant fragment = new EventFragmentEntrant();
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
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the fragment layout and initializes UI components.
     *
     * @param inflater LayoutInflater used to inflate the fragment view.
     * @param container Parent view that the fragment's UI should attach to.
     * @param savedInstanceState Bundle containing saved state (if any).
     * @return The root view of the fragment.
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

            eventNameText.setText("Event Name: " + getArguments().getString("eventName"));
            eventDescriptionText.setText("Description: " + getArguments().getString("eventDescription"));
            eventDateText.setText("Start Date: " + getArguments().getString("startDate"));
            eventLocationText.setText("Location: " + getArguments().getString("eventLocation"));

            String regStart = getArguments().getString("registrationStart");
            String regEnd = getArguments().getString("registrationEnd");
            registrationText.setText("Registration: " + regStart + " to " + regEnd);

            maxEntrantsText.setText("Max Entrants: " + getArguments().getLong("maxEntrants"));
            eventPriceText.setText("Price: $" + getArguments().getDouble("price"));

            db.collection("Events").document(eventId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) return;

                        ArrayList<String> waitingList = (ArrayList<String>) snapshot.get("waitingList");
                        if (waitingList == null) waitingList = new ArrayList<>();

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

                        // Authored by: Edeson Bizerril,
                        // Stack Overflow, https://stackoverflow.com/questions/65566970/how-to-cast-an-instance-of-querydocumentsnapshots-into-a-list-flutter-firestore
                        // Taken by: Bhoomi Bhoomi
                        // Taken on: 2025-11-07
                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String uid : new ArrayList<>(waitingList)) {
                            tasks.add(db.collection("Users").document(uid).get());
                        }

                        ArrayList<String> finalWaitingList = waitingList;
                        com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(results -> {
                                    ArrayList<String> cleanList = new ArrayList<>();
                                    for (int i = 0; i < results.size(); i++) {
                                        DocumentSnapshot userSnap = (DocumentSnapshot) results.get(i);
                                        if (userSnap.exists())
                                            cleanList.add(finalWaitingList.get(i));
                                    }

                                    isInWaitingList = cleanList.contains(currentUserId);
                                    joinWaitingListButton.setText(isInWaitingList ? "Leave Waiting List" : "Join Waiting List");

                                    snapshot.getReference().update("waitingList", cleanList);
                                    joinWaitingListButton.setVisibility(View.VISIBLE);
                                });
                    });
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Event Details");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationIcon(android.R.drawable.ic_media_previous);
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        joinWaitingListButton.setOnClickListener(v -> toggleWaitingList());

        return view;
    }

    /**
     * Toggles the user's membership in the event's waiting list.
     * Adds the user if not present, removes if already present.
     * Updates Firestore and UI accordingly.
     */
    private void toggleWaitingList() {
        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> waitingList = (List<String>) snapshot.get("waitingList");
            if (waitingList == null) waitingList = new ArrayList<>();

            Long maxEntrants = snapshot.getLong("maxEntrants");
            String registrationEndStr = snapshot.getString("registrationEnd");

            // Check if registration period ended (inclusive)
            boolean isClosedByDate = false;
            if (registrationEndStr != null) {
                // Authored by: Quinteger,
                // Stack Overflow, https://stackoverflow.com/questions/55588323/safe-simpledateformat-parsing
                // Taken by: Bhoomi Bhoomi
                // Taken on: 2025-11-07
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date regEnd = sdf.parse(registrationEndStr);

                    // Authored by: Katherine,
                    // Stack Overflow, https://stackoverflow.com/questions/30434334/gregoriancalendar-outputs-the-date-is-java-util-gregoriancalendartime-11415564
                    // Taken by: Bhoomi Bhoomi
                    // Taken on: 2025-11-07
                    if (regEnd != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(regEnd);
                        cal.add(Calendar.DATE, 1); // add 1 day to include the last day
                        Date regEndInclusive = cal.getTime();
                        Date today = new Date();

                        if (today.after(regEndInclusive)) isClosedByDate = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            boolean isFull = maxEntrants != null && waitingList.size() >= maxEntrants;

            if (isClosedByDate || isFull) {
                String msg = isClosedByDate ?
                        "The waiting list is closed because registration has ended." :
                        "The waiting list is full. You cannot join this event.";
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                return;
            }

            if (waitingList.contains(currentUserId)) {
                waitingList.remove(currentUserId);
                isInWaitingList = false;
                Toast.makeText(getContext(), "You left the waiting list", Toast.LENGTH_SHORT).show();
            } else {
                waitingList.add(currentUserId);
                isInWaitingList = true;
                Toast.makeText(getContext(), "You joined the waiting list", Toast.LENGTH_SHORT).show();
            }
            joinWaitingListButton.setText(isInWaitingList ? "Leave Waiting List" : "Join Waiting List");

            eventRef.update("waitingList", waitingList,
                            "waitingListCount", waitingList.size())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error updating waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Generates a QR code bitmap for the event.
     *
     * @param eventId Firestore document ID of the event.
     * @return Bitmap representing the QR code.
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