package com.example.tidalapplication.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tidalapplication.R;

import java.util.ArrayList;
import java.util.List;

public class DownloadedDataFragment extends Fragment {

    private List<String> savedTideData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloaded_data, container, false);

        ListView listView = view.findViewById(R.id.savedDataListView);
        savedTideData = new ArrayList<>();
        // Sample simplified data
        savedTideData.add("Tide Data on 07 May 2025");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, savedTideData);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((AdapterView<?> parent, View view1, int position, long id) -> {
            String selectedData = savedTideData.get(position);
            showTideDetails(selectedData);
        });

        return view;
    }

    private void showTideDetails(String tideData) {
        // Replace with actual tide details
        Toast.makeText(getActivity(), "Details for: " + tideData, Toast.LENGTH_SHORT).show();
        // You can create a dialog or a new fragment to show detailed tide info
    }
}