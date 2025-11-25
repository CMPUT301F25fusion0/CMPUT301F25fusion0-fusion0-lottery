package com.example.fusion0_lottery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FragmentAdminUsers extends Fragment {

    private LinearLayout userLayout;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference userRef = db.collection("Users");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.adminUserToolbar);
        toolbar.setTitle("All Users");
        toolbar.setNavigationOnClickListener(_v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Initialize Views and Adapters
        userLayout = view.findViewById(R.id.userLinearLayout);

        // Load users from Firestore
        userRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }
            List<DocumentSnapshot> users = new ArrayList<>();
            for (DocumentSnapshot doc : value.getDocuments()) {
                users.add(doc);
            }
            displayUsers(users);
        });

        return view;
    }
    public void displayUsers(List<DocumentSnapshot> users) {
        if (!isAdded() || getContext() == null || userLayout == null) return;

        userLayout.removeAllViews();

        for (DocumentSnapshot eventDoc : users) {
            String userId = eventDoc.getId();
            String email = eventDoc.getString("email");
            String name = eventDoc.getString("name");
            String phoneNumber  = eventDoc.getString("phone_number");
            String role = eventDoc.getString("role");

            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            card.setPadding(24, 24, 24, 24);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 24);
            card.setLayoutParams(cardParams);

            TextView nameText = new TextView(getContext());
            nameText.setText("Name: " + name);
            nameText.setTextSize(20f);
            nameText.setTextColor(getResources().getColor(android.R.color.black));
            nameText.setPadding(0, 0, 0, 8);

            TextView roleText = new TextView(getContext());
            roleText.setText("Role: " + role);
            roleText.setTextSize(20f);
            roleText.setTextColor(getResources().getColor(android.R.color.black));
            roleText.setPadding(0, 0, 0, 8);

            Button detailsButton = new Button(getContext());
            detailsButton.setText("Details");
            detailsButton.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
            detailsButton.setTextColor(getResources().getColor(android.R.color.white));

            detailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentAdminUserDetails fragment = new FragmentAdminUserDetails();
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userId);
                    bundle.putString("email", email);
                    bundle.putString("name", name);
                    bundle.putString("role", role);
                    bundle.putString("phoneNumber", phoneNumber);
                    fragment.setArguments(bundle);
                    ((MainActivity) requireActivity()).replaceFragment(fragment);
                }
            });
            card.addView(nameText);
            card.addView(roleText);
            card.addView(detailsButton);
            userLayout.addView(card);
        }
    }

    }
