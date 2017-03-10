package com.chrsrck.quaketracker;

import android.os.AsyncTask;
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

public class DataFetchTask extends AsyncTask<String, Void, JSONObject> {

    public final String TAG = getClass().getSimpleName();
    public AsyncResponse mAsyncResponse = null;
    private OkHttpClient client;

    public DataFetchTask(AsyncResponse listener) {
        client = new OkHttpClient();
        mAsyncResponse = listener;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        try {
                Request request = new Request.Builder().url(strings[0]).build();
                Response response = client.newCall(request).execute();
//                Log.d(TAG, response.body().string());
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            }
            catch (IOException | JSONException e) {
                Log.e(TAG, "" + e.getLocalizedMessage());
            }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        mAsyncResponse.processFinish(result);
    }


}
