package com.example.fusion0_lottery;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectfusion0.R;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class MainActivityOrganizer extends AppCompatActivity {

    private Users organizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_activity_organizer);

        // Test events
        Event fortnite = new Event("fortnite",
                "gaming event", "march",
                "april", "6:07",
                6.7, "edmonton",
                "feb", "march",
                2);
        ArrayList<Event> testEvent = new ArrayList<>();
        testEvent.add(fortnite);

        ListView eventsOrg = findViewById(R.id.eventsOrg);
        ArrayAdapter<Event> testAdapter = new ArrayAdapter<>(this, R.layout.event_layout, testEvent);
        eventsOrg.setAdapter(testAdapter);



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}