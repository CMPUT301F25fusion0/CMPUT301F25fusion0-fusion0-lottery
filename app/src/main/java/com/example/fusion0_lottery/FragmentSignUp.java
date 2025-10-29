package com.example.projectfusion0;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fusion0_lottery.FragmentOrganizer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class FragmentSignUp extends Fragment {
    private EditText usernameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private EditText userRoleInput;


    private Button buttonSignUp;
    private Button buttonLogin;
    private FirebaseFirestore db;
    private String documentID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.example.projectfusion0.R.layout.fragment_signup, container, false);

        db = FirebaseFirestore.getInstance();

        // all sign up screen inputs and buttons
        usernameInput = view.findViewById(R.id.usernameInput);
        emailInput = view.findViewById(R.id.emailInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        userRoleInput = view.findViewById(R.id.userRoleInput);
        buttonSignUp = view.findViewById(R.id.buttonSignUp);
        buttonLogin = view.findViewById(R.id.buttonLogIn);

        // when user clicks sign up button, get all user inputs
        buttonSignUp.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String email = emailInput.getText().toString();
            String phone = phoneInput.getText().toString();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();
            String userRole = userRoleInput.getText().toString();

            // check if important info are all filled
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || userRole.isEmpty()) {
                Toast.makeText(getContext(), "Provide all necessary information", Toast.LENGTH_SHORT).show();
                return;
            }

            // check if password = confirm password
            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // check if user input Entrant or Organizer, else: show error
            if (!userRole.equalsIgnoreCase("Entrant") && !userRole.equalsIgnoreCase("Organizer")) {
                Toast.makeText(getContext(), "Invalid role", Toast.LENGTH_SHORT).show();
                return;
            }

            // add user to the database if all info is correctly filled
            addUserToDatabase(username, email, phone, password, userRole);
            ((com.example.projectfusion0.MainActivity) requireActivity()).replaceFragment(new FragmentOrganizer());
        });

        // if user clicks on the log in button, take them to the log in screen
        buttonLogin.setOnClickListener(v -> {
            ((com.example.projectfusion0.MainActivity) requireActivity()).replaceFragment(new com.example.projectfusion0.LogIn());
        });

        return view;
    }

    /**
     * this function adds users to the database
     * @param username is the user's username to be added
     * @param email is the user's email to be added
     * @param phone is the user's phone to be added
     * @param password is the user's password to be added
     * @param userRole is the user's role to be added
     */
    public void addUserToDatabase(String username, String email, String phone, String password, String userRole) {
        com.example.projectfusion0.Users user = new com.example.projectfusion0.Users(username, email, phone, password, userRole);

        db.collection("Users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentID = documentReference.getId();
                        Toast.makeText(getContext(), "Sign-up successful!", Toast.LENGTH_SHORT).show();

                        // clear inputs
                        usernameInput.setText("");
                        emailInput.setText("");
                        phoneInput.setText("");
                        passwordInput.setText("");
                        confirmPasswordInput.setText("");
                        userRoleInput.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}