package toll.db;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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


	public void notifyTollPassed() {
		notifyTollPassed(System.currentTimeMillis());
	}

	public void notifyTollPassed(long time) {
		getSharedPreferences().edit().putLong("" +time, time).apply();
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TOLL_PASSED));
	}

	/**
	 * Returns a toll passing events grouped by day sorted in ascending order.
	 * The values can be sent to TollCalculator for determine the tax cost for that day.
	 *
	 * @return
	 */
	public Map<Date, List<Date>> getData() {
		List<Date> allData = new ArrayList<>();
		for (long timestamp : (Collection<Long>) getSharedPreferences().getAll().values()) {
			allData.add(new Date(timestamp));
		}

		Collections.sort(allData);

		return new Util().groupByDay(allData);
	}

	public static class Util {
		public static Map<Date, List<Date>> groupByDay(List<Date> input) {
			// Sorted in ascending time order
			Map<Date, List<Date>> result = new TreeMap<>(new Comparator<Date>() {
				@Override public int compare(Date lhs, Date rhs) {
					return -1*lhs.compareTo(rhs);
				}
			});

			DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

			for(Date date : input) {
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

	}


	public void deleteAll() {
		getSharedPreferences().edit().clear().apply();
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TOLL_PASSED));
	}

	public void delete(Date dateToDelete) {
		getSharedPreferences().edit().putString("" +dateToDelete.getTime(), null).apply();
	}

	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences("toll_history", Context.MODE_PRIVATE);
	}


}
