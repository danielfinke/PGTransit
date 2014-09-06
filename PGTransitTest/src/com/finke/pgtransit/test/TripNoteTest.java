/**
 * 
 */
package com.finke.pgtransit.test;

import com.finke.pgtransit.model.TripNote;

import android.test.AndroidTestCase;

/**
 * @author Daniel
 *
 */
public class TripNoteTest extends AndroidTestCase {

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
	 * Test method for {@link com.finke.pgtransit.model.TripNote#TripNote(int, int, java.lang.String, java.lang.String)}.
	 */
	public void testTripNote() {
		TripNote t1 = new TripNote(1, 0, "name", "desc");
		assertEquals("name", t1.getName());
		assertEquals("desc", t1.getDescription());
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TripNote#getName()}.
	 */
	public void testGetName() {
		TripNote t1 = new TripNote(1, 0, "name", null);
		TripNote t2 = new TripNote(2, 0, "wahooo", null);
		assertEquals("name", t1.getName());
		assertEquals("wahooo", t2.getName());
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TripNote#getDescription()}.
	 */
	public void testGetDescription() {
		TripNote t1 = new TripNote(1, 0, null, "desc");
		TripNote t2 = new TripNote(2, 0, null, "come at me bro");
		assertEquals("desc", t1.getDescription());
		assertEquals("come at me bro", t2.getDescription());
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.TripNote#fetchFromDatabase(int)}.
	 */
	public void testFetchFromDatabase() {
		fail("Not yet implemented");
	}

}
