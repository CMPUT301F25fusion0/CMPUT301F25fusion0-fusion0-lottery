package com.example.fusion0_lottery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

public class FragmentUpdatePoster extends Fragment {


    private ImageView posterPreview;
    private Button pickImageButton, uploadButton;
    private Uri selectedImageUri;
    private String eventId;
    private FirebaseFirestore db;

    // launch a screen where the user can upload/update the poster image
    private final ActivityResultLauncher<Intent> pickImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // check if user chose an image, get the URI of the image and store in the image preview
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    posterPreview.setImageURI(selectedImageUri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_update_poster, container, false);

        posterPreview = view.findViewById(R.id.posterPreview);
        pickImageButton = view.findViewById(R.id.pickImageButton);
        uploadButton = view.findViewById(R.id.uploadButton);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        pickImageButton.setOnClickListener(v -> {
            pickImage();
        });

        // after selecting an image to update poster, if image is valid then upload to Firestore
        uploadButton.setOnClickListener(v -> {
            if (selectedImageUri != null && eventId != null) {
                uploadPosterToFirestore(selectedImageUri);
            }
            else {
                Toast.makeText(requireContext(), "Select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        Button backButton = view.findViewById(R.id.backButton);

        // when back button is clicked, go back to the Manage Events screen
        backButton.setOnClickListener(v -> {
            ManageEvents manageEvents = new ManageEvents();
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            manageEvents.setArguments(args);
            ((MainActivity) requireActivity()).replaceFragment(manageEvents);
        });

        return view;
    }


    // new screen to let the organizer select an image to update the poster
    private void pickImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImage.launch(intent);
    }


    /**
     * Upload poster image to Firebase Storage
     * Convert image to Base64
     * Scale the image then upload to Firestore
     * References:
     *     https://stackoverflow.com/questions/65210522/how-to-get-bitmap-from-imageuri-in-api-level-30
     *     https://stackoverflow.com/questions/4830711/how-can-i-convert-an-image-into-a-base64-string
     *     https://stackoverflow.com/questions/9224056/android-bitmap-to-base64-string
     *     https://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android
     */
    private void uploadPosterToFirestore(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            // scale the image down and keep aspect ratio
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000 * bitmap.getHeight() / bitmap.getWidth(), true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            db.collection("Events").document(eventId).set(Collections.singletonMap("posterImage", encodedImage), SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Poster uploaded successfully!", Toast.LENGTH_SHORT).show();
                        posterPreview.setImageBitmap(scaledBitmap);
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
