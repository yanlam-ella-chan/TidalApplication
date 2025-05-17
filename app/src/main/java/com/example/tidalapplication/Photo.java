package com.example.tidalapplication;

public class Photo {
    private String imageUrl;
    private float tideLevel;

    public Photo(String imageUrl, float tideLevel) {
        this.imageUrl = imageUrl;
        this.tideLevel = tideLevel;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public float getTideLevel() {
        return tideLevel;
    }
}


