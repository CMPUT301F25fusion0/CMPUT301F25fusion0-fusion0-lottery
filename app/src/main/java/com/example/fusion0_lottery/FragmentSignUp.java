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

    private EditText nameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput;
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
        passwordInput = view.findViewById(R.id.passwordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        signupButton = view.findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();
            String phone_number = phoneInput.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "All fields not filled", Toast.LENGTH_SHORT).show();
            }

            else if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
            else {
                createUser(name, email, phone_number, password, "");
            }
        });

        return view;
    }

    private void createUser(String name, String email, String phone_number, String password, String role) {
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String device_id = auth.getCurrentUser().getUid();
                User user = new User(name, email, phone_number, password, "" , device_id);

                db.collection("Users").document(device_id).set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Sign up successful", Toast.LENGTH_SHORT).show();
                            ((MainActivity) requireActivity()).replaceFragment(new FragmentRoleSelection());
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Sign up failed", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
