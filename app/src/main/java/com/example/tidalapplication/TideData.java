package com.example.tidalapplication;

import java.time.LocalDateTime;
import java.util.List;

public class TideData {
    private String locationId; // Change from locationName to locationId
    private double latitude;
    private double longitude;
    private String downloadedBy;
    private LocalDateTime tideDate;
    private List<Double> tideLevels;

    public TideData(String locationId, double latitude, double longitude, String downloadedBy, LocalDateTime tideDate, List<Double> tideLevels) {
        this.locationId = locationId; // Use locationId
        this.latitude = latitude;
        this.longitude = longitude;
        this.downloadedBy = downloadedBy;
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

    public String getDownloadedBy() {
        return downloadedBy;
    }

    public LocalDateTime getTideDate() {
        return tideDate;
    }

    public List<Double> getTideLevels() {
        return tideLevels;
    }
}