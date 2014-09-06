package com.finke.pgtransit.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.finke.pgtransit.database.BusDatabaseHelper;

/* Represents a stop that a Bus will take during its tour
 */
public class Stop {
	
	private String mId;
	private int mBusId;
	// Stop location name
	private String mName;
	// Location of stop
	private double mLat;
	private double mLon;

	private Bus mBus;
	
	public final static String[] COLUMNS = {"_id", "bus_id", "name", "latitude", "longitude"};
	
	public Stop(String id, int busId, String name, double lat, double lon) {
		mId = id;
		mBusId = busId;
		mName = name;
		mLat = lat;
		mLon = lon;
	}
	
	public String getId() { return mId; }
	public int getBusId() { return mBusId; }
	// Lazy loaded Bus that this stop belongs to
	public Bus getBus() {
		if(mBus != null) {
			return mBus;
		}
		try {
			return Bus.fetchFromDatabase(mBusId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public String getName() { return mName; }
	public double getLat() { return mLat; }
	public double getLon() { return mLon; }
	
	/* Fetches all stops for a given bus (specified by PK) */
	public static List<Stop> fetchFromDatabase(int routeId) throws Exception {
		Cursor c = BusDatabaseHelper.getInstance().getStopsCursor(routeId);
		c.moveToFirst();
		
		List<Stop> items = new ArrayList<Stop>();
		while(!c.isAfterLast()) {
			items.add(new Stop(
					c.getString(c.getColumnIndex(COLUMNS[0])),
					c.getInt(c.getColumnIndex(COLUMNS[1])),
					c.getString(c.getColumnIndex(COLUMNS[2])),
					c.getDouble(c.getColumnIndex(COLUMNS[3])),
					c.getDouble(c.getColumnIndex(COLUMNS[4]))));
			c.moveToNext();
		}
		c.close();
		
		return items;
	}
	
	/* Fetches a specific stop from the database, using its StopId PK */
	public static Stop fetchFromDatabase(String id) throws Exception {
		Cursor c = BusDatabaseHelper.getInstance().getStopCursor(id);
		c.moveToFirst();
		
		Stop s = new Stop(
				c.getString(c.getColumnIndex(COLUMNS[0])),
				c.getInt(c.getColumnIndex(COLUMNS[1])),
				c.getString(c.getColumnIndex(COLUMNS[2])),
				c.getDouble(c.getColumnIndex(COLUMNS[3])),
				c.getDouble(c.getColumnIndex(COLUMNS[4])));
		
		c.close();
		return s;
	}
	
	/* Fetches all stops (in a single list) that belong to any of
	 * the busses specified in the parameter
	 */
	public static List<Stop> fetchFromDatabase(ArrayList<Integer> routeIds) throws Exception {
		ArrayList<Stop> items = new ArrayList<Stop>();
		for(int id : routeIds) {
			items.addAll(fetchFromDatabase(id));
		}
		return items;
	}
}
