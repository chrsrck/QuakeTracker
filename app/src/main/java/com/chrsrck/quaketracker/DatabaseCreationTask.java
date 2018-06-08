package com.chrsrck.quaketracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chrsrck on 9/27/17.
 */

public class DatabaseCreationTask extends AsyncTask<JSONObject, Void, SQLiteDatabase> {
    private static final String TAG = DatabaseCreationTask.class.getSimpleName();
    public AsyncResponse mAsyncResponse = null;
    private Context mContext;
    private FeedReaderDbHelper mDbHelper;
    private SQLiteDatabase db;

    public DatabaseCreationTask(FeedReaderDbHelper dbHelper, AsyncResponse listener, Context context) {
        mAsyncResponse = listener;
        mDbHelper = dbHelper;
        mContext = context;
    }

    @Override
    protected SQLiteDatabase doInBackground(JSONObject... jsonObjects) {
        try {
            JSONObject jsonObjectRequested = jsonObjects[0];
            db = mDbHelper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            if(jsonObjectRequested != null) {
                try {
                    JSONArray eventArray = jsonObjectRequested.getJSONArray("features");

                    for (int i = 0; i < eventArray.length(); i++) {
                        JSONObject eventJSONObject = eventArray.getJSONObject(i);
                        JSONObject propertiesJSONObject =  eventJSONObject.getJSONObject("properties");

                        contentValues.put(FeedContractUSGS.FeedEntry.ID_COLUMN,
                                eventJSONObject.getString(FeedContractUSGS.FeedEntry.ID_COLUMN));
                        contentValues.put(FeedContractUSGS.FeedEntry.LAT_COLUMN,
                                eventJSONObject.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1));
                        contentValues.put(FeedContractUSGS.FeedEntry.LONG_COLUMN,
                                eventJSONObject.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0));

                        contentValues.put(FeedContractUSGS.FeedEntry.TITLE_COLUMN,
                                propertiesJSONObject.getString(FeedContractUSGS.FeedEntry.TITLE_COLUMN));
                        contentValues.put(FeedContractUSGS.FeedEntry.MAG_COLUMN,
                                propertiesJSONObject.getDouble(FeedContractUSGS.FeedEntry.MAG_COLUMN));
                        contentValues.put(FeedContractUSGS.FeedEntry.PLACE_COLUMN,
                                propertiesJSONObject.getString(FeedContractUSGS.FeedEntry.PLACE_COLUMN));
                        contentValues.put(FeedContractUSGS.FeedEntry.TIME_COLUMN,
                                propertiesJSONObject.getLong(FeedContractUSGS.FeedEntry.TIME_COLUMN));
                        contentValues.put(FeedContractUSGS.FeedEntry.SIG_NUM_COLUMN,
                                propertiesJSONObject.getInt(FeedContractUSGS.FeedEntry.SIG_NUM_COLUMN));

                        if (propertiesJSONObject.has("dmin") && !propertiesJSONObject.isNull("dmin")) {
                            double duration = propertiesJSONObject.getDouble(FeedContractUSGS.FeedEntry.DURATION_MIN_COLUMN);
                            contentValues.put(FeedContractUSGS.FeedEntry.DURATION_MIN_COLUMN,
                                    duration);
                        }
                        else {
                            contentValues.put(FeedContractUSGS.FeedEntry.DURATION_MIN_COLUMN,
                                    0);
                        }

                        contentValues.put(FeedContractUSGS.FeedEntry.TYPE_EVENT_COLUMN,
                                propertiesJSONObject.getString(FeedContractUSGS.FeedEntry.TYPE_EVENT_COLUMN));

                        long rowID = db.insert(FeedContractUSGS.TABLE_NAME, null, contentValues);
                        contentValues.clear();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
            return null;
        }
        return db;
    }

    @Override
    protected void onPostExecute(SQLiteDatabase result) {
        mAsyncResponse.databaseCreationProcessFinish(result);
    }
}
