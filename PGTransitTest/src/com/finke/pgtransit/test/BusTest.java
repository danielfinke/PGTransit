/**
 * 
 */
package com.finke.pgtransit.test;

import com.finke.pgtransit.model.Bus;

import android.graphics.Color;
import android.test.AndroidTestCase;

/**
 * @author Daniel
 *
 */
public class BusTest extends AndroidTestCase {

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
	 * Test method for {@link com.finke.pgtransit.model.Bus#Bus(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	public void testBus() {
		Bus b = new Bus(0, "owner", "ownerNo", "desc", "icon", "#101010");
		assertEquals(b.getId(), 0);
		assertEquals(b.getOwner(), "owner");
		assertEquals(b.getOwnerNo(), "ownerNo");
		assertEquals(b.getDescription(), "desc");
		assertEquals(b.getIcon(), "icon");
		assertEquals(b.getColor(), Color.parseColor("#101010"));
		b = new Bus(0, "owner", "ownerNo", "desc", "icon", null);
		assertEquals(b.getId(), 0);
		assertEquals(b.getOwner(), "owner");
		assertEquals(b.getOwnerNo(), "ownerNo");
		assertEquals(b.getDescription(), "desc");
		assertEquals(b.getIcon(), "icon");
		assertEquals(b.getColor(), Color.BLACK);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getId()}.
	 */
	public void testGetId() {
		Bus b1 = new Bus(0, null, null, null, null, null);
		assertEquals(b1.getId(), 0);
		Bus b2 = new Bus(1, null, null, null, null, null);
		assertEquals(b2.getId(), 1);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getOwner()}.
	 */
	public void testGetOwner() {
		Bus b1 = new Bus(0, "owner", null, null, null, null);
		assertEquals(b1.getOwner(), "owner");
		Bus b2 = new Bus(1, "pizza", null, null, null, null);
		assertEquals(b2.getOwner(), "pizza");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getOwnerNo()}.
	 */
	public void testGetOwnerNo() {
		Bus b1 = new Bus(0, null, "Chinese", null, null, null);
		assertEquals(b1.getOwnerNo(), "Chinese");
		Bus b2 = new Bus(1, null, "pizza", null, null, null);
		assertEquals(b2.getOwnerNo(), "pizza");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getDescription()}.
	 */
	public void testGetDescription() {
		Bus b1 = new Bus(0, null, null, "Chinese", null, null);
		assertEquals(b1.getDescription(), "Chinese");
		Bus b2 = new Bus(1, null, null, "pizza", null, null);
		assertEquals(b2.getDescription(), "pizza");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getIcon()}.
	 */
	public void testGetIcon() {
		Bus b1 = new Bus(0, null, null, null, "Chinese", null);
		assertEquals(b1.getIcon(), "Chinese");
		Bus b2 = new Bus(1, null, null, null, "pizza", null);
		assertEquals(b2.getIcon(), "pizza");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getColor()}.
	 */
	public void testGetColor() {
		Bus b1 = new Bus(0, null, null, null, null, "#101010");
		assertEquals(b1.getColor(), Color.parseColor("#101010"));
		Bus b2 = new Bus(1, null, null, null, null, null);
		assertEquals(b2.getColor(), Color.BLACK);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getMaps()}.
	 */
	public void testGetMaps() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#fetchFromDatabase()}.
	 */
	public void testFetchFromDatabase() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#fetchFromDatabase(int)}.
	 */
	public void testFetchFromDatabaseInt() {
		fail("Not yet implemented");
	}

}
