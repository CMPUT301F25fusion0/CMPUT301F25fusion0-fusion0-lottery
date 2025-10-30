package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fusion0_lottery.MainActivity;
import com.example.fusion0_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Purpose:
 * This activity allows a user to select their role
 * The selected role is updated in Firestore database
 * Outstanding issues:
 * for now there is no outstanding issues
 */

public class RoleSelectionActivity extends AppCompatActivity {
    private Button event_enterant;
    private Button orgainzer;
    private FirebaseFirestore db;
    private String device_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        db = FirebaseFirestore.getInstance();
        device_id = getIntent().getStringExtra("device_id");

        event_enterant = findViewById(R.id.event_enterant);
        orgainzer = findViewById(R.id.organizer);

        event_enterant.setOnClickListener(view -> {
            select_role("entrant");
        });
        orgainzer.setOnClickListener(view -> {
            select_role("organizer");
        });

    }

    /**
     * used to update the user's role in Firestore and navigates to MainActivity.
     * @param role
     */
    private void select_role(String role) {
        db.collection("Users").document(device_id).update("role",role)
                .addOnSuccessListener(documentReference ->{
                    Intent intent = new Intent(RoleSelectionActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
    }
}