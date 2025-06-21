package com.example.tidalapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DownloadPeriodDialog extends DialogFragment {

    private LocalDateTime selectedDate;
    private DownloadCallback downloadCallback;

    public interface DownloadCallback {
        void onDownload(LocalDateTime date);
    }

    public DownloadPeriodDialog(LocalDateTime initialDate, DownloadCallback callback) {
        this.selectedDate = initialDate;
        this.downloadCallback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Save Tide Data");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.download_period_dialog, null);

        DatePicker datePicker = view.findViewById(R.id.datePicker);
        Button downloadButton = view.findViewById(R.id.downloadButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        // Set the current date on the DatePicker
        datePicker.init(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth(), null);

        downloadButton.setOnClickListener(v -> {
            selectedDate = LocalDateTime.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth(), 0, 0);
            if (downloadCallback != null) {
                downloadCallback.onDownload(selectedDate);
            }
            dismiss(); // Close the dialog after downloading
        });

        cancelButton.setOnClickListener(v -> dismiss()); // Close the dialog

        dialog.setContentView(view);
        return dialog;
    }
}