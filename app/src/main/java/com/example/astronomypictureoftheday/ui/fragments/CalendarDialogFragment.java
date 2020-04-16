package com.example.astronomypictureoftheday.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class CalendarDialogFragment extends DialogFragment {

    public CalendarDialogFragment() {
    }

    public static CalendarDialogFragment newInstance() {
        CalendarDialogFragment calendarDialogFragment = new CalendarDialogFragment();
        Bundle bundle = new Bundle();
        calendarDialogFragment.setArguments(bundle);
        return calendarDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener listener = (DatePickerDialog.OnDateSetListener) getActivity();
        return new DatePickerDialog(getActivity(), listener, currentYear, currentMonth, currentDay);
    }
}
