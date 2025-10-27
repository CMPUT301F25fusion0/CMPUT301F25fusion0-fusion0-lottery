package com.example.projectfusion0;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    /*
    private EditText usernameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private Button buttonName;
    private String username;
    private String email;
    private String phone;
    */

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // below is a test example to see if info is added to database (working)
        Map<String, Object> user1 = new HashMap<>();
        user1.put("username", "test");
        user1.put("email", "email@gmail.com");
        user1.put("phone", "123");

        db.collection("users")
                .add(user1)
                .addOnSuccessListener(documentReference ->
                        Log.d("Firestore", "Profile Created: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.w("Firestore", "Error Creating Profile", e));


        // below is a test example to see if user2 is added and then delete (working)
        Map<String, Object> user2 = new HashMap<>();
        user2.put("username", "testDelete");
        user2.put("email", "delete@gmail.com");
        user2.put("phone", "789");

        db.collection("users")
                .add(user2)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Profile Created: " + documentReference.getId());
                    db.collection("users").document(documentReference.getId())
                            .delete()
                            .addOnSuccessListener(aVoid ->
                                    Log.d("Firestore", "User2 successfully deleted!"))
                            .addOnFailureListener(e ->
                                    Log.w("Firestore", "Error deleting User2", e));
                })
                .addOnFailureListener(e ->
                        Log.w("Firestore", "Error Creating Profile", e));
    }
}

/*
// idk some random code that might work template
private void addUserToDatabase(String username, String email, String phoneNumber){
    CollectionReference UsersDB = db.collection("Users");
    Users user = new Users(username, email, phoneNumber);

     UsersDB.add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>(){
        @Override
        public void onSuccess(DocumentReference documentReference) {
            Toast.makeText(MainActivity.this, "User has successfully been added", Toast.LENGTH_SHORT).show();
        }
     }).addOnFailureListener(new OnFailureListener(){
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(MainActivity.this, "User Creation Failed" + e, Toast.LENGTH_SHORT).show();
        }
     });
}
*/
