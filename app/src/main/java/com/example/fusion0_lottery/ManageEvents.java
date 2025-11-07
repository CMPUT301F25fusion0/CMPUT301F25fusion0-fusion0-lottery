package com.example.fusion0_lottery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;

public class ManageEvents extends Fragment {

    private TabLayout tabLayout;
    private ImageView eventPosterImage, qrCodeImage;
    private TextView manageEventTitle, eventDescriptionText, eventInterests,
            eventTime, eventLocation, eventRegistration, eventMaxEntrants, eventPrice, qrCodeLabel;

    private Button editEventButton, updatePosterButton, notifyWaitlistButton, drawWinnersButton, exportCsvButton;
    private Button backToEventsButton;

    private FirebaseFirestore db;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.manage_events, container, false);

        // Tabs + buttons
        tabLayout = view.findViewById(R.id.manageEventTabs);
        eventPosterImage = view.findViewById(R.id.eventPosterImage);
        qrCodeLabel = view.findViewById(R.id.qrCodeLabel);
        qrCodeImage = view.findViewById(R.id.eventQrCode);
        manageEventTitle = view.findViewById(R.id.eventName);
        eventDescriptionText = view.findViewById(R.id.eventDescription);
        eventInterests = view.findViewById(R.id.eventInterests);
        eventTime = view.findViewById(R.id.eventTime);
        eventLocation = view.findViewById(R.id.eventLocation);
        eventRegistration = view.findViewById(R.id.eventRegistration);
        eventMaxEntrants = view.findViewById(R.id.eventMaxEntrants);
        eventPrice = view.findViewById(R.id.eventPrice);


        editEventButton = view.findViewById(R.id.editEventButton);
        updatePosterButton = view.findViewById(R.id.updatePosterButton);
        notifyWaitlistButton = view.findViewById(R.id.notifyWaitlistButton);
        drawWinnersButton = view.findViewById(R.id.drawWinnersButton);
        exportCsvButton = view.findViewById(R.id.exportCsvButton);
        backToEventsButton = view.findViewById(R.id.backToEventsButton);

        backToEventsButton.setOnClickListener(v -> {
            FragmentOrganizer organizerFragment = new FragmentOrganizer();
            ((MainActivity) requireActivity()).replaceFragment(organizerFragment);
        });

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventDetails();
        }

        setupTabs();
        setupButtonActions();

        return view;
    }

    /**
     * Setup the Manage Event tabs
     */
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Details"));
        tabLayout.addTab(tabLayout.newTab().setText("Waiting List"));
        tabLayout.addTab(tabLayout.newTab().setText("Selected Entrants"));
        tabLayout.addTab(tabLayout.newTab().setText("Manage Participants"));
        tabLayout.addTab(tabLayout.newTab().setText("Generate QR"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String title = tab.getText().toString();
                if (title.equals("Waiting List")) {
                    FragmentWaitingList fragmentWaitingList = new FragmentWaitingList();
                    Bundle args = new Bundle();
                    args.putString("eventId", eventId);
                    fragmentWaitingList.setArguments(args);
                    ((MainActivity) requireActivity()).replaceFragment(fragmentWaitingList);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Setup buttons at the bottom
     */
    private void setupButtonActions() {
        updatePosterButton.setOnClickListener(v -> {
            if (eventId != null) {
                FragmentUpdatePoster updatePosterImage = new FragmentUpdatePoster();
                Bundle args = new Bundle();
                args.putString("eventId", eventId);
                updatePosterImage.setArguments(args);
                ((MainActivity) requireActivity()).replaceFragment(updatePosterImage);
            } else {
                Toast.makeText(requireContext(), "Unable to edit Poster Image", Toast.LENGTH_SHORT).show();
            }
        });

        editEventButton.setOnClickListener(v -> {
            if (eventId != null) {
                FragmentEditEvent editFragment = new FragmentEditEvent();
                Bundle args = new Bundle();
                args.putString("eventId", eventId);
                editFragment.setArguments(args);
                ((MainActivity) requireActivity()).replaceFragment(editFragment);
            } else {
                Toast.makeText(requireContext(), "Event ID not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     *  function display the details for the event
     *  gets the reference from Firestore 'Events' based on event ID
     *  if document exists, convert the data into an object and display event information
     */
    private void loadEventDetails() {
        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.get().addOnSuccessListener(DocumentSnapshot -> {
            if (DocumentSnapshot.exists()) {
                Event event = DocumentSnapshot.toObject(Event.class);
                if (event != null) {
                    // get the event's description from the database
                    String description;
                    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                        description = event.getDescription();
                    }
                    // if no description set empty
                    else {
                        description = "";
                    }

                    eventDescriptionText.setText("Event Description: " + description);
                    manageEventTitle.setText(event.getEventName());
                    eventInterests.setText("Interests: "+ event.getInterests());
                    eventTime.setText("Time: " + event.getTime());
                    eventLocation.setText("Location: " + event.getLocation());
                    eventRegistration.setText("Registration Date: " + event.getStartDate() + " - " + event.getEndDate());
                    eventMaxEntrants.setText("Max Entrants: " + event.getMaxEntrants());
                    eventPrice.setText("Price: $" + event.getPrice());

                    // load poster from Base64
                    String poster = DocumentSnapshot.getString("posterImage");
                    if (poster != null && !poster.isEmpty()) {
                        byte[] imageBytes = Base64.decode(poster, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        eventPosterImage.setImageBitmap(bitmap);
                    }

                    // Generate and display QR code if enabled
                    Boolean hasQrCode = DocumentSnapshot.getBoolean("hasQrCode");
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
            }
        });
    }

    /**
     * Generate QR code from event ID
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
