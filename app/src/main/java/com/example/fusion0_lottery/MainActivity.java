package com.example.projectfusion0;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fusion0_lottery.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // sign up inputs
    private EditText usernameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput;

    // button for the sign up button
    private Button buttonSignUp;

    // when user is created in the database, store the document ID
    protected String documentID;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        buttonSignUp.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String email = emailInput.getText().toString();
            String phone = phoneInput.getText().toString();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            // some app pop-up messages if sign up isn't done correctly goes below
            // feel free to add some yourselves

            // check if name, email, and password are filled
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Provide necessary information", Toast.LENGTH_SHORT).show();
                return;
            }

            // check if password = confirm password
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // if sign up information is correctly inputted, add user to the database
            addUserToDatabase(username, email, phone, password);
        });
    }

    /**
     * this function adds users to the database
     * @param username is the user's username to be added
     * @param email is the user's email to be added
     * @param phone is the user's phone to be added
     * @param password is the user's password to be added
    */
    private void addUserToDatabase(String username, String email, String phone, String password) {
        Users user = new Users(username, email, phone, password);

        db.collection("Users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // store the documentID for that specific user (used to delete user account)
                        documentID = documentReference.getId();
                        Toast.makeText(MainActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                        // clear information after signing up
                        usernameInput.setText("");
                        emailInput.setText("");
                        phoneInput.setText("");
                        passwordInput.setText("");
                        confirmPasswordInput.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // function to delete user from the database (hasn't been tested yet)
    private void deleteUserFromDatabase() {
        // using the documentID stored from when user was created, delete the ID and the user
        db.collection("Users").document(documentID).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Successfully deleted user", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
