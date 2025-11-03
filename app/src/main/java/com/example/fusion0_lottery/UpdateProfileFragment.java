package com.example.fusion0_lottery;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfileFragment extends Fragment {

    private EditText inputUsername, inputEmail, inputPhone;
    private Button btnUpdate;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_update_profile, container, false);

        // ---- UI refs
        inputUsername = root.findViewById(R.id.inputUsername);
        inputEmail    = root.findViewById(R.id.inputEmail);
        inputPhone    = root.findViewById(R.id.inputPhone);
        btnUpdate     = root.findViewById(R.id.btnUpdate);

        // ---- Toolbar back
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        // ---- Firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            btnUpdate.setEnabled(false);
            return root;
        }
        uid = auth.getCurrentUser().getUid();

        // ---- Prefill fields
        final DocumentReference userRef = db.collection("Users").document(uid);
        userRef.get()
                .addOnSuccessListener(snap -> {
                    if (snap != null && snap.exists()) {
                        String username = snap.getString("username");
                        String email    = snap.getString("email");
                        String phone    = snap.getString("phone");
                        inputUsername.setText(username != null ? username : "");
                        inputEmail.setText(email != null ? email : "");
                        inputPhone.setText(phone != null ? phone : "");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );

        // ---- Update action
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                updateProfile();
            }
        });

        return root;
    }

    private void updateProfile() {
        String username = inputUsername.getText().toString().trim();
        String email    = inputEmail.getText().toString().trim();
        String phone    = inputPhone.getText().toString().trim();

        if (!validate(username, email, phone)) return;

        btnUpdate.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("email", email);
        updates.put("phone", phone);

        db.collection("Users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                )
                .addOnCompleteListener(task -> btnUpdate.setEnabled(true));
    }

    private boolean validate(String username, String email, String phone) {
        if (TextUtils.isEmpty(username)) { inputUsername.setError("Username required"); return false; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Valid email required"); return false;
        }
        if (!TextUtils.isEmpty(phone) && phone.length() < 3) {
            inputPhone.setError("Phone seems too short"); return false;
        }
        return true;
    }
}
