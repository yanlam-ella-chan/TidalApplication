package com.example.tidalapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.example.tidalapplication.fragments.AddPage;
import com.example.tidalapplication.fragments.HomePage;
import com.example.tidalapplication.fragments.ProfilePage;
import com.example.tidalapplication.fragments.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in
        UserSession.checkUserSession(); // Update session based on Firebase auth state

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Load the default fragment
        loadDefaultFragment();
    }

    private void loadDefaultFragment() {
        Fragment selectedFragment = new HomePage();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
    }

    private NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new HomePage();
        } /*else if (item.getItemId() == R.id.nav_contribute) {
            selectedFragment = new AddPage();
        } */else if (item.getItemId() == R.id.nav_profile) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                selectedFragment = new UserProfileFragment();
            } else {
                selectedFragment = new ProfilePage();
            }
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        return true;
    };
}