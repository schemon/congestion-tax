package com.example.simarv.toll;

import com.example.simarv.toll.fee.TollCalculator;
import com.example.simarv.toll.fee.Vehicle;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TollCalculatorTest {

	
	
	@Test
	public void testMultipleWithinOneHour() {
		TollCalculator tollCalculator = new TollCalculator();
		
		int fee = tollCalculator.getSumOfTollFeeForOneDay(Vehicle.Car,
				getCalendarDate(2014, 3, Calendar.MONDAY, 9, 0), // 9
				getCalendarDate(2014, 3, Calendar.MONDAY, 9, 30), // 0
				getCalendarDate(2014, 3, Calendar.MONDAY, 9, 30), // 0
				getCalendarDate(2014, 3, Calendar.MONDAY, 10, 0)); // 0

		assertEquals(9, fee);
	}

	@Test
	public void testTwoHoursAfterFirst() {
		TollCalculator tollCalculator = new TollCalculator();

		int fee = tollCalculator.getSumOfTollFeeForOneDay(Vehicle.Car,
				getCalendarDate(2014, 3, Calendar.MONDAY, 8, 30), // 9
				getCalendarDate(2014, 3, Calendar.MONDAY, 10, 30), // 9
				getCalendarDate(2014, 3, Calendar.MONDAY, 10, 30), // 0
				getCalendarDate(2014, 3, Calendar.MONDAY, 10, 31)); // 0

		assertEquals(18, fee);
	}


	/*
	06:00–06:29	SEK 9
	06:30–06:59	SEK 16
	07:00–07:59	SEK 22
	08:00–08:29	SEK 16
	08:30–14:59	SEK 9
	15:00–15:29	SEK 16
	15:30–16:59	SEK 22
	17:00–17:59	SEK 16
	18:00–18:29	SEK 9
    */
	@Test
	public void testGetTollFee() {
		assertEquals(0, createTollFeeData(5, 59));

		assertEquals(9, createTollFeeData(6, 0));
		assertEquals(9, createTollFeeData(6, 29));
		assertEquals(16, createTollFeeData(6, 30));
		assertEquals(16, createTollFeeData(6, 59));

		assertEquals(22, createTollFeeData(7, 0));
		assertEquals(22, createTollFeeData(7, 29));
		assertEquals(22, createTollFeeData(7, 30));
		assertEquals(22, createTollFeeData(7, 59));

		assertEquals(16, createTollFeeData(8, 0));
		assertEquals(16, createTollFeeData(8, 29));
		assertEquals(9, createTollFeeData(8, 30));
		assertEquals(9, createTollFeeData(8, 59));

		assertEquals(9, createTollFeeData(9, 0));
		assertEquals(9, createTollFeeData(9, 29));
		assertEquals(9, createTollFeeData(9, 30));
		assertEquals(9, createTollFeeData(9, 59));

		assertEquals(9, createTollFeeData(10, 0));
		assertEquals(9, createTollFeeData(10, 29));
		assertEquals(9, createTollFeeData(10, 30));
		assertEquals(9, createTollFeeData(10, 59));

		assertEquals(9, createTollFeeData(11, 0));
		assertEquals(9, createTollFeeData(11, 29));
		assertEquals(9, createTollFeeData(11, 30));
		assertEquals(9, createTollFeeData(11, 59));

		assertEquals(9, createTollFeeData(12, 0));
		assertEquals(9, createTollFeeData(12, 29));
		assertEquals(9, createTollFeeData(12, 30));
		assertEquals(9, createTollFeeData(12, 59));

		assertEquals(9, createTollFeeData(13, 0));
		assertEquals(9, createTollFeeData(13, 29));
		assertEquals(9, createTollFeeData(13, 30));
		assertEquals(9, createTollFeeData(13, 59));

		assertEquals(9, createTollFeeData(14, 0));
		assertEquals(9, createTollFeeData(14, 29));
		assertEquals(9, createTollFeeData(14, 30));
		assertEquals(9, createTollFeeData(14, 59));

		assertEquals(16, createTollFeeData(15, 0));
		assertEquals(16, createTollFeeData(15, 29));
		assertEquals(22, createTollFeeData(15, 30));
		assertEquals(22, createTollFeeData(15, 59));

		assertEquals(22, createTollFeeData(16, 0));
		assertEquals(22, createTollFeeData(16, 29));
		assertEquals(22, createTollFeeData(16, 30));
		assertEquals(22, createTollFeeData(16, 59));

		assertEquals(16, createTollFeeData(17, 0));
		assertEquals(16, createTollFeeData(17, 29));
		assertEquals(16, createTollFeeData(17, 30));
		assertEquals(16, createTollFeeData(17, 59));

		assertEquals(9, createTollFeeData(18, 0));
		assertEquals(9, createTollFeeData(18, 29));
		assertEquals(0, createTollFeeData(18, 30));
		assertEquals(0, createTollFeeData(18, 59));

		assertEquals(0, createTollFeeData(19, 0));
		assertEquals(0, createTollFeeData(19, 29));
		assertEquals(0, createTollFeeData(19, 30));
		assertEquals(0, createTollFeeData(19, 59));
	}


	private int createTollFeeData(int h, int m) {
		TollCalculator tollCalculator = new TollCalculator();
		return tollCalculator.getTollFee(getCalendarDate(2014, 2, Calendar.MONDAY, h, m));
	}
	
	@Test
	public void testIsTollFreeDate() throws ParseException {
		TollCalculator tollCalculator = new TollCalculator();
		assertFalse(tollCalculator.isTollFreeDate(new Date()));
	    
		assertTrue(tollCalculator.isTollFreeDate(getCalendarDate(2013, 2, Calendar.SUNDAY, 12, 0)));

	}

	@Test
	public void testCreateDate() throws ParseException {
		assertEquals("Mon May 23 23:01:01 CEST 2016", getCalendarDate(2016, 22, Calendar.MONDAY, 23, 1, 1, 1).toString());
	}
	
	private Date getCalendarDate(int year, int week, int weekday, int hour, int minute) {
		return getCalendarDate(year, week, weekday, hour, minute, 0 ,0);
	}

	private Date getCalendarDate(int year, int week, int weekday, int hour, int minute, int second, int millis) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, week);
		calendar.set(Calendar.DAY_OF_WEEK, weekday);

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, millis);
		return calendar.getTime();
	}

	private Date getDate(String string) throws ParseException {
		Calendar cal = GregorianCalendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
		cal.setTime(sdf.parse(string));
		return cal.getTime();
	}
	
}
