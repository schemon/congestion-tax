package toll.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by simarv on 2016-06-20.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Bundle args = new Bundle();
		args.putInt("year", year);
		args.putInt("month", monthOfYear);
		args.putInt("day", dayOfMonth);

		TimePickerFragment dialog = new TimePickerFragment();
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "DatePickerFragment");
	}
}