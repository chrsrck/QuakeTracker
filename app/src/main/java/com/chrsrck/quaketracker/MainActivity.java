package com.chrsrck.quaketracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.ListIterator;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        AsyncResponse {

    public final String TAG = getClass().getSimpleName();
    public static final String MAG_ALL_HOUR_URL =
            "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson";
    public static final String MAG_2_HALF_DAY_URL =
            "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson";
    public static final String MAG_4_HALF_WEEK_URL
            = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/4.5_week.geojson";
    public static final String MAG_SIGNIFICANT_MONTH_URL
            = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/significant_month.geojson";

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private DataFetchTask mDataFetchTask = new DataFetchTask(this);
    private JSONObject mJSONObjectData;
    private LinkedList<HazardEvent> hazardsList;

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

        mapFragment = (SupportMapFragment) SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);

        // code for asynctask
        mapFragment = null;
        hazardsList = new LinkedList<HazardEvent>();
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
        String option = "";

        if (id == R.id.all_mag_hour) {
            earthquakeOptionSelected(MAG_ALL_HOUR_URL);
            option = "Mag all Hour";
        } else if (id == R.id.two_half_mag_day) {
            earthquakeOptionSelected(MAG_2_HALF_DAY_URL);
            option = "2.5+ Day";
        } else if (id == R.id.four_half_mag_week) {
            earthquakeOptionSelected(MAG_4_HALF_WEEK_URL);
            option = "4.5+ Week";
        } else if (id == R.id.significant_mag_month) {
            earthquakeOptionSelected(MAG_SIGNIFICANT_MONTH_URL);
            option = "Signifcant quakes this month";
        }

        if(isConnectedToInternet()) {
            Toast toast = Toast.makeText(this, option, Toast.LENGTH_SHORT);
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
        LatLng latLng = new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mDataFetchTask.execute(MAG_ALL_HOUR_URL);

        isConnectedToInternet();
    }

    @Override
    public void processFinish(JSONObject result) {
        Log.d(TAG, "ProcessFinished called");
        mJSONObjectData = result;
        getEarthquakesFromCallback();
        updateEarthquakeOnMap();
    }

    private void earthquakeOptionSelected(String option) {
        Log.d(TAG, "earthquakeOptionSelectedCalled called");
        mMap.clear();
        hazardsList.clear();
        mDataFetchTask.cancel(true);
        mDataFetchTask = new DataFetchTask(this);
        mDataFetchTask.execute(option);
    }

    private void getEarthquakesFromCallback() {
        Log.d(TAG, "updateEQOnMap called");
        long latitude = 0;
        long longitude = 0;
        String eqTitle;


        if(mJSONObjectData != null) {
            try {
                for (int i = 0; i < mJSONObjectData.getJSONArray("features").length(); i++) {
                    longitude =
                            mJSONObjectData.getJSONArray("features").getJSONObject(i)
                                    .getJSONObject("geometry").getJSONArray("coordinates").getLong(0);
                    latitude = mJSONObjectData.getJSONArray("features").getJSONObject(i)
                            .getJSONObject("geometry").getJSONArray("coordinates").getLong(1);
                    eqTitle =  mJSONObjectData.getJSONArray("features").getJSONObject(i)
                            .getJSONObject("properties").getString("title");
                    hazardsList.add(new HazardEvent(eqTitle, latitude, longitude));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateEarthquakeOnMap() {
        ListIterator<HazardEvent> iterator = hazardsList.listIterator(0);
        while (iterator != null && iterator.hasNext()) {
            HazardEvent addedEvent = iterator.next();
            mMap.addMarker(new MarkerOptions().position(addedEvent.getLatLng())
                    .title(addedEvent.getTitle()).icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.eq_marker)));
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Toast toast = Toast.makeText(this,
                    "No internet, can't fetch data.", Toast.LENGTH_LONG);
            toast.show();
        }
        return isConnected;
    }
}
