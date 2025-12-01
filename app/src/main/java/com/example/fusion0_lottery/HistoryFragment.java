package com.example.fusion0_lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * Fragment that displays the user's event registration history.
 * <p>
 * Shows a list of events the user has registered for, along with basic details like
 * event name, date, location, status, and current entrants count.
 * If no history exists or the user is not signed in, displays an appropriate message.
 */
public class HistoryFragment extends Fragment {

    private LinearLayout historyContainer;
    private TextView emptyView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid;

    /**
     * Inflates the fragment layout.
     *
     * @param inflater LayoutInflater used to inflate views
     * @param container Parent view
     * @param savedInstanceState Previous saved state
     * @return Root view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * Initializes the toolbar, input views, and loads the user's history after the view is created.
     *
     * @param v The fragment's root view
     * @param savedInstanceState Previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        MaterialToolbar toolbar = v.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("History");
            toolbar.setNavigationOnClickListener(_v ->
                    requireActivity().getSupportFragmentManager().popBackStack());
        }

        historyContainer = v.findViewById(R.id.historyContainer);
        emptyView        = v.findViewById(R.id.emptyHistoryMessage);

        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        loadHistory();
    }

    /**
     * Loads the user's registration history from Firestore.
     * Displays the events in a scrollable list, or shows a message if no events are found.
     */
    private void loadHistory() {
        if (uid == null) {
            showEmpty("Please sign in to view your history.");
            return;
        }

        historyContainer.removeAllViews();

        db.collection("Users")
                .document(uid)
                .collection("Registrations")
                .orderBy("registeredAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        showEmpty("You haven’t registered in any events yet.");
                        return;
                    }

                    emptyView.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot doc : snap) {
                        View row = LayoutInflater.from(getContext())
                                .inflate(R.layout.row_history_item, historyContainer, false);

                        TextView title     = row.findViewById(R.id.historyTitle);
                        TextView descView  = row.findViewById(R.id.historyDescription);
                        TextView meta      = row.findViewById(R.id.historyMeta);
                        TextView status    = row.findViewById(R.id.historyStatus);
                        TextView entrants  = row.findViewById(R.id.historyEntrants);

                        String name   = doc.getString("eventName");
                        String date   = doc.getString("startDate");
                        String loc    = doc.getString("location");
                        String stat   = doc.getString("status");
                        String desc   = doc.getString("description");
                        String eventId = doc.getString("eventId");

                        title.setText(name != null ? name : "Untitled Event");
                        if (descView != null) descView.setText(desc != null ? desc : "");
                        meta.setText(((date != null) ? date : "—") + " • " + ((loc != null) ? loc : "—"));
                        status.setText(stat != null ? stat : "Pending");

                        // Show current entrants count from Event doc
                        if (eventId != null && entrants != null) {
                            db.collection("Events").document(eventId).get()
                                    .addOnSuccessListener(ev -> {
                                        @SuppressWarnings("unchecked")
                                        List<String> wl = (List<String>) ev.get("waitingList");
                                        int count = (wl != null) ? wl.size() : 0;
                                        entrants.setText("Entrants: " + count);
                                    })
                                    .addOnFailureListener(e -> entrants.setText("Entrants: —"));
                        }

                        // Tap row → open the entrant detail screen
                        final String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                : "";

                        row.setOnClickListener(__ -> {
                            if (eventId == null) return;

                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(
                                            R.id.fragment_container,
                                            EventFragmentEntrant.newInstance(
                                                    eventId,
                                                    currentUid,
                                                    (name != null ? name : "No Name"),
                                                    (desc != null ? desc : "No Description"),
                                                    "None",  // interests (optional cache)
                                                    (date != null ? date : "No Date"),
                                                    (loc  != null ? loc  : "No Location"),
                                                    false,   // isInWaitingList (recomputed inside)
                                                    "",      // registrationStart (optional cache)
                                                    "",      // registrationEnd   (optional cache)
                                                    0L,      // maxEntrants       (optional cache)
                                                    0.0,     // price             (optional cache)
                                                    false    // waitingListClosed (optional cache)
                                            )
                                    )
                                    .addToBackStack("EventFragmentEntrant")
                                    .commit();
                        });

                        historyContainer.addView(row);
                    }
                })
                .addOnFailureListener(e -> showEmpty("Failed to load history: " + e.getMessage()));
    }

    /**
     * Displays a message when no history is available.
     *
     * @param msg Message to show
     */
    private void showEmpty(String msg) {
        emptyView.setText(msg);
        emptyView.setVisibility(View.VISIBLE);
    }
}
