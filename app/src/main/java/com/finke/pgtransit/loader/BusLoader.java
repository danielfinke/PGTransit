package com.finke.pgtransit.loader;

import android.os.AsyncTask;

import com.finke.pgtransit.model.Bus;

public class BusLoader extends AsyncTask<Integer, Void, Bus> {

    private Callbacks mCallbacks;

    public BusLoader(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    protected Bus doInBackground(Integer... params) {
        try {
            return Bus.fetchFromDatabase(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bus bus) {
        if(bus != null) {
            mCallbacks.onBusLoaded(bus);
        }
    }

    public interface Callbacks {
        public void onBusLoaded(Bus bus);
    }

}
