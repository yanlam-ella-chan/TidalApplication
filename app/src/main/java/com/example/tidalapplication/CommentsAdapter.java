package com.example.tidalapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private String userRole;

    public CommentsAdapter(List<Comment> comments, String userRole) {
        this.comments = comments;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment, userRole);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameTextView;
        private TextView timeTextView;
        private TextView commentTextView;
        private TextView approvalStatusTextView;
        private LinearLayout buttonLayout;
        private Button approveButton;
        private Button rejectButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            approvalStatusTextView = itemView.findViewById(R.id.approvalStatusTextView);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }

        public void bind(Comment comment, String userRole) {
            String username = comment.getAddedBy().replaceAll("@.*", ""); // Remove email part
            usernameTextView.setText(username);
            timeTextView.setText(comment.getAddedDateTime());
            commentTextView.setText(comment.getCommentText());

            // Show approval status if the user is an admin
            String approvalStatus = comment.getApproval();

            if ("admin".equals(userRole)) {
                if ("pending".equals(approvalStatus)) {
                    buttonLayout.setVisibility(View.VISIBLE);
                    approveButton.setVisibility(View.VISIBLE);
                    rejectButton.setVisibility(View.VISIBLE);
                    approvalStatusTextView.setVisibility(View.GONE);
                    approveButton.setOnClickListener(v -> {
                        updateCommentStatus(comment, "approved");
                    });

                    rejectButton.setOnClickListener(v -> {
                        updateCommentStatus(comment, "rejected");
                    });
                } else if ("rejected".equals(approvalStatus)) {
                    buttonLayout.setVisibility(View.VISIBLE);
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    approvalStatusTextView.setVisibility(View.VISIBLE);
                    approvalStatusTextView.setText(approvalStatus != null ? " (" + approvalStatus + ")" : "");
                } else  {
                    buttonLayout.setVisibility(View.GONE);
                }
            } else if ("member".equals(userRole)) {
                if ("pending".equals(approvalStatus)) {
                    buttonLayout.setVisibility(View.VISIBLE);
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    approvalStatusTextView.setVisibility(View.VISIBLE);
                    approvalStatusTextView.setText(approvalStatus != null ? " (" + approvalStatus + ")" : "");
                    approvalStatusTextView.setTextColor(Color.BLUE);
                } else if ("rejected".equals(approvalStatus)) {
                    buttonLayout.setVisibility(View.VISIBLE);
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    approvalStatusTextView.setVisibility(View.VISIBLE);
                    approvalStatusTextView.setText(approvalStatus != null ? " (" + approvalStatus + ")" : "");
                    approvalStatusTextView.setTextColor(Color.RED);
                } else  {
                    buttonLayout.setVisibility(View.GONE);
                }
            }else {
                buttonLayout.setVisibility(View.GONE);
                approvalStatusTextView.setText(approvalStatus != null ? " (" + approvalStatus + ")" : "");
            }
        }

        private void updateCommentStatus(Comment comment, String newStatus) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("comments").document(comment.getCommentId()) // Assuming you have a method to get the document ID
                    .update("approval", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        if ("approved".equals(newStatus)) {
                            buttonLayout.setVisibility(View.GONE);
                        } else if ("rejected".equals(newStatus)) {
                            buttonLayout.setVisibility(View.VISIBLE);
                            approveButton.setVisibility(View.GONE);
                            rejectButton.setVisibility(View.GONE);
                            approvalStatusTextView.setVisibility(View.VISIBLE);
                            approvalStatusTextView.setText(newStatus != null ? " (" + newStatus + ")" : "");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                    });
        }
    }
}