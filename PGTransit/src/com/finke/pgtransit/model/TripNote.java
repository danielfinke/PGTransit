package com.finke.pgtransit.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.finke.pgtransit.database.BusDatabaseHelper;

/* TripNotes are additional pieces of information about a TimeSlot
 * such as the fact that the time may take place at a slightly different
 * exact geographical location
 */
public class TripNote {
	// No getters needed yet for these
	private int mId;
	private int mTimeSlotId;
	private String mName;
	private String mDescription;
	
	public final static String[] COLUMNS = {"_id", "time_slot_id", "name", "description"};
	
	public TripNote(int id, int timeSlotId, String name, String description) {
		this.mId = id;
		this.mTimeSlotId = timeSlotId;
		this.mName = name;
		this.mDescription = description;
	}
	
	public String getName() { return mName; }
	public String getDescription() { return mDescription; }
	
	/* Fetches a list of notes for a time slot given the TimeSlot PK */
	public static List<TripNote> fetchFromDatabase(int timeSlotId) throws Exception {
		Cursor c = BusDatabaseHelper.getInstance().getTripNotesCursor(timeSlotId);
		c.moveToFirst();
		
		List<TripNote> data = new ArrayList<TripNote>();
		while(!c.isAfterLast()) {
			data.add(new TripNote(
					c.getInt(c.getColumnIndex(COLUMNS[0])),
					c.getInt(c.getColumnIndex(COLUMNS[1])),
					c.getString(c.getColumnIndex(COLUMNS[2])),
					c.getString(c.getColumnIndex(COLUMNS[3]))));
			c.moveToNext();
		}
		c.close();
		
		return data;
	}
}
