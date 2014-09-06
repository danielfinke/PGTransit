/**
 * 
 */
package com.finke.pgtransit.test;

import com.finke.pgtransit.model.Map;

import android.test.AndroidTestCase;

/**
 * @author Daniel
 *
 */
public class MapTest extends AndroidTestCase {

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
	 * Test method for {@link com.finke.pgtransit.model.Map#Map(int, int, java.lang.String, java.lang.String, int)}.
	 */
	public void testMap() {
		Map m1 = new Map(0, 0, null, null, 0);
		assertEquals(m1.getId(), 0);
		assertEquals(m1.getBusId(), 0);
		assertEquals(m1.getWeekday(), null);
		assertEquals(m1.getKmlFile(), null);
		assertFalse(m1.isPrimary());
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Map#getId()}.
	 */
	public void testGetId() {
		Map m1 = new Map(0, 0, null, null, 0);
		Map m2 = new Map(1, 0, null, null, 0);
		assertEquals(m1.getId(), 0);
		assertEquals(m2.getId(), 1);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Map#getBusId()}.
	 */
	public void testGetBusId() {
		Map m1 = new Map(0, 0, null, null, 0);
		Map m2 = new Map(1, 1, null, null, 0);
		assertEquals(m1.getBusId(), 0);
		assertEquals(m2.getBusId(), 1);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Map#getWeekday()}.
	 */
	public void testGetWeekday() {
		Map m1 = new Map(0, 0, null, null, 0);
		Map m2 = new Map(1, 0, "weekdays", null, 0);
		assertEquals(m1.getWeekday(), null);
		assertEquals(m2.getWeekday(), "weekdays");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Map#getKmlFile()}.
	 */
	public void testGetKmlFile() {
		Map m1 = new Map(0, 0, null, null, 0);
		Map m2 = new Map(1, 0, null, "bus15", 0);
		assertEquals(m1.getKmlFile(), null);
		assertEquals(m2.getKmlFile(), "bus15");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Map#isPrimary()}.
	 */
	public void testIsPrimary() {
		Map m1 = new Map(0, 0, null, null, 0);
		Map m2 = new Map(1, 0, null, null, 1);
		assertFalse(m1.isPrimary());
		assertTrue(m2.isPrimary());
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Map#getLines(android.content.res.AssetManager)}.
	 */
	public void testGetLines() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Map#fetchFromDatabase(int)}.
	 */
	public void testFetchFromDatabase() {
		fail("Not yet implemented");
	}

}
