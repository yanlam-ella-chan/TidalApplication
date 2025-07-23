package com.example.tidalapplication;

public class Comment {
    private String commentText;
    private String addedBy;
    private String addedDateTime;

    private String approval;
    private String commentId;

    public Comment(String commentText, String addedBy, String addedDateTime, String approval) {
        this.commentText = commentText;
        this.addedBy = addedBy;
        this.addedDateTime = addedDateTime;
        this.approval = approval;
    }
    public Comment(String commentId, String commentText, String addedBy, String addedDateTime, String approval) {
        this.commentId = commentId;
        this.commentText = commentText;
        this.addedBy = addedBy;
        this.addedDateTime = addedDateTime;
        this.approval = approval;
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

    public String getApproval() {
        return approval;
    }
    public String getCommentId() {
        return commentId;
    }
}
