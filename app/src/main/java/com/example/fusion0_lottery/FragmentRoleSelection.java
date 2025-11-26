package com.example.fusion0_lottery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This fragment allows users to select their role
 * The selected role is stored in Firestore
 */

public class FragmentRoleSelection extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_role_selection, container, false);

        Button entrantBtn = view.findViewById(R.id.entrantButton);
        Button organizerBtn = view.findViewById(R.id.organizerButton);
        Button adminBtn = view.findViewById(R.id.adminButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        entrantBtn.setOnClickListener(v -> updateUserRole("Entrant"));
        organizerBtn.setOnClickListener(v -> updateUserRole("Organizer"));

        // Optional: Secure Admin access via secret code
        adminBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Enter Admin Access Code");

            final EditText input = new EditText(getContext());
            input.setHint("Enter secret code");
            builder.setView(input);

            builder.setPositiveButton("Confirm", (dialog, which) -> {
                String code = input.getText().toString().trim();
                if (code.equals("SECRET123")) { // choose your own code
                    updateUserRole("Admin");
                } else {
                    Toast.makeText(getContext(), "Invalid Admin Code", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        return view;
    }

    private void updateUserRole(String role) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Users").document(uid).update("role", role)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Role set to " + role, Toast.LENGTH_SHORT).show();

                    if (role.equals("Entrant")) {
                        ((MainActivity) requireActivity()).replaceFragment(new EventLottery());
                    } else if (role.equals("Organizer")) {
                        ((MainActivity) requireActivity()).replaceFragment(new FragmentOrganizer());
                    } else if (role.equals("Admin")) {
                        ((MainActivity) requireActivity()).replaceFragment(new FragmentAdminDashboard());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to set role", Toast.LENGTH_SHORT).show());
    }
}
