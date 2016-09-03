package com.finke.pgtransit;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Telephony;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.finke.pgtransit.extensions.AppRater;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/* Provides a list of additional options
 * Includes About, Share to social media/by email,
 * pay to remove ads, rate app, and report bugs
 */
public class MoreActivity extends AppCompatActivity {
	
	private int mScrollOffset;
	// Auth status for Facebook SDK
	private boolean mHasAuthenticated;
	// Dialog that displays copyright and disclaimer information
	private AboutDialogFragment mAboutDialog;
	// Lifecycle helper provided by FB SDK
	private UiLifecycleHelper mUiHelper;
	// Callback for FB SDK
	private Session.StatusCallback mCallback = 
	    new Session.StatusCallback() {
	    @Override
	    public void call(Session session, 
	            SessionState state, Exception exception) {
	        if(state.isOpened() && mHasAuthenticated) {
	        	publishFeedDialog();
	        }
	    }
	};
	// Helper for Google Billing
	private IabHelper mHelper;
	// (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more);
		
		mHasAuthenticated = false;
		mAboutDialog = null;
		
		mUiHelper = new UiLifecycleHelper(this, mCallback);
	    mUiHelper.onCreate(savedInstanceState);
	    
	    // iabKey broken up for security purposes to prevent purchase cracking
	    String iabKey = getString(R.string.iab_part_1) + getString(R.string.iab_part_2) +
	    		getString(R.string.iab_part_3) + getString(R.string.iab_part_4);
	    // Only configure purchase features if Google Play Services present on device
	    if(isGooglePlayServicesInstalled()) {
		    mHelper = new IabHelper(this, iabKey);
		    
		    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
	    	   public void onIabSetupFinished(IabResult result) {
	    	      if (!result.isSuccess()) {
	    	         // Oh noes, there was a problem.
	    	         //Log.d(TAG, "Problem setting up In-app Billing: " + result);
	    	      }            
	    	         // Hooray, IAB is fully set up!  
	    	   }
	    	});
		}
	    
	    // About dialog
		LinearLayout about = (LinearLayout)findViewById(R.id.aboutLinearLayout);
		about.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View arg0) {
				mAboutDialog = new AboutDialogFragment();
				mAboutDialog.show(getSupportFragmentManager(), null);
			}
			
		});
		
		// Share via SMS
		LinearLayout smsShare = (LinearLayout)findViewById(R.id.smsShareLinearLayout);
		smsShare.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View arg0) {
				String smsBody = getString(R.string.more_share_body)
						+ getString(R.string.app_bundle_url);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("smsto:"));
                sendIntent.putExtra("sms_body", smsBody);

                // In KitKat and beyond users can specify a default SMS app
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(MoreActivity.this);
                    sendIntent.setPackage(defaultSmsPackageName);
                }

				if(isCallable(sendIntent)) {
					startActivity(sendIntent);
				}
				else {
					Toast.makeText(MoreActivity.this, "No messaging app installed.", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		// Share via Facebook
		LinearLayout fbShare = (LinearLayout)findViewById(R.id.fbShareLinearLayout);
		fbShare.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View arg0) {
				// start Facebook Login
				if(Session.getActiveSession() == null || !Session.getActiveSession().isOpened()) {
					mHasAuthenticated = true;
					Session.openActiveSession(MoreActivity.this, true, mCallback);
				}
				else {
					publishFeedDialog();
				}
			}
			
		});
		
		// Share via Twitter
		LinearLayout tweetShare = (LinearLayout)findViewById(R.id.tweetShareLinearLayout);
		tweetShare.setOnClickListener(new OnClickListener() {
	
			@Override
			// http://stackoverflow.com/a/21186753
			public void onClick(View v) {
				String tweetUrl = 
					    String.format("https://twitter.com/intent/tweet?text=%s&url=%s",
					        urlEncode(getString(R.string.more_share_body)),
					        urlEncode(getString(R.string.app_bundle_url)));
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));
				
				// Narrow down to official Twitter app, if available:
				List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
				for (ResolveInfo info : matches) {
				    if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
				        intent.setPackage(info.activityInfo.packageName);
				    }
				}

				// Only launch Twitter if possible
				if(isCallable(intent)) {
					startActivity(intent);
				}
				else {
					Toast.makeText(MoreActivity.this, "Twitter app not installed.", Toast.LENGTH_SHORT).show();
				}
				
				/* Broken code on more recent Twitter versions */
				/*String body = "Hey! You should check out this awesome transit app for Android:\n"
						+ getString(R.string.app_bundle_url);
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setClassName("com.twitter.android",
		                "com.twitter.applib.composer.TextFirstComposerActivity"); 
				sendIntent.setType("text/*");
				sendIntent.putExtra(Intent.EXTRA_TEXT, body);
				if(isCallable(sendIntent)) {
					startActivity(sendIntent);
				}
				else {
					sendIntent.setClassName("com.twitter.android", "com.twitter.applib.PostActivity");
					if(isCallable(sendIntent)) {
						startActivity(sendIntent);
					}
					else {
						sendIntent.setClassName("com.twitter.android", "com.twitter.android.PostActivity");
						if(isCallable(sendIntent)) {
							startActivity(sendIntent);
						}
						else {
							Toast.makeText(MoreActivity.this, "Twitter app not installed.", Toast.LENGTH_SHORT).show();
						}
					}
				}*/
			}
			
			/* Safely encode URL parameters */
			public String urlEncode(String s) {
			    try {
			        return URLEncoder.encode(s, "UTF-8");
			    }
			    catch (UnsupportedEncodingException e) {
			        Log.wtf("UTF-8 should always be supported", e);
			        throw new RuntimeException("URLEncoder.encode() failed for " + s);
			    }
			}
			
		});
		
		// Share via email
		LinearLayout emailShare = (LinearLayout)findViewById(R.id.emailShareLinearLayout);
		emailShare.setOnClickListener(new OnClickListener() {
	
			@Override
			// Launch some email client to share
			public void onClick(View arg0) {
				String body = getString(R.string.more_share_body)
						+ getString(R.string.app_bundle_url);
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
			            "mailto", "", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PG Transit Android App");
				emailIntent.putExtra(Intent.EXTRA_TEXT, body);
				startActivity(Intent.createChooser(emailIntent, "Share by email"));
			}
			
		});
		
		/*
		 * For IAB services
		 * Don't show GPS not installed warning if eval to true
		 */
		if(isGooglePlayServicesInstalled()) {
			TextView premiumTitle = (TextView)findViewById(R.id.more_iab_notice);
			LinearLayout moreContainer = (LinearLayout)findViewById(R.id.moreContainer);
			moreContainer.removeView(premiumTitle);
		}
		
		// Either ad a click listener or remove paid ads feature
		// depending on adsEnabled() status
		LinearLayout removeAds = (LinearLayout)findViewById(R.id.iabRemoveAdsLinearLayout);
		if(isGooglePlayServicesInstalled() && adsEnabled()) {
			removeAds.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View arg0) {
					/* TODO: for security, generate your payload here for verification. See the comments on
			         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
			         *        an empty string, but on a production app you should carefully generate this. */
			        String payload = "";
			        
			        try {
						int response = mHelper.mService.consumePurchase(3, getPackageName(), "inapp:"+getPackageName()+":android.test.purchased");
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
			        mHelper.launchPurchaseFlow(MoreActivity.this, getString(R.string.iab_sku_remove_ads), RC_REQUEST,
			                mPurchaseFinishedListener, payload);
				}
				
			});
		}
		// Remove pay option if paid
		else {
			LinearLayout moreContainer = (LinearLayout)findViewById(R.id.moreContainer);
			moreContainer.removeView(removeAds);
		}
		
		// Restore previous ad removal purchase
		LinearLayout restorePurchases = (LinearLayout)findViewById(R.id.iabRestorePurchasesLinearLayout);
		if(isGooglePlayServicesInstalled()) {
			restorePurchases.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View arg0) {
					mHelper.queryInventoryAsync(mGotInventoryListener);
				}
				
			});
		}
		// No GPS, can't restore purchases
		else {
			LinearLayout moreContainer = (LinearLayout)findViewById(R.id.moreContainer);
			moreContainer.removeView(restorePurchases);
		}
		/*
		 * End IAB services
		 */
		
		// Rate app using AppRater library
		LinearLayout rateApp = (LinearLayout)findViewById(R.id.rateAppLinearLayout);
		rateApp.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View arg0) {
				SharedPreferences prefs = arg0.getContext().getSharedPreferences("apprater", 0);
				SharedPreferences.Editor editor = prefs.edit();
				AppRater.showRateDialog(arg0.getContext(), editor);
				editor.commit();
			}
			
		});
		
		// Report bugs via email intent
		LinearLayout reportBugs = (LinearLayout)findViewById(R.id.reportBugsLinearLayout);
		reportBugs.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View arg0) {
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
			            "mailto", arg0.getContext().getString(R.string.dev_email), null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "<PG Transit> Bug Report");
				startActivity(Intent.createChooser(emailIntent, "Send email bug report"));
				
				/*Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("message/rfc822");
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "<PG Transit> Bug Report");
				if(isCallable(sendIntent)) {
					startActivity(sendIntent);
				}
				else {
					Toast.makeText(getActivity(), "No email app installed.", Toast.LENGTH_SHORT).show();
				}*/
			}
			
		});
		
		final ScrollView listView = (ScrollView)findViewById(R.id.moreScrollView);
		listView.post(new Runnable() {
			public void run() {
				listView.scrollTo(listView.getScrollX(), mScrollOffset);
			}
		});
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    mUiHelper.onPause();
	}
	
	public void onResume() {
		super.onResume();
		
		// No longer needed since More is its own Activity
		/*Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }*/
		
		mUiHelper.onResume();
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("More");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    mUiHelper.onDestroy();
	    
	    if (mHelper != null) mHelper.dispose();
	    mHelper = null;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    mUiHelper.onSaveInstanceState(outState);
	}
	
	// Helper consumes activity result, unless it's not for Google Billing
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getUiHelper().onActivityResult(requestCode, resultCode, data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

	// Home button closes More activity
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// No longer needed since More is its own Activity
	/*@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    mMoreFrag.getUiHelper().onActivityResult(requestCode, resultCode, data);
	    mMoreFrag.onActivityResult(requestCode, resultCode, data);
	}*/
	
	/*private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
//	    	publishStory();
	    	publishFeedDialog();
        }
	}*/
	
	// Returns true if an intent has applicable handlers on the device
	private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        return list.size() > 0;
	}
	
	public UiLifecycleHelper getUiHelper() { return mUiHelper; }
	
	public int getScrollIndex() {
		return 0;
	}
	
	public int getScrollOffset() {
		return ((ScrollView)findViewById(R.id.moreScrollView)).getScrollY();
	}
	
	public void setScrollOffset(int index, int offset) {
		mScrollOffset = offset;
	}
	
	/* Creates a Facebook SDK post popup to share this app to Facebook */
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", "PG Transit Android app");
	    params.putString("description", "The PG Transit app for Android phones puts Prince George's bus schedules in the palm of your hand.");
	    params.putString("link", getString(R.string.app_bundle_url));

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

				@Override
				public void onComplete(Bundle values, FacebookException error) {
					if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(MoreActivity.this,
	                            "Posted to Facebook",
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(MoreActivity.this.getApplicationContext(), 
	                            "Publish cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(MoreActivity.this.getApplicationContext(), 
	                        "Publish cancelled", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(MoreActivity.this.getApplicationContext(), 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
				}

	        })
	        .build();
	    feedDialog.show();
	}
	
	// Checks to see if GPS is installed on the device
	public boolean isGooglePlayServicesInstalled()
	  {
	      return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ==
	    		  ConnectionResult.SUCCESS;
	  }
	
	// Checks if ads were disabled by purchase
	public boolean adsEnabled() {
		SharedPreferences prefs = getSharedPreferences(getString(R.string.iab_prefs), 0);
        return !prefs.getBoolean("removedAds", false);
	}
	
	/* Handle completed ad-removal purchase workflow */
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        	// if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
            
            if(result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
            	Toast.makeText(MoreActivity.this,
                		"You have already purchased this item. Please choose the Restore Purchases option below.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            else if (result.isFailure()) {
                Toast.makeText(MoreActivity.this,
                		"Error purchasing. Did you cancel?",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                Toast.makeText(MoreActivity.this,
                		"Error purchasing. Authenticity verification failed.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            /* If a successful purchase of remove-ads,
             * set the prefs variable to make it permanent and
             * remove the pay option from the More page
             */
            if (purchase.getSku().equals(getString(R.string.iab_sku_remove_ads))) {
                // Register the removal of ads
            	SharedPreferences prefs = getSharedPreferences(getString(R.string.iab_prefs), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("removedAds", true);
                editor.commit();
                //((MainActivity)getActivity()).getAdManager().removeAd();
                LinearLayout moreContainer = (LinearLayout)findViewById(R.id.moreContainer);
                LinearLayout removeAds = (LinearLayout)findViewById(R.id.iabRemoveAdsLinearLayout);
        		moreContainer.removeView(removeAds);
            }
        }
    };
    
    /* Check for paid feature to remove ads, and check its purchase status
     * If paid, set prefs value and remove pay option
     */
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
            	Toast.makeText(MoreActivity.this,
                		"Error when trying to restore purchase. If you own this item, please email the developer at danielfinke2011@gmail.com.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            Purchase removeAdsPurchase = inventory.getPurchase(getString(R.string.iab_sku_remove_ads));
            boolean adsRemoved = (removeAdsPurchase != null && verifyDeveloperPayload(removeAdsPurchase));
            
            if(adsRemoved) {
            	/* Set prefs value to persist paid status */
	            SharedPreferences prefs = getSharedPreferences(getString(R.string.iab_prefs), 0);
	            SharedPreferences.Editor editor = prefs.edit();
	            boolean alreadyRemoved = prefs.getBoolean("removedAds", false);
	            editor.putBoolean("removedAds", true);
	            editor.commit();
	            
	            /* Remove pay option from More page */
	            if(!alreadyRemoved) {
		            //((MainActivity)getActivity()).getAdManager().removeAd();
		            LinearLayout moreContainer = (LinearLayout)findViewById(R.id.moreContainer);
	                LinearLayout removeAds = (LinearLayout)findViewById(R.id.iabRemoveAdsLinearLayout);
	        		moreContainer.removeView(removeAds);
	            }
	            
	            Toast.makeText(MoreActivity.this,
                		"Your purchases have been restored.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
            	Toast.makeText(MoreActivity.this,
                		"You have not purchased anything. If this is incorrect, please email me at danielfinke2011@gmail.com.",
                        Toast.LENGTH_LONG).show();
            }
        }
    };
    
    /** Verifies the developer payload of a purchase. */
    /** Currently no validation **/
    boolean verifyDeveloperPayload(Purchase p) {
        //String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }
	
    /** No longer needed with recent FB SDK **/
	/*private void publishStory() {
	    Session session = Session.getActiveSession();

	    if (session != null){

	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permissions)) {
	            Session.NewPermissionsRequest newPermissionsRequest = new Session
	                    .NewPermissionsRequest(this, PERMISSIONS);
	            session.requestNewPublishPermissions(newPermissionsRequest);
	            return;
	        }

	        Bundle postParams = new Bundle();
	        postParams.putString("name", "Test");
	        //postParams.putString("caption", "Build great social apps and get more installs.");
	        postParams.putString("description", "Testing");
	        //postParams.putString("link", "https://developers.facebook.com/android");
	        //postParams.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

	        Request.Callback callback= new Request.Callback() {
	            public void onCompleted(Response response) {
	                JSONObject graphResponse = response
	                                           .getGraphObject()
	                                           .getInnerJSONObject();
	                String postId = null;
	                try {
	                    postId = graphResponse.getString("id");
	                } catch (JSONException e) {
//	                    Log.i(TAG,
//	                        "JSON error "+ e.getMessage());
	                }
	                FacebookRequestError error = response.getError();
	                if (error != null) {
	                    Toast.makeText(getActivity()
	                         .getApplicationContext(),
	                         error.getErrorMessage(),
	                         Toast.LENGTH_SHORT).show();
	                    } else {
	                        Toast.makeText(getActivity()
	                             .getApplicationContext(), 
	                             postId,
	                             Toast.LENGTH_LONG).show();
	                }
	            }
	        };

	        Request request = new Request(session, "me/feed", postParams, 
	                              HttpMethod.POST, callback);

	        RequestAsyncTask task = new RequestAsyncTask(request);
	        task.execute();
	    }

	}
	
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}*/
	
}
