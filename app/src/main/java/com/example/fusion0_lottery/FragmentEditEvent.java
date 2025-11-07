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

    private EditText titleInput, eventDescriptionInput, startDateInput, endDateInput, timeInput, priceInput, maxEntrantsInput;
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
        startDateInput = view.findViewById(R.id.editStartDate);
        endDateInput = view.findViewById(R.id.editEndDate);
        timeInput = view.findViewById(R.id.editTime);
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
                            startDateInput.setText(event.getStartDate());
                            endDateInput.setText(event.getEndDate());
                            timeInput.setText(event.getTime());
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
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        String price = priceInput.getText().toString().trim();
        String maxEntrants = maxEntrantsInput.getText().toString().trim();

        // check if all the fields are filled and not empty
        if (title.isEmpty() || eventDescription.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || time.isEmpty() || price.isEmpty() || maxEntrants.isEmpty()) {
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
                        "startDate", startDate,
                        "endDate", endDate,
                        "time", time,
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

    void setMockInputs(EditText title, EditText desc, EditText start, EditText end,
                       EditText time, EditText price, EditText max) {
        this.titleInput = title;
        this.eventDescriptionInput = desc;
        this.startDateInput = start;
        this.endDateInput = end;
        this.timeInput = time;
        this.priceInput = price;
        this.maxEntrantsInput = max;
    }


    void testSaveEventChanges(String title, String eventDescription, String startDate,
                              String endDate, String time, String price, String maxEntrants) {

        if (title.isEmpty() || eventDescription.isEmpty() || startDate.isEmpty() ||
                endDate.isEmpty() || time.isEmpty() || price.isEmpty() || maxEntrants.isEmpty()) {
            return;
        }

        double doublePrice = Double.parseDouble(price);
        int intMaxEntrants = Integer.parseInt(maxEntrants);

        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.update(
                "eventName", title,
                "description", eventDescription,
                "startDate", startDate,
                "endDate", endDate,
                "time", time,
                "price", doublePrice,
                "maxEntrants", intMaxEntrants
        );
    }
}
