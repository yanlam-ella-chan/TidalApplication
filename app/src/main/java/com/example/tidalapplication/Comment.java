package com.example.tidalapplication;

public class Comment {
    private String commentText;
    private String addedBy;
    private String addedDateTime;

    public Comment(String commentText, String addedBy, String addedDateTime) {
        this.commentText = commentText;
        this.addedBy = addedBy;
        this.addedDateTime = addedDateTime;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public String getAddedDateTime() {
        return addedDateTime;
    }
}
