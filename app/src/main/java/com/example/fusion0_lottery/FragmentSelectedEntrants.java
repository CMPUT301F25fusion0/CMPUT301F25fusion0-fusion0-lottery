package com.example.fusion0_lottery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FragmentSelectedEntrants extends Fragment {

    private ListView winnersListView;
    private TextView emptyText;
    private Button backButton, sendNotificationButton, redrawButton;
    private ArrayList<LotteryWinners> winnersList;
    private ArrayAdapter<String> winnersAdapter;
    private Spinner sortFilter;

    private FirebaseFirestore db;
    private String eventId;

    public FragmentSelectedEntrants() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_selected_entrants, container, false);

        winnersListView = view.findViewById(R.id.winnerListView);
        emptyText = view.findViewById(R.id.emptyText);
        backButton = view.findViewById(R.id.backButton);
        sendNotificationButton = view.findViewById(R.id.sendNotificationButton);
        redrawButton = view.findViewById(R.id.redrawButton);
        sortFilter = view.findViewById(R.id.sortFilter);

        db = FirebaseFirestore.getInstance();
        winnersList = new ArrayList<>();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadWinnersList();
        }

        sendNotificationButton.setOnClickListener(v -> showConfirmDialog());
        redrawButton.setOnClickListener(v -> redrawWinners());
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        sortFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateWinnersListDisplay(winnersList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        return view;
    }

    /**
     *  function to get all entrants the winners list for an event
     *  find all entrants in the winners list (selected) and display them
     *  same functionality loadWaitingList function in FragmentWaitingList but for winners
     */
    private void loadWinnersList() {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Map<String, Object>> winnersListEntrants = (List<Map<String, Object>>) snapshot.get("winnersList");

                    if (winnersListEntrants == null || winnersListEntrants.isEmpty()) {
                        winnersListView.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }
                    else {
                        winnersListView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }

                    winnersList.clear();

                    for (Map<String, Object> entrant : winnersListEntrants) {
                        String userId = (String) entrant.get("userId");
                        db.collection("Users").document(userId).get()
                                .addOnSuccessListener(userSnap -> {
                                    if (userSnap.exists()) {
                                        String name = userSnap.getString("name");
                                        String status = userSnap.getString("status");
                                        if (status == null) {
                                            status = "Pending";
                                        }
                                        winnersList.add(new LotteryWinners(name, status));
                                        updateWinnersListDisplay(winnersList);
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load winners list: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * function to redraw and select entrants from waiting list if there are entrants who declined
     * go through all selected entrants and see if there are any status = Declined
     * for every decline, redraw in the waiting list and update winners list
     */
    private void redrawWinners() {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Map<String, Object>> waitingList = (List<Map<String, Object>>) snapshot.get("waitingList");
                    List<Map<String, Object>> entrantWinners = (List<Map<String, Object>>) snapshot.get("winnersList");

                    if (waitingList == null || waitingList.isEmpty()) {
                        Toast.makeText(getContext(), "Waiting List is empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (entrantWinners == null || entrantWinners.isEmpty()) {
                        Toast.makeText(getContext(), "No Entrants in winners list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (Map<String, Object> entry : entrantWinners) {
                        String userId = (String) entry.get("userId");
                        tasks.add(db.collection("Users").document(userId).get());
                    }

                    com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(results -> {
                                int declinedCount = 0;
                                for (Object obj : results) {
                                    DocumentSnapshot userSnap = (DocumentSnapshot) obj;
                                    if (userSnap.exists()) {
                                        String status = userSnap.getString("status");
                                        if (status.equals("Declined")) {
                                            declinedCount++;
                                        }
                                    }
                                }

                                if (declinedCount == 0) {
                                    Toast.makeText(getContext(), "No redraws can be made", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                int numberOfRedraws = Math.min(declinedCount, waitingList.size());
                                List<Map<String, Object>> tempList = new ArrayList<>(waitingList);
                                List<Map<String, Object>> newWinners = new ArrayList<>();

                                Random random = new Random();
                                for (int i = 0; i < numberOfRedraws; i++) {
                                    int index = random.nextInt(tempList.size());
                                    newWinners.add(tempList.get(index));
                                    tempList.remove(index);
                                }

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("waitingList", tempList);
                                updates.put("winnersList", newWinners);

                                db.collection("Events").document(eventId)
                                        .update(updates)
                                        .addOnSuccessListener(v -> {
                                            Toast.makeText(getContext(), "Selected " + newWinners.size() + " winners!", Toast.LENGTH_LONG).show();
                                            loadWinnersList();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update winners: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load user statuses: " + e.getMessage(), Toast.LENGTH_LONG).show());

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * displays winners in the ListView
     * includes sort by name or status
     */
    private void updateWinnersListDisplay(ArrayList<LotteryWinners> winnersList) {

        if (sortFilter.getSelectedItemPosition() == 0) {
            // sort by name
            Collections.sort(winnersList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        }
        else {
            // sort by status
            Collections.sort(winnersList, (a, b) -> a.getStatus().compareToIgnoreCase(b.getStatus()));
        }

        ArrayList<String> displayList = new ArrayList<>();

        for (LotteryWinners w : winnersList) {
            displayList.add("Name: " + w.getName() + "\n" +
                            "Status: " + w.getStatus()
            );
        }
        winnersAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayList);
        winnersListView.setAdapter(winnersAdapter);
    }

    /**
     * placeholder function to send notifications to all winners
     * replace this with the notification stuff
     */
    private void showConfirmDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Send Notification?")
                .setMessage("Do you want to send notification to all selected entrants?")
                .setPositiveButton("Send", (dialog, which) -> {Toast.makeText(getContext(), "Notifications sent!", Toast.LENGTH_SHORT).show();})
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
