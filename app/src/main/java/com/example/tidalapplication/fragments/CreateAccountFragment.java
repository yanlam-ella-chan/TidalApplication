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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountFragment extends Fragment {

    private View view;
    private EditText emailEditText, passwordEditText;
    private Button createAccountButton, backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_create_account, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        createAccountButton = view.findViewById(R.id.createAccountButton);
        backButton = view.findViewById(R.id.backButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        // Create a new user account with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        Map<String, String> userRole = new HashMap<>();
                        userRole.put("role", "member");
                        db.collection("userRoles").document(userId).set(userRole)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getActivity(), "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                    navigateBackToSignIn(); // Navigate back to sign in after successful account creation
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Failed to save user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(getActivity(), "Account Creation Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateBackToSignIn() {
        // Navigate back to the ProfilePage fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfilePage())
                .addToBackStack(null) // Optional: add to back stack
                .commit();
    }
}