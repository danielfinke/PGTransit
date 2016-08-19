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
	 * Test method for {@link com.finke.pgtransit.model.Bus#Bus(int, java.lang.String, int, int, java.lang.String, java.lang.String)}.
	 */
	public void testBus() {
		Bus b = new Bus(0, "name", 0, 0, "to direction", "#101010");
		assertEquals(b.getId(), 0);
        assertEquals(b.getName(), "name");
        assertEquals(b.getNumber(), 0);
        assertEquals(b.getDirection(), 0);
        assertEquals(b.getDirectionName(), "to direction");
		assertEquals(b.getColor(), Color.parseColor("#101010"));
        b = new Bus(0, "name", 0, 0, "to direction", null);
        assertEquals(b.getId(), 0);
        assertEquals(b.getName(), "name");
        assertEquals(b.getNumber(), 0);
        assertEquals(b.getDirection(), 0);
        assertEquals(b.getDirectionName(), "to direction");
        assertEquals(b.getColor(), Color.BLACK);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getId()}.
	 */
	public void testGetId() {
		Bus b1 = new Bus(0, "name", 0, 0, "to direction", "#101010");
		assertEquals(b1.getId(), 0);
		Bus b2 = new Bus(1, "name", 0, 0, "to direction", "#101010");
		assertEquals(b2.getId(), 1);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.model.Bus#getColor()}.
	 */
	public void testGetColor() {
		Bus b1 = new Bus(0, "name", 0, 0, "to direction", "#101010");
		assertEquals(b1.getColor(), Color.parseColor("#101010"));
		Bus b2 = new Bus(1, "name", 0, 0, "to direction", null);
		assertEquals(b2.getColor(), Color.BLACK);
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
