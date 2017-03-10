package com.chrsrck.quaketracker;


import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by chrsrck on 3/10/17.
 */

public class DataFetcher {

    public final String TAG = getClass().getSimpleName();
    private static final String USGS_URL
            = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2014-01-01&endtime=2014-01-02";

    private OkHttpClient client;
    private static Response response;

    public DataFetcher() {
        client = new OkHttpClient();
    }

    public JSONObject fetchEarthquakes() {
        return fetch(USGS_URL);
    }

    private JSONObject fetch(String pullURL) {
        try {
            Request request = new Request.Builder().url(pullURL).build();
            response = client.newCall(request).execute();
            return new JSONObject(response.body().string());
        }
        catch (IOException | JSONException e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        return null;
    }
}
