package com.finke.pgtransit.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.database.Cursor;

import com.finke.pgtransit.database.BusDatabaseHelper;

public class MinorStop {

	private int mId;
	private String mName;
	private double mLatitude;
	private double mLongitude;
	private ArrayList<TimeInterface> mMinorStopTimes;
	
	public final static String[] COLUMNS = {"_id", "name", "latitude", "longitude"};
	
	public MinorStop(int id, String name, double latitude, double longitude) {
		mId = id;
		mName = name;
		mLatitude = latitude;
		mLongitude = longitude;
		mMinorStopTimes = null;
	}
	
	public int getId() { return mId; }
	public String getName() { return mName; }
	public double getLatitude() { return mLatitude; }
	public double getLongitude() { return mLongitude; }

    public MinorStopTime getNextMinorStopTime(String weekday, int busId) {
        Calendar cal = Calendar.getInstance();
        ArrayList<TimeInterface> minorStopTimes = getMinorStopTimes(weekday, busId);
        for(TimeInterface mst : minorStopTimes) {
            if(mst.getCalendarTime().after(cal)) {
                return (MinorStopTime)mst;
            }
        }
        return null;
    }
	
	public ArrayList<TimeInterface> getMinorStopTimes(String weekday, int busId) {
		if(mMinorStopTimes != null) {
			return mMinorStopTimes;
		}
		else {
			try {
				Cursor c = BusDatabaseHelper.getInstance().getMinorStopTimesCursorForMinorStop(mId, weekday, busId);
				c.moveToFirst();
				
				ArrayList<TimeInterface> minorStopTimes = new ArrayList<TimeInterface>();
				while(!c.isAfterLast()) {
					minorStopTimes.add(new MinorStopTime(
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[0])),
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[1])),
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[2])),
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[3])),
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[4]))));
					c.moveToNext();
				}
				c.close();
				return minorStopTimes;
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
}
