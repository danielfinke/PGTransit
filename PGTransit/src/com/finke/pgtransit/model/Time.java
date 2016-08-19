package com.finke.pgtransit.model;

import java.util.Calendar;

public abstract class Time implements TimeInterface {

	protected int mId;
	// Time the bus arrives in seconds
	protected int mTime;
	protected String mNoteCode;
	protected String mNote;
	
	public Time() {
		mId = 0;
		mTime = 0;
		mNoteCode = null;
		mNote = null;
	}
	
	public int getId() { return mId; }
	public Calendar getCalendarTime() {
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.HOUR_OF_DAY, mTime / 60);
		cal.set(Calendar.MINUTE, mTime % 60);
		
		return cal;
	}
	public String getNoteCode() { return mNoteCode; }
	public String getNote() { return mNote; }
	
}
