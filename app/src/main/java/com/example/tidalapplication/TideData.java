package com.example.tidalapplication;

import java.time.LocalDateTime;
import java.util.List;

public class TideData {
    private String locationId; // Change from locationName to locationId
    private double latitude;
    private double longitude;
    private String savedBy;
    private LocalDateTime tideDate;
    private List<Double> tideLevels;

    public TideData(String locationId, double latitude, double longitude, String savedBy, LocalDateTime tideDate, List<Double> tideLevels) {
        this.locationId = locationId; // Use locationId
        this.latitude = latitude;
        this.longitude = longitude;
        this.savedBy = savedBy;
        this.tideDate = tideDate;
        this.tideLevels = tideLevels;
    }

    // Getters and setters (optional)
    public String getLocationId() { // Update method name
        return locationId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSavedBy() {
        return savedBy;
    }

    public LocalDateTime getTideDate() {
        return tideDate;
    }

    public List<Double> getTideLevels() {
        return tideLevels;
    }
}