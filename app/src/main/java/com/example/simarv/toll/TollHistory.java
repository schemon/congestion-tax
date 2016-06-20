package com.example.simarv.toll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by simarv on 2016-05-28.
 */
public class TollHistory {

	private static final String TAG = TollHistory.class.getSimpleName();
	private final Context context;
	public static final String TOLL_PASSED = "TollPassed";

	public TollHistory(Context context) {
		this.context = context;
	}

	protected SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences("toll_history", Context.MODE_PRIVATE);
	}

	public void notifyTollPassed() {
		long time = System.currentTimeMillis();
		getSharedPreferences().edit().putLong("" +time, time).apply();
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TOLL_PASSED));
	}

	public void notifyTollPassed(long time) {
		getSharedPreferences().edit().putLong("" +time, time).apply();
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TOLL_PASSED));
	}

	public static Map<Date, List<Date>> groupedByIsoDayString(List<Date> data) {
		Map<Date, List<Date>> result = new HashMap<>();
		DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		for(Date date : data) {
			Date dateWithoutTime;
			try {
				dateWithoutTime = dayFormat.parse(dayFormat.format(date));
				if(!result.containsKey(dateWithoutTime)) {
					result.put(dateWithoutTime, new ArrayList<Date>());
				}
				List<Date> dates = result.get(dateWithoutTime);
				dates.add(date);
				result.put(dateWithoutTime, dates);
			} catch (ParseException e) {}
		}
		return result;
	}

	public List<Date> getData() {
		List<Date> result = new ArrayList<>();
		for (long timestamp : (Collection<Long>) getSharedPreferences().getAll().values()) {
			result.add(new Date(timestamp));
		}

		Collections.sort(result);
		return result;
	}

	public void deleteAll() {
		getSharedPreferences().edit().clear().apply();
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TOLL_PASSED));
	}

	public String formatDayString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("d/M\nyyyy");

		final String dateString;
		if(sdf.format(date).equals(sdf.format(new Date()))) {
			dateString = context.getString(R.string.Today);
		} else {
			dateString = sdf.format(date);
		}
		return dateString;
	}


	public void delete(Date dateToDelete) {
		getSharedPreferences().edit().putString("" +dateToDelete.getTime(), null).apply();
	}
}
