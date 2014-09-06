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
import com.finke.pgtransit.adapters.RoutesAdapter;
import com.finke.pgtransit.model.Bus;

/**
 * @author Daniel
 *
 */
public class RoutesAdapterTest extends AndroidTestCase {
	
	private RoutesAdapter mAdapter;

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
	 * Test method for {@link com.finke.pgtransit.adapters.RoutesAdapter#RoutesAdapter(android.content.Context)}.
	 */
	public void testRoutesAdapter() {
		mAdapter = new RoutesAdapter(getContext());
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
	 * Test method for {@link com.finke.pgtransit.adapters.RoutesAdapter#getCount()}.
	 */
	public void testGetCount() {
		mAdapter = new RoutesAdapter(getContext());
		assertEquals(mAdapter.getCount(), 0);
		ArrayList<Bus> items = new ArrayList<Bus>();
		items.add(new Bus(0, null, null, null, null, null));
		items.add(new Bus(1, null, null, null, null, null));
		mAdapter.setItems(items);
		assertEquals(mAdapter.getCount(), 2);
		items.clear();
		assertEquals(mAdapter.getCount(), 0);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.RoutesAdapter#getItem(int)}.
	 */
	public void testGetItem() {
		mAdapter = new RoutesAdapter(getContext());
		try {
			mAdapter.getItem(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<Bus> items = new ArrayList<Bus>();
		Bus b0 = new Bus(0, null, null, null, null, null);
		items.add(b0);
		items.add(new Bus(1, null, null, null, null, null));
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
	 * Test method for {@link com.finke.pgtransit.adapters.RoutesAdapter#getBus(int)}.
	 */
	public void testGetBus() {
		mAdapter = new RoutesAdapter(getContext());
		try {
			mAdapter.getBus(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<Bus> items = new ArrayList<Bus>();
		Bus b0 = new Bus(0, null, null, null, null, null);
		items.add(b0);
		items.add(new Bus(1, null, null, null, null, null));
		mAdapter.setItems(items);
		assertEquals(mAdapter.getBus(0), b0);
		items.clear();
		try {
			mAdapter.getBus(0);
			fail("items should be empty at end");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
	}

	/**
	 * Test method for {@link com.finke.pgtransit.adapters.RoutesAdapter#getItemId(int)}.
	 */
	public void testGetItemId() {
		mAdapter = new RoutesAdapter(getContext());
		try {
			mAdapter.getItemId(0);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<Bus> items = new ArrayList<Bus>();
		Bus b0 = new Bus(0, null, null, null, null, null);
		items.add(b0);
		items.add(new Bus(1, null, null, null, null, null));
		mAdapter.setItems(items);
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
	 * Test method for {@link com.finke.pgtransit.adapters.RoutesAdapter#getView(int, android.view.View, android.view.ViewGroup)}.
	 */
	public void testGetView() {
		Context context = getContext();
		mAdapter = new RoutesAdapter(context);
		View reusable = new View(context);
		ViewGroup vg = new ListView(context);
		try {
			mAdapter.getView(0, reusable, vg);
			fail("items should be empty");
		}
		catch(IndexOutOfBoundsException ex) {
			// Success
		}
		ArrayList<Bus> items = new ArrayList<Bus>();
		Bus b0 = new Bus(0, "owner", null, "desc", null, null);
		items.add(b0);
		items.add(new Bus(1, null, null, null, null, null));
		mAdapter.setItems(items);
		
		View v = mAdapter.getView(0, reusable, vg);
		assertEquals(v.findViewById(R.id.imageView1), null);
		assertEquals(((TextView)v.findViewById(R.id.name)).getText(), "owner");
		assertEquals(((TextView)v.findViewById(R.id.description)).getText(), "desc");
		
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
	 * Test method for {@link com.finke.pgtransit.adapters.RoutesAdapter#setItems(java.util.List)}.
	 */
	public void testSetItems() {
		mAdapter = new RoutesAdapter(getContext());
		ArrayList<Bus> items = new ArrayList<Bus>();
		Bus b0 = new Bus(0, null, null, null, null, null);
		Bus b1 = new Bus(1, null, null, null, null, null);
		items.add(b0);
		items.add(b1);
		mAdapter.setItems(items);
		assertEquals(mAdapter.getItem(0), b0);
		assertEquals(mAdapter.getItem(1), b1);
	}

}
