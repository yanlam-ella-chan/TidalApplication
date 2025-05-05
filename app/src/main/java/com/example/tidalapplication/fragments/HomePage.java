package com.example.tidalapplication.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class HomePage extends Fragment {

    private GoogleMap googleMap;
    private SearchView searchView;

    // Add a new LatLng for your special spot
    private final LatLng siuKauYiChau = new LatLng(22.288458, 114.058123);

    private Circle specialCircle;

    private TextView textPengChau;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        textPengChau = view.findViewById(R.id.textPengChau);

        searchView = view.findViewById(R.id.mapSearch);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    googleMap = map;
                    setupMap();
                }
            });
        }

        setupSearchView();

        // Set click listener for the text view
        textPengChau.setOnClickListener(v -> {
            // Toggle underline
            if ((textPengChau.getPaintFlags() & Paint.UNDERLINE_TEXT_FLAG) > 0) {
                textPengChau.setPaintFlags(textPengChau.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            } else {
                textPengChau.setPaintFlags(textPengChau.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
        });

        return view;
    }

    private void setupMap() {
        LatLng hk = new LatLng(22.285139175754413, 114.03969053259233);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // Set to satellite
        googleMap.addMarker(new MarkerOptions().position(hk).title("Hong Kong"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hk, 12.0f)); // Set initial zoom

        // Add the special spot
        addSpecialSpot();

        // Position the text view for "siuKauYiChau"
        positionTextView();

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Update circle radius on camera change
        googleMap.setOnCameraChangeListener(cameraPosition -> {
            float zoomLevel = cameraPosition.zoom;
            float newRadius = calculateCircleRadius(zoomLevel); // Calculate new radius based on zoom level
            specialCircle.setRadius(newRadius);
            positionTextView();
        });
    }

    private void positionTextView() {
        LatLng labelPosition = siuKauYiChau;

        googleMap.setOnMapLoadedCallback(() -> {
            Projection projection = googleMap.getProjection();
            Point screenPoint = projection.toScreenLocation(labelPosition);

            // Log the calculated screen coordinates
            Log.d("Map", "Text position: " + screenPoint.x + ", " + screenPoint.y);

            // Set the TextView position based on the screen coordinates
            textPengChau.setX(screenPoint.x - 110);
            textPengChau.setY(screenPoint.y); // Adjust for visibility

            // Make the TextView visible
            textPengChau.setVisibility(View.VISIBLE);
            textPengChau.bringToFront(); // Ensure it's on top
        });
    }

    private float calculateCircleRadius(float zoomLevel) {
        // Base radius (adjust as necessary)
        float baseRadius = 100; // Base radius at zoom level 12
        return baseRadius * (float) Math.pow(0.5, (zoomLevel - 12)); // Decrease radius as zoom increases
    }

    private void addSpecialSpot() {
        float radius = calculateCircleRadius(); // Calculate the radius based on screen size

        // Create a circle for the special spot
        specialCircle = googleMap.addCircle(new CircleOptions()
                .center(siuKauYiChau)
                .radius(radius) // Use the calculated radius
                .strokeColor(Color.parseColor("#FFD301"))
                .strokeWidth(5)
                .fillColor(Color.parseColor("#FFD301"))
                .clickable(true)); // Make the circle clickable

        // Optionally add a transparent marker for interaction
        googleMap.addMarker(new MarkerOptions()
                .position(siuKauYiChau)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .anchor(0.5f, 0.5f) // Center the marker
                .alpha(0)); // Make it transparent

        // Set click listener for the circle
        googleMap.setOnCircleClickListener(circle -> {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
        });
    }

    private float calculateCircleRadius() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // Set circle radius as a fixed size (e.g., 1/10th of the smallest dimension)
        return Math.min(metrics.widthPixels, metrics.heightPixels) / 10f; // Adjust the factor as needed
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

                //googleMap.clear();
                //googleMap.addMarker(new MarkerOptions().position(latLng).title(location));
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

    private void addSpot(LatLng position, String title, boolean withButton) {
        View markerView = LayoutInflater.from(getContext()).inflate(R.layout.marker_layout, null);

        TextView markerTitle = markerView.findViewById(R.id.marker_title);
        markerTitle.setText(title);

        Button moreInfoButton = markerView.findViewById(R.id.more_info_button);
        if (withButton) {
            moreInfoButton.setVisibility(View.VISIBLE);
            moreInfoButton.setOnClickListener(v -> {
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
            });
        } else {
            moreInfoButton.setVisibility(View.GONE);
        }

        // Create a larger Bitmap
        int width = 60; // Adjust width
        int height = 80; // Adjust height
        markerView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .anchor(0.5f, 1));
    }
}