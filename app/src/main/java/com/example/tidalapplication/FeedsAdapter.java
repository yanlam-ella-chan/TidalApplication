package com.example.tidalapplication;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.ViewHolder> {
    private List<String> feeds;

    public FeedsAdapter(List<String> feeds) {
        this.feeds = feeds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String feed = feeds.get(position);

        // Ensure feed is not null or empty
        if (feed != null && !feed.isEmpty()) {
            // Extract username and datetime
            int startIndex = feed.indexOf('(');
            int endIndex = feed.indexOf(')') + 1;

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String dateTime = feed.substring(startIndex, endIndex); // Get the date part
                String username = feed.substring(0, startIndex).trim(); // Get the username
                String action = feed.substring(endIndex).trim(); // Get the action part

                holder.usernameTextView.setText(username);
                holder.dateTimeTextView.setText(dateTime);
                holder.locationTextView.setText(action); // This can be adjusted based on your needs
            } else {
                Log.w("FeedsAdapter", "Feed format is unexpected: " + feed);
            }
        } else {
            Log.w("FeedsAdapter", "Feed is null or empty at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView dateTimeTextView;
        TextView locationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
        }
    }
}