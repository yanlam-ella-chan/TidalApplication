package com.example.tidalapplication.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.tidalapplication.R;
import com.example.tidalapplication.UserSession;

import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {

    private TextView userNameText;
    private Button logoutButton;
    private TabHost tabHost;
    private ListView downloadedDataListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        userNameText = view.findViewById(R.id.userNameText);
        logoutButton = view.findViewById(R.id.logoutButton);
        tabHost = view.findViewById(R.id.tabHost);
        downloadedDataListView = view.findViewById(R.id.downloadedDataListView); // Initialize ListView here

        String email = UserSession.isSignedIn ? "abc@gmail.com" : "Guest"; // Replace with actual email
        String username = email.split("@")[0]; // Extract username from email
        userNameText.setText("Welcome, " + username); // Display username

        logoutButton.setOnClickListener(v -> logout());

        setupTabs();
        populateDownloadedTideData(); // Move this line here

        return view;
    }

    private void setupTabs() {
        tabHost.setup();
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Downloaded Tide Info");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Downloaded Tide Info");
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("My Contribution");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("My Contribution");
        tabHost.addTab(spec2);

        // Show default tab
        tabHost.setCurrentTab(0);
    }

    private void populateDownloadedTideData() {
        if (downloadedDataListView != null) {
            List<String[]> downloadedTideData = new ArrayList<>();
            // Add title and date as an array
            downloadedTideData.add(new String[]{"Siu Kau Yi Chau", "07 May 2025 14:00 to 08 May 2025 13:00"});

            com.example.tidalapplication.adapters.TideInfoAdapter adapter = new com.example.tidalapplication.adapters.TideInfoAdapter(getActivity(), downloadedTideData);
            downloadedDataListView.setAdapter(adapter);

            downloadedDataListView.setOnItemClickListener((parent, view, position, id) -> {
                String[] selectedData = downloadedTideData.get(position);
                showTideDetails(selectedData[0], selectedData[1]);
            });
        }
    }

    private void showTideDetails(String title, String date) {
        Toast.makeText(getActivity(), "Details for: " + title + "\n" + date, Toast.LENGTH_SHORT).show();
        // You can create a dialog or a new fragment to show detailed tide info
    }

    private void logout() {
        UserSession.isSignedIn = false; // Set user as not signed in
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        // Navigate back to the profile page or login screen
        getActivity().onBackPressed(); // Example of navigating back
    }
}