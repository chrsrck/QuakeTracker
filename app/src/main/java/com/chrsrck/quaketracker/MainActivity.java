package com.chrsrck.quaketracker;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        AsyncResponse, Button.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private DataFetchTask mDataFetchTask;
    private DatabaseCreationTask mDatabaseCreationTask;

    private JSONObject mJSONObjectData;

    private HashSet<LatLng> coordinateSet;
    private SQLiteDatabase database;

    private FeedReaderDbHelper mDbHelper;
    private ClusterManager<HazardEvent> mClusterManager;
    private TileOverlay mTileOverlay;
    private boolean isMarkerMode;

    private Button modeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modeButton = (Button) findViewById(R.id.modeButton);
        modeButton.setOnClickListener(this);

        // code for asynctask
        mDbHelper = new FeedReaderDbHelper(this);
        if (isOnline()) {
            mDataFetchTask = new DataFetchTask(this);
            mDataFetchTask.execute(FeedContractUSGS.SIG_EQ_URL);
            //mDataFetchTask.execute(FeedContractUSGS.MAG_SIGNIFICANT_MONTH_URL);
            setUpMapFragment();
        }
        else {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.dialog_title_connection);
            dialog.setMessage(R.string.dialog_message_connection);
            dialog.create();
            dialog.show();
        }

        isMarkerMode = true;
        coordinateSet = new HashSet<>();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "On Stop Called");
        if (!mDataFetchTask.isCancelled()) {
            mDataFetchTask.cancel(true);
        }

        if (!mDatabaseCreationTask.isCancelled()) {
            mDatabaseCreationTask.cancel(true);
        }
        mDbHelper.close();
        this.deleteDatabase("FeedReader.db");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

        LatLng initialPos = new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPos));

        // adding plate boundaries
        addPlatesLayer();

        // adding earthquake points
        // await creation of database here
        //updateEarthquakesOnMap();
    }

    @Override
    public void dataFetchProcessFinish(JSONObject result) {
        Log.d(TAG, "dataFetchProcessFinish called");
        mJSONObjectData = result;
        mDatabaseCreationTask = new DatabaseCreationTask(mDbHelper, this, this);
        mDatabaseCreationTask.execute(mJSONObjectData);
        // set up fragment here for the circle
//        if(mJSONObjectData != null) {
//            Log.d(TAG, "dataFetchProcessFinish: jsonObject not null in process finish");
//            Log.d(TAG, "Has features: " + (mJSONObjectData.has("features")));
//        }
//        else {
//            Log.d(TAG, "dataFetchProcessFinish: jsonObehct Null in process finish");
//        }
    }

    public void setUpMapFragment() {
        if (mapFragment == null) {
            mapFragment = (SupportMapFragment) SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
            mapFragment.getMapAsync(this);
            Log.d(TAG, "MAP FRAG NULL");
        } else {
            mDatabaseCreationTask.cancel(true);
        }
    }

    @Override
    public void databaseCreationProcessFinish(SQLiteDatabase result) {
        Log.d(TAG, "databaseCreationProcessFinish called");
        database = result;
        // await map fragment ready?
        updateEarthquakesOnMap();
    }


    private LatLng updateEarthquakesOnMap() {
        Log.d(TAG, "updateEQOnMap called");
        double latitude = 0;
        double longitude = 0;
        double mag = 0;
        String eqTitle;
        LatLng quakePos = new LatLng(latitude, longitude);

        if (isMarkerMode) {
            if (mTileOverlay != null) {
                mTileOverlay.remove();
            }


            String[] projection = {
                    FeedContractUSGS.FeedEntry._ID,
                    FeedContractUSGS.FeedEntry.TITLE_COLUMN,
                    FeedContractUSGS.FeedEntry.LONG_COLUMN,
                    FeedContractUSGS.FeedEntry.LAT_COLUMN,
                    FeedContractUSGS.FeedEntry.MAG_COLUMN
            };

            if (database != null && database.isOpen()) {

                String selection = FeedContractUSGS.FeedEntry.TYPE_EVENT_COLUMN + " = ?";
                String[] selectionArgs = {"earthquake"};
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

                setUpClusterer();

                while (cursor.moveToNext()) {
                    eqTitle = cursor.getString(
                            cursor.getColumnIndexOrThrow(FeedContractUSGS.FeedEntry.TITLE_COLUMN));
                    latitude = cursor.getDouble(
                            cursor.getColumnIndexOrThrow(FeedContractUSGS.FeedEntry.LAT_COLUMN));
                    longitude = cursor.getDouble(
                            cursor.getColumnIndexOrThrow(FeedContractUSGS.FeedEntry.LONG_COLUMN));
                    mag = cursor.getDouble(
                            cursor.getColumnIndexOrThrow(FeedContractUSGS.FeedEntry.MAG_COLUMN));
                    quakePos = new LatLng(latitude, longitude);

                    HazardEvent hazardEvent = new HazardEvent(eqTitle, quakePos);

                    if (mag < 4.5) {
                        hazardEvent.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.eq_marker_green));
                    }
                    else if (mag < 6.0) {
                        hazardEvent.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.eq_marker_yellow));
                    }
                    else {
                        hazardEvent.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.eq_marker));
                    }
                    coordinateSet.add(hazardEvent.getPosition());
                    mClusterManager.addItem(hazardEvent);
                }

                mClusterManager.cluster();
            } else {
                Toast toast = Toast.makeText(this, "Unable to retrieve Data", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            mClusterManager.clearItems();
            mClusterManager.cluster();

            HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                    .data(coordinateSet).build();
            provider.setOpacity(0.5);
            provider.setRadius(50);

            TileOverlayOptions tileOverlayOptions = new TileOverlayOptions().tileProvider(provider);
            mTileOverlay = mMap.addTileOverlay(tileOverlayOptions);

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

    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<HazardEvent>(this, mMap);
        EventIconRenderer eventIconRenderer = new EventIconRenderer(this, mMap, mClusterManager);
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<HazardEvent>() {
            @Override
            public boolean onClusterClick(Cluster<HazardEvent> cluster) {
                return false;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<HazardEvent>() {
            @Override
            public boolean onClusterItemClick(HazardEvent hazardEvent) {
                return false;
            }
        });

        mClusterManager.setRenderer(eventIconRenderer);
//        mClusterManager.setAlgorithm(new GridBasedAlgorithm<HazardEvent>());


        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.
        mClusterManager.setAnimation(true);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == modeButton.getId()) {
            if (isMarkerMode) {
                isMarkerMode = false;
                modeButton.setText("Marker Mode");
            }
            else {
                isMarkerMode = true;
                modeButton.setText("Heat Map Mode");
            }
            updateEarthquakesOnMap();
        }
    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}