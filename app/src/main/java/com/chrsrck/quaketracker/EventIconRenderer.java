package com.chrsrck.quaketracker;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by chrsrck on 9/28/17.
 */

public class EventIconRenderer extends DefaultClusterRenderer<HazardEvent> {

    private Context mContext;
    public EventIconRenderer(Context context, GoogleMap map,
                           ClusterManager<HazardEvent> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(HazardEvent item, MarkerOptions markerOptions) {
        markerOptions.icon(item.getIcon());
        markerOptions.snippet(item.getSnippet());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<HazardEvent> cluster) {
        return cluster.getSize() > 20; // when count of markers is more than 3, render as cluster
    }
}
