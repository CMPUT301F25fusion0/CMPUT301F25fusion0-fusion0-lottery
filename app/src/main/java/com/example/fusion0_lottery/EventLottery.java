package com.example.fusion0_lottery;

import android.app.DatePickerDialog;
import android.content.Intent;
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

/**
 * EventLottery.java
 *
 * Fragment that displays events available to users in the lottery system.
 * Users can filter events by interest and date, view details, and join/leave waiting lists.
 * Also supports QR code scanning for quick event access.
 */
public class EventLottery extends Fragment {

    private LinearLayout eventsContainer;
    private Button buttonBack, buttonApplyFilters, buttonStartDate, buttonEndDate, buttonClearFilters, scan_qr;
    private Spinner spinnerInterest;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private String userEmail;

    private static final int QR_SCAN_REQUEST_CODE = 100;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_lottery, container, false);

        // Toolbar
        toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Event Lottery");
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.event_lottery_menu);

            toolbar.setNavigationOnClickListener(v ->
                    getParentFragmentManager().popBackStack());

            toolbar.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_notifications) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new NotificationCenterFragment())
                            .addToBackStack("NotificationCenter")
                            .commit();
                    return true;
                } else if (id == R.id.action_account_settings) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new UpdateProfileFragment())
                            .addToBackStack("UpdateProfile")
                            .commit();
                    return true;
                } else if (id == R.id.action_history) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new HistoryFragment())
                            .addToBackStack("History")
                            .commit();
                    return true;
                }
                return false;
            });
        }

        // Views
        eventsContainer     = view.findViewById(R.id.eventsContainer);
        buttonBack          = view.findViewById(R.id.buttonBack);
        scan_qr             = view.findViewById(R.id.scan_qr);
        buttonApplyFilters  = view.findViewById(R.id.buttonApplyFilters);
        buttonStartDate     = view.findViewById(R.id.buttonStartDate);
        buttonEndDate       = view.findViewById(R.id.buttonEndDate);
        buttonClearFilters  = view.findViewById(R.id.buttonClearFilters);

        buttonBack.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).replaceFragment(new FragmentRoleSelection());
        });
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            userEmail        = getArguments().getString("userEmail");
            selectedInterest = getArguments().getString("selectedInterest", "All");
            selectedStartDate= getArguments().getString("selectedStartDate", null);
            selectedEndDate  = getArguments().getString("selectedEndDate", null);
        }

        // QR scan
        if (scan_qr != null) {
            scan_qr.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), QRScannerActivity.class);
                startActivityForResult(intent, QR_SCAN_REQUEST_CODE);
            });
        }

        // Spinner
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

        // Pre-fill date buttons
        if (selectedStartDate != null) buttonStartDate.setText("Start: " + selectedStartDate);
        if (selectedEndDate != null)   buttonEndDate.setText("End: " + selectedEndDate);

        buttonStartDate.setOnClickListener(v -> showDatePicker(true));
        buttonEndDate.setOnClickListener(v -> showDatePicker(false));

        buttonApplyFilters.setOnClickListener(v -> {
            selectedInterest = spinnerInterest.getSelectedItem().toString();
            applyFilters();
        });

        buttonClearFilters.setOnClickListener(v -> clearFilters());

        // Load events
        if (!selectedInterest.equals("All") || selectedStartDate != null || selectedEndDate != null) {
            applyFilters();
        } else {
            loadEvents();
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_SCAN_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK && data != null) {
                String eventId = data.getStringExtra("EVENT_ID");
                if (eventId != null && !eventId.isEmpty()) {
                    eventDetails(eventId);
                }
            } else {
                Toast.makeText(getContext(), "QR scan cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Load details and navigate to entrant fragment. */
    private void eventDetails(String eventId) {
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    if (eventSnapshot.exists()) {
                        String eventName   = eventSnapshot.getString("eventName");
                        String description = eventSnapshot.getString("description");
                        String interests   = eventSnapshot.getString("interests");
                        String startDate   = eventSnapshot.getString("startDate");
                        String location    = eventSnapshot.getString("location");
                        String regStart    = eventSnapshot.getString("registrationStart");
                        String regEnd      = eventSnapshot.getString("registrationEnd");
                        Long   maxEntrants = eventSnapshot.getLong("maxEntrants");
                        Double price       = eventSnapshot.getDouble("price");

                        List<String> waitingList = (List<String>) eventSnapshot.get("waitingList");
                        boolean isOnWaitlist = waitingList != null && userEmail != null && waitingList.contains(userEmail);

                        boolean waitingListClosed = Boolean.TRUE.equals(eventSnapshot.getBoolean("waitingListClosed"));

                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                : "";

                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(
                                        R.id.fragment_container,
                                        EventFragmentEntrant.newInstance(
                                                eventId,
                                                currentUserId,
                                                eventName != null ? eventName : "No Name",
                                                description != null ? description : "No Description",
                                                interests != null ? interests : "None",
                                                startDate != null ? startDate : "No Date",
                                                location != null ? location : "No Location",
                                                isOnWaitlist,
                                                regStart != null ? regStart : "",
                                                regEnd != null ? regEnd : "",
                                                maxEntrants != null ? maxEntrants : 0L,
                                                price != null ? price : 0.0,
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
    }

    /** Date picker for filters. */
    private void showDatePicker(boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

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

    /** Load all events (then show them). */
    private void loadEvents() {
        db.collection("Events")
                .get()
                .addOnSuccessListener(q -> {
                    Log.d("EventLottery", "Number of events fetched: " + q.size());
                    if (q.isEmpty()) {
                        Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Only show events the user can still join
                    List<DocumentSnapshot> joinable = new ArrayList<>();
                    for (DocumentSnapshot doc : q.getDocuments()) {
                        if (canJoinWaitingList(doc)) joinable.add(doc);
                    }
                    displayEvents(joinable);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EventLottery", "Firestore fetch error", e);
                });
    }

    /** Apply interest/date filters and show. */
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
                            if (eventInterest == null || !selectedInterest.equalsIgnoreCase(eventInterest.trim()))
                                continue;
                        }

                        // Date window (optional)
                        if (selectedStartDate != null || selectedEndDate != null) {
                            try {
                                Date filterStart = selectedStartDate != null ? dateFormat.parse(selectedStartDate) : null;
                                Date filterEnd   = selectedEndDate   != null ? dateFormat.parse(selectedEndDate)   : null;
                                Date eventStart  = dateFormat.parse(event.getString("startDate"));
                                Date eventEnd    = dateFormat.parse(event.getString("endDate"));

                                if ((filterStart != null && eventEnd.before(filterStart)) ||
                                        (filterEnd   != null && eventStart.after(filterEnd))) {
                                    continue;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }
                        }

                        if (!canJoinWaitingList(event)) continue;

                        filteredEvents.add(event);
                    }

                    displayEvents(filteredEvents);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error filtering events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Reset filters and reload. */
    private void clearFilters() {
        selectedInterest = "All";
        selectedStartDate = null;
        selectedEndDate = null;

        spinnerInterest.setSelection(0);
        buttonStartDate.setText("Start: All");
        buttonEndDate.setText("End: All");

        loadEvents();
    }

    /** Can user still join? date + cap check. */
    private boolean canJoinWaitingList(DocumentSnapshot eventDoc) {
        try {
            // registration end inclusive
            String regEndStr = eventDoc.getString("registrationEnd");
            if (regEndStr != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date regEnd = sdf.parse(regEndStr);

                Calendar cal = Calendar.getInstance();
                cal.setTime(regEnd);
                cal.add(Calendar.DATE, 1);
                Date regEndInclusive = cal.getTime();

                if (new Date().after(regEndInclusive)) return false;
            }

            // full?
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

    /** Render simple cards, each opens details screen. */
    private void displayEvents(List<DocumentSnapshot> events) {
        if (!isAdded() || getContext() == null || eventsContainer == null) return;

        eventsContainer.removeAllViews();

        for (DocumentSnapshot eventDoc : events) {
            String eventName      = eventDoc.getString("eventName");
            String eventStartDate = eventDoc.getString("startDate");
            String eventLocation  = eventDoc.getString("location");
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
            nameText.setText("Event: " + (eventName != null ? eventName : "Unnamed"));
            nameText.setTextSize(20f);
            nameText.setTextColor(getResources().getColor(android.R.color.black));
            nameText.setPadding(0, 0, 0, 8);

            TextView dateText = new TextView(getContext());
            dateText.setText("Start Date: " + (eventStartDate != null ? eventStartDate : "—"));
            dateText.setPadding(0, 0, 0, 8);

            TextView locationText = new TextView(getContext());
            locationText.setText("Location: " + (eventLocation != null ? eventLocation : "—"));
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
                            if (!eventSnapshot.exists()) {
                                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String eventNameStr     = eventSnapshot.getString("eventName");
                            String eventDescStr     = eventSnapshot.getString("description");
                            String eventInterests   = eventSnapshot.getString("interests");
                            String eventStartDateStr= eventSnapshot.getString("startDate");
                            String eventLocationStr = eventSnapshot.getString("location");
                            String regStart         = eventSnapshot.getString("registrationStart");
                            String regEnd           = eventSnapshot.getString("registrationEnd");
                            Long   maxEntrantsVal   = eventSnapshot.getLong("maxEntrants");
                            Double priceVal         = eventSnapshot.getDouble("price");

                            List<String> fullWaitlist = (List<String>) eventSnapshot.get("waitingList");
                            boolean isOnWaitlist = fullWaitlist != null
                                    && userEmail != null
                                    && fullWaitlist.contains(userEmail);

                            boolean waitingListClosed =
                                    Boolean.TRUE.equals(eventSnapshot.getBoolean("waitingListClosed"));

                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    : "";

                            getParentFragmentManager()
                                    .beginTransaction()
                                    .replace(
                                            R.id.fragment_container,
                                            EventFragmentEntrant.newInstance(
                                                    eventId,
                                                    currentUserId,
                                                    eventNameStr != null ? eventNameStr : "No Name",
                                                    eventDescStr != null ? eventDescStr : "No Description",
                                                    eventInterests != null ? eventInterests : "None",
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
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(),
                                        "Error loading event: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
            });

            card.addView(nameText);
            card.addView(dateText);
            card.addView(locationText);
            card.addView(waitlistText);
            card.addView(viewDetailsBtn);

            eventsContainer.addView(card);
        }
    }

    // Test helpers
    boolean canJoinWaitingListForTest(DocumentSnapshot eventDoc) { return canJoinWaitingList(eventDoc); }
    String getSelectedInterestForTest() { return selectedInterest; }
    String getSelectedStartDateForTest() { return selectedStartDate; }
    String getSelectedEndDateForTest() { return selectedEndDate; }
    void setSelectedInterestForTest(String interest) { this.selectedInterest = interest; }
    void setSelectedStartDateForTest(String startDate) { this.selectedStartDate = startDate; }
    void setSelectedEndDateForTest(String endDate) { this.selectedEndDate = endDate; }
}