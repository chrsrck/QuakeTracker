package com.chrsrck.quaketracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chrsrck on 9/27/17.
 */

public class FeedReaderDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedContractUSGS.FeedEntry.TABLE_NAME + " (" +
                    FeedContractUSGS.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedContractUSGS.FeedEntry.ID_COLUMN + " TEXT," +
                    FeedContractUSGS.FeedEntry.LAT_COLUMN + " DOUBLE," +
                    FeedContractUSGS.FeedEntry.LONG_COLUMN + " DOUBLE," +
                    FeedContractUSGS.FeedEntry.TITLE_COLUMN + " TEXT," +
                    FeedContractUSGS.FeedEntry.MAG_COLUMN + " DOUBLE," +
                    FeedContractUSGS.FeedEntry.PLACE_COLUMN + " TEXT," +
                    FeedContractUSGS.FeedEntry.TIME_COLUMN + " LONG," +
                    FeedContractUSGS.FeedEntry.SIG_NUM_COLUMN + " INTEGER," +
                    FeedContractUSGS.FeedEntry.DURATION_MIN_COLUMN + " DOUBLE," +
                    FeedContractUSGS.FeedEntry.TYPE_EVENT_COLUMN + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedContractUSGS.FeedEntry.TABLE_NAME;


    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
