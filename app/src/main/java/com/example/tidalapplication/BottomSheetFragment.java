package com.example.tidalapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private TextView tideLevelText;
    private TextView dateTimeText;
    private Button pickDateTimeButton;
    private Button downloadButton;
    private LocalDateTime selectedDateTime;

    public BottomSheetFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        tideLevelText = view.findViewById(R.id.tideLevelText);
        dateTimeText = view.findViewById(R.id.dateTimeText);
        pickDateTimeButton = view.findViewById(R.id.pickDateTimeButton);
        downloadButton = view.findViewById(R.id.downloadButton);

        // Set the current date and time
        selectedDateTime = LocalDateTime.now();
        updateTideLevel();

        // Set button click listeners
        pickDateTimeButton.setOnClickListener(v -> showDateTimePicker());
        downloadButton.setOnClickListener(v -> showDownloadPeriodDialog());

        return view;
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    LocalDateTime newDateTime = selectedDateTime.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth);
                    showTimePicker(newDateTime);
                },
                selectedDateTime.getYear(), selectedDateTime.getMonthValue() - 1, selectedDateTime.getDayOfMonth());

        datePickerDialog.show();
    }

    private void showTimePicker(LocalDateTime newDateTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime = newDateTime.withHour(hourOfDay).withMinute(minute);
                    updateTideLevel();
                },
                selectedDateTime.getHour(), selectedDateTime.getMinute(), true);

        timePickerDialog.show();
    }

    private void updateTideLevel() {
        dateTimeText.setText("Selected Date & Time: " + selectedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        double tideLevel = getTideLevel(selectedDateTime); // Get tide level in meters
        tideLevelText.setText(String.format("Tide Level: %.2f meters", tideLevel)); // Format to 2 decimal places
    }

    private double getTideLevel(LocalDateTime dateTime) {
        // Placeholder logic for tide level (replace with actual data retrieval)
        return 1.5; // Example: tide level in meters
    }

    private void showDownloadPeriodDialog() {
        com.example.tidalapplication.DownloadPeriodDialog dialog = new com.example.tidalapplication.DownloadPeriodDialog(selectedDateTime, this::downloadTideData);
        dialog.show(getChildFragmentManager(), "DownloadPeriodDialog");
    }

    private void downloadTideData(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Implement your download logic here
        Toast.makeText(getContext(), "Downloading tide data from: " +
                startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                " to " + endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), Toast.LENGTH_SHORT).show();
        // TODO: Add actual download logic
    }
}