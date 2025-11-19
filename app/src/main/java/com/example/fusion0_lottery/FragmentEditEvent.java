package com.example.fusion0_lottery;

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

public class FragmentEditEvent extends Fragment {

    private EditText titleInput, eventDescriptionInput, interestsInput, locationInput, startDateInput, endDateInput, timeInput, priceInput, maxEntrantsInput;
    private Button saveButton;

    FirebaseFirestore db;
    String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
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

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventDetails();
        }

        saveButton.setOnClickListener(v -> saveEventChanges());

        return view;
    }

    /**
     * function to get all of the event's details
     * then set all of the inputs as the existing event details
     * organizers can then update the information using saveEventChanges()
     */
    private void loadEventDetails() {
        if (eventId == null) return;
        // find event based on event ID
        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // get event data from firestore
                        Event event = snapshot.toObject(Event.class);
                        if (event != null) {
                            // get all information from the event if the event exists and automatically set it in the inputs
                            titleInput.setText(event.getEventName());
                            eventDescriptionInput.setText(event.getDescription());
                            interestsInput.setText(event.getInterests());
                            startDateInput.setText(event.getStartDate());
                            endDateInput.setText(event.getEndDate());
                            timeInput.setText(event.getTime());
                            locationInput.setText(event.getLocation());
                            priceInput.setText(String.valueOf(event.getPrice()));
                            maxEntrantsInput.setText(String.valueOf(event.getMaxEntrants()));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    /**
     * function to allow organizer to make changes to an event and save the changes
     * allow organizer inputs as strings
     * if any of the field are empty, display error message
     * convert any fields to different datatypes if needed
     * once changes are saved, return to Manage Events screen
     */
    private void saveEventChanges() {
        String title = titleInput.getText().toString().trim();
        String eventDescription = eventDescriptionInput.getText().toString().trim();
        String interests = interestsInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String price = priceInput.getText().toString().trim();
        String maxEntrants = maxEntrantsInput.getText().toString().trim();

        // check if all the fields are filled and not empty
        if (title.isEmpty() || eventDescription.isEmpty() || interests.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || time.isEmpty() || location.isEmpty() || price.isEmpty() || maxEntrants.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // convert the price and max entrants to double and int
        double doublePrice = Double.parseDouble(price);
        int intMaxEntrants = Integer.parseInt(maxEntrants);

        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.update(
                        "eventName", title,
                        "description", eventDescription,
                        "interests", interests,
                        "startDate", startDate,
                        "endDate", endDate,
                        "time", time,
                        "location", location,
                        "price", doublePrice,
                        "maxEntrants", intMaxEntrants
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    // go back to Manage Events screen after updating
                    ManageEvents manageEvents = new ManageEvents();
                    Bundle args = new Bundle();
                    args.putString("eventId", eventId);
                    manageEvents.setArguments(args);
                    ((MainActivity) requireActivity()).replaceFragment(manageEvents);
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }




    // testing inputs
    void testingEdit(EditText title1, EditText text, EditText editText, EditText title, EditText desc, EditText start, EditText end,
                     EditText time, EditText location, EditText price, EditText max, EditText interests) {
        this.titleInput = title;
        this.eventDescriptionInput = desc;
        this.startDateInput = start;
        this.endDateInput = end;
        this.timeInput = time;
        this.locationInput = location;
        this.priceInput = price;
        this.maxEntrantsInput = max;
        this.interestsInput = interests;
    }


    /**
     * make sure that all of the fields are filled
     * @return true none of the fields are empty
     */
    boolean validateInputs() {
        if (titleInput.getText().toString().trim().isEmpty() || eventDescriptionInput.getText().toString().trim().isEmpty() || interestsInput.getText().toString().trim().isEmpty() ||
                startDateInput.getText().toString().trim().isEmpty() || endDateInput.getText().toString().trim().isEmpty() ||  timeInput.getText().toString().trim().isEmpty() ||
                locationInput.getText().toString().trim().isEmpty() || priceInput.getText().toString().trim().isEmpty() || maxEntrantsInput.getText().toString().trim().isEmpty()) {
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
