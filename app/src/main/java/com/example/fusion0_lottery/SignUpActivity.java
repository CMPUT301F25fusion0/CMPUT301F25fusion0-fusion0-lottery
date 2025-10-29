package com.example.fusion0_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectfusion0.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * purpose:
 * This activity allows user to register and
 * once they registered they will be identified by their device id
 * User enter their full name, email, and phone number(optional).
 * outstanding issues:
 * it does not validate whether the input is valid
 *
 */

public class SignUpActivity extends AppCompatActivity {

    private EditText name;
    private EditText email_txt;
    private EditText phone;
    private Button signup;
    private String full_name;
    private String phone_number;
    private String email;
    private FirebaseAuth auth;
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        name = findViewById(R.id.name);
        email_txt = findViewById(R.id.email_txt);
        phone = findViewById(R.id.phone);
        signup = findViewById(R.id.signup);

            signup.setOnClickListener(view -> {
                full_name = name.getText().toString();
                email = email_txt.getText().toString();
                phone_number = phone.getText().toString();
                if (full_name.isEmpty() || email.isEmpty()){
                    Toast.makeText(this,"Please enter name and email", Toast.LENGTH_SHORT).show();
                }else {
                    create_user(full_name, email, phone_number);
                }
            });


    }

    /**
     * Creates a new user in Firebase using anonymous authentication.
     * @param full_name
     * @param email
     * @param phone_number
     */
    private void create_user(String full_name, String email, String phone_number) {
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String device_id = auth.getCurrentUser().getUid();
                User user = new User(device_id, full_name, email, phone_number, "");

                db.collection("Users").document(device_id).set(user)
                        .addOnSuccessListener(documentReference->{
                            Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, RoleSelectionActivity.class);
                            intent.putExtra("device_id", device_id);
                            startActivity(intent);
                            finish();
                        }).addOnFailureListener(e->{
                            Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show();
                        });

            }
        });
    }
}