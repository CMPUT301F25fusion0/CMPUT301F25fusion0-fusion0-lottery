package com.example.lottery;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;



import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // We'll create this layout next

        db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("name", "Alice");
        user.put("age", 25);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference ->
                        Log.d("Firestore", "Document added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.w("Firestore", "Error adding document", e));
    }
}
