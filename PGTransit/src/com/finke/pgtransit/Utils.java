package com.finke.pgtransit;

import java.util.Calendar;

import android.util.SparseArray;

public class Utils {

	/* Gets current Calendar week day based on today's weekday
	 */
	public static String getCurrentWeekday() {
		int weekdayNo = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		
		if(weekdayNo == Calendar.SATURDAY) {
			return "Saturday";
		}
		else if(weekdayNo == Calendar.SUNDAY) {
			return "Sunday";
		}
		else {
			return "Weekday";
		}
	}

	/* Gets current weekday option based on mWeekdays
	 * All weekdays are bundled together since schedule is consistent
	 */
	public static String getWeekdayString(String weekday) {
		if(weekday.equals("Saturday") || weekday.equals("Sunday")) {
			return weekday;
		}

		if(!isWeekend()) {
			SparseArray<String> days = new SparseArray<String>();
			days.put(Calendar.SUNDAY, "Sunday");
			days.put(Calendar.MONDAY, "Monday");
			days.put(Calendar.TUESDAY, "Tuesday");
			days.put(Calendar.WEDNESDAY, "Wednesday");
			days.put(Calendar.THURSDAY, "Thursday");
			days.put(Calendar.FRIDAY, "Friday");
			days.put(Calendar.SATURDAY, "Saturday");
			
			return days.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
		}
		else {
			return "Weekdays";
		}
	}
	
	/* Returns true if today is a weekend */
	public static boolean isWeekend() {
		int weekdayNo = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		
		return weekdayNo == Calendar.SATURDAY || weekdayNo == Calendar.SUNDAY;
	}
}
