package com.finke.pgtransit.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.finke.pgtransit.R;
import com.finke.pgtransit.model.Stop;

/* Creates row views for a list of stops */
public class StopsAdapter extends BaseAdapter {
	
	private List<Stop> items;
	private LayoutInflater inflater;

	public StopsAdapter(Context context) {
		super();

		items = new ArrayList<Stop>();
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
	public Stop getStop(int index) {
		return (Stop)getItem(index);
	}

	@Override
	// TODO: translate String ids into longs to cover BaseAdapter data type
	public long getItemId(int index) {
		//return ((Stop)items.get(index)).getId();
		return 0;
	}

	@Override
	public View getView(int index, View reusable, ViewGroup parent) {
		reusable = inflater.inflate(R.layout.row_stops, parent, false);
	    TextView name = (TextView)reusable.findViewById(R.id.name);
    	name.setText(items.get(index).getName());
    	return reusable;
	}
	
	public void setItems(List<Stop> data) {
		items = data;
	}
}
