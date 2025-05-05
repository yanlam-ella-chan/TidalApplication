package com.example.tidalapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tidalapplication.R;

import java.util.List;

public class TideInfoAdapter extends ArrayAdapter<String[]> {
    public TideInfoAdapter(Context context, List<String[]> tideInfos) {
        super(context, 0, tideInfos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        String[] tideInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_tide_info, parent, false);
        }

        // Lookup view for data population
        TextView tideTitle = convertView.findViewById(R.id.tideTitle);
        TextView tideDate = convertView.findViewById(R.id.tideDate);

        // Populate the data into the template view using the data object
        tideTitle.setText(tideInfo[0]); // Title
        tideDate.setText(tideInfo[1]); // Date

        // Return the completed view to render on screen
        return convertView;
    }
}