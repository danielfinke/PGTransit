package com.finke.pgtransit.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.finke.pgtransit.database.BusDatabaseHelper;

public class Trip {

	private int mId;
	private int mBusId;
	private String mDay;
	private ArrayList<MinorStopTime> mMinorStopTimes;
	
	public final static String[] COLUMNS = {"_id", "bus_id", "day"};
	
	public Trip(int id, int busId, String day) {
		mId = id;
		mBusId = busId;
		mDay = day;
		mMinorStopTimes = null;
	}
	
	public int getId() { return mId; }
	public int getBusId() { return mBusId; }
	public String getDay() { return mDay; }

    /**
     * Get all unique map points within a list of trips
     * @param trips Filter the results by this list of trips
     * @return All unique map points with the list of trips
     */
    public static List<MapPoint> getDistinctMapPoints(List<Trip> trips) {
        ArrayList<MapPoint> items = new ArrayList<>();
        try {
            Cursor c = BusDatabaseHelper.getInstance().getDistinctMapPointsCursor(idsFromList(trips));

            if (c.getCount() == 0) {
                return items;
            }

            c.moveToFirst();

            while (!c.isAfterLast()) {
                items.add(new MapPoint(
                        c.getInt(c.getColumnIndex(MapPoint.COLUMNS[0])),
                        c.getInt(c.getColumnIndex(MapPoint.COLUMNS[1])),
                        c.getDouble(c.getColumnIndex(MapPoint.COLUMNS[2])),
                        c.getDouble(c.getColumnIndex(MapPoint.COLUMNS[3]))));
                c.moveToNext();
            }
            c.close();

            return items;
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return items;
        }
    }

    /**
     * Get all unique minor stops within a list of trips
     * @param trips Filter the results by this list of trips
     * @return All unique minor stops within the list of trips
     */
    public static List<MinorStop> getDistinctMinorStops(List<Trip> trips) {
        ArrayList<MinorStop> items = new ArrayList<>();
        try {
            Cursor c = BusDatabaseHelper.getInstance().getDistinctMinorStopsCursor(idsFromList(trips));

            if(c.getCount() == 0) {
                return items;
            }

            c.moveToFirst();

            while(!c.isAfterLast()) {
                items.add(new MinorStop(
                        c.getInt(c.getColumnIndex(MinorStop.COLUMNS[0])),
                        c.getString(c.getColumnIndex(MinorStop.COLUMNS[1])),
                        c.getDouble(c.getColumnIndex(MinorStop.COLUMNS[2])),
                        c.getDouble(c.getColumnIndex(MinorStop.COLUMNS[3]))
                ));
                c.moveToNext();
            }
            c.close();

            return items;
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return items;
        }
    }

    /**
     * Extract a list of trip IDs from a list of trips
     * @param trips The list from which to extract IDs
     * @return The list of IDs from the list of trips
     */
    private static List<Integer> idsFromList(List<Trip> trips) {
        List<Integer> tripIds = new ArrayList<>();
        for(Trip trip : trips) {
            tripIds.add(trip.getId());
        }
        return tripIds;
    }
}
