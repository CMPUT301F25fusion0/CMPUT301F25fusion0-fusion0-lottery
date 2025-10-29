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
import com.google.firebase.firestore.FirebaseFirestore;

public class LogIn extends Fragment {

    private EditText emailInput;
    private EditText passwordInput;
    private EditText userRoleInput;
    private Button buttonLogin;
    private Button buttonSignUp;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        db = FirebaseFirestore.getInstance();

        // allow user input information for login
        emailInput = view.findViewById(R.id.loginEmail);
        passwordInput = view.findViewById(R.id.loginPassword);
        userRoleInput = view.findViewById(R.id.loginUserRole);

        // buttons on the login screen
        buttonLogin = view.findViewById(R.id.buttonLogin);
        buttonSignUp = view.findViewById(R.id.buttonSignup);

        // log in button, when clicked, check if user with inputted information exists in database
        buttonLogin.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            String userRole = userRoleInput.getText().toString();

            // make sure email, password, and userRole isn't empty
            if (email.isEmpty() || password.isEmpty() || userRole.isEmpty()) {
                Toast.makeText(getContext(), "Provide all necessary information", Toast.LENGTH_SHORT).show();
                return;
            }

            // check if user inputs valid role, if not show error
            if (!userRole.equalsIgnoreCase("Entrant") && !userRole.equalsIgnoreCase("Organizer")) {
                Toast.makeText(getContext(), "Invalid role", Toast.LENGTH_SHORT).show();
                return;
            }

            // in the database, find where email, password, and userRole all match the user's input
            db.collection("Users").whereEqualTo("email", email).whereEqualTo("password", password).whereEqualTo("userRole", userRole).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                // if found user with correct email and password
                if (!queryDocumentSnapshots.isEmpty()) {
                    // show successful login message
                    Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                    // set input fields to be empty
                    emailInput.setText("");
                    passwordInput.setText("");
                    userRoleInput.setText("");

                    // below is the code to send the user to the dashboard screen (incomplete)
                    /*
                    // if user is an Entrant, send them to Entrant dashboard screen
                    if (userRole.equalsIgnoreCase("Entrant")) {
                        // ((com.example.projectfusion0.MainActivity) requireActivity()).replaceFragment(new EntrantDashboard());
                    }
                    */

                    // if user is an Organizer, send them to Organizer dashboard screen
                    if (userRole.equalsIgnoreCase("Organizer")) {
                        ((com.example.projectfusion0.MainActivity) requireActivity()).replaceFragment(new FragmentOrganizer());
                    }

                }

                // if we did not find a user with inputted email and password
                else {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // button to take user back to the sign up screen
        buttonSignUp.setOnClickListener(v -> {
            ((com.example.projectfusion0.MainActivity) requireActivity()).replaceFragment(new com.example.projectfusion0.FragmentSignUp());
        });

        return view;
    }

}
