package toll.fee;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TollCalculator {


	private static final int MAX_FEE_PER_DAY = 60;

	public int getSumOfTollFeeForOneDay(Vehicle vehicle, List<Date> dates) {
		return getSumOfTollFeeForOneDay(vehicle, dates.toArray(new Date[]{}));
	}

		/**
		 * Calculate the total toll fee for one day
		 *
		 * @param vehicle - the vehicle
		 * @param dates   - date and time of all passes on one day
		 * @return - the total toll fee for that day
		 */
	public int getSumOfTollFeeForOneDay(Vehicle vehicle, Date... dates) {
		// Time of first pass
		Date intervalStart = dates[0];

		int totalFee = 0;
		for (Date currentDate : dates) {
			int nextFee = getTollFee(vehicle, currentDate);  // Current fee
			int startFee = getTollFee(vehicle, intervalStart);  //

			TimeUnit timeUnit = TimeUnit.MINUTES;
			long diffInMillies = currentDate.getTime() - intervalStart.getTime();
			long diffInMinutes = timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);

			if (diffInMinutes <= 60) {
				if (totalFee > 0) totalFee -= startFee;
				if (nextFee >= startFee) startFee = nextFee;
				totalFee += startFee;
			} else {
				// Change intervalStart
				intervalStart = currentDate;
				totalFee += nextFee;
			}
		}

		if (totalFee > MAX_FEE_PER_DAY) totalFee = MAX_FEE_PER_DAY;
		return totalFee;
	}


	/**
	 * @param vehicle
	 * @param date
	 * @return
	 */
	public int getTollFee(Vehicle vehicle, final Date date) {
		if (isTollFreeDate(date) || isTollFreeVehicle(vehicle)) {
			return 0;
		} else {
			return getTollFee(date);
		}
	}

	private boolean isTollFreeVehicle(Vehicle vehicle) {
		if (vehicle == null) return false;
		String vehicleType = vehicle.toString();
		return vehicleType.equals(TollFreeVehicles.MOTORBIKE.getType()) ||
				vehicleType.equals(TollFreeVehicles.TRACTOR.getType()) ||
				vehicleType.equals(TollFreeVehicles.EMERGENCY.getType()) ||
				vehicleType.equals(TollFreeVehicles.DIPLOMAT.getType()) ||
				vehicleType.equals(TollFreeVehicles.FOREIGN.getType()) ||
				vehicleType.equals(TollFreeVehicles.MILITARY.getType());
	}

	/**
	 *
	 06:00–06:29	9 kr
	 06:30–06:59	16 kr
	 07:00–07:59	22 kr
	 08:00–08:29	16 kr
	 08:30–14:59	9 kr
	 15:00–15:29	16 kr
	 15:30–16:59	22 kr
	 17:00–17:59	16 kr
	 18:00–18:29	9 kr
	 18:30–05:59	0 kr
	 */
	public int getTollFee(final Date date) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int m = calendar.get(Calendar.MINUTE);

		if (hour < 6) return 0;
		else if (hour == 6 && 0 <= m && m <= 29) return 9;
		else if (hour == 6 && 30 <= m && m <= 59) return 16;
		else if (hour == 7 && 0 <= m && m <= 59) return 22;
		else if (hour == 8 && 0 <= m && m <= 29) return 16;
		else if (hour == 8 && 30 <= m && m <= 59) return 9;
		else if (hour == 9 && 0 <= m && m <= 59) return 9;
		else if (hour == 10 && 0 <= m && m <= 59) return 9;
		else if (hour == 11 && 0 <= m && m <= 59) return 9;
		else if (hour == 12 && 0 <= m && m <= 59) return 9;
		else if (hour == 13 && 0 <= m && m <= 59) return 9;
		else if (hour == 14 && 0 <= m && m <= 59) return 9;
		else if (hour == 15 && 0 <= m && m <= 29) return 16;
		else if (hour == 15 && 30 <= m && m <= 59) return 22;
		else if (hour == 16 && 0 <= m && m <= 59) return 22;
		else if (hour == 17 && 0 <= m && m <= 59) return 16;
		else if (hour == 18 && 0 <= m && m <= 29) return 9;
		else if (hour == 18 && 30 <= m && m <= 59) return 0;
		else return 0;
	}


	/**
	 * Dagar då trängselskatt inte tas ut
	 Trängselskatt tas inte ut helgdag, dag före helgdag eller under juli månad. Trängselskatt tas inte heller ut under de dagar som listas nedan:

	 * 2016
	 1 januari
	 5-6 januari
	 24-25 mars
	 28 mars
	 4-5 maj
	 6 juni
	 24 juni
	 4 november
	 26 december

	 * @param date
	 * @return
	 */
	public Boolean isTollFreeDate(Date date) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return true;

		if (year == 2016) {
			if (month == Calendar.JANUARY && (day == 1 || day == 5 || day == 6)
					|| month == Calendar.MARCH && (day == 24 || day == 25 || day == 28)
					|| month == Calendar.MAY && (day == 4 || day == 5)
					|| month == Calendar.JUNE && (day == 6 || day == 24)
					|| month == Calendar.JULY
					|| month == Calendar.NOVEMBER && (day == 4)
					|| month == Calendar.DECEMBER && (day == 26)) {
				return true;
			}
		}
		return false;
	}

	private enum TollFreeVehicles {
		MOTORBIKE("Motorbike"),
		TRACTOR("Tractor"),
		EMERGENCY("Emergency"),
		DIPLOMAT("Diplomat"),
		FOREIGN("Foreign"),
		MILITARY("Military");
		private final String type;

		TollFreeVehicles(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}
}

