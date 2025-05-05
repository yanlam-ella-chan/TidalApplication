package com.example.tidalapplication.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tidalapplication.R;

public class CreateAccountFragment extends Fragment {

    View view;
    EditText emailEditText, passwordEditText;
    Button createAccountButton, backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_create_account, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        createAccountButton = view.findViewById(R.id.createAccountButton);
        backButton = view.findViewById(R.id.backButton); // Add a back button

        createAccountButton.setOnClickListener(v -> createAccount());
        backButton.setOnClickListener(v -> navigateBackToSignIn());

        return view;
    }

    private void createAccount() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle account creation logic here (e.g., save user to database)
        Toast.makeText(getActivity(), "Account Created", Toast.LENGTH_SHORT).show();

        // Optional: Navigate back to sign in after successful account creation
        navigateBackToSignIn();
    }

    private void navigateBackToSignIn() {
        // Navigate back to the ProfilePage fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfilePage()) // Ensure you specify the correct container
                .commit();
    }
}