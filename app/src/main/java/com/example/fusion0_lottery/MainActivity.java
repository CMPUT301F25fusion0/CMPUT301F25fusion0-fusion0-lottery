package com.example.fusion0_lottery;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.fusion0_lottery.EventLottery;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (savedInstanceState == null) {
            if (auth.getCurrentUser() != null) {
                // get device ID from current user in the Firestore database
                String device_id = auth.getCurrentUser().getUid();
                db.collection("Users").document(device_id).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            // if user exists
                            if (documentSnapshot.exists()) {
                                // get the user role
                                String role = documentSnapshot.getString("role");
                                // if the user currently doesn't have a role (user is currently signing up)
                                if (role == null || role.isEmpty()) {
                                    // user is taken to the role selection screen
                                    replaceFragment(new FragmentRoleSelection());
                                }
                                // if the user has a role and they are an "Entrant"
                                else if (role.equalsIgnoreCase("Entrant")) {
                                    // take them to the Entrant screen
                                    replaceFragment(EventLottery.newInstance(auth.getCurrentUser().getEmail()));
                                }

                                // if the user has a role and they are an "Organizer"
                                else if (role.equalsIgnoreCase("Organizer")) {
                                    // take them to the Organizer screen
                                    replaceFragment(new FragmentOrganizer());
                                }
                                else {
                                    replaceFragment(new FragmentRoleSelection());
                                }
                            }
                            // if they don't exist, then show them the signup screen
                            else {
                                replaceFragment(new FragmentSignUp());
                            }
                        });
            }
            else {
                replaceFragment(new FragmentSignUp());
            }
        }
    }

    public void replaceFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}