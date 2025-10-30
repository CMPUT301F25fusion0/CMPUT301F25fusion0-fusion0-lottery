package com.example.fusion0_lottery;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        // Check if user already exists in Firestore
        checkIfUserExists();
    }

    private void checkIfUserExists() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("Users")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String userRole = userDoc.getString("userRole");
                        String email = userDoc.getString("email");

                        if ("Entrant".equalsIgnoreCase(userRole)) {
                            // Navigate to Event Lottery directly
                            replaceFragment(EventLottery.newInstance(email));
                        } else if ("Organizer".equalsIgnoreCase(userRole)) {
                            // Navigate to Organizer Dashboard
                            replaceFragment(new OrganizerDashboard());
                        }
                    } else {
                        // No user found — open Sign-Up fragment
                        replaceFragment(new FragmentSignUp());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // fallback to signup
                    replaceFragment(new FragmentSignUp());
                });
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
