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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;


public class EventLottery extends Fragment {

    private LinearLayout eventsContainer;
    private Button buttonBack, buttonApplyFilters, buttonStartDate, buttonEndDate, buttonClearFilters;
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

        eventsContainer = view.findViewById(R.id.eventsContainer);
        buttonBack = view.findViewById(R.id.buttonBack);
        spinnerInterest = view.findViewById(R.id.spinnerInterest);
        buttonApplyFilters = view.findViewById(R.id.buttonApplyFilters);
        buttonStartDate = view.findViewById(R.id.buttonStartDate);
        buttonEndDate = view.findViewById(R.id.buttonEndDate);
        Button buttonClearFilters = view.findViewById(R.id.buttonClearFilters);

        buttonClearFilters.setOnClickListener(v -> clearFilters());


        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        db = FirebaseFirestore.getInstance();

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
        if (selectedInterest != null) {
            int position = adapter.getPosition(selectedInterest);
            if (position >= 0) spinnerInterest.setSelection(position);
        }

        if (selectedStartDate != null) buttonStartDate.setText("Start: " + selectedStartDate);
        if (selectedEndDate != null) buttonEndDate.setText("End: " + selectedEndDate);

        buttonStartDate.setOnClickListener(v -> showDatePicker(true));
        buttonEndDate.setOnClickListener(v -> showDatePicker(false));

        buttonApplyFilters.setOnClickListener(v -> {
            selectedInterest = spinnerInterest.getSelectedItem().toString();
            applyFilters();
        });

        // Load events automatically
        if (!selectedInterest.equals("All") || selectedStartDate != null || selectedEndDate != null) {
            applyFilters();
        } else {
            loadEvents();
        }

        return view;
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
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DocumentSnapshot> joinableEvents = new ArrayList<>();
                    for (DocumentSnapshot eventDoc : queryDocumentSnapshots.getDocuments()) {
                        if (canJoinWaitingList(eventDoc)) joinableEvents.add(eventDoc);
                    }

                    displayEvents(joinableEvents);
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

                    List<DocumentSnapshot> filteredEvents = new ArrayList<>();

                    for (DocumentSnapshot event : queryDocumentSnapshots.getDocuments()) {
                        // Interest filter
                        if (!selectedInterest.equals("All")) {
                            String eventInterest = event.getString("interests");
                            if (eventInterest == null || !selectedInterest.equalsIgnoreCase(eventInterest.trim())) continue;
                        }

                        // Availability filter (optional)
                        if (selectedStartDate != null || selectedEndDate != null) {
                            try {
                                Date filterStart = selectedStartDate != null ? dateFormat.parse(selectedStartDate) : null;
                                Date filterEnd = selectedEndDate != null ? dateFormat.parse(selectedEndDate) : null;
                                Date eventStart = dateFormat.parse(event.getString("startDate"));
                                Date eventEnd = dateFormat.parse(event.getString("endDate"));

                                if ((filterStart != null && eventEnd.before(filterStart)) ||
                                        (filterEnd != null && eventStart.after(filterEnd))) {
                                    continue; // event outside filter range
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }
                        }

                        // Can join waiting list
                        if (!canJoinWaitingList(event)) continue;

                        filteredEvents.add(event);
                    }

                    displayEvents(filteredEvents);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error filtering events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFilters() {
        // Reset filter variables
        selectedInterest = "All";
        selectedStartDate = null;
        selectedEndDate = null;

        // Reset UI elements
        spinnerInterest.setSelection(0); // "All" position
        buttonStartDate.setText("Start: All");
        buttonEndDate.setText("End: All");

        // Reload all events
        loadEvents();
    }


    private boolean canJoinWaitingList(DocumentSnapshot eventDoc) {
        try {
            // Registration end inclusive
            String regEndStr = eventDoc.getString("registrationEnd");
            if (regEndStr != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date regEnd = sdf.parse(regEndStr);

                Calendar cal = Calendar.getInstance();
                cal.setTime(regEnd);
                cal.add(Calendar.DATE, 1); // include last day
                Date regEndInclusive = cal.getTime();

                Date today = new Date();
                if (today.after(regEndInclusive)) return false;
            }

            // Waiting list full
            Long maxEntrants = eventDoc.getLong("maxEntrants");
            List<String> waitingList = (List<String>) eventDoc.get("waitingList");
            int waitlistCount = waitingList != null ? waitingList.size() : 0;
            if (maxEntrants != null && maxEntrants > 0 && waitlistCount >= maxEntrants) return false;

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
                String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

                EventFragmentEntrant fragment = EventFragmentEntrant.newInstance(
                        eventId,
                        currentUserId,
                        eventDoc.getString("eventName"),
                        eventDoc.getString("description"),
                        eventDoc.getString("startDate"),
                        eventDoc.getString("location"),
                        false,
                        eventDoc.getString("registrationStart"),
                        eventDoc.getString("registrationEnd"),
                        eventDoc.getLong("maxEntrants"),
                        eventDoc.getDouble("price"),
                        false
                );

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
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
