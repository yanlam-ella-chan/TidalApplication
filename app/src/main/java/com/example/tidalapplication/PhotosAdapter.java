package com.example.tidalapplication;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {
    private List<PhotoGroup> photoGroups = new ArrayList<>();

    public PhotosAdapter() {

    }

    public void setPhotoGroups(List<PhotoGroup> photoGroups) {
        this.photoGroups = photoGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhotoGroup group = photoGroups.get(position);
        holder.tideLevelText.setText(String.format("%.1fm", group.getTideLevel()));

        // Set up a child RecyclerView to display the photos in a grid
        holder.photoRecyclerView.setLayoutManager(new GridLayoutManager(holder.photoRecyclerView.getContext(), 3)); // 3 columns
        holder.photoRecyclerView.setAdapter(new PhotoGridAdapter(group.getPhotos()));
    }

    @Override
    public int getItemCount() {
        return photoGroups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tideLevelText;
        RecyclerView photoRecyclerView;

        ViewHolder(View itemView) {
            super(itemView);
            tideLevelText = itemView.findViewById(R.id.tideLevelText);
            photoRecyclerView = itemView.findViewById(R.id.photoRecyclerView);
        }
    }
}