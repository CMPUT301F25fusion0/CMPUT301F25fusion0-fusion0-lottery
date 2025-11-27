package com.example.fusion0_lottery;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageEvents extends Fragment {

    private TabLayout tabLayout;
    private ImageView eventPosterImage, qrCodeImage;
    private TextView manageEventTitle, eventDescriptionText, eventInterests,
            eventTime, eventLocation, eventRegistration, eventMaxEntrants, eventPrice, qrCodeLabel, eventLotteryCriteria;

    private Button editEventButton, updatePosterButton, notifyWaitlistButton, exportCsvButton;
    private Button backToEventsButton;

    private FirebaseFirestore db;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.manage_events, container, false);

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
        eventLotteryCriteria = view.findViewById(R.id.eventLotteryCriteria);



        editEventButton = view.findViewById(R.id.editEventButton);
        updatePosterButton = view.findViewById(R.id.updatePosterButton);
        notifyWaitlistButton = view.findViewById(R.id.notifyWaitlistButton);
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
     * setup the Manage Event tabs at the top
     */
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Details"));
        tabLayout.addTab(tabLayout.newTab().setText("Waiting List"));
        tabLayout.addTab(tabLayout.newTab().setText("Selected Entrants"));
        tabLayout.addTab(tabLayout.newTab().setText("Final List"));
        tabLayout.addTab(tabLayout.newTab().setText("Cancelled"));

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

                else if (title.equals("Selected Entrants")) {
                    FragmentSelectedEntrants fragmentSelectedEntrants = new FragmentSelectedEntrants();
                    Bundle args = new Bundle();
                    args.putString("eventId", eventId);
                    fragmentSelectedEntrants.setArguments(args);
                    ((MainActivity) requireActivity()).replaceFragment(fragmentSelectedEntrants);
                }

                else if (title.equals("Final List")) {
                    FragmentFinalList fragmentFinalList = new FragmentFinalList();
                    Bundle args = new Bundle();
                    args.putString("eventId", eventId);
                    fragmentFinalList.setArguments(args);
                    ((MainActivity) requireActivity()).replaceFragment(fragmentFinalList);
                }

                else if (title.equals("Cancelled")) {
                    FragmentCancelledEntrants fragmentCancelledEntrants = new FragmentCancelledEntrants();
                    Bundle args = new Bundle();
                    args.putString("eventId", eventId);
                    fragmentCancelledEntrants.setArguments(args);
                    ((MainActivity) requireActivity()).replaceFragment(fragmentCancelledEntrants);
                }
            }


            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Setup buttons at the bottom of the manage events screen
     * updatePosterButton: Update existing or non-existing event poster by uploading an image
     * editEventButton: Change Event's details
     *
     *
     *
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
            }
            else {
                Toast.makeText(requireContext(), "Event ID not available", Toast.LENGTH_SHORT).show();
            }
        });

        // notifyWaitlistButton.setOnClickListener(v -> {} });

        exportCsvButton.setOnClickListener(v -> exportFinalListToCsv());

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

                    // Set lottery criteria
                    String lotteryCriteria = event.getLotteryCriteria();
                    if (lotteryCriteria != null && !lotteryCriteria.isEmpty()) {
                        eventLotteryCriteria.setText("Lottery Criteria: " + lotteryCriteria);
                        eventLotteryCriteria.setVisibility(View.VISIBLE);
                    } else {
                        eventLotteryCriteria.setVisibility(View.GONE);
                    }

                    // Generate and display QR code if enabled
                    Boolean hasQrCode = DocumentSnapshot.getBoolean("hasQrCode");
                    String eventId = event.getEventId();

                    // Generate QR code if hasQrCode is true OR if field is not set (null) - for backward compatibility
                    if (eventId != null && (hasQrCode == null || hasQrCode)) {
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

    /**
     * Export the final list of entrants to a CSV file
     * Fetches the finalList from Firebase, retrieves user details, and generates a CSV file
     */
    private void exportFinalListToCsv() {
        if (eventId == null) {
            Toast.makeText(requireContext(), "Event ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Preparing CSV export...", Toast.LENGTH_SHORT).show();

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get the final list from Firebase
                    List<Object> finalListData = (List<Object>) snapshot.get("finalList");

                    if (finalListData == null || finalListData.isEmpty()) {
                        Toast.makeText(requireContext(), "No entrants in final list to export", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String eventName = snapshot.getString("eventName");
                    if (eventName == null) eventName = "Event";

                    // Extract user IDs from final list (handles both String and Map formats)
                    ArrayList<String> userIds = new ArrayList<>();
                    for (Object item : finalListData) {
                        if (item == null) continue;

                        String userId;
                        if (item instanceof String) {
                            userId = (String) item;
                        } else if (item instanceof Map) {
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
                        Toast.makeText(requireContext(), "No valid entrants found in final list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Fetch user details for all entrants
                    fetchUserDetailsAndGenerateCsv(userIds, eventName);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load event data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetch user details for all entrants and generate CSV file
     */
    private void fetchUserDetailsAndGenerateCsv(ArrayList<String> userIds, String eventName) {
        ArrayList<String[]> entrantData = new ArrayList<>();

        // Add CSV header
        entrantData.add(new String[]{"Name", "Email", "Phone Number", "User ID"});

        final int[] fetchedCount = {0};
        final int totalUsers = userIds.size();

        for (String userId : userIds) {
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.exists()) {
                            String name = userSnapshot.getString("name");
                            String email = userSnapshot.getString("email");
                            String phone = userSnapshot.getString("phoneNumber");

                            // Add user data to CSV
                            entrantData.add(new String[]{
                                    name != null ? name : "N/A",
                                    email != null ? email : "N/A",
                                    phone != null ? phone : "N/A",
                                    userId
                            });
                        }

                        fetchedCount[0]++;

                        // When all users are fetched, generate the CSV file
                        if (fetchedCount[0] == totalUsers) {
                            generateCsvFile(entrantData, eventName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        fetchedCount[0]++;

                        // Continue even if one user fails to load
                        if (fetchedCount[0] == totalUsers) {
                            generateCsvFile(entrantData, eventName);
                        }
                    });
        }
    }

    /**
     * Generate and save CSV file to device storage
     */
    private void generateCsvFile(ArrayList<String[]> data, String eventName) {
        try {
            // Create CSV content
            StringBuilder csvContent = new StringBuilder();
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    // Escape quotes and wrap in quotes if contains comma
                    String value = row[i].replace("\"", "\"\"");
                    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                        value = "\"" + value + "\"";
                    }
                    csvContent.append(value);
                    if (i < row.length - 1) {
                        csvContent.append(",");
                    }
                }
                csvContent.append("\n");
            }

            // Generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = eventName.replaceAll("[^a-zA-Z0-9]", "_") + "_FinalList_" + timestamp + ".csv";

            // Save file using MediaStore for Android 10+ or traditional method for older versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveCsvFileModern(csvContent.toString(), filename);
            } else {
                saveCsvFileLegacy(csvContent.toString(), filename);
            }

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to generate CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Save CSV file using MediaStore API (Android 10+)
     */
    @SuppressLint("InlinedApi")
    private void saveCsvFileModern(String csvContent, String filename) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            saveCsvFileLegacy(csvContent, filename);
            return;
        }

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(csvContent.getBytes());
                    outputStream.close();
                    Toast.makeText(requireContext(), "CSV exported successfully to Downloads/" + filename, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(), "Failed to create file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to save CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Save CSV file using legacy method (Android 9 and below)
     */
    private void saveCsvFileLegacy(String csvContent, String filename) {
        try {
            java.io.File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            java.io.File file = new java.io.File(downloadsDir, filename);

            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(csvContent);
            writer.close();

            Toast.makeText(requireContext(), "CSV exported successfully to Downloads/" + filename, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to save CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
