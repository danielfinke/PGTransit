/**
 * 
 */
package com.finke.pgtransit.test;

import com.finke.pgtransit.model.Stop;

import android.test.AndroidTestCase;

/**
 * @author Daniel
 *
 */
public class StopTest extends AndroidTestCase {

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
	 * Test method for {@link com.finke.pgtransit.model.Stop#Stop(java.lang.String, int, java.lang.String, java.lang.String)}.
	 */
	public void testStop() {
		Stop s1 = new Stop("0", 0, "name", "day");
		assertEquals(s1.getId(), "0");
		assertEquals(s1.getBusId(), 1);
		assertEquals(s1.getName(), "name");
		assertEquals(s1.getDay(), "day");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#getId()}.
	 */
	public void testGetId() {
		Stop s1 = new Stop("0", 0, "name", "day");
		Stop s2 = new Stop("1", 0, "name", "day");
		assertEquals(s1.getId(), "0");
		assertEquals(s2.getId(), "1");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#getBusId()}.
	 */
	public void testGetBusId() {
		Stop s1 = new Stop("0", 0, "name", "day");
		Stop s2 = new Stop("1", 1, "name", "day");
		assertEquals(s1.getBusId(), 0);
		assertEquals(s2.getBusId(), 1);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#getBus()}.
	 */
	public void testGetBus() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#getName()}.
	 */
	public void testGetName() {
		Stop s1 = new Stop("0", 0, null, "day");
		Stop s2 = new Stop("1", 0, "name", "day");
		assertEquals(s1.getName(), null);
		assertEquals(s2.getName(), "name");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#fetchFromDatabase(int, java.lang.String)}.
	 */
	public void testFetchFromDatabaseInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#fetchFromDatabase(java.lang.String)}.
	 */
	public void testFetchFromDatabaseString() {
		fail("Not yet implemented");
	}

}
