package com.example.tidalapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tidalapplication.R;
import com.example.tidalapplication.fragments.UserProfileFragment.TideInfo;

import java.util.List;

public class TideInfoAdapter extends ArrayAdapter<TideInfo> {
    private final Context context;
    private final List<TideInfo> tideInfoList;

    public TideInfoAdapter(Context context, List<TideInfo> tideInfoList) {
        super(context, R.layout.item_tide_info, tideInfoList);
        this.context = context;
        this.tideInfoList = tideInfoList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the layout for each list item
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_tide_info, parent, false);
        }

        // Get the current TideInfo object
        TideInfo currentTideInfo = tideInfoList.get(position);

        // Set up the views
        TextView locationNameTextView = convertView.findViewById(R.id.locationNameTextView);
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);

        // Populate the data into the template view using the TideInfo object
        locationNameTextView.setText(currentTideInfo.locationName);
        dateTextView.setText(currentTideInfo.date);

        return convertView;
    }
}