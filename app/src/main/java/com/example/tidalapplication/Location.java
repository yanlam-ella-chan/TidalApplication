package com.example.tidalapplication;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class Location {
    private String name;
    private double latitude;
    private double longitude;
    private String addedBy; // Changed from updatedBy to addedBy
    private String addedDateTime; // Field for timestamp
    private List<Double> tideLevels; // New field for hourly tide levels

    // Required empty constructor for Firestore serialization
    public Location() {
    }

    public Location(String name, double latitude, double longitude, String addedBy, String addedDateTime, List<Double> tideLevels) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.addedBy = addedBy; // Initialize addedBy
        this.addedDateTime = addedDateTime; // Initialize timestamp
        this.tideLevels = tideLevels; // Initialize tide levels
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

    @PropertyName("tideLevels")
    public List<Double> getTideLevels() {
        return tideLevels; // Getter for tide levels
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

    public void setTideLevels(List<Double> tideLevels) { // Setter for tide levels
        this.tideLevels = tideLevels;
    }
}