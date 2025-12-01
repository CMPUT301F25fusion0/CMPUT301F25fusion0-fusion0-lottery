package com.example.fusion0_lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Allows the user to select a role ("Entrant" or "Organizer").
 * Saves the choice in Firestore and navigates to the corresponding screen.
 */
public class FragmentRoleSelection extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    /**
     * Inflates the role selection layout and sets button click actions.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_role_selection, container, false);

        Button entrantBtn = view.findViewById(R.id.entrantButton);
        Button organizerBtn = view.findViewById(R.id.organizerButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        entrantBtn.setOnClickListener(v -> updateUserRole("Entrant"));
        organizerBtn.setOnClickListener(v -> updateUserRole("Organizer"));

        return view;
    }

    /**
     * Updates the current user's role in Firestore and navigates accordingly.
     *
     * @param role "Entrant" or "Organizer"
     */
    private void updateUserRole(String role) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Users").document(uid).update("role", role)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Role set to " + role, Toast.LENGTH_SHORT).show();

                    if (role.equals("Entrant")) {
                        // Navigate to EventLottery screen
                        ((MainActivity) requireActivity()).replaceFragment(new EventLottery());
                    } else if (role.equals("Organizer")) {
                        ((MainActivity) requireActivity()).replaceFragment(new FragmentOrganizer());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to set role", Toast.LENGTH_SHORT).show());
    }
}