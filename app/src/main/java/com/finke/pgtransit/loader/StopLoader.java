package com.finke.pgtransit.loader;

import android.os.AsyncTask;

import com.finke.pgtransit.model.Stop;

public class StopLoader extends AsyncTask<String, Void, Stop> {

    private Callbacks mCallbacks;

    public StopLoader(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    protected Stop doInBackground(String... params) {
        try {
            return Stop.fetchFromDatabase(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Stop stop) {
        if(stop != null) {
            mCallbacks.onStopLoaded(stop);
        }
    }

    public interface Callbacks {
        public void onStopLoaded(Stop stop);
    }

}
