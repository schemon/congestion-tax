package com.example.simarv.toll.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.simarv.toll.DateUtil;
import com.example.simarv.toll.R;
import com.example.simarv.toll.db.Settings;
import com.example.simarv.toll.db.TollHistory;
import com.example.simarv.toll.fee.TollCalculator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by simarv on 2016-06-08.
 */
public class DetailActivity extends AppCompatActivity {

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
		getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);

		setContentView(R.layout.detail_activity);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		init();
	}

	private void init() {
		Date date = (Date) getIntent().getExtras().get("date");
		final TollHistory tollHistory = new TollHistory(this);

		String dateString = DateUtil.formatDayString(date, this);

		TextView dateView = (TextView) findViewById(R.id.date);
		dateView.setText(dateString);
		dateView.setTransitionName("date" +date.getTime());

		final List<Date> dates = tollHistory.getData().get(date);

		if(dates == null) {
			((TextView) findViewById(R.id.price)).setText(0 +" kr");
			return;
		}

		int price = new TollCalculator().getSumOfTollFeeForOneDay(new Settings(this).getVehicle(), dates);
		((TextView) findViewById(R.id.price)).setText(price +" kr");

		int oldPrice = 0;
		ViewGroup timeHolder = (ViewGroup) findViewById(R.id.time_holder);
		timeHolder.removeAllViews();
		for (int i = 0; i < dates.size(); i++) {
			final View view = getLayoutInflater().inflate(R.layout.list_item_date_detail, timeHolder, false);

			((TextView) view.findViewById(R.id.date)).setText(new SimpleDateFormat("HH:mm").format(dates.get(i)));

			int newPrice = new TollCalculator().getSumOfTollFeeForOneDay(new Settings(this).getVehicle(), dates.subList(0, i+1));
			int diffPrice = newPrice - oldPrice;
			if(diffPrice > 0) {
				((TextView) view.findViewById(R.id.price)).setText(diffPrice +"kr");
			} else {
				((TextView) view.findViewById(R.id.price)).setText("-");
			}
			oldPrice = newPrice;
			timeHolder.addView(view);

			final Date dateToDelete = dates.get(i);
			view.findViewById(R.id.deleteTime).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					tollHistory.delete(dateToDelete);
					view.animate().alpha(0.0f).withEndAction(new Runnable() {
						@Override public void run() {
							init();
						}
					});
				}
			});

		}
	}

	@Override public void onBackPressed() {
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
