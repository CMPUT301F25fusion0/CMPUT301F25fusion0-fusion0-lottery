package com.example.fusion0_lottery;

import android.app.AlertDialog;
import android.graphics.Color;
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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Fragment for displaying and managing notifications for the current user.
 *
 * Features:
 * - Toggle notifications on/off.
 * - Display user's notifications.
 * - Clear all notifications with confirmation.
 */
public class NotificationCenterFragment extends Fragment {

    private LinearLayout notificationsContainer;
    private SwitchMaterial switchNotifications;
    private Button clearAllButton;
    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_center, container, false);
    }

    /**
     * Called after view creation. Initializes UI elements, Firebase references,
     * and loads notifications.
     *
     * @param v      the fragment's root view
     * @param savedInstanceState saved state
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Toolbar setup (title + back)
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
        clearAllButton = v.findViewById(R.id.clearAllButton);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Load toggle state (default = true if missing)
        if (uid != null) {
            db.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
                boolean enabled = doc.contains("notificationsEnabled")
                        ? Boolean.TRUE.equals(doc.getBoolean("notificationsEnabled"))
                        : true;
                switchNotifications.setChecked(enabled);
            });

            switchNotifications.setOnCheckedChangeListener((button, isChecked) ->
                    db.collection("Users").document(uid).update("notificationsEnabled", isChecked));
        } else {
            switchNotifications.setEnabled(false);
        }

        // Clear All button listener
        clearAllButton.setOnClickListener(v1 -> showClearAllConfirmation());

        loadNotifications();
    }

    /**
     * Loads notifications from Firebase and displays them in the container.
     * Shows a message if there are no notifications or if user is not signed in.
     */
    private void loadNotifications() {
        if (notificationsContainer == null) return;
        notificationsContainer.removeAllViews();

        if (uid == null) {
            addMessage("Please sign in to view notifications.");
            return;
        }

        db.collection("Users").document(uid).collection("Notifications")
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        addMessage("No new notifications.");
                    } else {
                        for (QueryDocumentSnapshot doc : q) {
                            addNotification(doc.getString("title"), doc.getString("body"));
                        }
                    }
                })
                .addOnFailureListener(e -> addMessage("Error loading notifications: " + e.getMessage()));
    }

    /**
     * Adds a single notification card to the notifications container.
     *
     * @param title title of the notification
     * @param body body/content of the notification
     */
    private void addNotification(String title, String body) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.row_notification, notificationsContainer, false);

        TextView titleView = card.findViewById(R.id.notificationTitle);
        TextView bodyView  = card.findViewById(R.id.notificationBody);

        titleView.setText(title != null ? title : "No Title");
        bodyView.setText(body != null ? body : "");

        notificationsContainer.addView(card);
    }

    /**
     * Adds a simple message (TextView) to the notifications container.
     * Used for empty state or error messages.
     *
     * @param msg message to display
     */
    private void addMessage(String msg) {
        TextView tv = new TextView(getContext());
        tv.setText(msg);
        tv.setPadding(32, 32, 32, 32);
        notificationsContainer.addView(tv);
    }

    /**
     * Show confirmation dialog before clearing all notifications
     */
    private void showClearAllConfirmation() {
        if (uid == null) {
            Toast.makeText(getContext(), "Please sign in to clear notifications", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Clear All Notifications")
                .setMessage("Are you sure you want to delete all notifications? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> clearAllNotifications())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Delete all notifications from Firebase for the current user
     */
    private void clearAllNotifications() {
        if (uid == null) {
            return;
        }

        db.collection("Users").document(uid).collection("Notifications")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "No notifications to clear", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Delete all notification documents
                    int totalNotifications = querySnapshot.size();
                    final int[] deletedCount = {0};

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    deletedCount[0]++;
                                    if (deletedCount[0] == totalNotifications) {
                                        // All deleted successfully
                                        Toast.makeText(getContext(),
                                                "Cleared " + totalNotifications + " notification(s)",
                                                Toast.LENGTH_SHORT).show();
                                        loadNotifications(); // Refresh the display
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    deletedCount[0]++;
                                    if (deletedCount[0] == totalNotifications) {
                                        Toast.makeText(getContext(),
                                                "Some notifications could not be cleared",
                                                Toast.LENGTH_SHORT).show();
                                        loadNotifications(); // Refresh the display anyway
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Failed to clear notifications: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
