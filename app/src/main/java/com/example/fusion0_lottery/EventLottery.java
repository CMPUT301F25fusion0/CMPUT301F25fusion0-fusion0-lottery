package com.example.fusion0_lottery;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventLottery extends Fragment {

    private LinearLayout eventsContainer;
    private Button buttonBack, buttonApplyFilters, buttonStartDate, buttonEndDate;
    private Spinner spinnerInterest;
    private FirebaseFirestore db;
    private String userEmail;

    private String selectedInterest = "All";
    private String selectedStartDate = null;
    private String selectedEndDate = null;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public EventLottery() {}

    public static EventLottery newInstance(String userEmail) {
        EventLottery fragment = new EventLottery();
        Bundle args = new Bundle();
        args.putString("userEmail", userEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_lottery, container, false);

        // UI setup
        eventsContainer = view.findViewById(R.id.eventsContainer);
        buttonBack = view.findViewById(R.id.buttonBack);
        spinnerInterest = view.findViewById(R.id.spinnerInterest);
        buttonApplyFilters = view.findViewById(R.id.buttonApplyFilters);
        buttonStartDate = view.findViewById(R.id.buttonStartDate);
        buttonEndDate = view.findViewById(R.id.buttonEndDate);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        db = FirebaseFirestore.getInstance();

        // Retrieve saved arguments (filter states)
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
            selectedInterest = getArguments().getString("selectedInterest", "All");
            selectedStartDate = getArguments().getString("selectedStartDate", null);
            selectedEndDate = getArguments().getString("selectedEndDate", null);
        }

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.interests_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInterest.setAdapter(adapter);

        // Restore spinner selection
        if (selectedInterest != null) {
            int position = adapter.getPosition(selectedInterest);
            if (position >= 0) spinnerInterest.setSelection(position);
        }

        // Restore date button text
        if (selectedStartDate != null)
            buttonStartDate.setText("Start: " + selectedStartDate);
        if (selectedEndDate != null)
            buttonEndDate.setText("End: " + selectedEndDate);

        // Date pickers
        buttonStartDate.setOnClickListener(v -> showDatePicker(true));
        buttonEndDate.setOnClickListener(v -> showDatePicker(false));

        // Apply filters
        buttonApplyFilters.setOnClickListener(v -> {
            selectedInterest = spinnerInterest.getSelectedItem().toString();
            applyFilters();
        });

        // Auto-load: if filters exist, apply them; else load all
        if (!selectedInterest.equals("All") || selectedStartDate != null || selectedEndDate != null) {
            applyFilters();
        } else {
            loadEvents();
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save filter state when leaving fragment
        Bundle args = getArguments();
        if (args != null) {
            args.putString("selectedInterest", selectedInterest);
            args.putString("selectedStartDate", selectedStartDate);
            args.putString("selectedEndDate", selectedEndDate);
        }
    }

    private void showDatePicker(boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String pickedDate = String.format(Locale.US, "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    if (isStartDate) {
                        selectedStartDate = pickedDate;
                        buttonStartDate.setText("Start: " + pickedDate);
                    } else {
                        selectedEndDate = pickedDate;
                        buttonEndDate.setText("End: " + pickedDate);
                    }
                }, year, month, day);
        dialog.show();
    }

    private void loadEvents() {
        db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("EventLottery", "Fetched " + queryDocumentSnapshots.size() + " events.");
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    displayEvents(queryDocumentSnapshots.getDocuments());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EventLottery", "Firestore fetch error", e);
                });
    }

    private void applyFilters() {
        db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DocumentSnapshot> filteredEvents = queryDocumentSnapshots.getDocuments();

                    // Interest filter
                    if (!selectedInterest.equals("All")) {
                        filteredEvents.removeIf(event ->
                                event.get("interest") == null ||
                                        !selectedInterest.equalsIgnoreCase(event.getString("interest"))
                        );
                    }

                    // Availability filter
                    if (selectedStartDate != null && selectedEndDate != null) {
                        try {
                            Date startFilter = dateFormat.parse(selectedStartDate);
                            Date endFilter = dateFormat.parse(selectedEndDate);

                            filteredEvents.removeIf(event -> {
                                try {
                                    Date eventStart = dateFormat.parse(event.getString("startDate"));
                                    Date eventEnd = dateFormat.parse(event.getString("endDate"));
                                    // Remove events that fall completely outside user's availability
                                    return eventStart.after(endFilter) || eventEnd.before(startFilter);
                                } catch (Exception e) {
                                    return true;
                                }
                            });
                        } catch (ParseException e) {
                            Toast.makeText(getContext(), "Invalid date filter", Toast.LENGTH_SHORT).show();
                        }
                    }

                    displayEvents(filteredEvents);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error filtering events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void displayEvents(List<DocumentSnapshot> events) {
        if (eventsContainer == null) return;
        eventsContainer.removeAllViews();

        for (DocumentSnapshot eventDoc : events) {
            String eventName = eventDoc.getString("eventName");
            String eventStartDate = eventDoc.getString("startDate");
            String eventLocation = eventDoc.getString("location");
            List<String> waitingList = (List<String>) eventDoc.get("waitingList");
            int waitlistCount = waitingList != null ? waitingList.size() : 0;
            String eventId = eventDoc.getId();

            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            card.setPadding(24, 24, 24, 24);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 24);
            card.setLayoutParams(cardParams);

            TextView nameText = new TextView(getContext());
            nameText.setText("Event: " + eventName);
            nameText.setTextSize(20f);
            nameText.setTextColor(getResources().getColor(android.R.color.black));
            nameText.setPadding(0, 0, 0, 8);

            TextView dateText = new TextView(getContext());
            dateText.setText("Start Date: " + eventStartDate);
            dateText.setPadding(0, 0, 0, 8);

            TextView locationText = new TextView(getContext());
            locationText.setText("Location: " + eventLocation);
            locationText.setPadding(0, 0, 0, 8);

            TextView waitlistText = new TextView(getContext());
            waitlistText.setText("Current Waitlist: " + waitlistCount);
            waitlistText.setPadding(0, 0, 0, 16);

            Button viewDetailsBtn = new Button(getContext());
            viewDetailsBtn.setText("View Details");
            viewDetailsBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
            viewDetailsBtn.setTextColor(getResources().getColor(android.R.color.white));

            viewDetailsBtn.setOnClickListener(v -> {
                db.collection("Events").document(eventId)
                        .get()
                        .addOnSuccessListener(eventSnapshot -> {
                            if (eventSnapshot.exists()) {
                                String eventNameStr = eventSnapshot.getString("eventName");
                                String eventDescStr = eventSnapshot.getString("description");
                                String eventStartDateStr = eventSnapshot.getString("startDate");
                                String eventLocationStr = eventSnapshot.getString("location");
                                String regStart = eventSnapshot.getString("registrationStart");
                                String regEnd = eventSnapshot.getString("registrationEnd");
                                Long maxEntrantsVal = eventSnapshot.getLong("maxEntrants");
                                Double priceVal = eventSnapshot.getDouble("price");
                                List<String> fullWaitlist = (List<String>) eventSnapshot.get("waitingList");
                                boolean isOnWaitlist = fullWaitlist != null && fullWaitlist.contains(userEmail);
                                boolean waitingListClosed = eventSnapshot.getBoolean("waitingListClosed") != null
                                        ? eventSnapshot.getBoolean("waitingListClosed") : false;
                                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                // Pass along filter state too so it stays when we come back
                                Bundle args = getArguments();
                                if (args != null) {
                                    args.putString("selectedInterest", selectedInterest);
                                    args.putString("selectedStartDate", selectedStartDate);
                                    args.putString("selectedEndDate", selectedEndDate);
                                }

                                getParentFragmentManager()
                                        .beginTransaction()
                                        .replace(
                                                R.id.fragment_container,
                                                EventFragmentEntrant.newInstance(
                                                        eventId,
                                                        currentUserId,
                                                        eventNameStr != null ? eventNameStr : "No Name",
                                                        eventDescStr != null ? eventDescStr : "No Description",
                                                        eventStartDateStr != null ? eventStartDateStr : "No Date",
                                                        eventLocationStr != null ? eventLocationStr : "No Location",
                                                        isOnWaitlist,
                                                        regStart != null ? regStart : "",
                                                        regEnd != null ? regEnd : "",
                                                        maxEntrantsVal != null ? maxEntrantsVal : 0L,
                                                        priceVal != null ? priceVal : 0.0,
                                                        waitingListClosed
                                                )
                                        )
                                        .addToBackStack(null)
                                        .commit();

                            } else {
                                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            card.addView(nameText);
            card.addView(dateText);
            card.addView(locationText);
            card.addView(waitlistText);
            card.addView(viewDetailsBtn);

            eventsContainer.addView(card);
        }
    }
}
