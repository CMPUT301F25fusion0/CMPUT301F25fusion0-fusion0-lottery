package com.example.fusion0_lottery;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class FragmentSignUp extends Fragment {

    private EditText usernameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput;
    private Button buttonEntrant, buttonOrganizer;
    private FirebaseFirestore db;
    private String documentID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Use the correct R from your current package
        View view = inflater.inflate(R.layout.fragment_signup, container, false);


        db = FirebaseFirestore.getInstance();

        // Toolbar back arrow
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        usernameInput = view.findViewById(R.id.usernameInput);
        emailInput = view.findViewById(R.id.emailInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        buttonEntrant = view.findViewById(R.id.buttonEntrant);
        buttonOrganizer = view.findViewById(R.id.buttonOrganizer);

        // Entrant button click
        buttonEntrant.setOnClickListener(v -> signupUser("Entrant"));

        // Organizer button click
        buttonOrganizer.setOnClickListener(v -> signupUser("Organizer"));

        return view;
    }

    private void signupUser(String role) {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get device ID
        Context context = getContext();
        String deviceId = "";
        if (context != null) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        // Use Users class from the current package
        Users user = new Users(username, email, phone, password, role, deviceId);

        db.collection("Users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    documentID = documentReference.getId();
                    Toast.makeText(getContext(), "Sign-up successful!", Toast.LENGTH_SHORT).show();

                    // Clear fields
                    usernameInput.setText("");
                    emailInput.setText("");
                    phoneInput.setText("");
                    passwordInput.setText("");
                    confirmPasswordInput.setText("");

                    // Navigate based on role using current package
                    if (role.equalsIgnoreCase("Entrant")) {
                        ((MainActivity) requireActivity()).replaceFragment(EventLottery.newInstance(email));
                    } else if (role.equalsIgnoreCase("Organizer")) {
                        ((MainActivity) requireActivity()).replaceFragment(new OrganizerDashboard());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
