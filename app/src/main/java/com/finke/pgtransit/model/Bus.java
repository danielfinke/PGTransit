package com.finke.pgtransit.model;

import android.database.Cursor;
import android.graphics.Color;

import com.finke.pgtransit.database.BusDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/* Represents a specific bus that drives around town
 * mOwnerNo is the number seen on the bus screen
 */
public class Bus {
	
	private int mId;
	private String mName;
	private int mNumber;
	private int mDirection;
	private String mDirectionName;
	private int mColor;
	
	public final static String[] COLUMNS = {"_id", "name", "number", "direction", "direction_name", "color"};

	public Bus(int id, String name, int number, int direction, String directionName, String color) {
		mId = id;
		mName = name;
		mNumber = number;
		mDirection = direction;
		mDirectionName = directionName;
		// Default color black for bus icon/lines
		if(color != null) {
			mColor = Color.parseColor(color);
		}
		else {
			mColor = Color.BLACK;
		}
	}
	
	public int getId() { return mId; }
	public String getName() { return mName; }
	public int getNumber() { return mNumber; }
	public int getDirection() { return mDirection; }
	public String getDirectionName() { return mDirectionName; }
	public int getColor() { return mColor; }

    /**
     * Get a list of trips that occur on the given day for this bus
     * @param weekday The day (Weekday, Saturday, Sunday) to filter trips
     * @return A list of trips occurring on the given day for this bus
     */
    public List<Trip> getTrips(String weekday) {
        try {
            Cursor c = BusDatabaseHelper.getInstance().getTripCursor(mId, weekday);

            if (c.getCount() == 0) {
                return null;
            }

            c.moveToFirst();

            ArrayList<Trip> items = new ArrayList<>();
            while (!c.isAfterLast()) {
                items.add(new Trip(
                        c.getInt(c.getColumnIndex(Trip.COLUMNS[0])),
                        c.getInt(c.getColumnIndex(Trip.COLUMNS[1])),
                        c.getString(c.getColumnIndex(Trip.COLUMNS[2]))
                ));
                c.moveToNext();
            }
            c.close();

            return items;
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
	
	/* Fetch all bus records from the db */
	public static List<Bus> fetchFromDatabase() throws Exception {
		Cursor c = BusDatabaseHelper.getInstance().getRoutesCursor();
		c.moveToFirst();
		
		List<Bus> items = new ArrayList<Bus>();
		while(!c.isAfterLast()) {
			items.add(new Bus(
					c.getInt(c.getColumnIndex(COLUMNS[0])),
					c.getString(c.getColumnIndex(COLUMNS[1])),
					c.getInt(c.getColumnIndex(COLUMNS[2])),
					c.getInt(c.getColumnIndex(COLUMNS[3])),
					c.getString(c.getColumnIndex(COLUMNS[4])),
					c.getString(c.getColumnIndex(COLUMNS[5]))));
			c.moveToNext();
		}
		c.close();
		
		return items;
	}
	
	/* Fetch a specific bus record from db via its PK */
	public static Bus fetchFromDatabase(int id) throws Exception {
		Cursor c = BusDatabaseHelper.getInstance().getRoutesCursor(id);
		c.moveToFirst();
		
		return new Bus(
				c.getInt(c.getColumnIndex(COLUMNS[0])),
				c.getString(c.getColumnIndex(COLUMNS[1])),
				c.getInt(c.getColumnIndex(COLUMNS[2])),
				c.getInt(c.getColumnIndex(COLUMNS[3])),
				c.getString(c.getColumnIndex(COLUMNS[4])),
				c.getString(c.getColumnIndex(COLUMNS[5])));
	}
}
