package com.finke.pgtransit.extensions;

import android.app.Activity;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.finke.pgtransit.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

/* Manages all Google AdMob functions for the app */
public class AdManager extends AdListener {

	// Container for a banner ad
    private LinearLayout _container;
    // Activity to contain banner ad
    // Also activity to display interstitial on top of
    private Activity _activity;
    // Banner ad view instance
    private AdView _admobBanner;
    // Interstitial ad view instance
    private InterstitialAd interstitial;
    
    public AdManager(LinearLayout container, Activity activity) {
        _container = container;
        _activity = activity;
    }

    // Display a banner ad in the LinearLayout passed in the constructor
    public void getNewAd() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        
        _admobBanner = new AdView(_activity);
        _admobBanner.setAdSize(AdSize.SMART_BANNER);
        _admobBanner.setAdUnitId(_activity.getString(R.string.ad_unit_id));
        AdRequest.Builder adReq = new AdRequest.Builder();
        adReq.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adReq.addTestDevice(_activity.getString(R.string.test_device_1));
        _container.addView(_admobBanner, params);
        _admobBanner.loadAd(adReq.build());
    }
    
    // Prepare a full-screen advertisement for later display
    // Call displayInterstitialIfReady() to display
    public void getNewInterstitialAd() {
    	interstitial = new InterstitialAd(_activity);
    	interstitial.setAdUnitId(_activity.getString(R.string.ad_unit_id));
    	AdRequest.Builder adReq = new AdRequest.Builder();
        adReq.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adReq.addTestDevice(_activity.getString(R.string.test_device_1));
        interstitial.loadAd(adReq.build());
        interstitial.setAdListener(this);
    }

    // Remove the banner ad from the ad container given in constructorr
    public void removeAd() {
        _container.removeAllViews();
        _admobBanner.destroy();
        _admobBanner = null;
    }
    
    // Display full-screen ad on top of the activity
    public void displayInterstitialIfReady() {
    	if(interstitial.isLoaded()) {
    		interstitial.show();
    	}
    }
}