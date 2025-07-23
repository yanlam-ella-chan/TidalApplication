package com.example.tidalapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsFragment extends Fragment {

    private String locationId; // Change from locationName to locationId
    private RecyclerView recyclerView;
    private TextView noCommentsText;
    private EditText commentEditText;
    private List<Comment> comments;
    private CommentsAdapter adapter;

    public CommentsFragment(String locationId) { // Update constructor
        this.locationId = locationId; // Set locationId
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);
        recyclerView = view.findViewById(R.id.commentsRecyclerView);
        noCommentsText = view.findViewById(R.id.noCommentsText);
        commentEditText = view.findViewById(R.id.commentEditText); // Initialize EditText
        TextView addCommentTextView = view.findViewById(R.id.addCommentTextView); // Initialize TextView

        comments = new ArrayList<>();
        adapter = new CommentsAdapter(comments, "");
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchCommentsFromFirebase();

        // Handle comment submission
        addCommentTextView.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String comment = commentEditText.getText().toString().trim();
                if (!comment.isEmpty()) {
                    addCommentToFirebase(comment);
                    commentEditText.setText(""); // Clear the input field
                } else {
                    Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please log in to add a comment.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchCommentsFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Get the current user's role
            String userId = currentUser.getUid();
            db.collection("userRoles").document(userId).get().addOnCompleteListener(roleTask -> {
                if (roleTask.isSuccessful() && roleTask.getResult() != null) {
                    String role = roleTask.getResult().getString("role");

                    // Fetch comments based on user role
                    if ("admin".equals(role)) {
                        // Admin can see all comments
                        db.collection("comments")
                                .whereEqualTo("locationId", locationId)
                                .get()
                                .addOnCompleteListener(commentsTask -> {
                                    processComments(commentsTask, true);
                                    adapter = new CommentsAdapter(comments, role);
                                    recyclerView.setAdapter(adapter);
                                });
                    } else {
                        // Regular users only see approved comments
                        db.collection("comments")
                                .whereEqualTo("locationId", locationId)
                                .get()
                                .addOnCompleteListener(commentsTask -> {
                                    processComments(commentsTask, false);
                                    // Initialize adapter with user role
                                    adapter = new CommentsAdapter(comments, role);
                                    recyclerView.setAdapter(adapter);
                                });
                    }
                }
            });
        } else {
            db.collection("comments")
                    .whereEqualTo("locationId", locationId)
                    .get()
                    .addOnCompleteListener(commentsTask -> {
                        processComments(commentsTask, false);
                        // Initialize adapter with user role
                        adapter = new CommentsAdapter(comments, "");
                        recyclerView.setAdapter(adapter);
                    });
        }
    }

    private void processComments(Task<QuerySnapshot> task, boolean isAdmin) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserEmail = currentUser != null ? currentUser.getEmail() : null;
        if (task.isSuccessful()) {
            comments.clear();
            for (DocumentSnapshot document : task.getResult()) {
                String commentId = document.getId();
                String commentText = document.getString("comment");
                String addedBy = document.getString("addedBy");
                String addedDateTime = document.getString("addedDateTime");
                String approval = document.getString("approval");

                // Create a new Comment object
                Comment comment = new Comment(commentId, commentText, addedBy, addedDateTime, approval);
                if (isAdmin || "approved".equals(approval) || (currentUserEmail != null && currentUserEmail.equals(addedBy))) {
                    comments.add(comment);
                }
            }
            updateUI();
        } else {
            Toast.makeText(getContext(), "Error fetching comments.", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateUI() {
        if (comments.isEmpty()) {
            noCommentsText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noCommentsText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void addCommentToFirebase(String comment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get user email from Firebase Auth
        String userEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : "Unknown User";

        // Get current date and time
        String addedDateTime = getCurrentDateTime();

        Map<String, Object> data = new HashMap<>();
        data.put("comment", comment);
        data.put("addedBy", userEmail);
        data.put("addedDateTime", addedDateTime);
        data.put("locationId", locationId);
        data.put("approval", "pending");

        db.collection("comments").add(data)
                .addOnSuccessListener(documentReference -> {
                    // Create a new Comment object and add it to the list
                    comments.add(new Comment(comment, userEmail, addedDateTime, "pending"));
                    adapter.notifyItemInserted(comments.size() - 1); // Notify adapter
                    Toast.makeText(getContext(), "Comment added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error adding comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}