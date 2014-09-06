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
import com.finke.pgtransit.model.TimeSlot;
import com.finke.pgtransit.model.TripNote;

/* Creates time slot rows given list of TimeSlots,
 * used both on the time list and for popup dialogs when clicking map pins
 */
public class TimesAdapter extends BaseAdapter {
	
	private List<TimeSlot> slots;
	private LayoutInflater inflater;

	public TimesAdapter(Context context) {
		super();

		slots = new ArrayList<TimeSlot>();
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
		return slots.size();
	}

	@Override
	public Object getItem(int index) {
		return slots.get(index);
	}
	public TimeSlot getTimeSlot(int index) {
		return (TimeSlot)getItem(index);
	}

	@Override
	public long getItemId(int index) {
		return ((TimeSlot)slots.get(index)).getId();
	}

	@Override
	public View getView(int index, View reusable, ViewGroup parent) {
		reusable = inflater.inflate(R.layout.row_times, parent, false);
	    TextView timeView = (TextView)reusable.findViewById(R.id.time1);
	    Button noteBtn = (Button)reusable.findViewById(R.id.tripNotes);
		
		styleDefault(reusable);

		TimeSlot slot = slots.get(index);
		Calendar time = slot.getCalendarTime();
		// Decrease minute by 1 to fix the style not being
		// applied when it is exactly a certain time
		time.roll(Calendar.MINUTE, false);
		// Check if the very first one is up next
		if(index == 0) {
			if(time.after(Calendar.getInstance())) {
				styleNext(reusable);
			}
		}
		// Now check subsequent time slots
		else if(index > 0) {
			Calendar time2 = getTimeSlot(index-1).getCalendarTime();
			if(time2.before(Calendar.getInstance()) && time.after(Calendar.getInstance())) {
				styleNext(reusable);
			}
			// Some times roll over to the next day, so need to check that as well
			if(slot.hasNextDayFlag()) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				// Last condition: the current row would have a time
				// earlier than now
				if(time2.before(cal) && time.after(cal)) {
					styleNext(reusable);
				}
			}
		}
    	
    	timeView.setText(new SimpleDateFormat("h:mm a", Locale.US).format(time.getTime()));
    	List<TripNote> notes = slot.getNotes();
    	
    	// Remove the views until they are needed
    	((RelativeLayout)reusable).removeView(reusable.findViewById(R.id.notesHeading));
		((RelativeLayout)reusable).removeView(reusable.findViewById(R.id.notes));
		
		// No notes = hide notes button
		if(notes.size() == 0) {
			noteBtn.setVisibility(View.GONE);
		}
		else {
			// Preserve time slot id in button tag
    		noteBtn.setTag(index);
    		// Displays notes button differently depending on whether
    		// there is 1 or more notes for this time slot
    		if(notes.size() == 1) {
    			noteBtn.setText("Note: " + notes.get(0).getName());
    		}
        	else if(notes.size() > 1) {
        		noteBtn.setText("Notes");
        	}
			
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
						notes.setText(slots.get((Integer)v.getTag()).getNotes().get(0).getDescription());
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
	
	public void setSlots(List<TimeSlot> data) {
		slots = data;
	}
}
