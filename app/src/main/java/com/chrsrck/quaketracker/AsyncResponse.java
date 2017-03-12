package com.chrsrck.quaketracker;

import org.json.JSONObject;

/**
 * Created by chrsrck on 3/10/17.
 */

public interface AsyncResponse {
    void processFinish(JSONObject result);
}
