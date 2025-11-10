package com.example.fusion0_lottery;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FragmentSignUp extends Fragment {

    private EditText nameInput, emailInput, phoneInput;
    private Button signupButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_sign_up, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameInput = view.findViewById(R.id.nameInput);
        emailInput = view.findViewById(R.id.emailInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        signupButton = view.findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim(); // optional

            // Validate only required fields: name and email
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Please fill name and email", Toast.LENGTH_SHORT).show();
                return;
            }

            createUser(name, email, phone);
        });

        return view;
    }

    private void createUser(String name, String email, String phone) {
        // Sign in anonymously to get a device ID
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                String deviceId = auth.getCurrentUser().getUid();

                User user = new User(name, email, phone, "", deviceId); // Role can be set later

                db.collection("Users").document(deviceId).set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Sign up successful", Toast.LENGTH_SHORT).show();
                            ((MainActivity) requireActivity()).replaceFragment(new FragmentRoleSelection());
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean validSignUp(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}
