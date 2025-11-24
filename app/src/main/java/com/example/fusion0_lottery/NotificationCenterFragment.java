package com.example.fusion0_lottery;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class NotificationCenterFragment extends Fragment {

    private LinearLayout notificationsContainer;
    private SwitchMaterial switchNotifications;
    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_center, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Toolbar setup
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Notifications");
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setNavigationOnClickListener(_v ->
                    requireActivity().getSupportFragmentManager().popBackStack());
            toolbar.setContentInsetStartWithNavigation(0);
        }

        // Views & Firebase
        notificationsContainer = v.findViewById(R.id.notificationsContainer);
        switchNotifications = v.findViewById(R.id.switch_notifications);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Load switch state (default ON if missing)
        if (uid != null) {
            db.collection("Users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        boolean enabled = doc.contains("notificationsEnabled")
                                ? Boolean.TRUE.equals(doc.getBoolean("notificationsEnabled"))
                                : true;
                        switchNotifications.setChecked(enabled);
                    });

            switchNotifications.setOnCheckedChangeListener(
                    (button, isChecked) ->
                            db.collection("Users")
                                    .document(uid)
                                    .update("notificationsEnabled", isChecked)
            );
        } else {
            switchNotifications.setEnabled(false);
        }

        loadNotifications();
    }

    private void loadNotifications() {
        if (notificationsContainer == null) return;
        notificationsContainer.removeAllViews();

        if (uid == null) {
            addMessage("Please sign in to view notifications.");
            return;
        }

        // ❗FIXED: Correct collection name: "notifications"
        db.collection("Users").document(uid).collection("notifications")
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        addMessage("No new notifications.");
                    } else {
                        for (QueryDocumentSnapshot doc : q) {
                            // ❗FIXED: Read "message" not "body"
                            String title = doc.getString("title");
                            String message = doc.getString("message");
                            addNotification(title, message);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        addMessage("Error loading notifications: " + e.getMessage())
                );
    }

    private void addNotification(String title, String message) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.row_notification, notificationsContainer, false);

        TextView titleView = card.findViewById(R.id.notificationTitle);
        TextView messageView = card.findViewById(R.id.notificationBody);

        titleView.setText(title != null ? title : "No Title");
        messageView.setText(message != null ? message : "");

        notificationsContainer.addView(card);
    }

    private void addMessage(String msg) {
        TextView tv = new TextView(getContext());
        tv.setText(msg);
        tv.setPadding(32, 32, 32, 32);
        notificationsContainer.addView(tv);
    }
}
