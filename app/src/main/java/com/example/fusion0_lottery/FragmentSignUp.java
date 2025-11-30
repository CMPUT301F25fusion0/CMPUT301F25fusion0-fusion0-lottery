package com.example.fusion0_lottery;


import static com.google.firebase.messaging.Constants.TAG;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * This fragment handles user sign up functionality
 * It allows user to register by providing their name, email, and optional phone number
 * It uses firebase Anonymous Authentication to give a unique device ID
 */

public class FragmentSignUp extends Fragment {

    private EditText nameInput, emailInput, phoneInput, admin_verification;
    private Button signupButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String admin_key= "";
    private boolean isAdminEmail = false;


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

        admin_verification = new EditText(getContext());
        admin_verification.setHint("Admin Verification");
        admin_verification.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        admin_verification.setVisibility(View.GONE);

        LinearLayout layout = view.findViewById(R.id.main);
        layout.addView(admin_verification);

        loadAdminCode();
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                String email = s.toString().trim().toLowerCase();
                checkIfAdminEmail(email);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String code = admin_verification.getText().toString().trim();
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Please fill name and email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isAdminEmail && code.isEmpty()) {
                Toast.makeText(getContext(), "Admin verification code required", Toast.LENGTH_SHORT).show();
                return;
            }
            checkAdminAndCreateUser(name, email, phone, code);
        });

        return view;
    }

    /**
     * Loads the current admin verification code from Firestore.
     */
    private void loadAdminCode() {
        db.collection("AdminSecurity").document("adminCodes")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("currentCode")) {
                        admin_key = documentSnapshot.getString("currentCode");
                    }
                });
    }

    /**
     * Checks if the provided email address belongs to an admin user.
     * @param email The email address to check for admin privileges
     */
    private void checkIfAdminEmail(String email) {
        db.collection("admins").document(email.toLowerCase())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isAdminEmail = documentSnapshot.exists();
                    admin_verification.setVisibility(isAdminEmail ? View.VISIBLE : View.GONE);

                    if (isAdminEmail) {
                        Toast.makeText(getContext(), "Admin email detected - enter verification code", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking admin email: " + e.getMessage());
                    isAdminEmail = false;
                    admin_verification.setVisibility(View.GONE);
                });
    }

    /**
     * Validates admin credentials and initiates user creation.
     * @param name The user's full name
     * @param email The user's email address
     * @param phone The user's phone number (optional)
     * @param enteredCode enteredCode The admin verification code entered by the user
     */
    private void checkAdminAndCreateUser(String name, String email, String phone, String enteredCode) {
        if (!isAdminEmail) {
            createUser(name, email, phone, false);
            return;
        }
        db.collection("admins").document(email.toLowerCase())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isAdminEmail = documentSnapshot.exists();
                    boolean isAdmin = false;

                    if (isAdminEmail) {
                        if (admin_key != null && admin_key.equals(enteredCode)) {
                            isAdmin = true;
                            Toast.makeText(getContext(), "Admin access granted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Invalid admin password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    createUser(name, email, phone, isAdmin);
                });
    }

    /**
     * Creates a new user account in Firebase Authentication and Firestore
     * @param name The user's full name
     * @param email The user's email address
     * @param phone The user's phone number (optional)
     * @param isAdmin  Boolean indicating whether the user should have admin privileges
     */
    private void createUser(String name, String email, String phone, boolean isAdmin) {
        // Sign in anonymously to get a device ID
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                String deviceId = auth.getCurrentUser().getUid();
                String role = isAdmin ? "admin" : "";

                User user = new User(name, email, phone, role, deviceId);

                db.collection("Users").document(deviceId).set(user)
                        .addOnSuccessListener(aVoid -> {
                            if (isAdmin) {
                                Toast.makeText(getContext(), "Admin account created!", Toast.LENGTH_SHORT).show();
                                ((MainActivity) requireActivity()).replaceFragment(new BrowseEventsFragment());
                            } else {
                                Toast.makeText(getContext(), "Sign up successful!", Toast.LENGTH_SHORT).show();
                                ((MainActivity) requireActivity()).replaceFragment(new FragmentRoleSelection());
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}