package com.finke.pgtransit.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import android.database.Cursor;

import com.finke.pgtransit.database.BusDatabaseHelper;

/* Represents an arrival time - that is, the time a bus arrives at
 * a stop
 */
public class TimeSlot {
	// Lots of left over variables from earlier versions
	private int mId;
	private String mLocationId;
	private String mWeekday;
	// Time the bus arrives
	private String mTime;
	private boolean mFridayFlag;
	// Time might be early morning after overnight rollover
	// This flag identifies such a case
	private boolean mNextDayFlag;
	// All extra bits of information about this time/stop
	private List<TripNote> mNotes;
	
	public final static String[] COLUMNS = {"_id", "location_id", "weekday", "time", "friday_flag", "next_day_flag"};
	
	public TimeSlot(int id, String locationId, String weekday,
			String time, int fridayFlag, int nextDayFlag) {
		this.mId = id;
		this.mLocationId = locationId;
		this.mWeekday = weekday;
		this.mTime = time;
		this.mFridayFlag = fridayFlag == 1;
		this.mNextDayFlag = nextDayFlag == 1;
	}
	
	public int getId() { return mId; }
	// Returns Calendar version of the time of this stop
	public Calendar getCalendarTime() {
		StringTokenizer tok = new StringTokenizer(mTime, ":");
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tok.nextToken()));
		cal.set(Calendar.MINUTE, Integer.parseInt(tok.nextToken()));
		
		// Next day flag
		if(mNextDayFlag) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		return cal;
	}
	/* Returns lazy-loaded list of all notes for this stop */
	public List<TripNote> getNotes() {
		if(mNotes != null) {
			return mNotes;
		}
		try {
			return TripNote.fetchFromDatabase(mId);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<TripNote>();
		}
	}
	public boolean hasNextDayFlag() { return mNextDayFlag; }
	
	/* Fetches all times for a stop, given a certain day of week
	 * The stop is identified by its PK
	 */
	public static List<TimeSlot> fetchFromDatabase(String locationId, String weekday) throws Exception {
		Cursor c = BusDatabaseHelper.getInstance().getTimesCursor(locationId, weekday);
		c.moveToFirst();
		
		List<TimeSlot> slots = new ArrayList<TimeSlot>();
		while(!c.isAfterLast()) {
			slots.add(new TimeSlot(
					c.getInt(c.getColumnIndex(COLUMNS[0])),
					c.getString(c.getColumnIndex(COLUMNS[1])),
					c.getString(c.getColumnIndex(COLUMNS[2])),
					c.getString(c.getColumnIndex(COLUMNS[3])),
					c.getInt(c.getColumnIndex(COLUMNS[4])),
					c.getInt(c.getColumnIndex(COLUMNS[5]))));
			c.moveToNext();
		}
		c.close();
		
		return slots;
	}
}
