package com.example.tidalapplication.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tidalapplication.BottomSheetFragment;
import com.example.tidalapplication.R;
import com.example.tidalapplication.UserSession;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends Fragment implements OnMapReadyCallback, BottomSheetFragment.OnPlaceAddedListener {

    private GoogleMap googleMap;

    public static Marker currentMarker; // Store the current marker
    private SearchView searchView;
    private Button showBottomSheetButton;
    private FirebaseFirestore db;

    private String locationName;
    private List<LatLng> locations = new ArrayList<>();
    private List<String> locationNames = new ArrayList<>();
    private List<Circle> specialCircles = new ArrayList<>();
    private List<TextView> textViews = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        searchView = view.findViewById(R.id.mapSearch);
        showBottomSheetButton = view.findViewById(R.id.showBottomSheetButton);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }

        setupSearchView();

        showBottomSheetButton.setOnClickListener(v -> {
            boolean showLoginMessage = !UserSession.isSignedIn; // Determine if login message should show
            boolean isAddingPlace = true; // Indicate the intent to add a place
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(showLoginMessage, isAddingPlace, this, locationName);
            bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        fetchLocationsFromFirestore(); // Fetch locations from Firestore
        googleMap = map;
        setupMap();
    }

    private void setupMap() {
        LatLng hk = new LatLng(22.285139175754413, 114.03969053259233);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hk, 12.0f));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Place initial marker at specified location
        currentMarker = googleMap.addMarker(new MarkerOptions().position(hk).title("Initial Position"));

        // Add an OnMapClickListener
        googleMap.setOnMapClickListener(latLng -> {
            // Remove the existing marker
            if (currentMarker != null) {
                currentMarker.remove();
            }
            // Place a new marker at the clicked location
            currentMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("New Position"));
            showBottomSheetButton.setVisibility(View.VISIBLE);
        });

        googleMap.setOnCameraChangeListener(cameraPosition -> {
            for (Circle circle : specialCircles) {
                circle.setRadius(calculateCircleRadius(cameraPosition.zoom));
            }
            positionAllTextViews();
        });
    }

    private void logTextViewPositions() {
        for (int i = 0; i < textViews.size(); i++) {
            TextView textView = textViews.get(i);
            Log.d("TextViewPosition", "TextView " + i + " position: (" + textView.getX() + ", " + textView.getY() + ")");
        }
    }

    private void fetchLocationsFromFirestore() {
        db.collection("locations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            locationName = document.getString("name");
                            double latitude = document.getDouble("latitude");
                            double longitude = document.getDouble("longitude");
                            LatLng position = new LatLng(latitude, longitude);

                            locations.add(position);
                            locationNames.add(locationName);
                            addSpecialSpot(position, locationName); // Pass the name for display
                        }
                    } else {
                        Log.w("HomePage", "Error getting documents.", task.getException());
                    }
                });
    }

    private void addSpecialSpot(LatLng position, String name) {
        float radius = calculateCircleRadius(12);
        Circle specialCircle = googleMap.addCircle(new CircleOptions()
                .center(position)
                .radius(radius)
                .strokeColor(Color.parseColor("#FFD301"))
                .strokeWidth(5)
                .fillColor(Color.parseColor("#FFD301"))
                .clickable(true));

        specialCircles.add(specialCircle);

        // Create and position TextView for the location name
        TextView textView = createTextView(name);
        textViews.add(textView);

        // Set click listener for the TextView
        textView.setOnClickListener(v -> {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(false, false, this, name); // Pass location name as title
            bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
        });

        // Add TextView to the map layout
        ViewGroup mapLayout = (ViewGroup) getView().findViewById(R.id.google_map);
        mapLayout.addView(textView);

        // Position the TextView slightly above the circle
        positionTextView(textView, position);
    }

    private void positionAllTextViews() {
        float currentZoom = googleMap.getCameraPosition().zoom; // Get current zoom level
        for (int i = 0; i < locations.size(); i++) {
            LatLng position = locations.get(i);
            TextView textView = textViews.get(i); // Get the corresponding TextView
            positionTextView(textView, position); // Position the TextView correctly based on LatLng
            if (currentZoom >= 12) {
                textView.setVisibility(View.VISIBLE); // Show TextView when zoom is >= 14
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    private void positionTextView(TextView textView, LatLng position) {
        Projection projection = googleMap.getProjection();
        Point screenPoint = projection.toScreenLocation(position);

        // Get the current circle radius based on zoom level
        float circleRadius = calculateCircleRadius(googleMap.getCameraPosition().zoom);

        // Adjust the Y position to be above the circle
        int offsetY = -((int) circleRadius + 30); // Adjust for space above the circle

        // Center the TextView above the circle
        textView.setX(screenPoint.x - textView.getMeasuredWidth() / 2);
        textView.setY(screenPoint.y + offsetY - textView.getMeasuredHeight() / 2);
        textView.setVisibility(View.VISIBLE);
        textView.bringToFront();
    }

    private TextView createTextView(String name) {
        // Use getContext() safely
        if (getContext() == null) {
            return new TextView(getActivity()); // Return a default TextView or handle appropriately
        }

        TextView textView = new TextView(getContext());
        textView.setText(name);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(12);
        textView.setVisibility(View.VISIBLE);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setPadding(10, 10, 10, 10); // Optional padding for better touch area
        textView.setClickable(true); // Ensure it's clickable

        return textView;
    }

    private float calculateCircleRadius(float zoomLevel) {
        float baseRadius = 100; // Base radius for zoom level 12
        return baseRadius * (float) Math.pow(0.5, (zoomLevel - 12)); // Decrease radius as zoom increases
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error fetching location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            getChildFragmentManager().beginTransaction().remove(mapFragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onPlaceAdded() {
        fetchLocationsFromFirestore(); // Fetch updated locations from Firestore
    }
}