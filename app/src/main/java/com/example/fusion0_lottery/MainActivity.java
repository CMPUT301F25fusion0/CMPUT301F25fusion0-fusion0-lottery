package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * MainActivity serves as the host activity for the app.
 * <p>
 * It handles user authentication, role-based navigation, and fragment management.
 * Depending on the current user's role (Entrant, Organizer, Admin), it loads the corresponding fragment.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes Firebase instances and determines which fragment to display based on user login status and role.
     *
     * @param savedInstanceState Bundle containing saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (savedInstanceState == null) {
            if (auth.getCurrentUser() != null) {
                String device_id = auth.getCurrentUser().getUid();

                db.collection("Users").document(device_id).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");

                                if (role == null || role.isEmpty()) {
                                    replaceFragment(new FragmentRoleSelection());
                                } else if ("Entrant".equalsIgnoreCase(role)) {
                                    replaceFragment(EventLottery.newInstance(auth.getCurrentUser().getEmail()));
                                } else if ("Organizer".equalsIgnoreCase(role)) {
                                    replaceFragment(new FragmentOrganizer());
                                } else if("Admin".equalsIgnoreCase((role))) {
                                    replaceFragment(new BrowseEventsFragment());
                                } else {
                                    replaceFragment(new FragmentRoleSelection());
                                }
                            } else {
                                replaceFragment(new FragmentSignUp());
                            }
                        });
            } else {
                replaceFragment(new FragmentSignUp());
            }
        }
    }

    /**
     * Replaces the current fragment with the specified fragment.
     * <p>
     * Adds the transaction to the back stack so the user can navigate back.
     *
     * @param fragment Fragment to display
     */
    public void replaceFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}