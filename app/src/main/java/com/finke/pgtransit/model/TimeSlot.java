package com.finke.pgtransit.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.finke.pgtransit.database.BusDatabaseHelper;

/* Represents an arrival time - that is, the time a bus arrives at
 * a stop
 */
public class TimeSlot extends Time {
	private String mMajorStopId;
	
	public final static String[] COLUMNS = {"_id", "major_stop_id", "time", "note_code", "note"};
	
	public TimeSlot(int id, String majorStopId, int time,
			String noteCode, String note) {
		this.mId = id;
		this.mMajorStopId = majorStopId;
		this.mTime = time;
		this.mNoteCode = noteCode;
		this.mNote = note;
	}
	
	public String getMajorStopId() { return mMajorStopId; }
	
	/* Fetches all times for a stop, given a certain day of week
	 * The stop is identified by its PK
	 */
	public static List<TimeInterface> fetchFromDatabase(String locationId) throws Exception {
		Cursor c = BusDatabaseHelper.getInstance().getTimesCursor(locationId);
		c.moveToFirst();
		
		List<TimeInterface> slots = new ArrayList<TimeInterface>();
		while(!c.isAfterLast()) {
			slots.add(new TimeSlot(
					c.getInt(c.getColumnIndex(COLUMNS[0])),
					c.getString(c.getColumnIndex(COLUMNS[1])),
					c.getInt(c.getColumnIndex(COLUMNS[2])),
					c.getString(c.getColumnIndex(COLUMNS[3])),
					c.getString(c.getColumnIndex(COLUMNS[4]))));
			c.moveToNext();
		}
		c.close();
		
		return slots;
	}
}
