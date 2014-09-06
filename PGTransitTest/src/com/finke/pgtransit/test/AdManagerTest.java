/**
 * 
 */
package com.finke.pgtransit.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.LinearLayout;

import com.finke.pgtransit.MainActivity;
import com.finke.pgtransit.extensions.AdManager;
import com.google.android.gms.ads.AdView;

/**
 * @author Daniel
 *
 */
public class AdManagerTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity mAct;
	
	public AdManagerTest() {
		super(MainActivity.class);
	}

	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		mAct = getActivity();
		assertNotNull(mAct);
	}

	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link com.finke.pgtransit.extensions.AdManager#AdManager(android.widget.LinearLayout, android.app.Activity)}.
	 */
	public void testAdManager() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.finke.pgtransit.extensions.AdManager#getNewAd()}.
	 */
	public void testGetNewAd() {
		LinearLayout ll = new LinearLayout(mAct);
		final AdManager adMan = new AdManager(ll, mAct);
		mAct.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				adMan.getNewAd();
			}
			
		});
		getInstrumentation().waitForIdleSync();
		// TODO: Do something better than sleeping here
		try {
			Thread.sleep(1000);
		}
		catch(InterruptedException e) {
			fail("Sleep interrupted");
		}
		assertEquals(ll.getChildCount(), 1);
		assertTrue(ll.getChildAt(0).getClass().equals(AdView.class));
	}

	/**
	 * Test method for {@link com.finke.pgtransit.extensions.AdManager#getNewInterstitialAd()}.
	 */
	public void testGetNewInterstitialAd() {
		LinearLayout ll = new LinearLayout(mAct);
		final AdManager adMan = new AdManager(ll, mAct);
		mAct.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				adMan.getNewInterstitialAd();
			}
			
		});
		getInstrumentation().waitForIdleSync();
		// TODO: Do something better than sleeping here
		try {
			Thread.sleep(1000);
		}
		catch(InterruptedException e) {
			fail("Sleep interrupted");
		}
		// Can't really check interstitial here
	}

	/**
	 * Test method for {@link com.finke.pgtransit.extensions.AdManager#removeAd()}.
	 */
	public void testRemoveAd() {
		LinearLayout ll = new LinearLayout(mAct);
		final AdManager adMan = new AdManager(ll, mAct);
		mAct.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				adMan.getNewAd();
			}
			
		});
		getInstrumentation().waitForIdleSync();
		// TODO: Do something better than sleeping here
		try {
			Thread.sleep(1000);
		}
		catch(InterruptedException e) {
			fail("Sleep interrupted");
		}
		assertEquals(ll.getChildCount(), 1);
		assertTrue(ll.getChildAt(0).getClass().equals(AdView.class));
		mAct.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				adMan.removeAd();
			}
			
		});
		getInstrumentation().waitForIdleSync();
		assertEquals(ll.getChildCount(), 0);
	}

	/**
	 * Test method for {@link com.finke.pgtransit.extensions.AdManager#displayInterstitialIfReady()}.
	 */
	public void testDisplayInterstitialIfReady() {
		// Might be no good way of checking this method
		fail("Not yet implemented");
	}

}
