package com.example.simarv.toll.activity;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.example.simarv.toll.db.TollHistory;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by simarv on 2016-06-20.
 */
public class TimePickerFragment extends android.app.DialogFragment
		implements TimePickerDialog.OnTimeSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		final Calendar c = GregorianCalendar.getInstance();

		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);

		int year = getArguments().getInt("year", -1);
		int month = getArguments().getInt("month", -1);
		int day = getArguments().getInt("day", -1);

		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);

		new TollHistory(getActivity()).notifyTollPassed(c.getTime().getTime());
	}
}