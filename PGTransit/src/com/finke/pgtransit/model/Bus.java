package com.finke.pgtransit.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.Color;

import com.finke.pgtransit.database.BusDatabaseHelper;

/* Represents a specific bus that drives around town
 * mOwnerNo is the number seen on the bus screen
 */
public class Bus {
	
	private int mId;
	private String mOwner;
	private String mOwnerNo;
	private String mDescription;
	private String mIcon;
	private int mColor;
	
	// Lazy loaded relationships
	private List<Map> mMaps;
	
	public final static String[] COLUMNS = {"_id", "owner", "owner_no", "direction", "icon", "color"};

	public Bus(int id, String owner, String ownerNo, String description, String icon, String color) {
		mId = id;
		mOwner = owner;
		mOwnerNo = ownerNo;
		mDescription = description;
		mIcon = icon;
		// Default color black for bus icon/lines
		if(color != null) {
			mColor = Color.parseColor(color);
		}
		else {
			mColor = Color.BLACK;
		}
		mMaps = null;
	}
	
	public int getId() { return mId; }
	public String getOwner() { return mOwner; }
	public String getOwnerNo() { return mOwnerNo; }
	public String getDescription() { return mDescription; }
	public String getIcon() { return mIcon; }
	public int getColor() { return mColor; }
	
	/* Lazy loaded maps for this bus */
	public List<Map> getMaps() {
		if(mMaps == null) {
			try {
				mMaps = Map.fetchFromDatabase(mId);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return mMaps;
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
					c.getString(c.getColumnIndex(COLUMNS[2])),
					c.getString(c.getColumnIndex(COLUMNS[3])),
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
				c.getString(c.getColumnIndex(COLUMNS[2])),
				c.getString(c.getColumnIndex(COLUMNS[3])),
				c.getString(c.getColumnIndex(COLUMNS[4])),
				c.getString(c.getColumnIndex(COLUMNS[5])));
	}
}
