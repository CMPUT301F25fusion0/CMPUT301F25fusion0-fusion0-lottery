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

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        entrantBtn.setOnClickListener(v -> {
            // go to Entrant dashboard
            updateUserRole("Entrant");
            // ((MainActivity) requireActivity()).replaceFragment(new FragmentEntrant());
        });

        organizerBtn.setOnClickListener(v -> {
            // go to Organizer dashboard
            updateUserRole("Organizer");
            ((MainActivity) requireActivity()).replaceFragment(new FragmentOrganizer());
        });

        return view;
    }

    private void updateUserRole(String role) {
        String device_id = auth.getCurrentUser().getUid();
        db.collection("Users").document(device_id).update("role", role)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Role set to " + role, Toast.LENGTH_SHORT).show();

                    if (role.equals("Entrant")) {
                        // ((MainActivity) requireActivity()).replaceFragment(new FragmentEntrant());
                        return;
                    }
                    else if (role.equals("Organizer")) {
                        ((MainActivity) requireActivity()).replaceFragment(new FragmentOrganizer());
                    }
                    else {
                        return;
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to set role", Toast.LENGTH_SHORT).show());
    }
}
