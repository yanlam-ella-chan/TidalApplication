package com.example.tidalapplication.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tidalapplication.R;
import com.example.tidalapplication.UserSession;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserProfileFragment extends Fragment {

    private TextView userNameText;

    private TextView noActivitiesTextView;
    private Button logoutButton;
    private TabHost tabHost;
    private ListView savedDataListView;
    private ListView activitiesListView; // New ListView for activities
    private FirebaseAuth mAuth;

    private List<TideInfo> savedTideData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        userNameText = view.findViewById(R.id.userNameText);
        logoutButton = view.findViewById(R.id.logoutButton);
        tabHost = view.findViewById(R.id.tabHost);
        savedDataListView = view.findViewById(R.id.savedDataListView);
        activitiesListView = view.findViewById(R.id.activitiesListView);
        noActivitiesTextView = view.findViewById(R.id.noActivitiesTextView);

        mAuth = FirebaseAuth.getInstance();
        String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Guest";
        String username = email.split("@")[0];
        userNameText.setText("Welcome, " + username);

        logoutButton.setOnClickListener(v -> logout());

        setupTabs();
        populateUserActivities(); // Populate activities
        populateSavedTideData(); // Populate tide data

        return view;
    }

    private void showTideDetails(TideInfo tideInfo) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_tide_details);
        dialog.setTitle("Tide Details");

        int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 90% of screen width
        dialog.getWindow().setLayout(dialogWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView locationNameText = dialog.findViewById(R.id.locationNameText);
        TextView dateText = dialog.findViewById(R.id.dateText);
        TableLayout tideLevelsTable = dialog.findViewById(R.id.tideLevelsTable);

        locationNameText.setText(tideInfo.locationName);
        dateText.setText(tideInfo.date);

        // Clear previous rows if any
        tideLevelsTable.removeViews(1, tideLevelsTable.getChildCount() - 1); // Keep the header

        for (int i = 0; i < tideInfo.tideLevels.size(); i++) {
            Double level = tideInfo.tideLevels.get(i);
            String timeRange = String.format("%02d:00 - %02d:59", i, i); // e.g., "00:00 - 00:59"

            TableRow tableRow = new TableRow(getActivity());

            TextView timeText = new TextView(getActivity());
            timeText.setText(timeRange);
            timeText.setPadding(8, 8, 8, 8);
            timeText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            timeText.setGravity(Gravity.CENTER); // Center the text

            TextView levelText = new TextView(getActivity());
            levelText.setText(String.format("%.2f m", level));
            levelText.setPadding(8, 8, 8, 8);
            levelText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            levelText.setGravity(Gravity.CENTER); // Center the text

            tableRow.addView(timeText);
            tableRow.addView(levelText);

            tideLevelsTable.addView(tableRow);
        }

        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupTabs() {
        tabHost.setup();
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Saved Tide Info");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Saved Tide Info");
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("My Activities");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("My Activities");
        tabHost.addTab(spec2);

        tabHost.setCurrentTab(0); // Show default tab
    }

    private void populateUserActivities() {
        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Guest";
        List<String> activities = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations")
                .whereEqualTo("addedBy", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String locationName = document.getString("name");
                            String addedDateTime = document.getString("addedDateTime");
                            activities.add("You added a " + locationName + ". " + addedDateTime);
                        }
                        // Fetch comments and photos after locations
                        fetchUserComments(activities, userEmail);
                        fetchUserPhotos(activities, userEmail); // Call this immediately to fetch photos
                    } else {
                        Log.w("UserProfileFragment", "Error fetching locations.", task.getException());
                    }
                });
    }

    private void fetchUserComments(List<String> activities, String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comments")
                .whereEqualTo("addedBy", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<Void>> tasks = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String locationId = document.getString("locationId");
                            String addedDateTime = document.getString("addedDateTime");
                            // Fetch location name using locationId
                            tasks.add(fetchLocationNameForComment(activities, locationId, addedDateTime));
                        }
                        Tasks.whenAllComplete(tasks).addOnCompleteListener(task1 -> {
                            sortActivities(activities); // Sort after all comments have been processed
                        });
                    } else {
                        Log.w("UserProfileFragment", "Error fetching comments.", task.getException());
                    }
                });
    }
    private Task<Void> fetchLocationNameForComment(List<String> activities, String locationId, String addedDateTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("locations").document(locationId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String locationName = document.getString("name");
                        activities.add("You added a comment to " + locationName + ". " + addedDateTime);
                    }
                    return null; // Return null for continuation
                });
    }

    private void fetchUserPhotos(List<String> activities, String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("photos")
                .whereEqualTo("addedBy", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<Void>> tasks = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String locationId = document.getString("locationId");
                            String addedDateTime = document.getString("addedDateTime");
                            // Fetch location name using locationId
                            tasks.add(fetchLocationNameForPhoto(activities, locationId, addedDateTime));
                        }
                        Tasks.whenAllComplete(tasks).addOnCompleteListener(task1 -> {
                            sortActivities(activities); // Sort after all photos have been processed
                        });
                    } else {
                        Log.w("UserProfileFragment", "Error fetching photos.", task.getException());
                    }
                });
    }
    private Task<Void> fetchLocationNameForPhoto(List<String> activities, String locationId, String addedDateTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("locations").document(locationId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String locationName = document.getString("name");
                        activities.add("You added a photo to " + locationName + ". " + addedDateTime);
                    }
                    return null; // Return null for continuation
                });
    }


    private void sortActivities(List<String> activities) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        activities.sort((a, b) -> {
            String dateA = a.substring(a.lastIndexOf(".") + 1).trim();
            String dateB = b.substring(b.lastIndexOf(".") + 1).trim();

            LocalDateTime dateTimeA = LocalDateTime.parse(dateA, formatter);
            LocalDateTime dateTimeB = LocalDateTime.parse(dateB, formatter);

            return dateTimeB.compareTo(dateTimeA);
        });

        updateActivitiesListView(activities);
    }

    private void updateActivitiesListView(List<String> activities) {
        if (activities.isEmpty()) {
            noActivitiesTextView.setVisibility(View.VISIBLE); // Show "No activities"
            activitiesListView.setVisibility(View.GONE); // Hide the ListView
        } else {
            noActivitiesTextView.setVisibility(View.GONE); // Hide "No activities"
            activitiesListView.setVisibility(View.VISIBLE); // Show the ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, activities);
            activitiesListView.setAdapter(adapter); // Update the activities ListView
        }
    }

    private void populateSavedTideData() {
        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Guest";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("savedTideData")
                .whereEqualTo("savedBy", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String locationId = document.getString("locationId");

                            if (locationId != null) {
                                Map<String, Object> tideDateMap = (Map<String, Object>) document.get("tideDate");
                                if (tideDateMap != null) {
                                    int year = ((Long) tideDateMap.get("year")).intValue();
                                    String monthString = (String) tideDateMap.get("month");
                                    int month = Month.valueOf(monthString.toUpperCase()).getValue();
                                    int day = ((Long) tideDateMap.get("dayOfMonth")).intValue();

                                    // Create a LocalDateTime object
                                    LocalDateTime tideDate = LocalDateTime.of(year, month, day, 0, 0); // Set hour and minute to 0

                                    // Format the date for display
                                    String formattedDate = tideDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

                                    List<Double> tideLevels = (List<Double>) document.get("tideLevels");
                                    fetchLocationName(locationId, formattedDate, tideLevels);
                                }
                            } else {
                                Log.w("UserProfileFragment", "Location ID is null for document: " + document.getId());
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to load tide data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchLocationName(String locationId, String formattedDate, List<Double> tideLevels) {
        if (locationId == null) {
            Log.w("UserProfileFragment", "Cannot fetch location name, locationId is null");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations").document(locationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String locationName = document.getString("name");
                        TideInfo tideInfo = new TideInfo(locationName, formattedDate, tideLevels);
                        savedTideData.add(tideInfo);

                        com.example.tidalapplication.TideInfoAdapter adapter = new com.example.tidalapplication.TideInfoAdapter(getActivity(), savedTideData);
                        savedDataListView.setAdapter(adapter);

                        // Set item click listener
                        savedDataListView.setOnItemClickListener((parent, view, position, id) -> {
                            showTideDetails(savedTideData.get(position));
                        });
                    } else {
                        Log.w("UserProfileFragment", "Error getting location name.", task.getException());
                    }
                });
    }

    private void logout() {
        mAuth.signOut();
        UserSession.isSignedIn = false;
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfilePage())
                .addToBackStack(null)
                .commit();
    }

    public static class TideInfo {
        public String locationName;
        public String date;
        List<Double> tideLevels;

        public TideInfo(String locationName, String date, List<Double> tideLevels) {
            this.locationName = locationName;
            this.date = date;
            this.tideLevels = tideLevels;
        }
    }
}