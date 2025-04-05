package com.example.tidalapplication.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.tidalapplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class HomePage extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_home_page, container, false);


        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {

                LatLng hk = new LatLng(22.3193, 114.1694);
                googleMap.addMarker(new MarkerOptions().position(hk).title("Hong Kong"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(hk));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hk, 11.0f));

                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        //Toast.makeText(HomePage.this, latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_SHORT).show();

                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(latLng));
                        //myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                });

            }
        });



        return view;
    }
}