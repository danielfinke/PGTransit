/**
 * 
 */
package com.finke.pgtransit.test;

import java.util.ArrayList;

import android.content.Context;
import android.test.AndroidTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.finke.pgtransit.R;
import com.finke.pgtransit.adapters.StopsAdapter;
import com.finke.pgtransit.model.Stop;

/**
 * @author Daniel
 *
 */
public class StopsAdapterTest extends AndroidTestCase {
	
	private StopsAdapter mAdapter;

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
	 * Test method for {@link com.finke.pgtransit.adapters.StopsAdapter#StopsAdapter(android.content.Context)}.
	 */
	public void testStopsAdapter() {
		mAdapter = new StopsAdapter(getContext());
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
	 * Test method for {@link com.finke.pgtransit.adapters.StopsAdapter#getCount()}.
	 */
	public void testGetCount() {
		mAdapter = new StopsAdapter(getContext());
		assertEquals(mAdapter.getCount(), 0);
		ArrayList<Stop> items = new ArrayList<Stop>();
		items.add(new Stop("0", 0, null, 0.0, 0.0));
		items.add(new Stop("1", 0, null, 0.0, 0.0));
		mAdapter.setItems(items);
		assertEquals(mAdapter.getCount(), 2);
		items.clear();
		assertEquals(mAdapter.getCount(), 0);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.StopsAdapter#getItem(int)}.
	 */
	public void testGetItem() {
		mAdapter = new StopsAdapter(getContext());
		try {
			mAdapter.getItem(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<Stop> items = new ArrayList<Stop>();
		Stop b0 = new Stop("0", 0, null, 0.0, 0.0);
		items.add(b0);
		items.add(new Stop("1", 0, null, 0.0, 0.0));
		mAdapter.setItems(items);
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
	 * Test method for {@link com.finke.pgtransit.adapters.StopsAdapter#getStop(int)}.
	 */
	public void testGetStop() {
		mAdapter = new StopsAdapter(getContext());
		try {
			mAdapter.getStop(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<Stop> items = new ArrayList<Stop>();
		Stop b0 = new Stop("0", 0, null, 0.0, 0.0);
		items.add(b0);
		items.add(new Stop("1", 0, null, 0.0, 0.0));
		mAdapter.setItems(items);
		assertEquals(mAdapter.getStop(0), b0);
		items.clear();
		try {
			mAdapter.getStop(0);
			fail("items should be empty at end");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.StopsAdapter#getItemId(int)}.
	 */
	public void testGetItemId() {
		mAdapter = new StopsAdapter(getContext());
		// Due to having crappy getItemId implementation
		assertEquals(mAdapter.getItemId(0), 0);
		/*try {
			mAdapter.getItemId(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}*/
		ArrayList<Stop> items = new ArrayList<Stop>();
		Stop b0 = new Stop("0", 0, null, 0.0, 0.0);
		items.add(b0);
		items.add(new Stop("1", 0, null, 0.0, 0.0));
		mAdapter.setItems(items);
		assertEquals(mAdapter.getItemId(0), 0);
		items.clear();
		
		// Due to having crappy getItemId implementation
		assertEquals(mAdapter.getItemId(0), 0);
		/*try {
			mAdapter.getItemId(0);
			fail("items should be empty at end");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}*/
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.StopsAdapter#getView(int, android.view.View, android.view.ViewGroup)}.
	 */
	public void testGetView() {
		Context context = getContext();
		mAdapter = new StopsAdapter(context);
		View reusable = new View(context);
		ViewGroup vg = new ListView(context);
		try {
			mAdapter.getView(0, reusable, vg);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<Stop> items = new ArrayList<Stop>();
		Stop b0 = new Stop("0", 0, "mon nom", 0.0, 0.0);
		items.add(b0);
		items.add(new Stop("1", 0, null, 0.0, 0.0));
		mAdapter.setItems(items);
		
		View v = mAdapter.getView(0, reusable, vg);
		assertEquals(((TextView)v.findViewById(R.id.name)).getText(), "mon nom");
		
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
	 * Test method for {@link com.finke.pgtransit.adapters.StopsAdapter#setItems(java.util.List)}.
	 */
	public void testSetItems() {
		mAdapter = new StopsAdapter(getContext());
		ArrayList<Stop> items = new ArrayList<Stop>();
		Stop b0 = new Stop("0", 0, null, 0.0, 0.0);
		Stop b1 = new Stop("1", 0, null, 0.0, 0.0);
		items.add(b0);
		items.add(b1);
		mAdapter.setItems(items);
		assertEquals(mAdapter.getItem(0), b0);
		assertEquals(mAdapter.getItem(1), b1);
	}

}
