package com.finke.pgtransit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.finke.pgtransit.adapters.ViewPagerAdapter;
import com.finke.pgtransit.database.BusDatabaseHelper;
import com.finke.pgtransit.extensions.AdManager;
import com.finke.pgtransit.extensions.AppRater;

/* Handles all activity life cycle/UI interactions for the
 * Schedules and Maps tabs */
public class MainActivity extends AppCompatActivity {
    private final static int LOCATION_PERMISSIONS_REQUEST_CODE = 1234;

	// Container for Google AdMob advertisements
	private LinearLayout mAdCont;
	// ViewPager for tabs
	private ViewPager mViewPager;
	private ViewPagerAdapter mViewPagerAdapter;
    // Used to automatically push map fragment after resume when location
    // permission is accepted
    private boolean mLocationPermissionAccepted = false;
	
	// Handles displaying banner ad in container
	// And the interstitial for times list
	private AdManager mAdManager;
	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.main_activity);
		
		// App rating service (prompts periodically)
		AppRater.app_launched(this);
		
		// Ad manager to prevent background CPU usage of the ad view
		mAdCont = (LinearLayout)findViewById(R.id.adContainer);
		
		// Prepare the interstitial ad for when the users tries to pick a route/stop
		mAdManager = null;
		// Checks to see if user has paid for removing ads
		// If not, set up the ad banner and also load a full-screen
		// ad in the background for later
		if(adsEnabled()) {
			mAdManager = new AdManager(mAdCont, this);
			mAdManager.getNewInterstitialAd();
		}
		
		// Fire up the database helper
		BusDatabaseHelper.createInstance(this);

		setupTabs();
		
		onFirstLaunch();
	}

	protected void onResume() {
		super.onResume();

		// Restart ads after app resumes if allowed
		if(adsEnabled()) {
			mAdManager.getNewAd();
		}

		// Callback for location has told us to push the map now
		if(mLocationPermissionAccepted) {
			mLocationPermissionAccepted = false;
		}
	}

	protected void onPause() {
		super.onPause();
		
		// Ads paused in background to prevent
		// CPU usage
		if(adsEnabled()) {
			mAdManager.removeAd();
		}
	}
	
	protected void onDestroy() {
		BusDatabaseHelper.destroyInstance();
		super.onDestroy();
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if(grantResults.length == 2 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionAccepted = true;
                }
        }
    }

    public AdManager getAdManager() { return mAdManager; }
	
	// Checks if ads were disabled by purchase
	public boolean adsEnabled() {
		SharedPreferences prefs = getSharedPreferences(getString(R.string.iab_prefs), 0);
        return !prefs.getBoolean("removedAds", false);
	}

	/**
	 *
	 */
	private void setupTabs() {
		mViewPager = (ViewPager)findViewById(R.id.viewPager);
		mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mViewPagerAdapter);

		PagerTabStrip pagerTabStrip = (PagerTabStrip)findViewById(R.id.pagerTabStrip);
		pagerTabStrip.setBackgroundColor(Color.BLACK);
		pagerTabStrip.setTextColor(Color.WHITE);
		pagerTabStrip.setTabIndicatorColor(Color.WHITE);
	}

	/**
	 *
	 */
	public void pushFragment(Fragment fragment, int position) {
		Fragment pagerFragment = mViewPagerAdapter.getItem(position);
		FragmentManager fm = pagerFragment.getChildFragmentManager();
		fm.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right,
						R.anim.slide_out_left,
						R.anim.slide_in_left,
						R.anim.slide_out_right)
				.addToBackStack(null)
				.replace(R.id.container, fragment)
				.commit();
	}

	/**
	 *
	 */
	public void popBackStack() {
		Fragment pagerFragment = mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
		FragmentManager fm = pagerFragment.getChildFragmentManager();
		fm.popBackStack(null, 0);
	}
	
	/* Displays new features dialog on new installs or when
	 * the application is updated
	 */
	private void onFirstLaunch() {
		SharedPreferences prefs = getSharedPreferences(getString(R.string.iab_prefs), 0);
		
		// Display new paid ads removal feature notice
		if(prefs.getBoolean("firstLaunch1", true)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			TextView textView = new TextView(this);
			textView.setText(R.string.you_can_now_remove_ads);
			float scale = getResources().getDisplayMetrics().density;
			int dpAsPixels = (int) (10*scale + 0.5f);
			textView.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
			textView.setBackgroundColor(Color.WHITE);
			textView.setTextColor(Color.BLACK);
			builder.setTitle("New Paid Feature");
			builder.setView(textView);
	        builder.setNeutralButton("Dismiss", new OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.cancel();
				}
	        	
	        });
	        AlertDialog dialog = builder.create();
			dialog.show();
			
			SharedPreferences.Editor editor = prefs.edit();
	        editor.putBoolean("firstLaunch1", false);
	        editor.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Replace home button functionality with a slightly
		// friendlier back button navigation model
		case android.R.id.home:
			this.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 *
	 */
	public void onBackPressed() {
		Fragment pagerFragment = mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
		FragmentManager fm = pagerFragment.getChildFragmentManager();
		if(fm.getBackStackEntryCount() > 0) {
			popBackStack();
		}
		else {
			super.onBackPressed();
		}

	}
	
	/* Redirects hardware menu key button to each fragment that
	 * wants it
	 * (non-Javadoc)
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
//	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		boolean ret = false;
//		if(mSc.getActiveFragment() == mListFrag) {
//			ret = mListFrag.onKeyUp(keyCode, event);
//		}
//		return ret || super.onKeyUp(keyCode, event);
//	}
}
