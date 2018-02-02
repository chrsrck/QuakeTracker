package com.chrsrck.quaketracker;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by chrsrck on 3/12/17.
 */

public class HazardEvent implements ClusterItem {

    private String title;
    private LatLng mLatLng;
    private String mSnippet;
    private BitmapDescriptor mIcon;

    public HazardEvent() {
        title = "";
        mLatLng = null;
    }

    public HazardEvent(String title, LatLng latLng) {
        this.title = title;
//        mLatLng = new LatLng(latitude, longitude);
        mLatLng = latLng;
        mSnippet = "";
    }

    @Override
    public LatLng getPosition() {
        return mLatLng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public BitmapDescriptor getIcon() {
        return mIcon;
    }

    public void setIcon(BitmapDescriptor icon) {
        mIcon = icon;
    }
}
