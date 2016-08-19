package com.finke.pgtransit.model;

import java.util.Calendar;

import com.finke.pgtransit.database.BusDatabaseHelper;

public class MinorStopTime extends Time {

	private int mTripId;
	private int mMinorStopId;
	private int mDepartureTime;
	private MinorStop mMinorStop;
	
	public final static String[] COLUMNS = {"_id", "trip_id", "minor_stop_id", "arrival_time", "departure_time"};
	
	public MinorStopTime(int id, int tripId, int minorStopId, int arrivalTime, int departureTime) {
		mId = id;
		mTripId = tripId;
		mMinorStopId = minorStopId;
		mTime = arrivalTime;
		mDepartureTime = departureTime;
		mMinorStop = null;
	}
	
	public int getTripId() { return mTripId; }
	public int getMinorStopId() { return mMinorStopId; }
	public Calendar getDepartureCalendarTime() {
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.HOUR_OF_DAY, mDepartureTime / 60);
		cal.set(Calendar.MINUTE, mDepartureTime % 60);
		
		return cal;
	}
	
	public MinorStop getMinorStop() {
		if(mMinorStop != null) {
			return mMinorStop;
		}
		else {
			try {
				return BusDatabaseHelper.getInstance().getMinorStop(mMinorStopId);
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
}
