package com.example.simarv.toll.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.simarv.toll.R;
import com.example.simarv.toll.Settings;
import com.example.simarv.toll.TollHistory;
import com.example.simarv.toll.fee.TollCalculator;
import com.example.simarv.toll.geofence.GeoFenceHandler;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * Created by simarv on 2016-06-08.
 */
public class ListActivity extends AppCompatActivity {

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		if (new GeoFenceHandler(this).hasStarted()) {
			menu.add(R.string.Disable);
		}
		menu.add(R.string.Add_toll_passing);
		menu.add(R.string.Delete_all);
		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals(getString(R.string.Disable))) {
			new GeoFenceHandler(this).stop(new ResultCallback() {
				@Override public void onResult(@NonNull Result result) {
					startActivity(new Intent(ListActivity.this, MainActivity.class));
					finish();
				}
			});
		} else if (item.getTitle().equals(getString(R.string.Add_toll_passing))) {
			new DatePickerFragment().show(getFragmentManager(), "ListActivity");
		} else if (item.getTitle().equals(getString(R.string.Delete_all))) {
			new AlertDialog
					.Builder(this)
					.setMessage(R.string.Delete_all_toll_passings)
					.setNegativeButton(R.string.Cancel, null)
					.setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							new TollHistory(ListActivity.this).deleteAll();
						}
					}).show();
		} else {
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver(tollPassListener, new IntentFilter(TollHistory.TOLL_PASSED));
		refreshTollData();
	}

	@Override protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(tollPassListener);
		super.onStop();
	}

	private BroadcastReceiver tollPassListener = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			refreshTollData();
		}
	};

	private void refreshTollData() {
		final Map<Date, List<Date>> data = TollHistory.groupedByIsoDayString(new TollHistory(this).getData());

		if(data.isEmpty()) {
			findViewById(R.id.card_view).setVisibility(View.VISIBLE);
			findViewById(R.id.buttonAddPassing).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					new DatePickerFragment().show(getFragmentManager(), "ListActivity");
				}
			});
		} else {
			findViewById(R.id.card_view).setVisibility(View.GONE);
		}

		final List<Date> keys = Arrays.asList(data.keySet().toArray(new Date[]{}));
		Collections.sort(keys, new Comparator<Date>() {
			@Override public int compare(Date lhs, Date rhs) {
				return -1*lhs.compareTo(rhs);
			}
		});

		((ListView) findViewById(R.id.listView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				View viewItem = view.findViewById(R.id.date);
				Value item = (Value) parent.getAdapter().getItem(position);
				ActivityOptionsCompat options = ActivityOptionsCompat
						.makeSceneTransitionAnimation(ListActivity.this, viewItem, viewItem.getTransitionName());


				Intent intent = new Intent(ListActivity.this, DetailActivity.class);
				intent.putExtra("date", item.key);
				startActivity(intent, options.toBundle());
			}
		});

		((ListView) findViewById(R.id.listView)).setAdapter(new ArrayAdapter<Value>(this, R.layout.list_item_date) {

			@Override public int getCount() {
				return TollHistory.groupedByIsoDayString(new TollHistory(getBaseContext()).getData()).size();
			}


			@Override public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View view = inflater.inflate(R.layout.list_item_date, parent, false);
				Value data = getItem(position);


				String dateString = new TollHistory(ListActivity.this).formatDayString(data.key);
				//view.setTransitionName(dateString);

				TextView dateView = (TextView) view.findViewById(R.id.date);
				dateView.setTransitionName("date"+data.key.getTime());
				dateView.setText(dateString);

				((TextView) view.findViewById(R.id.price)).setText(data.price +" kr");
				for(int i = 0; i < Math.min(6, data.dates.size()); i ++) {
					View dateItem = inflater.inflate(R.layout.small_time_item, parent, false);
					final String timeString;
					if(i < 5) {
						timeString = new SimpleDateFormat("HH:mm").format(data.dates.get(i));
					} else {
						timeString = "...";
					}
					((TextView) dateItem.findViewById(R.id.text_view)).setText(timeString);
					((ViewGroup) view.findViewById(R.id.time_holder)).addView(dateItem);
				}

				return view;
			}

			@Override public long getItemId(int position) {
				return position;
			}

			@Override public Value getItem(int position) {
				Value result = new Value();
				result.key = keys.get(position);
				result.dates = data.get(result.key);
				result.price = new TollCalculator().getSumOfTollFeeForOneDay(new Settings(getBaseContext()).getVehicle(), result.dates.toArray(new Date[]{}));
				return result;
			}


		});

	}

	class Value {
		Date key;
		List<Date> dates;
		int price;
	}

	public static class TimePickerFragment extends android.app.DialogFragment
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

	public static class DatePickerFragment extends android.app.DialogFragment
			implements DatePickerDialog.OnDateSetListener {

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

}
