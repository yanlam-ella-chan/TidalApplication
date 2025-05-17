package com.example.tidalapplication.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.example.tidalapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPage extends Fragment {

    private View view;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.fragment_add_page, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up button listeners
        setupButtons();

        return view;
    }

    private void setupButtons() {
        Button buttonAddPlace = view.findViewById(R.id.button_add_place);

        buttonAddPlace.setOnClickListener(v -> showBottomSheet());
    }

    private void showBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_place, null);

        EditText locationName = bottomSheetView.findViewById(R.id.location_name);
        EditText editLatitude = bottomSheetView.findViewById(R.id.edit_latitude);
        EditText editLongitude = bottomSheetView.findViewById(R.id.edit_longitude);

        Button btnCancel = bottomSheetView.findViewById(R.id.btn_cancel);
        Button btnConfirm = bottomSheetView.findViewById(R.id.btn_confirm);

        bottomSheetDialog.setContentView(bottomSheetView);

        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String name = locationName.getText().toString();
            String latString = editLatitude.getText().toString();
            String lngString = editLongitude.getText().toString();

            if (name.isEmpty() || latString.isEmpty() || lngString.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                double latitude = Double.parseDouble(latString);
                double longitude = Double.parseDouble(lngString);

                // Create a new location object
                Location location = new Location(name, latitude, longitude);

                // Store the location in Firestore
                db.collection("locations")
                        .add(location)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getActivity(), "Location added!", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Error adding location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        bottomSheetDialog.show();
    }

    // Location class to represent the data structure
    public static class Location {
        private String name;
        private double latitude;
        private double longitude;

        public Location() {
            // Firestore requires a no-argument constructor
        }

        public Location(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters
        public String getName() { return name; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}