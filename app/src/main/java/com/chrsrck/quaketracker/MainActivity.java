package com.chrsrck.quaketracker;

import android.content.res.Resources;
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
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;


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
    private HashSet<Marker> quakeMarkers;

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
        mDataFetchTask.execute(MAG_ALL_HOUR_URL);
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
        LatLng quakePos = updateEarthquakesOnMap();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(quakePos));
    }

    @Override
    public void processFinish(JSONObject result) {
        Log.d(TAG, "ProcessFinished called");
        mJSONObjectData = result;
//        if(mJSONObjectData != null) {
//            Log.d(TAG, "processFinish: jsonObject not null in process finish");
//            Log.d(TAG, "Has features: " + (mJSONObjectData.has("features")));
//        }
//        else {
//            Log.d(TAG, "processFinish: jsonObehct Null in process finish");
//        }

        if (mapFragment == null) {
            mapFragment = (SupportMapFragment) SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
            mapFragment.getMapAsync(this);
            Log.d(TAG, "MAP FRAG NULL");
        }
        else {
            updateEarthquakesOnMap();
            mDataFetchTask.cancel(true);
//            addPlatesLayer();
        }
    }
    private void earthquakeOptionSelected(String option) {
        Log.d(TAG, "earthquakeOptionSelectedCalled called");
//        mMap.clear();
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
        long latitude = 0;
        long longitude = 0;
        String eqTitle;
        LatLng quakePos = new LatLng(latitude, longitude);
        if(mJSONObjectData != null) {
            try {
                for (int i = 0; i < mJSONObjectData.getJSONArray("features").length(); i++) {
                    longitude =
                            mJSONObjectData.getJSONArray("features").getJSONObject(i)
                                    .getJSONObject("geometry").getJSONArray("coordinates").getLong(0);
                    latitude = mJSONObjectData.getJSONArray("features").getJSONObject(i)
                            .getJSONObject("geometry").getJSONArray("coordinates").getLong(1);
                    quakePos = new LatLng(latitude, longitude);
                    eqTitle =  mJSONObjectData.getJSONArray("features").getJSONObject(i)
                            .getJSONObject("properties").getString("title");
                    Marker addedMarker = mMap.addMarker(new MarkerOptions().position(quakePos).title(eqTitle)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.eq_marker)));
                    quakeMarkers.add(addedMarker);
                }

            } catch (JSONException e) {
                e.printStackTrace();
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
    }
}