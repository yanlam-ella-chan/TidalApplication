package com.example.tidalapplication;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class Location {
    private String name;
    private double latitude;
    private double longitude;
    private String addedBy; // Changed from updatedBy to addedBy
    private String addedDateTime; // Field for timestamp

    // Required empty constructor for Firestore serialization
    public Location() {
    }

    public Location(String name, double latitude, double longitude, String addedBy, String addedDateTime) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.addedBy = addedBy; // Initialize addedBy
        this.addedDateTime = addedDateTime; // Initialize timestamp
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("latitude")
    public double getLatitude() {
        return latitude;
    }

    @PropertyName("longitude")
    public double getLongitude() {
        return longitude;
    }

    @PropertyName("addedBy")
    public String getAddedBy() {
        return addedBy; // Updated getter
    }

    @PropertyName("addedDateTime")
    public String getAddedDateTime() {
        return addedDateTime;
    }

    // Optionally, you can set the properties if you need to update them later
    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAddedBy(String addedBy) { // Updated setter
        this.addedBy = addedBy;
    }

    public void setAddedDateTime(String addedDateTime) {
        this.addedDateTime = addedDateTime;
    }

}