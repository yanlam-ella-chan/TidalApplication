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
import com.google.firebase.auth.FirebaseUser;
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

        checkUserRole();

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
    private void checkUserRole() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Get the current user's role
            String userId = currentUser.getUid();
            db.collection("userRoles").document(userId).get().addOnCompleteListener(roleTask -> {
                if (roleTask.isSuccessful() && roleTask.getResult() != null) {
                    String role = roleTask.getResult().getString("role");

                    if ("admin".equals(role)) {
                        //setupAdminTabs(); // Setup tabs for admin
                    } else {
                        setupUserTabs(); // Setup tabs for regular users
                    }
                }
            });
        }
    }
    /*private void setupAdminTabs() {
        tabHost.setup();
        TabHost.TabSpec spec = tabHost.newTabSpec("Add Location Requests");
        spec.setContent(R.id.tab3); // Layout for the admin tab
        spec.setIndicator("Add Location Requests");
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0); // Show default tab


        // Fetch and display all pending locations
        fetchPendingLocations();
    }

    private void fetchPendingLocations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations")
                .whereEqualTo("approval", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> pendingLocations = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String locationName = document.getString("name");
                            String addedBy = document.getString("addedBy");
                            String addedDateTime = document.getString("addedDateTime");
                            String documentId = document.getId(); // Get the document ID

                            // Check for null values before formatting
                            if (addedBy != null && locationName != null) {
                                // Format: "<addedBy> created a location <locationName> | <addedDateTime> | <documentId>"
                                String displayText = String.format("%s created a location %s | %s | %s",
                                        addedBy.split("@")[0], locationName, addedDateTime, documentId);
                                pendingLocations.add(displayText); // Add the formatted string
                            } else {
                                Log.w("UserProfileFragment", "One of the fields is null for document: " + document.getId());
                            }
                        }
                        updatePendingLocationsListView(pendingLocations);
                    } else {
                        Log.w("UserProfileFragment", "Error fetching pending locations.", task.getException());
                    }
                });
    }

    private void updatePendingLocationsListView(List<String> pendingLocations) {
        ListView pendingLocationsListView = getView().findViewById(R.id.pendingLocationsListView);

        // Create an ArrayAdapter with the custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_pending_location, pendingLocations) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                // Inflate the view if it has not been created yet
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_pending_location, parent, false);
                }

                TextView locationDescriptionTextView = convertView.findViewById(R.id.locationDescriptionTextView);
                TextView dateTimeTextView = convertView.findViewById(R.id.dateTimeTextView);
                Button approveButton = convertView.findViewById(R.id.approveButton);
                Button rejectButton = convertView.findViewById(R.id.rejectButton);
                TextView statusTextView = convertView.findViewById(R.id.statusTextView); // New TextView for status

                // Split the string into description and dateTime
                String[] parts = pendingLocations.get(position).split(" \\| ");
                String locationDescription = parts[0];
                String locationId = parts[2];
                locationDescriptionTextView.setText(locationDescription); // Description
                dateTimeTextView.setText(parts.length > 1 ? parts[1] : ""); // DateTime
                statusTextView.setVisibility(View.GONE); // Initially hide the status text

                // Set onClickListener for the approve button
                approveButton.setOnClickListener(v -> {
                    approveLocation(locationId, position, statusTextView, approveButton, rejectButton);
                });

                // Set onClickListener for the reject button
                rejectButton.setOnClickListener(v -> {
                    rejectLocation(locationId, position, statusTextView, approveButton, rejectButton);
                });

                return convertView;
            }
        };


        pendingLocationsListView.setAdapter(adapter);

        // Show or hide the "No Requests" message
        TextView noRequestsTextView = getView().findViewById(R.id.noRequestsTextView);
        if (pendingLocations.isEmpty()) {
            noRequestsTextView.setVisibility(View.VISIBLE); // Show "No Requests"
            pendingLocationsListView.setVisibility(View.GONE); // Hide the ListView
        } else {
            noRequestsTextView.setVisibility(View.GONE); // Hide "No Requests"
            pendingLocationsListView.setVisibility(View.VISIBLE); // Show the ListView
        }
    }

    private void approveLocation(String locationId, int position, TextView statusTextView, Button approveButton, Button rejectButton) {
        Log.d("UserProfileFragment", "Approving location ID: " + locationId); // Log the document ID

        if (locationId.isEmpty()) {
            Log.e("UserProfileFragment", "Invalid location ID for approval.");
            return; // Exit if the ID is invalid
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations").document(locationId)
                .update("approval", "approved")
                .addOnSuccessListener(aVoid -> {
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    statusTextView.setText("Approved");
                    statusTextView.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.w("UserProfileFragment", "Error approving location.", e);
                });
    }

    private void rejectLocation(String locationId, int position, TextView statusTextView, Button approveButton, Button rejectButton) {
        Log.d("UserProfileFragment", "Rejecting location ID: " + locationId); // Log the document ID

        if (locationId.isEmpty()) {
            Log.e("UserProfileFragment", "Invalid location ID for rejection.");
            return; // Exit if the ID is invalid
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations").document(locationId)
                .update("approval", "rejected")
                .addOnSuccessListener(aVoid -> {
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    statusTextView.setText("Rejected");
                    statusTextView.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.w("UserProfileFragment", "Error rejecting location.", e);
                });
    }

    private String extractLocationId(String locationDescription) {
        // The format is "<addedBy> created a location <locationName> | <addedDateTime> | <documentId>"
        String[] parts = locationDescription.split(" \\| ");
        if (parts.length == 4) { // Ensure we have 4 parts
            return parts[3]; // Return the document ID
        }
        return ""; // Return empty if not valid
    }*/

    private void setupUserTabs() {
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
                            activities.add("You added a location -- " + locationName + ". " + addedDateTime);
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
        // Check if the user is an admin
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("userRoles").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String role = task.getResult().getString("role");

                if (!"admin".equals(role)) { // Only update if the user is not an admin
                    if (activities.isEmpty()) {
                        noActivitiesTextView.setVisibility(View.VISIBLE); // Show "No activities"
                        activitiesListView.setVisibility(View.GONE); // Hide the ListView
                    } else {
                        noActivitiesTextView.setVisibility(View.GONE); // Hide "No activities"
                        activitiesListView.setVisibility(View.VISIBLE); // Show the ListView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, activities);
                        activitiesListView.setAdapter(adapter); // Update the activities ListView
                    }
                } else {
                    noActivitiesTextView.setVisibility(View.GONE); // Always hide for admin users
                    activitiesListView.setVisibility(View.GONE); // Optionally hide the activities list for admin
                }
            }
        });
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