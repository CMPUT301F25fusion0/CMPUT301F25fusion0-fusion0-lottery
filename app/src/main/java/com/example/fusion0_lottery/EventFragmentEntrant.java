package com.example.fusion0_lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * EventFragmentEntrant
 * Fields-first version to avoid "local variables referenced from a lambda must be final" errors.
 */
public class EventFragmentEntrant extends Fragment {

    // ==========
    // FIELDS (were locals before; safe to capture in lambdas)
    // ==========
    private String eventId;
    private String currentUserId;
    private String eventName;
    private String description;
    private String startDate;
    private String location;
    private String regStart;
    private String regEnd;
    private long   maxEntrants;
    private double price;
    private boolean isOnWaitlist;
    private boolean waitingListClosed;

    private FirebaseFirestore db;

    // ==========
    // Factory
    // ==========
    public static EventFragmentEntrant newInstance(
            String eventId,
            String currentUserId,
            String eventName,
            String description,
            String startDate,
            String location,
            boolean isOnWaitlist,
            String regStart,
            String regEnd,
            long maxEntrants,
            double price,
            boolean waitingListClosed
    ) {
        Bundle b = new Bundle();
        b.putString("eventId", eventId);
        b.putString("currentUserId", currentUserId);
        b.putString("eventName", eventName);
        b.putString("description", description);
        b.putString("startDate", startDate);
        b.putString("location", location);
        b.putBoolean("isOnWaitlist", isOnWaitlist);
        b.putString("regStart", regStart);
        b.putString("regEnd", regEnd);
        b.putLong("maxEntrants", maxEntrants);
        b.putDouble("price", price);
        b.putBoolean("waitingListClosed", waitingListClosed);

        EventFragmentEntrant f = new EventFragmentEntrant();
        f.setArguments(b);
        return f;
    }

    // ==========
    // Lifecycle
    // ==========
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        Bundle a = getArguments();
        if (a != null) {
            eventId           = a.getString("eventId", "");
            currentUserId     = a.getString("currentUserId", "");
            eventName         = a.getString("eventName", "");
            description       = a.getString("description", "");
            startDate         = a.getString("startDate", "");
            location          = a.getString("location", "");
            isOnWaitlist      = a.getBoolean("isOnWaitlist", false);
            regStart          = a.getString("regStart", "");
            regEnd            = a.getString("regEnd", "");
            maxEntrants       = a.getLong("maxEntrants", 0L);
            price             = a.getDouble("price", 0.0);
            waitingListClosed = a.getBoolean("waitingListClosed", false);
        }

        // If caller forgot currentUserId, try Firebase session just in case
        if ((currentUserId == null || currentUserId.isEmpty()) &&
                FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Use your actual layout name here. If it's different, change the line below.
        View v = inflater.inflate(R.layout.fragment_event_activity_entrant, container, false);


        // --- OPTIONAL: populate simple labels if present (ids looked up by name to avoid compile errors)
        setTextIfExists(v, "eventTitleText", eventName);
        setTextIfExists(v, "eventDescriptionText", description);
        setTextIfExists(v, "eventInfoText",
                "Date: " + safe(startDate) + "\nLocation: " + safe(location) +
                        "\nReg: " + safe(regStart) + " → " + safe(regEnd) +
                        "\nCap: " + maxEntrants + "  Price: " + price);

        // --- Hook up buttons if your layout has them
        Button joinBtn  = findButtonByName(v, "joinWaitingListButton");
        Button leaveBtn = findButtonByName(v, "leaveWaitingListButton");

        if (joinBtn != null) {
            joinBtn.setOnClickListener(view -> joinWaitlist());
            joinBtn.setEnabled(!waitingListClosed);
        }
        if (leaveBtn != null) {
            leaveBtn.setOnClickListener(view -> leaveWaitlist());
        }

        // You can also refresh waitlist state on open:
        refreshWaitlistState(joinBtn, leaveBtn);

        return v;
    }

    // ==========
    // Actions
    // ==========
    private void joinWaitlist() {
        if (waitingListClosed) {
            toast("Waiting list is closed.");
            return;
        }
        if (eventId == null || eventId.isEmpty()) {
            toast("Invalid event.");
            return;
        }
        if (currentUserId == null || currentUserId.isEmpty()) {
            toast("Please sign in.");
            return;
        }

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        toast("Event not found.");
                        return;
                    }

                    // enforce capacity if present
                    Long cap = snap.getLong("maxEntrants");
                    List<String> list = (List<String>) snap.get("waitingList");
                    int count = (list != null) ? list.size() : 0;
                    if (cap != null && cap > 0 && count >= cap) {
                        toast("Waitlist is full.");
                        return;
                    }

                    db.collection("Events").document(eventId)
                            .update("waitingList", FieldValue.arrayUnion(currentUserId))
                            .addOnSuccessListener(unused -> {
                                isOnWaitlist = true;  // field — safe for lambdas
                                toast("Joined waitlist.");
                                refreshWaitlistState(null, null);
                            })
                            .addOnFailureListener(e ->
                                    toast("Failed to join: " + e.getMessage()));
                })
                .addOnFailureListener(e -> toast("Error: " + e.getMessage()));
    }

    private void leaveWaitlist() {
        if (eventId == null || eventId.isEmpty() || currentUserId == null || currentUserId.isEmpty()) {
            toast("Invalid state.");
            return;
        }

        db.collection("Events").document(eventId)
                .update("waitingList", FieldValue.arrayRemove(currentUserId))
                .addOnSuccessListener(unused -> {
                    isOnWaitlist = false;
                    toast("Left waitlist.");
                    refreshWaitlistState(null, null);
                })
                .addOnFailureListener(e -> toast("Failed to leave: " + e.getMessage()));
    }

    private void refreshWaitlistState(@Nullable Button joinBtnMaybe, @Nullable Button leaveBtnMaybe) {
        // Re-find buttons (if caller passed null) using safe lookup
        Button joinBtn  = (joinBtnMaybe  != null) ? joinBtnMaybe  : findButtonByName(requireView(), "joinWaitingListButton");
        Button leaveBtn = (leaveBtnMaybe != null) ? leaveBtnMaybe : findButtonByName(requireView(), "leaveWaitingListButton");

        if (joinBtn != null) {
            joinBtn.setEnabled(!isOnWaitlist && !waitingListClosed);
            joinBtn.setAlpha((!isOnWaitlist && !waitingListClosed) ? 1f : 0.5f);
        }
        if (leaveBtn != null) {
            leaveBtn.setEnabled(isOnWaitlist);
            leaveBtn.setAlpha(isOnWaitlist ? 1f : 0.5f);
        }
    }

    // ==========
    // Helpers
    // ==========
    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private static String safe(String s) { return (s == null) ? "" : s; }

    /** Find a view id by name at runtime so the class compiles even if your ids differ. */
    @Nullable
    private View findByName(View root, String idName) {
        if (getContext() == null) return null;
        int id = getResources().getIdentifier(idName, "id", requireContext().getPackageName());
        if (id == 0) return null;
        return root.findViewById(id);
    }

    @Nullable
    private Button findButtonByName(View root, String idName) {
        View v = findByName(root, idName);
        return (v instanceof Button) ? (Button) v : null;
    }

    private void setTextIfExists(View root, String idName, String text) {
        View v = findByName(root, idName);
        if (v instanceof TextView) ((TextView) v).setText(text != null ? text : "");
    }
}
