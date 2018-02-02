package com.chrsrck.quaketracker;

import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;

/**
 * Created by chrsrck on 3/10/17.
 */

public interface AsyncResponse {
    void dataFetchProcessFinish(JSONObject result);
    void databaseCreationProcessFinish(SQLiteDatabase result);
}
