package com.example.fusion0_lottery;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class FragmentEditEvent extends Fragment {

    private EditText titleInput, eventDescriptionInput, interestsInput, locationInput,
            startDateInput, endDateInput, timeInput, priceInput, maxEntrantsInput, lotteryCriteriaInput;
    private Button saveButton;

    FirebaseFirestore db;
    String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_event, container, false);

        titleInput = view.findViewById(R.id.editEventTitle);
        eventDescriptionInput = view.findViewById(R.id.editEventDescription);
        interestsInput = view.findViewById(R.id.editEventInterests);
        startDateInput = view.findViewById(R.id.editStartDate);
        endDateInput = view.findViewById(R.id.editEndDate);
        timeInput = view.findViewById(R.id.editTime);
        locationInput = view.findViewById(R.id.editLocation);
        priceInput = view.findViewById(R.id.editPrice);
        maxEntrantsInput = view.findViewById(R.id.editMaxEntrants);
        saveButton = view.findViewById(R.id.saveEventButton);
        lotteryCriteriaInput = view.findViewById(R.id.editLotteryCriteria);


        db = FirebaseFirestore.getInstance();

        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));
        timeInput.setOnClickListener(v -> showTimePicker(timeInput));

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventDetails();
        }

        saveButton.setOnClickListener(v -> saveEventChanges());

        return view;
    }

    private void showDatePicker(EditText inputField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    inputField.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePicker(EditText inputField) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d",
                            selectedHour, selectedMinute);
                    inputField.setText(time);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private void loadEventDetails() {
        if (eventId == null) return;

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        return;
                    }
                    Event event = snapshot.toObject(Event.class);
                    if (event == null) {
                        return;
                    }

                    titleInput.setText(event.getEventName());
                    eventDescriptionInput.setText(event.getDescription());
                    interestsInput.setText(event.getInterests());
                    startDateInput.setText(event.getStartDate());
                    endDateInput.setText(event.getEndDate());
                    timeInput.setText(event.getTime());
                    locationInput.setText(event.getLocation());
                    priceInput.setText(String.valueOf(event.getPrice()));
                    maxEntrantsInput.setText(String.valueOf(event.getMaxEntrants()));
                    lotteryCriteriaInput.setText(event.getLotteryCriteria());
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveEventChanges() {
        // Validate
        if (!validateInputs()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare values
        double doublePrice = Double.parseDouble(priceInput.getText().toString().trim());
        int intMaxEntrants = Integer.parseInt(maxEntrantsInput.getText().toString().trim());

        db.collection("Events").document(eventId)
                .update(
                        "eventName", titleInput.getText().toString().trim(),
                        "description", eventDescriptionInput.getText().toString().trim(),
                        "interests", interestsInput.getText().toString().trim(),
                        "startDate", startDateInput.getText().toString().trim(),
                        "endDate", endDateInput.getText().toString().trim(),
                        "time", timeInput.getText().toString().trim(),
                        "location", locationInput.getText().toString().trim(),
                        "lotteryCriteria", lotteryCriteriaInput.getText().toString().trim(),
                        "price", doublePrice,
                        "maxEntrants", intMaxEntrants
                )
                .addOnSuccessListener(aVoid -> {Toast.makeText(requireContext(), "Event updated!", Toast.LENGTH_SHORT).show();
                    ((MainActivity) requireActivity()).replaceFragment(new ManageEvents());
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** * make sure that all of the fields are filled * @return true none of the fields are empty */
    boolean validateInputs() {
        if (titleInput.getText().toString().trim().isEmpty() ||
                eventDescriptionInput.getText().toString().trim().isEmpty() ||
                interestsInput.getText().toString().trim().isEmpty() ||
                startDateInput.getText().toString().trim().isEmpty() ||
                endDateInput.getText().toString().trim().isEmpty() ||
                timeInput.getText().toString().trim().isEmpty() ||
                locationInput.getText().toString().trim().isEmpty() ||
                priceInput.getText().toString().trim().isEmpty() ||
                maxEntrantsInput.getText().toString().trim().isEmpty() ||
                lotteryCriteriaInput.getText().toString().trim().isEmpty()){
            return false;
        }
        else {
            return true;
        }
    }


    /**
     * check if the information is saves after editing
     */
    boolean editThenSave() {
        if (!validateInputs()) {
            return false;
        }
        return true;
    }


}
