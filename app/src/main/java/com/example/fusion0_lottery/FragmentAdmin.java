package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This is a default menu fragment for the admin once they log in.
 * They are able to click buttons to see all users and events from here
 * NOTE: Currently (as of Nov 19) you have to have access to admin, you must
 * go onto firestore database on the web and change your role to "Admin"
 * under your device ID.
 */
public class FragmentAdmin extends Fragment {

    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        // Initialize buttons and TextView
        Button showUsers = view.findViewById(R.id.showUsers);
        Button showEvents = view.findViewById(R.id.showEvents);
        TextView greeting = view.findViewById(R.id.greeting);

        //OnClickListeners
        showUsers.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).replaceFragment(new FragmentAdminUsers());
        });

        showEvents.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).replaceFragment(new FragmentAdminEvents());
        });

        return view;
    }
}