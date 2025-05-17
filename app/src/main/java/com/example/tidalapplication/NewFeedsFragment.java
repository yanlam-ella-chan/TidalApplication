package com.example.tidalapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewFeedsFragment extends Fragment {

    private static final String ARG_LOCATION_ID = "locationId";
    private String locationId; // Store the location ID
    private RecyclerView recyclerView;
    private TextView noFeedsText;
    private List<String> feeds;
    private FeedsAdapter adapter;

    // Factory method to create a new instance of the fragment
    public static NewFeedsFragment newInstance(String locationId) {
        NewFeedsFragment fragment = new NewFeedsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION_ID, locationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_feeds, container, false);
        recyclerView = view.findViewById(R.id.newFeedsRecyclerView);
        noFeedsText = view.findViewById(R.id.noFeedsText);

        feeds = new ArrayList<>();
        adapter = new FeedsAdapter(feeds);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Retrieve the location ID from arguments
        if (getArguments() != null) {
            locationId = getArguments().getString(ARG_LOCATION_ID);
            fetchUpdatesFromFirebase(locationId);
        }

        return view;
    }

    private void fetchUpdatesFromFirebase(String locationId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> updates = new ArrayList<>();

        // Fetch location updates
        db.collection("locations").document(locationId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                String locationName = document.getString("name");
                String addedBy = document.getString("addedBy");
                String addedDateTime = document.getString("addedDateTime");

                if (addedDateTime != null) {
                    try {
                        LocalDateTime addedDate = LocalDateTime.parse(addedDateTime,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        String formattedDate = addedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));

                        String locationUpdate = addedBy.split("@")[0] + " (" + formattedDate + "): " + locationName + " is added.";
                        updates.add(locationUpdate);
                    } catch (Exception e) {
                        Log.w("NewFeedsFragment", "Error parsing date for location: " + locationName, e);
                    }
                }
            } else {
                Log.w("NewFeedsFragment", "No location found for ID: " + locationId);
            }

            // Fetch comments
            db.collection("comments").whereEqualTo("locationId", locationId).get().addOnCompleteListener(commentTask -> {
                if (commentTask.isSuccessful()) {
                    for (DocumentSnapshot commentDoc : commentTask.getResult()) {
                        String commentAddedBy = commentDoc.getString("addedBy");
                        String commentDateTime = commentDoc.getString("addedDateTime");

                        if (commentDateTime != null) {
                            try {
                                LocalDateTime commentDate = LocalDateTime.parse(commentDateTime,
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                String formattedDate = commentDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));

                                String commentUpdate = commentAddedBy.split("@")[0] + " (" + formattedDate + ") added a comment.";
                                updates.add(commentUpdate);
                            } catch (Exception e) {
                                Log.w("NewFeedsFragment", "Error parsing date for comment.", e);
                            }
                        }
                    }

                    // Fetch photos
                    db.collection("photos").whereEqualTo("locationId", locationId).get().addOnCompleteListener(photoTask -> {
                        if (photoTask.isSuccessful()) {
                            for (DocumentSnapshot photoDoc : photoTask.getResult()) {
                                String photoAddedBy = photoDoc.getString("addedBy");
                                String photoDateTime = photoDoc.getString("addedDateTime");

                                if (photoDateTime != null) {
                                    try {
                                        LocalDateTime photoDate = LocalDateTime.parse(photoDateTime,
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                        String formattedDate = photoDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));

                                        String photoUpdate = photoAddedBy.split("@")[0] + " (" + formattedDate + ") added a photo.";
                                        updates.add(photoUpdate);
                                    } catch (Exception e) {
                                        Log.w("NewFeedsFragment", "Error parsing date for photo.", e);
                                    }
                                }
                            }

                            // Log updates for debugging
                            Log.d("NewFeedsFragment", "Updates retrieved: " + updates);

                            // Sort updates by date
                            Collections.sort(updates, (a, b) -> {
                                LocalDateTime dateA = extractDateFromUpdate(a);
                                LocalDateTime dateB = extractDateFromUpdate(b);
                                return dateB.compareTo(dateA); // Sort from latest to earliest
                            });

                            // Update UI with sorted updates
                            updateUIWithUpdates(updates);
                        } else {
                            Log.w("NewFeedsFragment", "Error fetching photos: " + photoTask.getException());
                        }
                    });
                } else {
                    Log.w("NewFeedsFragment", "Error fetching comments: " + commentTask.getException());
                }
            });
        });
    }

    private LocalDateTime extractDateFromUpdate(String update) {
        String datePart = update.substring(update.indexOf("(") + 1, update.indexOf(")"));
        return LocalDateTime.parse(datePart, DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
    }

    private void updateUIWithUpdates(List<String> updates) {
        feeds.clear();
        feeds.addAll(updates);
        adapter.notifyDataSetChanged();

        if (feeds.isEmpty()) {
            noFeedsText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noFeedsText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}