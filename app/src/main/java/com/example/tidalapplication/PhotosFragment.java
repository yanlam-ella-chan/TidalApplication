package com.example.tidalapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotosFragment extends Fragment {

    private String locationId; // Change from locationName to locationId
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private RecyclerView recyclerView;

    private TextView noPhotosText;
    private List<Bitmap> photoBitmaps; // Store Bitmaps instead of URLs
    private PhotosAdapter adapter;

    private ImageView photoImageView;

    // Update constructor to accept locationId
    public PhotosFragment(String locationId) {
        this.locationId = locationId; // Initialize locationId
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        recyclerView = view.findViewById(R.id.photosRecyclerView);
        noPhotosText = view.findViewById(R.id.noPhotosText);
        Button addPhotoButton = view.findViewById(R.id.addPhotoButton);

        photoBitmaps = new ArrayList<>();
        adapter = new PhotosAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchPhotosFromFirebase();

        addPhotoButton.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                showAddPhotoDialog(); // Show the dialog if the user is logged in
            } else {
                Toast.makeText(getContext(), "Please log in to add a photo.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchPhotosFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query for photos by location ID
        db.collection("photos")
                .whereEqualTo("locationId", locationId) // Filter by locationId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<Float, List<Photo>> groupedPhotos = new HashMap<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String base64Image = document.getString("image");
                            float tideLevel = document.getDouble("tideLevel").floatValue();
                            if (base64Image != null) {
                                Photo photo = new Photo(base64Image, tideLevel);
                                groupedPhotos.computeIfAbsent(tideLevel, k -> new ArrayList<>()).add(photo);
                            }
                        }
                        updateUI(groupedPhotos);
                    } else {
                        Toast.makeText(getContext(), "Error fetching photos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); // Decode to Bitmap
    }

    private void updateUI(Map<Float, List<Photo>> groupedPhotos) {
        if (groupedPhotos.isEmpty()) {
            noPhotosText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noPhotosText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        // Create a list to hold photo groups
        List<PhotoGroup> photoGroups = new ArrayList<>();
        for (Map.Entry<Float, List<Photo>> entry : groupedPhotos.entrySet()) {
            photoGroups.add(new PhotoGroup(entry.getKey(), entry.getValue()));
        }

        adapter.setPhotoGroups(photoGroups); // Update your adapter with new data
        adapter.notifyDataSetChanged();
    }

    private void showAddPhotoDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_photo);

        // Set the width and height of the dialog
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        photoImageView = dialog.findViewById(R.id.photoImageView);
        Button selectPhotoButton = dialog.findViewById(R.id.selectPhotoButton);
        Button addButton = dialog.findViewById(R.id.addButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        SeekBar tideLevelSeekBar = dialog.findViewById(R.id.tideLevelSeekBar);
        TextView tideLevelValue = dialog.findViewById(R.id.tideLevelValue);

        // Set the default visibility state
        photoImageView.setVisibility(View.VISIBLE);

        // Initialize tide level variable
        final float[] tideLevel = {0.0f};

        tideLevelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tideLevel[0] = progress / 10.0f; // Convert to float (0.0 to 3.0)
                tideLevelValue.setText(String.format("%.1f", tideLevel[0])); // Display with one decimal place
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        selectPhotoButton.setOnClickListener(v -> openFileChooser());

        addButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadPhoto(selectedImageUri, tideLevel[0]); // Upload photo with tide level
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please select a photo first.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void uploadPhoto(Uri photoUri, float tideLevel) {
        try {
            // Convert the image to a byte array
            InputStream inputStream = getContext().getContentResolver().openInputStream(photoUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            // Convert byte array to Base64
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Get user email from Firebase Auth
            String userEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getEmail() : "Unknown User";

            // Get current date and time
            String addedDateTime = getCurrentDateTime();

            // Log values
            Log.d("PhotosFragment", "User Email: " + userEmail);
            Log.d("PhotosFragment", "Added DateTime: " + addedDateTime);

            // Save metadata to Firestore
            savePhotoMetadata(base64Image, tideLevel, userEmail, addedDateTime);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error uploading photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private void savePhotoMetadata(String base64Image, float tideLevel, String userEmail, String addedDateTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("image", base64Image);
        data.put("locationId", locationId); // Save locationId instead of locationName
        data.put("tideLevel", tideLevel);
        data.put("addedBy", userEmail);
        data.put("addedDateTime", addedDateTime);

        // Use a unique document ID (e.g., timestamp or user ID)
        String documentId = String.valueOf(System.currentTimeMillis()); // Use current timestamp as ID

        db.collection("photos").document(documentId).set(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Photo uploaded successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving photo metadata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            if (photoImageView != null) {
                photoImageView.setImageURI(selectedImageUri);
            }
        } else {
            if (photoImageView != null) {
                photoImageView.setImageResource(R.drawable.baseline_add_a_photo_24);
            }
        }
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId; // Set the location ID
    }
}