package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This is a default menu fragment for the admin once they log in.
 * They are able to click buttons to see all users and events from here
 * NOTE: Currently (as of Nov 19) you have to have access to admin, you must
 * go onto firestore database on the web and change your role to "Admin"
 * under your device ID.
 */
public class BrowseEventsFragment extends Fragment {

    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_events, container, false);
        bottomNavigation = view.findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_events);
        bottom_navigation();
        return view;
    }

    private void bottom_navigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profiles) {
                navigateToFragment(new BrowseProfileFragment());
                return true;
            } else if (itemId == R.id.nav_events) {
                return true;
            } else if (itemId == R.id.nav_images) {
                navigateToFragment(new BrowseImagesFragment());
                return true;
            } else if (itemId == R.id.nav_logs) {
                navigateToFragment(new BrowseNotificationsFragment());
                return true;
            }
            return false;
        });
    }

    private void navigateToFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /*
    public void onEventClicked(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Event Details")
                .setMessage("Name: " + event.getEventName() +
                        "\nDescription: " + event.getDescription())
                .setPositiveButton("Remove", (dialog, which) ->  confirmDelete(event))
                .setNegativeButton("Exit", null)
                .show();
    }

    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Event?")
                .setMessage("Are you sure you want to remove this event?")
                .setPositiveButton("Remove", (dialog, which) -> removeEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeEvent(Event event) {
         db.collection("Events").document(event.getEventId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        eventList.remove(event);
                        browseEventAdapter.updateList(eventList);
                        updateUI();
                        Toast.makeText(requireContext(), "Event removed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

     */
    }