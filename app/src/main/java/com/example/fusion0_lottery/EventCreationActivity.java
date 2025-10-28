package com.example.fusion0_lottery;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.projectfusion0.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for creating events with Firebase integration
 * This activity allows organizers to:
 * - Create events with all required details
 * - Upload event posters
 * - Generate QR codes for event sharing
 * - Store everything in Firebase
 */
public class EventCreationActivity extends AppCompatActivity {

    // UI Elements - all the input fields from the layout
    private TextInputEditText eventNameInput, descriptionInput, startDateInput, endDateInput;
    private TextInputEditText timeInput, priceInput, locationInput, maxEntrantsInput;
    private TextInputEditText registrationStartInput, registrationEndInput;
    private Button uploadPosterButton, createEventButton, cancelButton, generateQrButton;
    private ImageView posterImageView, qrCodeImageView;

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Variables to store data
    private Uri posterImageUri; // Stores the selected poster image
    private String createdEventId; // Stores the ID of the created event

    // Request code for image picker
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize all UI elements
        initializeViews();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all the UI elements from the layout
     */
    private void initializeViews() {
        eventNameInput = findViewById(R.id.eventNameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        timeInput = findViewById(R.id.timeInput);
        priceInput = findViewById(R.id.priceInput);
        locationInput = findViewById(R.id.locationInput);
        maxEntrantsInput = findViewById(R.id.maxEntrantsInput);
        registrationStartInput = findViewById(R.id.registrationStartInput);
        registrationEndInput = findViewById(R.id.registrationEndInput);

        uploadPosterButton = findViewById(R.id.uploadPosterButton);
        createEventButton = findViewById(R.id.createEventButton);
        cancelButton = findViewById(R.id.cancelButton);
        generateQrButton = findViewById(R.id.generateQrButton);

        posterImageView = findViewById(R.id.posterImageView);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
    }

    /**
     * Set up all button click listeners
     */
    private void setupClickListeners() {
        // Date picker for Start Date
        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));

        // Date picker for End Date
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));

        // Date picker for Registration Start
        registrationStartInput.setOnClickListener(v -> showDatePicker(registrationStartInput));

        // Date picker for Registration End
        registrationEndInput.setOnClickListener(v -> showDatePicker(registrationEndInput));

        // Time picker for event time
        timeInput.setOnClickListener(v -> showTimePicker());

        // Upload poster image
        uploadPosterButton.setOnClickListener(v -> selectImage());

        // Create event button
        createEventButton.setOnClickListener(v -> createEvent());

        // Cancel button - just close the activity
        cancelButton.setOnClickListener(v -> finish());

        // Generate QR code button
        generateQrButton.setOnClickListener(v -> generateQRCode());
    }

    /**
     * Show a date picker dialog and set the selected date to the input field
     */
    private void showDatePicker(TextInputEditText inputField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format: YYYY-MM-DD
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    inputField.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    /**
     * Show a time picker dialog and set the selected time to the input field
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    // Format: HH:MM
                    String time = String.format(Locale.getDefault(), "%02d:%02d",
                            selectedHour, selectedMinute);
                    timeInput.setText(time);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    /**
     * Open image picker to select a poster image
     */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Event Poster"), PICK_IMAGE_REQUEST);
    }

    /**
     * Handle the result from image picker
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            posterImageUri = data.getData();
            posterImageView.setImageURI(posterImageUri);
            posterImageView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Poster selected!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate all input fields before creating the event
     * Returns true if all required fields are filled correctly
     */
    private boolean validateInputs() {
        // Check if required fields are empty
        if (eventNameInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter event name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (descriptionInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (startDateInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select start date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (endDateInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select end date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (timeInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (priceInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter price", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (locationInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (registrationStartInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select registration start date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (registrationEndInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select registration end date", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate dates - end date should be after start date
        if (!validateDateOrder(startDateInput.getText().toString(), endDateInput.getText().toString())) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate registration dates
        if (!validateDateOrder(registrationStartInput.getText().toString(), registrationEndInput.getText().toString())) {
            Toast.makeText(this, "Registration end date must be after registration start date", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Check if date2 is after date1
     */
    private boolean validateDateOrder(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date startDate = sdf.parse(date1);
            Date endDate = sdf.parse(date2);
            return endDate.after(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create the event and save it to Firebase
     */
    private void createEvent() {
        // First validate all inputs
        if (!validateInputs()) {
            return;
        }

        // Show loading message
        Toast.makeText(this, "Creating event...", Toast.LENGTH_SHORT).show();

        // Get all the input values
        String eventName = eventNameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        double price = Double.parseDouble(priceInput.getText().toString().trim());
        String location = locationInput.getText().toString().trim();
        String registrationStart = registrationStartInput.getText().toString().trim();
        String registrationEnd = registrationEndInput.getText().toString().trim();

        // Max entrants is optional
        Integer maxEntrants = null;
        if (!maxEntrantsInput.getText().toString().trim().isEmpty()) {
            maxEntrants = Integer.parseInt(maxEntrantsInput.getText().toString().trim());
        }

        // Create Event object
        Event event = new Event(eventName, description, startDate, endDate, time,
                price, location, registrationStart, registrationEnd, maxEntrants);

        // If poster is selected, upload it first, then save event
        if (posterImageUri != null) {
            uploadPosterAndSaveEvent(event);
        } else {
            // No poster, just save the event
            saveEventToFirestore(event);
        }
    }

    /**
     * Upload poster image to Firebase Storage, then save event to Firestore
     */
    private void uploadPosterAndSaveEvent(Event event) {
        // Create a unique filename for the poster
        String fileName = "event_posters/" + System.currentTimeMillis() + ".jpg";
        StorageReference posterRef = storageRef.child(fileName);

        // Upload the image
        posterRef.putFile(posterImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Set the poster URL in the event
                        event.setPosterUrl(uri.toString());
                        // Now save the event to Firestore
                        saveEventToFirestore(event);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload poster: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Save the event to Firebase Firestore
     */
    private void saveEventToFirestore(Event event) {
        db.collection("Events")
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    // Store the document ID
                    createdEventId = documentReference.getId();

                    // Update the event with its ID
                    documentReference.update("eventId", createdEventId);

                    Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();

                    // Show the Generate QR Code button
                    generateQrButton.setVisibility(View.VISIBLE);

                    // Disable the create button to prevent duplicate creation
                    createEventButton.setEnabled(false);
                    createEventButton.setText("Event Created");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Generate QR code for the event
     * The QR code contains the event ID that can be scanned to view event details
     */
    private void generateQRCode() {
        if (createdEventId == null) {
            Toast.makeText(this, "Please create an event first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // The QR code will contain the event ID
            // You can modify this to include a deep link or URL to your app
            String qrContent = "event://" + createdEventId;

            // Generate the QR code bitmap
            Bitmap qrBitmap = generateQRCodeBitmap(qrContent, 500, 500);

            // Display the QR code
            qrCodeImageView.setImageBitmap(qrBitmap);
            qrCodeImageView.setVisibility(View.VISIBLE);

            // Optionally, upload the QR code to Firebase Storage
            uploadQRCodeToStorage(qrBitmap);

            Toast.makeText(this, "QR Code generated!", Toast.LENGTH_SHORT).show();

        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR code: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Generate a QR code bitmap from a string
     */
    private Bitmap generateQRCodeBitmap(String content, int width, int height) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }

    /**
     * Upload QR code image to Firebase Storage and update the event
     */
    private void uploadQRCodeToStorage(Bitmap qrBitmap) {
        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create a unique filename for the QR code
        String fileName = "qr_codes/" + createdEventId + ".png";
        StorageReference qrRef = storageRef.child(fileName);

        // Upload the QR code
        qrRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    qrRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update the event with the QR code URL
                        db.collection("Events").document(createdEventId)
                                .update("qrCodeUrl", uri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "QR Code saved to cloud!",
                                            Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload QR code: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
