package com.example.tidalapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.tidalapplication.fragments.AddPage;
import com.example.tidalapplication.fragments.HomePage;
import com.example.tidalapplication.fragments.ProfilePage;
import com.example.tidalapplication.fragments.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        UserSession.isSignedIn = sharedPreferences.getBoolean("isSignedIn", false);


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
            if (UserSession.isSignedIn) {
                selectedFragment = new UserProfileFragment(); // Navigate to UserProfileFragment if signed in
            } else {
                selectedFragment = new ProfilePage(); // Navigate to ProfilePage if not signed in
            }
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        return true;
    };
}