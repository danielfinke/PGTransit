package com.finke.pgtransit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.finke.pgtransit.database.BusDatabaseHelper;
import com.finke.pgtransit.extensions.AdManager;
import com.finke.pgtransit.extensions.AppRater;
import com.finke.pgtransit.extensions.StackController;

/* Handles all activity life cycle/UI interactions for the
 * Schedules and Maps tabs */
public class MainActivity extends AppCompatActivity
	implements ActionBar.TabListener {
    private final static int LOCATION_PERMISSIONS_REQUEST_CODE = 1234;
	
	private boolean mRestored;
	// Container for Google AdMob advertisements
	private LinearLayout mAdCont;
	// Left tab view container
	private RoutesFragment mListFrag;
	// Right tab view container for MapFragment
	private MapFragment mMapFrag;
    // Used to automatically push map fragment after resume when location
    // permission is accepted
    private boolean mLocationPermissionAccepted = false;
	
	// Handles displaying banner ad in container
	// And the interstitial for times list
	private AdManager mAdManager;
	// Manages the back stack for Android back button presses
	private StackController mSc;
	
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
		
		// Start managing fragment back stack
		mSc = new StackController(this);
		
		// Activity stores state for all fragments
		// Might be on a different page than the default
		// If not, initialize the default tab fragments
		if(state == null) {
			mListFrag = new RoutesFragment();
			mMapFrag = new MapFragment();
			// First selected tab can be set automatically
			// This flag permits that
			mRestored = false;
		}
		// Restoring previous state
		else {
			// Need to restore back stack
			mSc.restoreState(state.getBundle("StackController"));
			// Restore tab 1 fragment, unless it was not
			// instantiated last time
			try {
				mListFrag = (RoutesFragment)mSc.getFragment(0, 0);
			}
			catch(IndexOutOfBoundsException ex) {
				mListFrag = new RoutesFragment();
			}
			// Restore tab 2 fragment, unless it was not
			// instantiated last time
			try {
				mMapFrag = (MapFragment)mSc.getFragment(1, 0);
			}
			catch(IndexOutOfBoundsException ex) {
				mMapFrag = new MapFragment();
			}
			// Automatically select default tab based on StackController
			// later during SetupActionBar
			mRestored = true;
		}
		// Setup top bar, title, home button
		setupActionBar();
		
		onFirstLaunch();
	}

	protected void onResume() {
		super.onResume();

		// Restart ads after app resumes if allowed
		if(adsEnabled()) {
			mAdManager.getNewAd();
		}
	}

    /**
     * At onResume there is a chance that fragments still have saved state
     * which will cause an error when performing fragment transactions.
     * Instead, perform them here. This issue seems to occur in API 19
     */
	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// StackController takes over back stack management
		mSc.start();

        // Callback for location has told us to push the map now
        if(mLocationPermissionAccepted) {
            mSc.push(mMapFrag, 1);
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
	
	/* Save state of the StackController
	 * Such as the current tab and back stack */
	public void onSaveInstanceState(Bundle state) {
		Bundle mScState = new Bundle();
		mSc.saveState(mScState);
		state.putBundle("StackController", mScState);
		mSc.stop();
		super.onSaveInstanceState(state);
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
	public StackController getStackController() { return mSc; }
	
	// Checks if ads were disabled by purchase
	public boolean adsEnabled() {
		SharedPreferences prefs = getSharedPreferences(getString(R.string.iab_prefs), 0);
        return !prefs.getBoolean("removedAds", false);
	}
	
	/* Initialize the ActionBar */
	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ActionBar.Tab routes = actionBar.newTab().setText("Routes");
		ActionBar.Tab more = actionBar.newTab().setText("Map");
		
		routes.setTabListener(this);
		more.setTabListener(this);
		actionBar.addTab(routes);
		actionBar.addTab(more);
		
		// Set the tab if a restore from state
		if(mRestored) {
			mRestored = false;
			getSupportActionBar().setSelectedNavigationItem(mSc.getStackNo());
		}
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
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		// Do not select first tab initially when tabs
		// added (during setupActionBar()) if StackController state restored
		if(mRestored) {
			return;
		}
		// StackController manages pushing/popping of tabs
		switch(tab.getPosition()) {
		case 0:
			mSc.push(mListFrag, 0);
			break;
		case 1:
            // Permissions check for API 23+
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        LOCATION_PERMISSIONS_REQUEST_CODE
                );
            }
            else {
                mSc.push(mMapFrag, 1);
            }
			break;
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		
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
	
	/* Back button tells StackController to pop, unless all back stack
	 * items have been popped, and then kills app instead
	 */
	public void onBackPressed() {
		if(mSc.onBackPressed()) {
			return;
		}
		else if(mSc.getStackCount() > 1) {
			mSc.pop();
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
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		if(mSc.getActiveFragment() == mListFrag) {
			ret = mListFrag.onKeyUp(keyCode, event);
		}
		return ret || super.onKeyUp(keyCode, event);
	}
}
