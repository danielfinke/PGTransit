/**
 * 
 */
package com.finke.pgtransit.test;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.test.AndroidTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.finke.pgtransit.R;
import com.finke.pgtransit.adapters.TimesAdapter;
import com.finke.pgtransit.model.TimeInterface;
import com.finke.pgtransit.model.TimeSlot;

/**
 * @author Daniel
 *
 */
public class TimesAdapterTest extends AndroidTestCase {
	
	private TimesAdapter mAdapter;

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
	 * Test method for {@link com.finke.pgtransit.adapters.TimesAdapter#TimesAdapter(android.content.Context)}.
	 */
	public void testTimesAdapter() {
		mAdapter = new TimesAdapter(getContext());
		assertEquals(mAdapter.getCount(), 0);
		try {
			mAdapter.getItem(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.TimesAdapter#getCount()}.
	 */
	public void testGetCount() {
		mAdapter = new TimesAdapter(getContext());
		assertEquals(mAdapter.getCount(), 0);
		ArrayList<TimeInterface> items = new ArrayList<>();
		items.add(new TimeSlot(0, "0", 400, "R", "note"));
		items.add(new TimeSlot(1, "0", 400, "R", "note"));
		mAdapter.setTimes(items);
		assertEquals(mAdapter.getCount(), 2);
		items.clear();
		assertEquals(mAdapter.getCount(), 0);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.TimesAdapter#getItem(int)}.
	 */
	public void testGetItem() {
		mAdapter = new TimesAdapter(getContext());
		try {
			mAdapter.getItem(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<TimeInterface> items = new ArrayList<>();
		TimeSlot b0 = new TimeSlot(0, "0", 400, "R", "note");
		items.add(b0);
		items.add(new TimeSlot(1, "0", 400, "R", "note"));
		mAdapter.setTimes(items);
		assertEquals(mAdapter.getItem(0), b0);
		items.clear();
		try {
			mAdapter.getItem(0);
			fail("items should be empty at end");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.TimesAdapter#getTimeSlot(int)}.
	 */
	public void testGetTimeSlot() {
		mAdapter = new TimesAdapter(getContext());
		try {
			mAdapter.getTimeSlot(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<TimeInterface> items = new ArrayList<>();
		TimeSlot b0 = new TimeSlot(0, "0", 400, "R", "note");
		items.add(b0);
		items.add(new TimeSlot(1, "0", 400, "R", "note"));
		mAdapter.setTimes(items);
		assertEquals(mAdapter.getTimeSlot(0), b0);
		items.clear();
		try {
			mAdapter.getTimeSlot(0);
			fail("items should be empty at end");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.TimesAdapter#getItemId(int)}.
	 */
	public void testGetItemId() {
		mAdapter = new TimesAdapter(getContext());
		try {
			mAdapter.getItemId(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<TimeInterface> items = new ArrayList<>();
		TimeSlot b0 = new TimeSlot(0, "0", 400, "R", "note");
		items.add(b0);
		items.add(new TimeSlot(1, "0", 400, "R", "note"));
		mAdapter.setTimes(items);
		assertEquals(mAdapter.getItemId(0), 0);
		items.clear();
		try {
			mAdapter.getItemId(0);
			fail("items should be empty at end");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.TimesAdapter#getView(int, android.view.View, android.view.ViewGroup)}.
	 */
	public void testGetView() {
		Context context = getContext();
		mAdapter = new TimesAdapter(context);
		View reusable = new View(context);
		ViewGroup vg = new ListView(context);
		try {
			mAdapter.getView(0, reusable, vg);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<TimeInterface> items = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		// Setting up a calendar time that should be after now
		boolean nextDayFlag = false;
		String hour = "" + cal.get(Calendar.HOUR_OF_DAY);
		String min = "" + (cal.get(Calendar.MINUTE) + 2);
		if(min.equals("60")) {
			min.equals("00");
			hour = "" + (cal.get(Calendar.HOUR_OF_DAY) + 1);
			if(hour.equals("24")) {
				hour = "00";
				nextDayFlag = true;
			}
		}
		if(min.length() == 1) {
			min = "0" + min;
		}
		String time1 = hour + ":" + min;
		TimeSlot b0 = new TimeSlot(0, "0", 400, "R", "note");
		items.add(b0);
		mAdapter.setTimes(items);
		
		// TODO: test notes
		
		View v = mAdapter.getView(0, reusable, vg);
		assertEquals(View.VISIBLE, ((TextView)v.findViewById(R.id.nextArrival1)).getVisibility());
		
		items.clear();
		try {
			mAdapter.getView(0, reusable, vg);
			fail("items should be empty at end");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.TimesAdapter#setTimes(java.util.List)}.
	 */
	public void testSetSlots() {
		mAdapter = new TimesAdapter(getContext());
		ArrayList<TimeInterface> items = new ArrayList<>();
		TimeSlot b0 = new TimeSlot(0, "0", 400, "R", "note");
		TimeSlot b1 = new TimeSlot(1, "0", 400, "R", "note");
		items.add(b0);
		items.add(b1);
		mAdapter.setTimes(items);
		assertEquals(mAdapter.getItem(0), b0);
		assertEquals(mAdapter.getItem(1), b1);
	}

}
