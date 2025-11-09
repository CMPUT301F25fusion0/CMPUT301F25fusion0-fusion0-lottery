package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView recycler_waiting;
    private RecyclerView recycler_selected;

    private FirebaseFirestore db;
    private FirebaseUser current_user;
    private List<Event> waiting_events;
    private List<Event> selected_event;
    private MyEventAdapter waitingAdapter;
    private MyEventAdapter selectedAdapter;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        db = FirebaseFirestore.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser();

        recycler_waiting = findViewById(R.id.recycler_waiting);
        recycler_selected = findViewById(R.id.recycler_selected);



        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();


        waiting_events = new ArrayList<>();
        selected_event = new ArrayList<>();


        horizontal_scroll(recycler_waiting);
        horizontal_scroll(recycler_selected);

        waitingAdapter = new MyEventAdapter(waiting_events,"waiting");
        selectedAdapter = new MyEventAdapter(selected_event,"selected");



        recycler_waiting.setAdapter(waitingAdapter);
        recycler_selected.setAdapter(selectedAdapter);
        load_events();
    }

    private void horizontal_scroll(RecyclerView recycler) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(layoutManager);
    }


    private void load_events() {

        waiting_events.clear();
        selected_event.clear();
        updateAdapters();

        db.collection("Events").whereArrayContains("waitingList", current_user
                .getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                Event event = snapshot.toObject(Event.class);
                event.setEventId(snapshot.getId());
                waiting_events.add(event);
            }

            updateAdapters();
        });
        db.collection("Events").whereArrayContains("selectedUser",current_user
                .getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots ->{
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                 Event event = document.toObject(Event.class);
                 event.setEventId(document.getId());
                 selected_event.add(event);
            }

            updateAdapters();
        });
    }

    private void updateAdapters() {
        if(waitingAdapter != null){
            waitingAdapter.notifyDataSetChanged();
        }
        if (selectedAdapter != null){
            selectedAdapter.notifyDataSetChanged();
        }
    }
    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(MyEventsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_my_events) {
                return true;
            } else if (itemId == R.id.navigation_profile) {

//                Intent intent = new Intent(MyEventsActivity.this, ProfileActivity.class);
//                startActivity(intent);
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_my_events);
    }

}