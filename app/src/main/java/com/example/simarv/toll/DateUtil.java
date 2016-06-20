package com.example.simarv.toll;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by simarv on 2016-06-20.
 */
public class DateUtil {

	public static String formatDayString(Date date, Context context) {
		SimpleDateFormat sdf = new SimpleDateFormat("d/M\nyyyy");

		final String dateString;
		if(sdf.format(date).equals(sdf.format(new Date()))) {
			dateString = context.getString(R.string.Today);
		} else {
			dateString = sdf.format(date);
		}
		return dateString;
	}
}
