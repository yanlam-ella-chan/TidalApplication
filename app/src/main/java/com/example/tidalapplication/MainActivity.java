package com.example.tidalapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.tidalapplication.databinding.ActivityMainBinding;
import com.example.tidalapplication.fragments.AddPage;
import com.example.tidalapplication.fragments.HomePage;
import com.example.tidalapplication.fragments.ProfilePage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(navListener);

        Fragment selectedFragment = new HomePage();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

    }

    private NavigationBarView.OnItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();

        Fragment selectedFragment = null;

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomePage();
        } else if (itemId == R.id.nav_add) {
            selectedFragment = new AddPage();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfilePage();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

        return  true;
    };
}