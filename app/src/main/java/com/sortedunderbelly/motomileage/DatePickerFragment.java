package com.sortedunderbelly.motomileage;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;

/**
 * Created by maxr on 5/9/14.
 */
public class DatePickerFragment extends DialogFragment {

    static final String INIT_DATE_AS_LONG = "initDateAsLong";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(
                getArguments().getLong(INIT_DATE_AS_LONG, System.currentTimeMillis()));
        // Use the current date as the default date in the picker
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), (MainActivity) getActivity(), year, month, day);
    }
}