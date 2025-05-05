package com.example.tidalapplication.fragments;

import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tidalapplication.R;
import com.example.tidalapplication.UserSession; // Import the UserSession class

public class ProfilePage extends Fragment {

    View view;
    EditText emailEditText, passwordEditText;
    Button signInButton, viewDownloadsButton;
    TextView createAccountText, userProfileText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_profile_page, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        signInButton = view.findViewById(R.id.signInButton);
        createAccountText = view.findViewById(R.id.createAccountText);
        userProfileText = view.findViewById(R.id.userProfileText);
        viewDownloadsButton = view.findViewById(R.id.viewDownloadsButton);

        signInButton.setOnClickListener(v -> signIn());
        createAccountText.setOnClickListener(v -> navigateToCreateAccount());
        viewDownloadsButton.setOnClickListener(v -> navigateToDownloadedData());

        return view;
    }

    private void signIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authenticateUser(email, password)) {
            UserSession.isSignedIn = true; // Set the user as signed in
            saveUserSession(true);
            Toast.makeText(getActivity(), "Sign In Successful", Toast.LENGTH_SHORT).show();
            navigateToUserProfile(); // Navigate to user profile fragment
        } else {
            Toast.makeText(getActivity(), "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserSession(boolean isSignedIn) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isSignedIn", isSignedIn);
        editor.apply();
    }

    private void navigateToUserProfile() {
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, userProfileFragment)
                .addToBackStack(null)
                .commit();
    }

    private boolean authenticateUser(String email, String password) {
        // Placeholder for actual authentication logic
        return true; // Replace with actual authentication check
    }

    private void navigateToCreateAccount() {
        CreateAccountFragment createAccountFragment = new CreateAccountFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, createAccountFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToDownloadedData() {
        DownloadedDataFragment downloadedDataFragment = new DownloadedDataFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, downloadedDataFragment)
                .addToBackStack(null)
                .commit();
    }
}