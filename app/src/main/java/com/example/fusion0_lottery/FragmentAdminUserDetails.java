package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FragmentAdminUserDetails extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference userRef = db.collection("Users");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user_details, container, false);

        // Initialize Views and Buttons
        TextView nameText = view.findViewById(R.id.name);
        TextView idText = view.findViewById(R.id.id);
        TextView emailText = view.findViewById(R.id.email);
        TextView phoneNumberText = view.findViewById(R.id.phoneNumber);
        TextView roleText = view.findViewById(R.id.role);
        Button delete = view.findViewById(R.id.deleteUser);

        //Toolbar
        Toolbar toolbar = view.findViewById(R.id.adminUserDetailsToolbar);
        toolbar.setNavigationOnClickListener(_v ->
                requireActivity().getSupportFragmentManager().popBackStack());


        if (getArguments() != null) {
            String name = getArguments().getString("name");
            String userId = getArguments().getString("userId");
            String email = getArguments().getString("email");
            String phoneNumber = getArguments().getString("phoneNumber");
            String role = getArguments().getString("role");

            nameText.setText("Name: " + name);
            idText.setText("ID: " + userId);
            emailText.setText("Email: " + email);
            if (phoneNumber == null) {
                phoneNumber = "None";
            }
            phoneNumberText.setText("Phone Number: " + phoneNumber);
            roleText.setText("Role: " + role);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeProfile(userId);
                }
            });
        }

        return view;
    }

    public void removeProfile(String userId) {
        // Check if it is current user
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!currentUser.equals(userId)) {
            userRef.document(userId).delete();
            Toast.makeText(getContext(), "User deleted", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }
        else {
            Toast.makeText(getContext(), "You can not delete your own profile!", Toast.LENGTH_SHORT).show();
        }
    }
}