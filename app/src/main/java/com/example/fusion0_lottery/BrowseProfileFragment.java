package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment is used for browsing user profiles in the admin dashboard
 * provides functionality to view all users and filter them by role
 *  AI Assistance Reference: for method structure optimization, code structure
 *  Date: 2025-11-21, DeepSeek AI
 */

public class BrowseProfileFragment extends Fragment implements BrowseProfileAdapter.OnUserActionListener{
        private RecyclerView profilesRecyclerView;
        private TextView profileCount;
        private Spinner roleSpinner;
        private LinearLayout emptyLayout;
        private Button applyFilter;
        private List<User> userList = new ArrayList<>();
        private FirebaseFirestore db;
        private BrowseProfileAdapter browseProfileAdapter;
        private BottomNavigationView bottomNavigation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_profile, container, false);

        profilesRecyclerView = view.findViewById(R.id.recyclerView_profiles);
        profileCount = view.findViewById(R.id.profile_count);
        roleSpinner = view.findViewById(R.id.spinnerRole);
        emptyLayout = view.findViewById(R.id.empty);
        applyFilter = view.findViewById(R.id.applyFilter);
        bottomNavigation = view.findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_profiles);

        db = FirebaseFirestore.getInstance();
        String[] roles = {"All", "Entrant", "Organizer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        applyFilter.setOnClickListener(v -> {
            String selectedRole = roleSpinner.getSelectedItem().toString();
            filterUsersByRole(selectedRole);
        });

        setupRecyclerView();
        bottom_navigation();
        loadUsers();

        return view;
    }

    private void bottom_navigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profiles) {
                loadUsers();
                return true;
            } else if (itemId == R.id.nav_events) {
                navigateToFragment(new BrowseEventsFragment());
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


    private void setupRecyclerView() {
        profilesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        browseProfileAdapter = new BrowseProfileAdapter(userList, this);
        profilesRecyclerView.setAdapter(browseProfileAdapter);
    }

    private void loadUsers() {

        db.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        int count = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            userList.add(user);
                            count++;
                        }
                        browseProfileAdapter.updateList(userList);
                        updateUI();
                    }
                });
    }
    private void filterUsersByRole(String role) {
        if (role.equals("All")) {
            browseProfileAdapter.updateList(userList);
        } else {
            List<User> filteredList = new ArrayList<>();
            for (User user : userList) {
                if (user.getRole() != null && user.getRole().equals(role)) {
                    filteredList.add(user);
                }
            }
            browseProfileAdapter.updateList(filteredList);
        }
        updateUI();
    }
    private void updateUI() {
        int count = browseProfileAdapter.getItemCount();
        profileCount.setText("Total: " + count + " profiles");

        if (count == 0) {
            emptyLayout.setVisibility(View.VISIBLE);
            profilesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            profilesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles user profile click events
     * @param user the clicked user
     */
    @Override
    public void onUserClicked(User user) {
        if ("Organizer".equals(user.getRole())) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Remove Organizer")
                    .setMessage("Remove " + user.getName() + "?")
                    .setPositiveButton("Remove", (dialog, which) -> removeOrganizer(user))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            new AlertDialog.Builder(requireContext())
                    .setTitle("User Details")
                    .setMessage("Name: " + user.getName() +
                            "\nEmail: " + user.getEmail() +
                            "\nRole: " + user.getRole() +
                            "\nPhone: " + user.getPhone_number())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void removeOrganizer(User organizer) {
        db.collection("Users").document(organizer.getDevice_id()).delete()
                .addOnSuccessListener(aVoid -> {
                    userList.remove(organizer);
                    browseProfileAdapter.updateList(userList);
                    updateUI();
                    Toast.makeText(requireContext(), "Organizer removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
  