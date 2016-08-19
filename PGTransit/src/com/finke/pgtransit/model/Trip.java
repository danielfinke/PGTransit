package com.finke.pgtransit.model;

import java.util.ArrayList;

import android.database.Cursor;

import com.finke.pgtransit.database.BusDatabaseHelper;

public class Trip {

	private int mId;
	private int mBusId;
	private String mDay;
	private ArrayList<MapPoint> mMapPoints;
	private ArrayList<MinorStopTime> mMinorStopTimes;
	
	public final static String[] COLUMNS = {"_id", "bus_id", "day"};
	
	public Trip(int id, int busId, String day) {
		mId = id;
		mBusId = busId;
		mDay = day;
		mMapPoints = null;
		mMinorStopTimes = null;
	}
	
	public int getId() { return mId; }
	public int getBusId() { return mBusId; }
	public String getDay() { return mDay; }
	
	public ArrayList<MapPoint> getMapPoints() {
		if(mMapPoints != null) {
			return mMapPoints;
		}
		else {
			try {
				Cursor c = BusDatabaseHelper.getInstance().getMapPointsCursor(mId);
				c.moveToFirst();
				
				mMapPoints = new ArrayList<MapPoint>();
				while(!c.isAfterLast()) {
					mMapPoints.add(new MapPoint(
							c.getInt(c.getColumnIndex(MapPoint.COLUMNS[0])),
							c.getInt(c.getColumnIndex(MapPoint.COLUMNS[1])),
							c.getDouble(c.getColumnIndex(MapPoint.COLUMNS[2])),
							c.getDouble(c.getColumnIndex(MapPoint.COLUMNS[3]))));
					c.moveToNext();
				}
				c.close();
				return mMapPoints;
			}
			catch(Exception e) {
				e.printStackTrace();
				return new ArrayList<MapPoint>();
			}
		}
	}
	
	public ArrayList<MinorStopTime> getMinorStopTimes() {
		if(mMinorStopTimes != null) {
			return mMinorStopTimes;
		}
		else {
			try {
				Cursor c = BusDatabaseHelper.getInstance().getMinorStopTimesCursor(mId);
				c.moveToFirst();
				
				mMinorStopTimes = new ArrayList<MinorStopTime>();
				while(!c.isAfterLast()) {
					mMinorStopTimes.add(new MinorStopTime(
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[0])),
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[1])),
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[2])),
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[3])),
							c.getInt(c.getColumnIndex(MinorStopTime.COLUMNS[4]))));
					c.moveToNext();
				}
				c.close();
				return mMinorStopTimes;
			}
			catch(Exception e) {
				e.printStackTrace();
				return new ArrayList<MinorStopTime>();
			}
		}
	}
	
}
