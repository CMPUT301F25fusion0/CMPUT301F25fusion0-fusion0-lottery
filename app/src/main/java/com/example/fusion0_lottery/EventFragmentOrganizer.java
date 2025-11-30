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
 * Fragment for displaying detailed event information to organizers.
 *
 * <p>This fragment presents a comprehensive view of an event's details including
 * name, description, dates, location, pricing, and registration information. It also
 * generates and displays a QR code for the event if QR code functionality is enabled.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Display event details retrieved from Firestore</li>
 *   <li>Generate and display QR codes for event registration</li>
 *   <li>Provide navigation back to previous screens via toolbar</li>
 *   <li>Handle both events with and without QR code functionality</li>
 * </ul>
 *
 * <p><b>Design Pattern:</b> This fragment follows the MVC pattern where Firestore
 * serves as the model, the fragment acts as controller, and XML layout as view.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently identified.</p>
 *
 * @see Event
 * @see ManageEvents
 *
 * @version 1.0
 * @since 2024-11-30
 *
 * Code Attribution:
 * - Code for lines 49 to 52 was inspired by Dhara Bhavsar on StackOverflow at
 *   https://stackoverflow.com/a/49822393, published April 13 2018
 */
public class EventFragmentOrganizer extends Fragment {

    /**
     * Default constructor required for fragment instantiation.
     */
    public EventFragmentOrganizer() {}

    /**
     * Creates and initializes the event detail view for organizers.
     *
     * <p>This method inflates the layout, retrieves the event ID from arguments,
     * fetches event data from Firestore, and displays all event information including
     * QR code generation if applicable.</p>
     *
     * @param inflater The LayoutInflater object to inflate views in the fragment
     * @param container The parent view that this fragment's UI will be attached to
     * @param savedInstanceState Previously saved state of the fragment, if any
     * @return The root View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_organizer, container, false);
        super.onCreate(savedInstanceState);

        // Views Setup
        TextView eventName = view.findViewById(R.id.eventName);
        TextView eventInterests = view.findViewById(R.id.eventInterests);
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
                eventInterests.setText("Interests: " + event.getInterests());
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
     * Generates a QR code bitmap image from an event ID.
     *
     * <p>This method creates a square QR code containing the event URI scheme
     * (event://eventId) which can be scanned by attendees to join the event.</p>
     *
     * @param eventId The unique identifier of the event
     * @return A Bitmap containing the generated QR code
     * @throws WriterException If QR code generation fails
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