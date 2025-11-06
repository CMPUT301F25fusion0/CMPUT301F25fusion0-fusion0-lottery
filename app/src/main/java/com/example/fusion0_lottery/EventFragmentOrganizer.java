package com.example.fusion0_lottery;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * Displays the event details for Organizers
 * - Code for lines 49 to 52 was inspired by
 *      Dhara Bhavsar on StackOverflow at
 *      https://stackoverflow.com/a/49822393,
 *      published April 13 2018
 */
public class EventFragmentOrganizer extends Fragment {

    public EventFragmentOrganizer() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_organizer, container, false);
        super.onCreate(savedInstanceState);

        // Views Setup
        TextView eventName = view.findViewById(R.id.eventName);
        TextView eventDesc = view.findViewById(R.id.eventDescription);
        TextView eventDate = view.findViewById(R.id.eventDate);
        TextView eventLoc = view.findViewById(R.id.eventLocation);
        TextView eventRegDate = view.findViewById(R.id.eventEndDate);
        TextView eventMaxEntrant = view.findViewById(R.id.eventEntrants);
        TextView eventPrice = view.findViewById(R.id.eventPrice);
        ImageView qrCodeImage = view.findViewById(R.id.eventQrCode);
        TextView qrCodeLabel = view.findViewById(R.id.qrCodeLabel);

        // Retrieve eventId
        String eventId = getArguments().getString("eventId");

        // Database Setup
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("Events").document(eventId);

        eventRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Event event = documentSnapshot.toObject(Event.class);

                eventName.setText("Event Name: " + event.getEventName());
                eventDesc.setText("Description: " + event.getDescription());
                eventDate.setText("Start Date: " + event.getStartDate());
                eventLoc.setText("Location: " + event.getLocation());

                String regStart = event.getRegistrationStart();
                String regEnd = event.getRegistrationEnd();
                eventRegDate.setText("Registration: " + regStart + " to " + regEnd);

                if (event.getMaxEntrants() < 0) {
                    eventMaxEntrant.setText("Max Entrants: " + "No Limit");
                }
                else {
                    eventMaxEntrant.setText("Max Entrants: " + event.getMaxEntrants());
                }
                eventPrice.setText("Price: $" + event.getPrice());

                // Generate and display QR code if enabled
                Boolean hasQrCode = documentSnapshot.getBoolean("hasQrCode");
                String eventId = event.getEventId();

                if (hasQrCode != null && hasQrCode && eventId != null) {
                    try {
                        Bitmap qrBitmap = generateQRCode(eventId);
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

            }

        });

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Event Details");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationIcon(android.R.drawable.ic_media_previous);
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
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