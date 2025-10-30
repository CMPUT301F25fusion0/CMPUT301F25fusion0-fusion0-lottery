package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.fusion0_lottery.RoleSelectionActivity;
import com.example.fusion0_lottery.SignUpActivity;
import com.example.fusion0_lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;



public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null){
            String device_id = auth.getCurrentUser().getUid();
            db.collection("Users").document(device_id).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            String role = documentSnapshot.getString("role");
                            if (role == null || role.isEmpty()){
                                Intent intent = new Intent(MainActivity.this, RoleSelectionActivity.class);
                                intent.putExtra("device_id", device_id);
                                startActivity(intent);
                                finish();
                            } else{
                                setContentView(R.layout.activity_main);

                            }
                        }else{
                            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                            finish();
                        }
                    });
        }else{
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            finish();
        }

    }
}


