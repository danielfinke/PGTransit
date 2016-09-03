package com.finke.pgtransit.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.finke.pgtransit.R;
import com.finke.pgtransit.model.Bus;

/* Populates row views for a list of routes, including their icons */
public class RoutesAdapter extends BaseAdapter {
	
	private List<Bus> items;
	private LayoutInflater inflater;
	private int mBgColor;
	private boolean mBgColorSet;

	public RoutesAdapter(Context context) {
		super();

		items = new ArrayList<Bus>();
		inflater = LayoutInflater.from(context);
		mBgColor = 0;
		mBgColorSet = false;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int index) {
		return items.get(index);
	}
	public Bus getBus(int index) {
		return (Bus)getItem(index);
	}

	@Override
	public long getItemId(int index) {
		return ((Bus)items.get(index)).getId();
	}

	@Override
	public View getView(int index, View reusable, ViewGroup parent) {
		reusable = inflater.inflate(R.layout.row_routes, parent, false);
        FrameLayout layout = (FrameLayout)reusable.findViewById(R.id.circle_frame_layout);
	    TextView circleNumber = (TextView)reusable.findViewById(R.id.circle_number);
	    TextView name = (TextView)reusable.findViewById(R.id.name);
	    TextView desc = (TextView)reusable.findViewById(R.id.description);
	    
	    Bus bus = getBus(index);

	    GradientDrawable background = (GradientDrawable)layout.getBackground();
        background.setColor(bus.getColor());
    	
	    circleNumber.setText(Integer.toString(bus.getNumber()));
    	name.setText(Integer.toString(bus.getNumber()) + " " + bus.getName());
    	desc.setText(bus.getDirectionName());
    	
    	if(mBgColorSet) {
    		reusable.setBackgroundColor(mBgColor);
    	}
    	
    	return reusable;
	}
	
	public void setItems(List<Bus> data) {
		items = data;
	}
	
	public void setColor(String color) {
		mBgColor = Color.parseColor(color);
		mBgColorSet = true;
	}
}
