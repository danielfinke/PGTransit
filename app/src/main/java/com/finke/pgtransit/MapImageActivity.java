package com.finke.pgtransit;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

/* Displays JPG full screen image of map, supplied by
 * PG Transit website
 */
public class MapImageActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_image);
		
		// Action bar not visible on this activity
		hideActionBar();
	}
	
	protected void onDestroy() {
		showActionBar();
		
		super.onDestroy();
	}
	
	private void hideActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
	}
	private void showActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.show();
	}
	
	// Go Back button closes activity
	public void goBack(View v) {
		this.finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
