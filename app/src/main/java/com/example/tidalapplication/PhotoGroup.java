package com.example.tidalapplication;

import java.util.List;

public class PhotoGroup {
    private float tideLevel;
    private List<Photo> photos;

    public PhotoGroup(float tideLevel, List<Photo> photos) {
        this.tideLevel = tideLevel;
        this.photos = photos;
    }

    public float getTideLevel() {
        return tideLevel;
    }

    public List<Photo> getPhotos() {
        return photos;
    }
}