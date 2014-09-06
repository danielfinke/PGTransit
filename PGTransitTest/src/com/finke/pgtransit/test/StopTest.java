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
	 * Test method for {@link com.finke.pgtransit.model.Stop#Stop(java.lang.String, int, java.lang.String, double, double)}.
	 */
	public void testStop() {
		Stop s1 = new Stop("0", 1, "stop", 5.0, -25.0);
		assertEquals(s1.getId(), "0");
		assertEquals(s1.getBusId(), 1);
		assertEquals(s1.getName(), "stop");
		assertEquals(s1.getLat(), 5.0);
		assertEquals(s1.getLon(), -25.0);
		Stop s2 = new Stop("1", 3, "not really a stop", -34.56, -53.998);
		assertEquals(s2.getId(), "1");
		assertEquals(s2.getBusId(), 3);
		assertEquals(s2.getName(), "not really a stop");
		assertEquals(s2.getLat(), -34.56);
		assertEquals(s2.getLon(), -53.998);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#getId()}.
	 */
	public void testGetId() {
		Stop s1 = new Stop("0", 0, null, 1.0, 1.0);
		Stop s2 = new Stop("1", 0, null, 1.0, 1.0);
		assertEquals(s1.getId(), "0");
		assertEquals(s2.getId(), "1");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#getBusId()}.
	 */
	public void testGetBusId() {
		Stop s1 = new Stop("0", 0, null, 1.0, 1.0);
		Stop s2 = new Stop("1", 1, null, 1.0, 1.0);
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
		Stop s1 = new Stop("0", 0, null, 1.0, 1.0);
		Stop s2 = new Stop("1", 0, "name", 1.0, 1.0);
		assertEquals(s1.getName(), null);
		assertEquals(s2.getName(), "name");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#getLat()}.
	 */
	public void testGetLat() {
		Stop s1 = new Stop("0", 0, null, 1.0, 1.0);
		Stop s2 = new Stop("1", 0, null, 1.35, -9.45);
		assertEquals(s1.getLat(), 1.0);
		assertEquals(s2.getLat(), 1.35);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#getLon()}.
	 */
	public void testGetLon() {
		Stop s1 = new Stop("0", 0, null, 1.0, 1.0);
		Stop s2 = new Stop("1", 0, null, 1.35, -9.45);
		assertEquals(s1.getLon(), 1.0);
		assertEquals(s2.getLon(), -9.45);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#fetchFromDatabase(int)}.
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

	/**
	 * Test method for {@link com.finke.pgtransit.model.Stop#fetchFromDatabase(java.util.ArrayList)}.
	 */
	public void testFetchFromDatabaseArrayListOfInteger() {
		fail("Not yet implemented");
	}

}
