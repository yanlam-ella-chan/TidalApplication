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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BottomSheetFragment extends BottomSheetDialogFragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;
    private String locationTitle;
    private double selectedLocationLat;
    private double selectedLocationLng;
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

    private TideApiService tideApiService;

    public interface NearestStationCallback {
        void onStationFound(String stationCode);
        void onFailure(String error);
    }

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://data.weather.gov.hk/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        tideApiService = retrofit.create(TideApiService.class);

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
            fetchLocationInfo(locationTitle);
        }

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

    private void fetchLocationInfo(String locationTitle) {
        db.collection("locations").whereEqualTo("name", locationTitle).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        locationId = document.getId(); // Get the location ID
                        selectedLocationLat = document.getDouble("latitude");
                        selectedLocationLng = document.getDouble("longitude");
                        fetchTideLevelsFromApi(selectedLocationLat, selectedLocationLng,null);
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

                // Create a location object with the current date and time
                Location location = new Location(locationName, latitude, longitude, userEmail, getCurrentDateTime());
                location.setApproval("pending");

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
        fetchLocationInfo(locationTitle);

        // Hide add place UI
        locationNameEditText.setVisibility(View.GONE);
        addPlaceButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime = selectedDateTime.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth);
                    showTimePicker(selectedDateTime);
                },
                selectedDateTime.getYear(), selectedDateTime.getMonthValue() - 1, selectedDateTime.getDayOfMonth());

        datePickerDialog.show();
    }

    private void showTimePicker(LocalDateTime newDateTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime = newDateTime.withHour(hourOfDay).withMinute(minute);
                    updateTideLevel(); // Call to update tide level based on the picked date and time
                    fetchTideLevelsFromApi(selectedLocationLat, selectedLocationLng, selectedDateTime);
                },
                selectedDateTime.getHour(), selectedDateTime.getMinute(), true);

        timePickerDialog.show();
    }

    private void updateTideLevel() {
        if (selectedDateTime == null) {
            Log.w("BottomSheetFragment", "selectedDateTime is null, cannot update tide level.");
            return; // Exit if null
        }

        if (tideLevels != null && !tideLevels.isEmpty()) {
                double tideLevel = tideLevels.get(0); // Adjust for zero-based index
                dateTimeText.setText(selectedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                // Set the tide level text
                tideLevelText.setText("Tide Level:");
                tideValueText.setText(String.format("%.2fm", tideLevel)); // Format to 2 decimal places
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
        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Guest";
        LatLng currentLocation = HomePage.currentMarker.getPosition();
        double latitude = currentLocation.latitude;
        double longitude = currentLocation.longitude;

        fetchNearestTideStation(latitude, longitude, new NearestStationCallback() {
            @Override
            public void onStationFound(String stationCode) {
                // Fetch tide levels for the entire day using the nearest station code
                fetchAllTideLevelsForDay(date, userEmail, latitude, longitude, stationCode);
            }

            @Override
            public void onFailure(String error) {
                Log.w("BottomSheetFragment", error);
                // Use default station if necessary
                fetchAllTideLevelsForDay(date, userEmail, latitude, longitude, "CCH");
            }
        });
    }

    private void fetchAllTideLevelsForDay(LocalDateTime date, String userEmail, double latitude, double longitude, String stationCode) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        Call<TideResponse> call = tideApiService.getTideLevels("HHOT", "en", "json", stationCode, year, month, day, 0);
        call.enqueue(new retrofit2.Callback<TideResponse>() {
            @Override
            public void onResponse(Call<TideResponse> call, retrofit2.Response<TideResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TideResponse tideResponse = response.body();
                    List<List<String>> tideData = tideResponse.getData();
                    if (!tideData.isEmpty()) {
                        List<String> levels = tideData.get(0); // The first entry contains the data
                        List<Double> allTideLevels = new ArrayList<>();

                        // Parse tide levels starting from index 2
                        for (int i = 2; i < levels.size(); i++) {
                            allTideLevels.add(Double.parseDouble(levels.get(i)));
                        }

                        // Save tide data to Firestore with correct indices (1-24)
                        saveTideDataToFirestore(allTideLevels, userEmail, latitude, longitude, date);
                    } else {
                        Log.w("BottomSheetFragment", "No tide data available.");
                    }
                } else {
                    Log.w("BottomSheetFragment", "API response unsuccessful.");
                }
            }
            @Override
            public void onFailure(Call<TideResponse> call, Throwable t) {
                Log.e("BottomSheetFragment", "Error fetching tide levels: " + t.getMessage());
            }
        });
    }

    private void saveTideDataToFirestore(List<Double> tideLevels, String userEmail, double latitude, double longitude, LocalDateTime dateTime) {
        TideData tideData = new TideData(locationId, latitude, longitude, userEmail, dateTime, tideLevels);

        db.collection("savedTideData").add(tideData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Tide data saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error downloading tide data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void fetchTideLevelsFromApi(double lat, double lng, LocalDateTime selectedDateTime) {

        LocalDateTime now;

        if (selectedDateTime == null) {
            now = LocalDateTime.now();
        } else {
            now = selectedDateTime;
        }

        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();

        // Handle the case where the hour is 0 (midnight)
        if (now.getMinute() > 0) {
            // If it's past midnight but before 1 AM, use hour 24
            hour = (hour == 0) ? 24 : hour;
        }

        int finalHour = hour;
        fetchNearestTideStation(lat, lng, new NearestStationCallback() {
            @Override
            public void onStationFound(String stationCode) {
                Call<TideResponse> call = tideApiService.getTideLevels("HHOT", "en", "json", stationCode, year, month, day, finalHour);
                call.enqueue(new retrofit2.Callback<TideResponse>() {
                    @Override
                    public void onResponse(Call<TideResponse> call, retrofit2.Response<TideResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TideResponse tideResponse = response.body();
                            List<List<String>> tideData = tideResponse.getData();
                            if (!tideData.isEmpty()) {
                                tideLevels = new ArrayList<>();
                                for (String tideLevel : tideData.get(0).subList(2, tideData.get(0).size())) {
                                    tideLevels.add(Double.parseDouble(tideLevel));
                                }
                                updateTideLevel();
                            } else {
                                Log.w("BottomSheetFragment", "No tide data available.");
                            }
                        } else {
                            Log.w("BottomSheetFragment", "API response unsuccessful.");
                        }
                    }

                    @Override
                    public void onFailure(Call<TideResponse> call, Throwable t) {
                        Log.e("BottomSheetFragment", "Error fetching tide levels: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.w("BottomSheetFragment", error);
                // Optionally handle the case where no tide station was found
            }
        });
    }

    private void fetchNearestTideStation(double latitude, double longitude, NearestStationCallback callback) {
        db.collection("tideStations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double closestDistance = Double.MAX_VALUE;
                        String nearestStationCode = null;

                        for (DocumentSnapshot document : task.getResult()) {
                            double stationLat = document.getDouble("latitude");
                            double stationLon = document.getDouble("longitude");
                            double distance = calculateDistance(latitude, longitude, stationLat, stationLon);

                            if (distance < closestDistance) {
                                closestDistance = distance;
                                nearestStationCode = document.getString("code"); // Store the ID of the nearest station
                            }
                        }

                        if (nearestStationCode != null) {
                            callback.onStationFound(nearestStationCode);
                        } else {
                            callback.onFailure("No tide stations found.");
                        }
                    } else {
                        callback.onFailure("Error fetching tide stations: " + task.getException());
                    }
                });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Convert to kilometers
    }

    /*private void fetchTideLevelsForStation(String stationId, String userEmail) {
    // Assuming that the stationId is used to get tide levels
    Call<TideResponse> call = tideApiService.getTideLevels("HHOT", "en", "json", stationId, year, month, day, 0);
    call.enqueue(new retrofit2.Callback<TideResponse>() {
        @Override
        public void onResponse(Call<TideResponse> call, retrofit2.Response<TideResponse> response) {
            // Handle the response as done before
        }

        @Override
        public void onFailure(Call<TideResponse> call, Throwable t) {
            Log.e("BottomSheetFragment", "Error fetching tide levels: " + t.getMessage());
        }
    });
}*/


}