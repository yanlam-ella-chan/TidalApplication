package com.example.tidalapplication;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.tidalapplication.fragments.UserProfileFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TideDetailsDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TideDetailsDialogFragment extends DialogFragment {
    private static final String ARG_TIDE_INFO = "tide_info";

    public static TideDetailsDialogFragment newInstance(UserProfileFragment.TideInfo tideInfo) {
        TideDetailsDialogFragment fragment = new TideDetailsDialogFragment();
        Bundle args = new Bundle();
        //args.putSerializable(ARG_TIDE_INFO, tideInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_tide_details);
        dialog.setTitle("Tide Details");

        UserProfileFragment.TideInfo tideInfo = (UserProfileFragment.TideInfo) getArguments().getSerializable(ARG_TIDE_INFO);

        // Set up UI elements like TextViews here using tideInfo

        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }
}