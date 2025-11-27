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
import java.util.List;
import java.util.Map;

public class FragmentCancelledEntrants extends Fragment {

    private ListView winnersListView;
    private TextView emptyText;
    private Button backButton, sendNotificationButton;
    private ArrayList<LotteryWinners> declinedWinnersList;

    private ArrayAdapter<String> winnersAdapter;
    private Spinner sortFilter;

    private FirebaseFirestore db;
    private String eventId;

    public FragmentCancelledEntrants() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_selected_entrants, container, false);

        winnersListView = view.findViewById(R.id.winnerListView);
        emptyText = view.findViewById(R.id.emptyText);
        backButton = view.findViewById(R.id.backButton);
        sendNotificationButton = view.findViewById(R.id.sendNotificationButton);
        sortFilter = view.findViewById(R.id.sortFilter);

        db = FirebaseFirestore.getInstance();
        declinedWinnersList = new ArrayList<>();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadWinnersList();
        }

        sendNotificationButton.setOnClickListener(v -> showConfirmDialog());
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        sortFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDeclinedWinnersListDisplay(declinedWinnersList);
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
     *  find all entrants in the winners list (selected) and only show entrants who accepted
     *  same functionality as loadWaitingList function in FragmentWaitingList but for accepted winners
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

                    declinedWinnersList.clear();

                    List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();

                    for (Map<String, Object> entrant : winnersListEntrants) {
                        String userId = (String) entrant.get("userId");
                        if (userId == null) {
                            continue;
                        }
                        tasks.add(db.collection("Users").document(userId).get());
                    }

                    com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(results -> {
                                for (Object obj : results) {
                                    DocumentSnapshot userSnap = (DocumentSnapshot) obj;
                                    String name = userSnap.getString("name");
                                    String status = userSnap.getString("status");
                                    if (status == null) {
                                        status = "Pending";
                                    }

                                    if (status.equals("Declined")) {
                                        declinedWinnersList.add(new LotteryWinners(name, status));
                                    }
                                }
                                updateDeclinedWinnersListDisplay(declinedWinnersList);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }


    /**
     * displays winners in the ListView
     * includes sort by name or status
     */
    private void updateDeclinedWinnersListDisplay(ArrayList<LotteryWinners> winnersList) {

        if (sortFilter.getSelectedItemPosition() == 0) {
            // sort by name
            Collections.sort(declinedWinnersList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
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
                .setPositiveButton("Send", (dialog, which) -> {Toast.makeText(getContext(), "Notifications sent", Toast.LENGTH_SHORT).show();})
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
