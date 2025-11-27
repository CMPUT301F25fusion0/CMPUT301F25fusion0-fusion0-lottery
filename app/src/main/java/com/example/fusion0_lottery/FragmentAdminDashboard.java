package com.example.fusion0_lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FragmentAdminDashboard extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout eventsContainer, usersContainer;
    private Button refreshButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        eventsContainer = view.findViewById(R.id.eventsContainer);
        usersContainer = view.findViewById(R.id.usersContainer);
        refreshButton = view.findViewById(R.id.refreshButton);

        refreshButton.setOnClickListener(v -> {
            loadEvents();
            loadUsers();
        });

        loadEvents();
        loadUsers();
        return view;
    }

    /** Load all events */
    private void loadEvents() {
        eventsContainer.removeAllViews();

        db.collection("Events")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String name = doc.getString("eventName");
                        String id = doc.getId();

                        LinearLayout card = new LinearLayout(getContext());
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setPadding(24, 24, 24, 24);

                        TextView title = new TextView(getContext());
                        title.setText("Event: " + name);
                        title.setTextSize(18);

                        Button deleteBtn = new Button(getContext());
                        deleteBtn.setText("Delete Event");
                        deleteBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        deleteBtn.setTextColor(getResources().getColor(android.R.color.white));

                        deleteBtn.setOnClickListener(v -> deleteEvent(id));

                        card.addView(title);
                        card.addView(deleteBtn);
                        eventsContainer.addView(card);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Delete event by ID */
    private void deleteEvent(String eventId) {
        db.collection("Events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Event deleted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Load all user profiles */
    private void loadUsers() {
        usersContainer.removeAllViews();

        db.collection("Users")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String id = doc.getId();

                        LinearLayout card = new LinearLayout(getContext());
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setPadding(24, 24, 24, 24);

                        TextView info = new TextView(getContext());
                        info.setText("User: " + name + "\nEmail: " + email);
                        info.setTextSize(16);

                        Button deleteBtn = new Button(getContext());
                        deleteBtn.setText("Delete Profile");
                        deleteBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                        deleteBtn.setTextColor(getResources().getColor(android.R.color.white));

                        deleteBtn.setOnClickListener(v -> deleteUser(id));

                        card.addView(info);
                        card.addView(deleteBtn);
                        usersContainer.addView(card);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Delete user by ID */
    private void deleteUser(String userId) {
        db.collection("Users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "User profile deleted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
