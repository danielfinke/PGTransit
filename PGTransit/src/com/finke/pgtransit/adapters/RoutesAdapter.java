package com.finke.pgtransit.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.finke.pgtransit.R;
import com.finke.pgtransit.model.Bus;

/* Populates row views for a list of routes, including their icons */
public class RoutesAdapter extends BaseAdapter {
	
	private List<Bus> items;
	private LayoutInflater inflater;

	public RoutesAdapter(Context context) {
		super();

		items = new ArrayList<Bus>();
		inflater = LayoutInflater.from(context);
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
	    ImageView icon = (ImageView)reusable.findViewById(R.id.imageView1);
	    TextView name = (TextView)reusable.findViewById(R.id.name);
	    TextView desc = (TextView)reusable.findViewById(R.id.description);
	    
	    Bus bus = getBus(index);
	    
	    // Only change icon if it has one
    	if(bus.getIcon() != null) {
	    	icon.setImageDrawable(reusable.getResources().getDrawable(reusable.getResources().getIdentifier("drawable/" + bus.getIcon(), "drawable", reusable.getContext().getPackageName())));
    	}
    	else {
    		((ViewGroup)reusable).removeView(icon);
    	}
    	
    	name.setText(bus.getOwner());
    	desc.setText(bus.getDescription());
    	return reusable;
	}
	
	public void setItems(List<Bus> data) {
		items = data;
	}
}
