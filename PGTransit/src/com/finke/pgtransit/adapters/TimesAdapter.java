package com.finke.pgtransit.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.finke.pgtransit.R;
import com.finke.pgtransit.model.TimeInterface;

/* Creates time slot rows given list of TimeSlots,
 * used both on the time list and for popup dialogs when clicking map pins
 */
public class TimesAdapter extends BaseAdapter {
	
	private List<TimeInterface> times;
	private LayoutInflater inflater;

	public TimesAdapter(Context context) {
		super();

		times = new ArrayList<TimeInterface>();
		inflater = LayoutInflater.from(context);
	}
	
	/* Set the style of a row to be plain */
	private void styleDefault(View v) {
		TextView timeView = (TextView)v.findViewById(R.id.time1);
		TextView nextArrivalView = (TextView)v.findViewById(R.id.nextArrival1);
		
		v.setBackgroundColor(Color.WHITE);
		timeView.setTextSize(18);
		nextArrivalView.setVisibility(View.INVISIBLE);
	}
	
	/* Increase text size and highlight background
	 * for the row that represents the upcoming stop time
	 */
	private void styleNext(View v) {
		TextView timeView = (TextView)v.findViewById(R.id.time1);
		TextView nextArrivalView = (TextView)v.findViewById(R.id.nextArrival1);
		
		v.setBackgroundColor(Color.parseColor("#c0c0c0"));
		timeView.setTextSize(24);
		nextArrivalView.setVisibility(View.VISIBLE);
	}

	@Override
	public int getCount() {
		return times.size();
	}

	@Override
	public Object getItem(int index) {
		return times.get(index);
	}
	public TimeInterface getTimeSlot(int index) {
		return (TimeInterface)getItem(index);
	}

	@Override
	public long getItemId(int index) {
		return ((TimeInterface)times.get(index)).getId();
	}

	@Override
	public View getView(int index, View reusable, ViewGroup parent) {
		reusable = inflater.inflate(R.layout.row_times, parent, false);
	    TextView timeView = (TextView)reusable.findViewById(R.id.time1);
	    Button noteBtn = (Button)reusable.findViewById(R.id.tripNotes);
		
		styleDefault(reusable);

		TimeInterface slot = times.get(index);
		Calendar time = slot.getCalendarTime();
		int hour = time.get(Calendar.HOUR_OF_DAY);
		int min = time.get(Calendar.MINUTE);
		Calendar curTime = Calendar.getInstance();
		int curH = curTime.get(Calendar.HOUR_OF_DAY);
		int curM = curTime.get(Calendar.MINUTE);
//		// Decrease minute by 1 to fix the style not being
//		// applied when it is exactly a certain time
//		if(time.get(Calendar.MINUTE) == 0) {
//			time.roll(Calendar.HOUR_OF_DAY, false);
//		}
//		time.roll(Calendar.MINUTE, false);
		// Check if the very first one is up next
		if(index == 0) {
//			if(time.after(Calendar.getInstance())) {
//				styleNext(reusable);
//			}
			if(hour > curH || hour == curH && min >= curM) {
				styleNext(reusable);
			}
		}
		// Now check subsequent time slots
		else if(index > 0) {
			Calendar time2 = getTimeSlot(index-1).getCalendarTime();
			int lastH = time2.get(Calendar.HOUR_OF_DAY);
			int lastM = time2.get(Calendar.MINUTE);
			
			if((lastH < curH || lastH == curH && lastM < curM) &&
					(hour > curH || hour == curH && min >= curM)) {
				styleNext(reusable);
			}
//			if(time2.before(Calendar.getInstance()) && time.after(Calendar.getInstance())) {
//				styleNext(reusable);
//			}
		}
    	
		// Undo the style fix
//		if(time.get(Calendar.MINUTE) == 59) {
//			time.roll(Calendar.HOUR_OF_DAY, true);
//		}
//		time.roll(Calendar.MINUTE, true);
    	timeView.setText(new SimpleDateFormat("h:mm a", Locale.US).format(time.getTime()));
    	
    	// Remove the views until they are needed
    	((RelativeLayout)reusable).removeView(reusable.findViewById(R.id.notesHeading));
		((RelativeLayout)reusable).removeView(reusable.findViewById(R.id.notes));
		
		// No notes = hide notes button
		if(slot.getNoteCode() == null) {
			noteBtn.setVisibility(View.GONE);
		}
		else {
			// Preserve time slot id in button tag
    		noteBtn.setTag(index);
    		// Displays notes button differently depending on whether
    		// there is 1 or more notes for this time slot
//    		if(notes.size() == 1) {
    			noteBtn.setText("Note: " + slot.getNoteCode());
//    		}
//        	else if(notes.size() > 1) {
//        		noteBtn.setText("Notes");
//        	}
			
    		// Handle clicks on the Note buttons
    		noteBtn.setOnClickListener(new OnClickListener() {
				@Override
				// Expand/collapse the current row and include the notes details
				public void onClick(View v) {
					ViewGroup parent = (ViewGroup)v.getParent();
					TextView notesH = (TextView)parent.findViewById(R.id.notesHeading);
					TextView notes = (TextView)parent.findViewById(R.id.notes);
					// Expand
					if(notesH == null || notes == null) {
						RelativeLayout temp = (RelativeLayout)inflater.inflate(R.layout.row_times, parent, false);
						notesH = (TextView)temp.findViewById(R.id.notesHeading);
						notes = (TextView)temp.findViewById(R.id.notes);
						temp.removeView(notesH);
						temp.removeView(notes);
						parent.addView(notesH);
						parent.addView(notes);
						// Fetching the data using earlier preserved time slot id
						notes.setText(times.get((Integer)v.getTag()).getNote());
					}
					// Collapse
					else {
						parent.removeView(notesH);
						parent.removeView(notes);
					}
				}
    		});
    	}
    	
    	return reusable;
	}
	
	public void setTimes(List<TimeInterface> data) {
		times = data;
	}
}
