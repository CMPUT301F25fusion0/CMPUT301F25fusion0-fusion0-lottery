package com.example.fusion0_lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a RecyclerView adapter for displaying user profiles in admin dashboard
 * It displays user information including name, email, and role
 */

public class BrowseProfileAdapter extends RecyclerView.Adapter<BrowseProfileAdapter.UserViewHolder> {
    private List<User> users;
    private OnUserActionListener listener;

    /**
     * Interface for handling user actions on profiles in the RecyclerView.
     */
    public interface OnUserActionListener {
        void onUserClicked(User user);
    }

    /**
     * Constructs a new UserAdapter
     * @param users the list of users to display
     * @param listener the listener for handling user actions
     */
    public  BrowseProfileAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }
    /**
     * Creates new ViewHolder for RecyclerView
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View.
     * @return a new ViewHolder that holds a view
     */

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_profiles, parent, false);
        return new UserViewHolder(view);

    }

    /**
     * Binds user data to the ViewHolder
     * @param holder The ViewHolder which should be updated
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BrowseProfileAdapter.UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        holder.role.setText(user.getRole());

        // Set click listener if needed
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClicked(user);
            }
        });

    }

    /**
     * Returns the total number of items in the data set held by the adapter
     * @return the total number of items in this adapter
     */
    @Override
    public int getItemCount() {
        return users.size();
    }
    /**
     * Updates the list of users and refreshes the adapter
     * @param newList the new list of users
     */
    public void updateList(List<User> newList) {
        this.users = newList;
        notifyDataSetChanged();
    }
    /**
     * ViewHolder class for user profile items
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, role;

        /**
         * Constructs a ViewHolder for the user profile item view
         * @param itemView the view of the user profile item
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            email = itemView.findViewById(R.id.userEmail);
            role = itemView.findViewById(R.id.userRole);
        }
    }
}