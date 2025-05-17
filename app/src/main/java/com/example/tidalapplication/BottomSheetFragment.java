package com.example.tidalapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.example.tidalapplication.fragments.HomePage;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class BottomSheetFragment extends BottomSheetDialogFragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;
    private String locationTitle;
    private TextView dateTimeText, locationNameText, tideLevelText, tideValueText;

    private Button pickDateTimeButton, downloadButton, addPlaceButton, cancelButton;

    private ImageButton changeDateTimeIcon;

    private EditText locationNameEditText; // EditText for location name
    private LocalDateTime selectedDateTime;
    private boolean showLoginMessage, isAddingPlace;
    private FirebaseFirestore db; // Firestore reference
    private FirebaseAuth mAuth; // Firebase Auth reference

    private List<Double> tideLevels;

    private String locationId;

    public interface OnPlaceAddedListener {
        void onPlaceAdded();
    }

    private OnPlaceAddedListener listener;

    public BottomSheetFragment(boolean showLoginMessage, boolean isAddingPlace, OnPlaceAddedListener listener, String locationTitle) {
        this.showLoginMessage = showLoginMessage;
        this.isAddingPlace = isAddingPlace;
        this.listener = listener;
        this.locationTitle = locationTitle; // Set the title
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        ImageButton closeButton = view.findViewById(R.id.closeButton);
        tideLevelText = view.findViewById(R.id.tideLevelText);
        tideValueText = view.findViewById(R.id.tideValueText);
        dateTimeText = view.findViewById(R.id.dateTimeText);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        locationNameText = view.findViewById(R.id.locationNameText);

        // Initialize download button
        downloadButton = view.findViewById(R.id.downloadButton);

        closeButton.setOnClickListener(v -> dismiss());

        // Set the title if provided
        if (locationTitle != null) {
            locationNameText.setText(locationTitle);
            getDialog().setTitle(locationTitle);
            fetchLocationId(locationTitle);
        }

        fetchTideLevelsFromFirestore();

        // Change listener from dateTimeText to changeDateTimeIcon
        changeDateTimeIcon = view.findViewById(R.id.changeDateTimeIcon);
        changeDateTimeIcon.setOnClickListener(v -> showDateTimePicker());

        // Set up download button click listener
        downloadButton.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                showDownloadPeriodDialog(); // Show the dialog if the user is logged in
            } else {
                Toast.makeText(getContext(), "Please log in to download.", Toast.LENGTH_SHORT).show();
            }
        });

        setupViewPager(viewPager);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();

        // Initialize new UI elements for adding a place
        locationNameEditText = new EditText(getContext());
        locationNameEditText.setHint("Location Name (Required)");

        addPlaceButton = new Button(getContext());
        addPlaceButton.setText("Add Place");

        cancelButton = new Button(getContext());
        cancelButton.setText("Cancel");

        // Add new views to the layout programmatically
        ((ViewGroup) view).addView(locationNameEditText);
        ((ViewGroup) view).addView(addPlaceButton);
        ((ViewGroup) view).addView(cancelButton);

        if (showLoginMessage) {
            showLoginMessage();
        } else if (isAddingPlace) {
            showAddPlaceUI();
        } else {
            setUpBottomSheet();
        }

        return view;
    }

    private void fetchLocationId(String locationTitle) {
        db.collection("locations").whereEqualTo("name", locationTitle).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        locationId = document.getId(); // Get the location ID
                        if (isAdded()) { // Check if fragment is still attached
                            setupViewPager(viewPager); // Now call setupViewPager here
                        }
                    } else {
                        Log.w("BottomSheetFragment", "No matching location found.");
                    }
                });
    }

    private void showEditLocationDialog() {
        // Create a dialog
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_edit_location); // Create a separate XML layout for this dialog
        dialog.setTitle("Edit Location");

        // Initialize dialog elements
        EditText editLocationEditText = dialog.findViewById(R.id.editLocationEditText);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        ImageButton closeDialogButton = dialog.findViewById(R.id.closeDialogButton);

        // Set existing location name if applicable
        editLocationEditText.setText(locationNameText.getText().toString());

        // Save button action
        saveButton.setOnClickListener(v -> {
            String newLocationName = editLocationEditText.getText().toString();
            if (!newLocationName.isEmpty()) {
                locationNameText.setText(newLocationName); // Update the displayed location name
                dialog.dismiss(); // Close the dialog
            } else {
                Toast.makeText(getContext(), "Please enter a location name.", Toast.LENGTH_SHORT).show();
            }
        });

        // Close dialog button action
        closeDialogButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet));
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Always expanded
            behavior.setPeekHeight(0); // Set peek height to 0
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet));
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Always expanded
            behavior.setPeekHeight(0); // Set peek height to 0
        });
        return dialog;
    }


    private void setupViewPager(ViewPager2 viewPager) {
        if (getActivity() == null) {
            Log.w("BottomSheetFragment", "Activity is null, cannot setup ViewPager.");
            return; // Exit if activity is not available
        }

        adapter = new ViewPagerAdapter(getActivity(), locationId);
        adapter.addFragment(new PhotosFragment(locationId), "Photos");
        adapter.addFragment(new CommentsFragment(locationId), "Comments");
        adapter.addFragment(NewFeedsFragment.newInstance(locationId), "Updates");

        viewPager.setAdapter(adapter);
    }

    private void showLoginMessage() {
        Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
        dismiss(); // Dismiss the bottom sheet
    }

    private void showAddPlaceUI() {
        // Hide tide-related UI elements
        tideLevelText.setVisibility(View.GONE);
        dateTimeText.setVisibility(View.GONE);
        //editLocationIcon.setVisibility(View.GONE);
        tideValueText.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        changeDateTimeIcon.setVisibility(View.GONE);
        downloadButton.setVisibility(View.GONE);
        locationNameText.setVisibility(View.GONE);

        // Show only the EditText and buttons for adding a place
        locationNameEditText.setVisibility(View.VISIBLE);
        addPlaceButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        // Handle Add Place button click
        addPlaceButton.setOnClickListener(v -> {
            String locationName = locationNameEditText.getText().toString();
            if (!locationName.isEmpty() && getActivity() != null) {
                String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Guest";

                // Get the location's latitude and longitude from the current marker
                LatLng currentLocation = HomePage.currentMarker.getPosition();
                double latitude = currentLocation.latitude;
                double longitude = currentLocation.longitude;

                // Define the tide levels
                List<Double> tideLevels = new ArrayList<>();
                tideLevels.add(1.34);
                tideLevels.add(1.21);
                tideLevels.add(1.16);
                tideLevels.add(1.21);
                tideLevels.add(1.43);
                tideLevels.add(1.73);
                tideLevels.add(2.04);
                tideLevels.add(2.25);
                tideLevels.add(2.30);
                tideLevels.add(2.28);
                tideLevels.add(2.16);
                tideLevels.add(1.93);
                tideLevels.add(1.60);
                tideLevels.add(1.19);
                tideLevels.add(0.81);
                tideLevels.add(0.54);
                tideLevels.add(0.45);
                tideLevels.add(0.53);
                tideLevels.add(0.72);
                tideLevels.add(0.93);
                tideLevels.add(1.10);
                tideLevels.add(1.26);
                tideLevels.add(1.40);
                tideLevels.add(1.43);

                // Create a location object with the current date and time
                Location location = new Location(locationName, latitude, longitude, userEmail, getCurrentDateTime(), tideLevels);

                // Save to Firestore
                db.collection("locations").add(location)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getContext(), "Place added: " + locationName, Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onPlaceAdded(); // Notify the HomePage
                            }
                            dismiss(); // Close the bottom sheet
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error adding place: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getContext(), "Please enter a location name.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Cancel button click
        cancelButton.setOnClickListener(v -> {
            dismiss(); // Close the bottom sheet directly
        });
    }

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private void setUpBottomSheet() {
        // Initialize selectedDateTime
        selectedDateTime = LocalDateTime.now(); // Ensure it's initialized here
        updateTideLevel();

        // Hide add place UI
        locationNameEditText.setVisibility(View.GONE);
        addPlaceButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    LocalDateTime newDateTime = selectedDateTime.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth);
                    showTimePicker(newDateTime);
                },
                selectedDateTime.getYear(), selectedDateTime.getMonthValue() - 1, selectedDateTime.getDayOfMonth());

        datePickerDialog.show();
    }

    private void showTimePicker(LocalDateTime newDateTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime = newDateTime.withHour(hourOfDay).withMinute(minute);
                    updateTideLevel();
                },
                selectedDateTime.getHour(), selectedDateTime.getMinute(), true);

        timePickerDialog.show();
    }

    private void updateTideLevel() {
        if (selectedDateTime == null) {
            // Handle the case when selectedDateTime is null
            Log.w("BottomSheetFragment", "selectedDateTime is null, cannot update tide level.");
            return; // Exit the method if null
        }

        if (tideLevels != null && !tideLevels.isEmpty()) {
            int hour = selectedDateTime.getHour(); // Get the hour from the selected date and time
            if (hour >= 0 && hour < tideLevels.size()) {
                double tideLevel = tideLevels.get(hour); // Get tide level for the selected hour
                dateTimeText.setText(selectedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                // Set the tide level text
                tideLevelText.setText("Tide Level:");
                tideValueText.setText(String.format("%.2fm", tideLevel)); // Format to 2 decimal places
            }
        } else {
            tideLevelText.setText("Tide levels not available.");
            tideValueText.setText(""); // Clear the value
        }
    }

    private double getTideLevel(LocalDateTime dateTime) {
        // Placeholder logic for tide level (replace with actual data retrieval)
        return 1.5; // Example: tide level in meters
    }

    private void showDownloadPeriodDialog() {
        DownloadPeriodDialog dialog = new DownloadPeriodDialog(selectedDateTime, date -> downloadTideData(date));
        dialog.show(getChildFragmentManager(), "DownloadPeriodDialog");
    }

    private void downloadTideData(LocalDateTime date) {
        if (tideLevels == null || tideLevels.isEmpty()) {
            Toast.makeText(getContext(), "No tide levels available for download.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Guest";
        //String locationName = locationTitle; // Assuming locationTitle is the name of the location
        LatLng currentLocation = HomePage.currentMarker.getPosition();
        double latitude = currentLocation.latitude;
        double longitude = currentLocation.longitude;

        // Create an object to save
        TideData tideData = new TideData(locationId, latitude, longitude, userEmail, date, tideLevels);

        // Save to Firestore
        db.collection("downloadedTideData").add(tideData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Tide data downloaded successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error downloading tide data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTideLevelsFromFirestore() {
        // Assuming the location title corresponds to the document name in Firestore
        db.collection("locations").whereEqualTo("name", locationTitle).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Get the first document (you might want to handle multiple documents)
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            tideLevels = (List<Double>) document.get("tideLevels"); // Retrieve tide levels

                            // Update the tide level display based on the selected date and time
                            updateTideLevel();
                        } else {
                            Log.w("BottomSheetFragment", "No matching documents found.");
                        }
                    } else {
                        Log.w("BottomSheetFragment", "Error getting tide levels.", task.getException());
                    }
                });
    }
}