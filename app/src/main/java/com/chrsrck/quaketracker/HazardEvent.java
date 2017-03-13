package com.chrsrck.quaketracker;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by chrsrck on 3/12/17.
 */

public class HazardEvent {

    private String title;
    private LatLng mLatLng;

    public HazardEvent() {
        title = "";
        mLatLng = null;
    }

    public HazardEvent(String title, long latitude, long longitude) {
        this.title = title;
        mLatLng = new LatLng(latitude, longitude);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }
}
