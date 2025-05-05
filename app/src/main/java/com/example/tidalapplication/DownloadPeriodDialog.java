package com.example.tidalapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class DownloadPeriodDialog extends DialogFragment {

    private LocalDateTime selectedStartDateTime;
    private LocalDateTime selectedEndDateTime;
    private final DownloadListener listener;
    private TextView dateTimeText;

    public interface DownloadListener {
        void onDownloadSelected(LocalDateTime startDateTime, LocalDateTime endDateTime);
    }

    public DownloadPeriodDialog(LocalDateTime currentDateTime, DownloadListener listener) {
        this.selectedStartDateTime = currentDateTime;
        this.selectedEndDateTime = currentDateTime.plusHours(1); // Default 1 hour period
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.download_period_dialog, container, false);

        dateTimeText = view.findViewById(R.id.dateTimeText);
        Button pickStartButton = view.findViewById(R.id.pickStartButton);
        Button pickEndButton = view.findViewById(R.id.pickEndButton);
        Button downloadButton = view.findViewById(R.id.downloadButton);

        updateDateTimeText();

        pickStartButton.setOnClickListener(v -> showDateTimePicker(true));
        pickEndButton.setOnClickListener(v -> showDateTimePicker(false));
        downloadButton.setOnClickListener(v -> checkSignInAndDownload());

        return view;
    }

    private void checkSignInAndDownload() {
        if (!isUserSignedIn()) {
            showSignInPrompt();
        } else {
            listener.onDownloadSelected(selectedStartDateTime, selectedEndDateTime);
            dismiss();
        }
    }

    private boolean isUserSignedIn() {
        return UserSession.isSignedIn; // Check the sign-in status
    }

    private void showSignInPrompt() {
        new AlertDialog.Builder(getContext())
                .setTitle("Sign In Required")
                .setMessage("Please sign in to download tide information.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate to sign-in activity or fragment
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDateTimePicker(boolean isStart) {
        LocalDateTime currentDateTime = isStart ? selectedStartDateTime : selectedEndDateTime;
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentDateTime.getYear(), currentDateTime.getMonthValue() - 1, currentDateTime.getDayOfMonth(),
                currentDateTime.getHour(), currentDateTime.getMinute());

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                            (timeView, hourOfDay, minute) -> {
                                if (isStart) {
                                    selectedStartDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute);
                                } else {
                                    selectedEndDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute);
                                }
                                updateDateTimeText(); // Update the displayed text
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void updateDateTimeText() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateTimeText.setText("Selected Start: " + selectedStartDateTime.format(formatter) +
                "\nSelected End: " + selectedEndDateTime.format(formatter));
    }
}