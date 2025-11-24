package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;


public class FragmentAdminEvents extends Fragment {

    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_events, container, false);
        bottomNavigation = view.findViewById(R.id.bottom_navigation);

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
//                navigateToFragment(new BrowseEventsFragment());
                return true;
            } else if (itemId == R.id.nav_images) {
//                navigateToFragment(new BrowseImagesFragment());
                return true;
            } else if (itemId == R.id.nav_logs) {
//                navigateToFragment(new BrowseLogsFragment());
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