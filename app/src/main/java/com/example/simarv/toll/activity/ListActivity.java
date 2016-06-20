package com.example.simarv.toll.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.simarv.toll.DateUtil;
import com.example.simarv.toll.R;
import com.example.simarv.toll.db.Settings;
import com.example.simarv.toll.db.TollHistory;
import com.example.simarv.toll.fee.TollCalculator;
import com.example.simarv.toll.geofence.GeoFenceHandler;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
	}

	@Override protected void onResume() {
		super.onResume();
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
		final Map<Date, List<Date>> data = new TollHistory(this).getData();

		if (data.isEmpty()) {
			findViewById(R.id.card_view).setVisibility(View.VISIBLE);
			findViewById(R.id.buttonAddPassing).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					new DatePickerFragment().show(getFragmentManager(), "ListActivity");
				}
			});
		} else {
			findViewById(R.id.card_view).setVisibility(View.GONE);
		}

		ListView listView = (ListView) findViewById(R.id.listView);

		if (listView != null) {
			init(listView, data);
		}

		((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
	}

	private void init(ListView listView, final Map<Date, List<Date>> data) {

		List<Pair<Date, List<Date>>> blurg = new ArrayList<>();
		for(Date date : data.keySet()) {
			blurg.add(new Pair<>(date, data.get(date)));
		}

		listView.setAdapter(createAdapter(blurg));

		((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				View viewItem = view.findViewById(R.id.date);
				Pair<Date, Object> item = (Pair<Date, Object>) parent.getAdapter().getItem(position);
				ActivityOptionsCompat options = ActivityOptionsCompat
						.makeSceneTransitionAnimation(ListActivity.this, viewItem, viewItem.getTransitionName());


				Intent intent = new Intent(ListActivity.this, DetailActivity.class);
				intent.putExtra("date", item.first);
				startActivity(intent, options.toBundle());
			}
		});
	}


	private ArrayAdapter<Pair<Date, List<Date>>> createAdapter(List<Pair<Date, List<Date>>> data) {

		return new ArrayAdapter<Pair<Date, List<Date>>>(this, R.layout.list_item_date, data) {

			@Override public int getCount() {
				return new TollHistory(getBaseContext()).getData().size();
			}


			@Override public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				final View view;
				if(convertView == null) {
					view = inflater.inflate(R.layout.list_item_date, parent, false);
				} else {
					view = convertView;
				}

				Pair<Date, List<Date>> value = getItem(position);
				int price = new TollCalculator().getSumOfTollFeeForOneDay(new Settings(getBaseContext()).getVehicle(), value.second);

				String dateString = DateUtil.formatDayString(value.first, getBaseContext());

				TextView dateView = (TextView) view.findViewById(R.id.date);
				dateView.setTransitionName("date" + value.first.getTime());
				dateView.setText(dateString);

				((TextView) view.findViewById(R.id.price)).setText(price + " kr");
				for (int i = 0; i < Math.min(6, value.second.size()); i++) {
					View dateItem = inflater.inflate(R.layout.small_time_item, parent, false);
					final String timeString;
					if (i < 5) {
						timeString = new SimpleDateFormat("HH:mm").format(value.second.get(i));
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

		};
	}

	class Value {
		Date day;
		List<Date> timeOfTollPassing;
		int price;
	}




}
