package com.example.fusion0_lottery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment to update or delete user profile.
 * <p>
 * Features:
 * - View and edit username, email, phone
 * - Validate input before updating
 * - High-risk account deletion
 */
public class UpdateProfileFragment extends Fragment {

    private EditText inputUsername, inputEmail, inputPhone;
    private Button btnUpdate, btnDelete;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;

    /**
     * Inflates the profile update fragment, initializes UI components,
     * update and delete actions.
     *
     * @param inflater LayoutInflater to inflate the fragment
     * @param container Optional parent ViewGroup
     * @param savedInstanceState Optional saved state Bundle
     * @return Root view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_update_profile, container, false);

        inputUsername = root.findViewById(R.id.inputUsername);
        inputEmail    = root.findViewById(R.id.inputEmail);
        inputPhone    = root.findViewById(R.id.inputPhone);
        btnUpdate     = root.findViewById(R.id.btnUpdate);
        btnDelete     = root.findViewById(R.id.btnDeleteProfile);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v ->
                    requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        FirebaseUser current = auth.getCurrentUser();
        if (current == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            btnUpdate.setEnabled(false);
            btnDelete.setEnabled(false);
            return root;
        }
        uid = current.getUid();

        // prefill
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(snap -> {
                    if (snap != null && snap.exists()) {
                        inputUsername.setText(snap.getString("username"));
                        inputEmail.setText(snap.getString("email"));
                        inputPhone.setText(snap.getString("phone"));
                    }
                });

        btnUpdate.setOnClickListener(v -> updateProfile());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete profile?")
                    .setMessage("This will permanently delete your account and all related information. This cannot be undone.")
                    .setPositiveButton("Yes", (d, w) -> performAccountDeletion())
                    .setNegativeButton("No", null)
                    .show();
        });

        return root;
    }

    /**
     * Enable or disable UI elements during busy operations.
     * @param busy true to disable UI, false to enable
     */
    private void setBusy(boolean busy) {
        btnUpdate.setEnabled(!busy);
        btnDelete.setEnabled(!busy);
        inputUsername.setEnabled(!busy);
        inputEmail.setEnabled(!busy);
        inputPhone.setEnabled(!busy);
    }

    /**
     * Updates the user's profile in Firestore with current input values.
     * Validates input before sending updates.
     */
    private void updateProfile() {
        String username = inputUsername.getText().toString().trim();
        String email    = inputEmail.getText().toString().trim();
        String phone    = inputPhone.getText().toString().trim();

        if (!validate(username, email, phone)) return;

        setBusy(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("email", email);
        updates.put("phone", phone);

        db.collection("Users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                )
                .addOnCompleteListener(task -> setBusy(false));
    }

    /**
     * Validates user input.
     * @param username username input
     * @param email email input
     * @param phone phone input
     * @return true if valid, false if invalid (sets errors on fields)
     */
    private boolean validate(String username, String email, String phone) {
        if (TextUtils.isEmpty(username)) { inputUsername.setError("Username required"); return false; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Valid email required"); return false;
        }
        if (!TextUtils.isEmpty(phone) && phone.length() < 3) {
            inputPhone.setError("Phone seems too short"); return false;
        }
        return true;
    }

    /**
     * High-risk deletion:
     * 1) Fetch email from Firestore (not FirebaseAuth)
     * 2) Remove user from Events.waitingList
     * 3) Delete Notifications subcollection
     * 4) Delete Users/{uid}
     * 5) Delete FirebaseAuth user (may require re-login)
     * 6) Sign out and navigate to SignUp
     */
    private void performAccountDeletion() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true);

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists()) {
                        setBusy(false);
                        Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String email = snapshot.getString("email");
                    if (email == null || email.trim().isEmpty()) {
                        setBusy(false);
                        Toast.makeText(getContext(), "No email found in profile.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Step 1: remove from any Events.waitingList
                    Task<Void> removeFromEvents = db.collection("Events")
                            .whereArrayContains("waitingList", email)
                            .get()
                            .onSuccessTask(query -> {
                                List<Task<Void>> updates = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : query) {
                                    updates.add(
                                            doc.getReference().update("waitingList", FieldValue.arrayRemove(email))
                                    );
                                }
                                return Tasks.whenAll(updates);
                            });

                    // Step 2: delete Notifications subcollection
                    Task<Void> deleteNotifications = db.collection("Users")
                            .document(uid)
                            .collection("Notifications")
                            .get()
                            .onSuccessTask(q -> {
                                WriteBatch batch = db.batch();
                                for (QueryDocumentSnapshot d : q) {
                                    batch.delete(d.getReference());
                                }
                                return batch.commit();
                            });

                    // Step 3: delete user document
                    Task<Void> deleteUserDoc = db.collection("Users").document(uid).delete();

                    // Step 4–6: perform deletions and auth removal
                    Tasks.whenAll(removeFromEvents, deleteNotifications, deleteUserDoc)
                            .addOnSuccessListener(_v -> {
                                user.delete()
                                        .addOnSuccessListener(_v2 -> {
                                            auth.signOut();
                                            Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                            navigateToSignUp();
                                        })
                                        .addOnFailureListener(e -> {
                                            String msg = e.getMessage() != null ? e.getMessage() : "Delete failed";
                                            Toast.makeText(getContext(),
                                                    "Could not delete login: " + msg + ". Please sign in again and retry.",
                                                    Toast.LENGTH_LONG).show();
                                            auth.signOut();
                                            navigateToSignUp();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                setBusy(false);
                                Toast.makeText(getContext(), "Deletion failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setBusy(false);
                    Toast.makeText(getContext(), "Failed to fetch email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigates to SignUp fragment and clears back stack.
     */
    private void navigateToSignUp() {
        // Clear back stack and show SignUp
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(R.id.fragment_container, new FragmentSignUp())
                .commitAllowingStateLoss();
    }
}
