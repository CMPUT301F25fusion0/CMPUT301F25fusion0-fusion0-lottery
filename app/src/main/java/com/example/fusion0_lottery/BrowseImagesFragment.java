package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BrowseImagesFragment extends Fragment {
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private List<String> selected_event_id;
    private Button remove_btn;
    private ImageBrowserAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();
        selected_event_id = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_browse_images, container, false);
        bottomNavigation = view.findViewById(R.id.bottom_navigation);
        recyclerView = view.findViewById(R.id.recycler_images);
        remove_btn = view.findViewById(R.id.remove_btn);
        bottomNavigation.setSelectedItemId(R.id.nav_images);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ImageBrowserAdapter(eventList, selected_event_id, getContext());
        recyclerView.setAdapter(adapter);
        loadImages();
        bottom_navigation();
        remove_btn.setOnClickListener(v -> removeSelectedImages() );
        return view;
    }
    private void loadImages() {
        eventList.clear();

        db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());

                        if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (eventList.isEmpty()) {
                        Toast.makeText(getContext(), "No images found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading images: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    private void removeSelectedImages() {
        if (selected_event_id.isEmpty()) {
            Toast.makeText(getContext(), "No images selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Images")
                .setMessage("Remove " + selected_event_id.size() + " selected image(s)?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    deleteImagesFromFirebase();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteImagesFromFirebase() {
        int totalToDelete = selected_event_id.size();
        int[] deletedCount = {0};

        for (String eventId : selected_event_id) {
            db.collection("Events").document(eventId)
                    .update("posterImage", null)
                    .addOnSuccessListener(aVoid -> {
                        deletedCount[0]++;
                        if (deletedCount[0] == totalToDelete) {
                            Toast.makeText(getContext(), "Images removed successfully",
                                    Toast.LENGTH_SHORT).show();
                            selected_event_id.clear();
                            loadImages();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error removing image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void bottom_navigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profiles) {
                navigateToFragment(new BrowseProfileFragment());
                return true;
            } else if (itemId == R.id.nav_events) {
                navigateToFragment(new BrowseEventsFragment());
                return true;
            } else if (itemId == R.id.nav_images) {
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
}