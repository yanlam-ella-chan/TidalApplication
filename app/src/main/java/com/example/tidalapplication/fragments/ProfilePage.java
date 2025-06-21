package com.example.tidalapplication.fragments;

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
import com.example.tidalapplication.UserSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfilePage extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button signInButton, viewDownloadsButton;
    private TextView createAccountText;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_page, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        signInButton = view.findViewById(R.id.signInButton);
        createAccountText = view.findViewById(R.id.createAccountText);
        viewDownloadsButton = view.findViewById(R.id.viewDownloadsButton);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        signInButton.setOnClickListener(v -> signIn());
        createAccountText.setOnClickListener(v -> navigateToCreateAccount());
        viewDownloadsButton.setOnClickListener(v -> navigateToSavedData());

        return view;
    }

    private void signIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserSession.isSignedIn = true; // Set the user as signed in
                        Toast.makeText(getActivity(), "Sign In Successful", Toast.LENGTH_SHORT).show();
                        navigateToUserProfile(); // Navigate to user profile fragment
                    } else {
                        Toast.makeText(getActivity(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToUserProfile() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new UserProfileFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToCreateAccount() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CreateAccountFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToSavedData() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DownloadedDataFragment())
                .addToBackStack(null)
                .commit();
    }
}