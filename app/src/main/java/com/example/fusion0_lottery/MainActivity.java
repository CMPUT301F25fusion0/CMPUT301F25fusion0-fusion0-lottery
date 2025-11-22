package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
                                    replaceFragment(new FragmentAdmin());
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

    public void replaceFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
