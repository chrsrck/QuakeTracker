package com.chrsrck.quaketracker;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.data.kml.KmlLayer;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        AsyncResponse {

    public final String TAG = getClass().getSimpleName();
    public static final String MAG_ALL_HOUR_URL = FeedContractUSGS.MAG_ALL_HOUR_URL;
    public static final String MAG_2_HALF_DAY_URL = FeedContractUSGS.MAG_2_HALF_DAY_URL;
    public static final String MAG_4_HALF_WEEK_URL = FeedContractUSGS.MAG_4_HALF_WEEK_URL;
    public static final String MAG_SIGNIFICANT_MONTH_URL = FeedContractUSGS.MAG_SIGNIFICANT_MONTH_URL;


    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private DataFetchTask mDataFetchTask;
    private DatabaseCreationTask mDatabaseCreationTask;

    private JSONObject mJSONObjectData;
    private HashSet<Marker> quakeMarkers;
    private FeedReaderDbHelper mDbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // code for asynctask
        mapFragment = null;
        quakeMarkers = new HashSet<Marker>(30);
        mDbHelper = new FeedReaderDbHelper(this);

        mDataFetchTask = new DataFetchTask(this);
        mDataFetchTask.execute(FeedContractUSGS.SIG_EQ_URL);
    }

    public void onDestroy() {

        if(!mDataFetchTask.isCancelled()) {
            mDataFetchTask.cancel(true);
        }

        if(!mDatabaseCreationTask.isCancelled()) {
            mDatabaseCreationTask.cancel(true);
        }

        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.all_mag_hour) {
            earthquakeOptionSelected(MAG_ALL_HOUR_URL);
            Toast toast = Toast.makeText(this, "Mag all Hour", Toast.LENGTH_SHORT);
            toast.show();
        } else if (id == R.id.two_half_mag_day) {
            earthquakeOptionSelected(MAG_2_HALF_DAY_URL);
            Toast toast = Toast.makeText(this, "2.5+ Day", Toast.LENGTH_SHORT);
            toast.show();

        } else if (id == R.id.four_half_mag_week) {
            earthquakeOptionSelected(MAG_4_HALF_WEEK_URL);
            Toast toast = Toast.makeText(this, "4.5+ Week", Toast.LENGTH_SHORT);
            toast.show();

        } else if (id == R.id.significant_mag_month) {
            earthquakeOptionSelected(MAG_SIGNIFICANT_MONTH_URL);
            Toast toast = Toast.makeText(this, "Significant Month", Toast.LENGTH_SHORT);
            toast.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady called");
        mMap = googleMap;

        // setting map style
        mMap.getUiSettings().setMapToolbarEnabled(false);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // adding plate boundaries
        addPlatesLayer();

        // adding earthquake points
        LatLng initialPos = new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPos));
    }

    @Override
    public void dataFetchProcessFinish(JSONObject result) {
        Log.d(TAG, "dataFetchProcessFinish called");
        mJSONObjectData = result;
        mDatabaseCreationTask = new DatabaseCreationTask(mDbHelper, this);
        mDatabaseCreationTask.execute(mJSONObjectData);

//        if(mJSONObjectData != null) {
//            Log.d(TAG, "dataFetchProcessFinish: jsonObject not null in process finish");
//            Log.d(TAG, "Has features: " + (mJSONObjectData.has("features")));
//        }
//        else {
//            Log.d(TAG, "dataFetchProcessFinish: jsonObehct Null in process finish");
//        }

        if (mapFragment == null) {
            mapFragment = (SupportMapFragment) SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
            mapFragment.getMapAsync(this);
            Log.d(TAG, "MAP FRAG NULL");
        }
        else {
            mDataFetchTask.cancel(true);
        }
    }

    @Override
    public void databaseCreationProcessFinish(SQLiteDatabase result) {
        Log.d(TAG, "databaseCreationProcessFinish called");
        database = result;
        updateEarthquakesOnMap();
    }


    private void earthquakeOptionSelected(String option) {
        Log.d(TAG, "earthquakeOptionSelectedCalled called");
        Iterator<Marker> markerIterator = quakeMarkers.iterator();
        while (markerIterator.hasNext()) {
            Marker marker = markerIterator.next();
            marker.remove();
        }
        quakeMarkers.clear();

        mDataFetchTask.cancel(true);

        mDataFetchTask = new DataFetchTask(this);
        mDataFetchTask.execute(option);
    }


    private LatLng updateEarthquakesOnMap() {
        Log.d(TAG, "updateEQOnMap called");
        double latitude = 0;
        double longitude = 0;
        String eqTitle;
        LatLng quakePos = new LatLng(latitude, longitude);

        String[] projection = {
            FeedContractUSGS.FeedEntry._ID,
                    FeedContractUSGS.FeedEntry.TITLE_COLUMN,
                    FeedContractUSGS.FeedEntry.LONG_COLUMN,
            FeedContractUSGS.FeedEntry.LAT_COLUMN
        };

        if (database != null && database.isOpen()) {
            String selection = FeedContractUSGS.FeedEntry.TYPE_EVENT_COLUMN + " = ?";
            String[] selectionArgs = {"explosion"};
            String sortOrder = FeedContractUSGS.FeedEntry._ID + " DESC";

            Cursor cursor = database.query(
                    FeedContractUSGS.TABLE_NAME,                     // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );


            while (cursor.moveToNext()) {
                eqTitle = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedContractUSGS.FeedEntry.TITLE_COLUMN));
                latitude = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(FeedContractUSGS.FeedEntry.LAT_COLUMN));
                longitude = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(FeedContractUSGS.FeedEntry.LONG_COLUMN));
                quakePos = new LatLng(latitude, longitude);
                        Marker addedMarker = mMap.addMarker(new MarkerOptions().position(quakePos).title(eqTitle)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.eq_marker)));
                    quakeMarkers.add(addedMarker);
            }
        }
        else {
            Toast toast = Toast.makeText(this, "Unable to retrieve Data", Toast.LENGTH_LONG);
            toast.show();
        }
        return quakePos;
    }

    private void addPlatesLayer() {
        GeoJsonLayer plates_layer = null;
        try {
            plates_layer = new GeoJsonLayer(mMap, R.raw.plates, getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        plates_layer.addLayerToMap();

        GeoJsonLineStringStyle lineStringStyle = plates_layer.getDefaultLineStringStyle();
        lineStringStyle.setColor(Color.RED);
        lineStringStyle.setWidth(3);

    }
}