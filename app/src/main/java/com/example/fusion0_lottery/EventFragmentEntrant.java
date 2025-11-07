package com.example.fusion0_lottery;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class EventFragmentEntrant extends Fragment {

    private TextView eventNameText, eventDescriptionText, eventDateText, eventLocationText;
    private TextView registrationText, maxEntrantsText, eventPriceText;
    private Button joinWaitingListButton;
    private String eventId;
    private boolean isInWaitingList;
    private boolean waitingListClosed;

    private FirebaseFirestore db;
    private String currentUserId;

    public EventFragmentEntrant() {}

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
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date regEnd = sdf.parse(registrationEndStr);

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
