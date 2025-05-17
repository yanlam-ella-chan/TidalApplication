package com.example.tidalapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserSession {
    public static boolean isSignedIn = false; // User is not signed in by default

    public static void checkUserSession() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        isSignedIn = currentUser != null; // Update the session state
    }
}