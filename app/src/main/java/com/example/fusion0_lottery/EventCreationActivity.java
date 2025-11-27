package com.example.fusion0_lottery;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private TextInputEditText eventNameInput, interestInput, descriptionInput, startDateInput, endDateInput;
    private TextInputEditText timeInput, priceInput, locationInput, maxEntrantsInput, winnerInput, lotteryCriteriaInput;

    private TextInputEditText registrationStartInput, registrationEndInput;
    private Button uploadPosterButton, createEventButton, cancelButton;
    private CheckBox generateQrCheckbox, requireGeolocationCheckbox;
    private ImageView posterImageView;

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
        interestInput = findViewById(R.id.interestInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        timeInput = findViewById(R.id.timeInput);
        priceInput = findViewById(R.id.priceInput);
        winnerInput = findViewById(R.id.winnerInput);

        locationInput = findViewById(R.id.locationInput);
        maxEntrantsInput = findViewById(R.id.maxEntrantsInput);
        registrationStartInput = findViewById(R.id.registrationStartInput);
        registrationEndInput = findViewById(R.id.registrationEndInput);

        uploadPosterButton = findViewById(R.id.uploadPosterButton);
        createEventButton = findViewById(R.id.createEventButton);
        cancelButton = findViewById(R.id.cancelButton);
        generateQrCheckbox = findViewById(R.id.generateQrCheckbox);
        requireGeolocationCheckbox = findViewById(R.id.requireGeolocationCheckbox);

        posterImageView = findViewById(R.id.posterImageView);
        lotteryCriteriaInput = findViewById(R.id.lotteryCriteriaInput);

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
        if (interestInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter interests", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (descriptionInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (interestInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter interests", Toast.LENGTH_SHORT).show();
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
        if (winnerInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter number of Winners", Toast.LENGTH_SHORT).show();
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
        if (lotteryCriteriaInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter criteria or guidelines for this event", Toast.LENGTH_SHORT).show();
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
    /**
     * Check if date2 is after date1.
     * Handles null or empty strings gracefully.
     */
    private boolean validateDateOrder(String date1, String date2) {
        // If either date string is null or empty, we can't validate.
        // We return 'true' to let the empty-check validation handle the user message.
        if (date1 == null || date1.trim().isEmpty() || date2 == null || date2.trim().isEmpty()) {
            return true; // Let the required field validator catch this
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date startDate = sdf.parse(date1);
            Date endDate = sdf.parse(date2);
            // Important: Use !endDate.before(startDate) to allow same-day events.
            // If End Date must strictly be AFTER Start Date, use endDate.after(startDate)
            return endDate.after(startDate);
        } catch (ParseException e) {
            // This can happen if the date format is wrong, but our DatePicker prevents this.
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
        String interests = interestInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        double price = Double.parseDouble(priceInput.getText().toString().trim());
        Integer numberOfWinners = Integer.parseInt(winnerInput.getText().toString().trim());
        String location = locationInput.getText().toString().trim();
        String registrationStart = registrationStartInput.getText().toString().trim();
        String registrationEnd = registrationEndInput.getText().toString().trim();
        String lotteryCriteria = lotteryCriteriaInput.getText().toString().trim();


        // Max entrants is optional
        Integer maxEntrants = null;
        if (!maxEntrantsInput.getText().toString().trim().isEmpty()) {
            maxEntrants = Integer.parseInt(maxEntrantsInput.getText().toString().trim());
        }
        // Create Event object
        Event event = new Event(eventName, interests, description, startDate, endDate, time,
                price, location, registrationStart, registrationEnd, maxEntrants,
                0, 0, 0, numberOfWinners, lotteryCriteria);

        // First save event to Firestore
        db.collection("Events")
                // add the event to Events section in Firestore
                .add(event)
                // when successfully added:
                .addOnSuccessListener(documentReference -> {
                    // create the event ID
                    createdEventId = documentReference.getId();
                    // Update event ID, QR code setting, and geolocation requirement
                    documentReference.update("eventId", createdEventId,
                            "hasQrCode", generateQrCheckbox.isChecked(),
                            "requiresGeolocation", requireGeolocationCheckbox.isChecked());

                    if (posterImageUri != null) {
                        // upload the poster first then save the event
                        uploadPosterAndSaveEvent(posterImageUri);
                    }
                    else {
                        // if the poster DNE, save the event without it
                        Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Upload poster image to Firebase Storage, then save event to Firestore
     * Convert image to Base64 then upload to Firestore
     * References:
     *     https://stackoverflow.com/questions/65210522/how-to-get-bitmap-from-imageuri-in-api-level-30
     *     https://stackoverflow.com/questions/4830711/how-can-i-convert-an-image-into-a-base64-string
     *     https://stackoverflow.com/questions/9224056/android-bitmap-to-base64-string
     */
    private void uploadPosterAndSaveEvent(Uri posterUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), posterUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
            byte[] image = baos.toByteArray();

            String encodedImage = Base64.encodeToString(image, Base64.DEFAULT);

            db.collection("Events").document(createdEventId)
                    .update("posterImage", encodedImage)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Poster uploaded successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error uploading poster: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing poster image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

                    // Update the event with its ID, QR code enabled flag, and geolocation requirement
                    documentReference.update("eventId", createdEventId,
                            "hasQrCode", generateQrCheckbox.isChecked(),
                            "requiresGeolocation", requireGeolocationCheckbox.isChecked());

                    Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
