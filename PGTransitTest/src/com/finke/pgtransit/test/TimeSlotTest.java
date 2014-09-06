/**
 * 
 */
package com.finke.pgtransit.test;

import java.util.Calendar;

import android.test.AndroidTestCase;

import com.finke.pgtransit.model.TimeSlot;

/**
 * @author Daniel
 *
 */
public class TimeSlotTest extends AndroidTestCase {

	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TimeSlot#TimeSlot(int, java.lang.String, java.lang.String, java.lang.String, int, int)}.
	 */
	public void testTimeSlot() {
		TimeSlot t1 = new TimeSlot(0, null, null, "00:00", 0, 1);
		assertEquals(t1.getId(), 0);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		// Next day flag
		cal.add(Calendar.DAY_OF_MONTH, 1);
		assertEquals(t1.getCalendarTime(), cal);
		assertTrue(t1.hasNextDayFlag());
		
		TimeSlot t2 = new TimeSlot(1, null, null, "11:34", 0, 0);
		cal.set(Calendar.HOUR_OF_DAY, 11);
		cal.set(Calendar.MINUTE, 34);
		// Next day flag
		cal.roll(Calendar.DAY_OF_MONTH, false);
		assertEquals(t2.getCalendarTime(), cal);
		assertFalse(t2.hasNextDayFlag());
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TimeSlot#getId()}.
	 */
	public void testGetId() {
		TimeSlot s1 = new TimeSlot(0, null, null, null, 1, 1);
		TimeSlot s2 = new TimeSlot(1, null, null, null, 1, 1);
		assertEquals(s1.getId(), 0);
		assertEquals(s2.getId(), 1);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TimeSlot#getCalendarTime()}.
	 */
	public void testGetCalendarTime() {
		TimeSlot s1 = new TimeSlot(0, null, null, "00:00", 1, 1);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		// Next day flag
		cal.add(Calendar.DAY_OF_MONTH, 1);
		
		assertEquals(s1.getCalendarTime(), cal);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TimeSlot#getNotes()}.
	 */
	public void testGetNotes() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TimeSlot#hasNextDayFlag()}.
	 */
	public void testHasNextDayFlag() {
		TimeSlot t1 = new TimeSlot(0, null, null, null, 0, 0);
		TimeSlot t2 = new TimeSlot(1, null, null, null, 0, 1);
		assertFalse(t1.hasNextDayFlag());
		assertTrue(t2.hasNextDayFlag());
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TimeSlot#fetchFromDatabase(java.lang.String, java.lang.String)}.
	 */
	public void testFetchFromDatabase() {
		fail("Not yet implemented");
	}

}
