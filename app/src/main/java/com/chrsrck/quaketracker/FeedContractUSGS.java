package com.chrsrck.quaketracker;

import android.provider.BaseColumns;

/**
 * Created by chrsrck on 9/27/17.
 */

public final class FeedContractUSGS {

    private FeedContractUSGS () {}

    /* - Earthquake Search parameters
    - Minimum 2.5 Magnitude
    - Time range: January 1st 2013 00:00:00 to September 25th 23:59:59 PM
    - Include earthquakes and non-earthquake events
    - Number of events: 1000
    - Actual number: 604
    - Offset of 1
    - Significance: 600
    - Geographic Region: World
    - Search tool: https://earthquake.usgs.gov/earthquakes/search/
    - Significant Earthquakes
        - Definition found here https://earthquake.usgs.gov/earthquakes/browse/significant.php?year=2017#sigdef
     */
    public static final String SIG_EQ_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query.geojson?starttime=2013-01-01%2000:00:00&endtime=2017-09-25%2023:59:59&minmagnitude=2.5&eventtype=earthquake,acoustic%20noise,acoustic_noise,anthropogenic_event,building%20collapse,chemical%20explosion,chemical_explosion,collapse,experimental%20explosion,explosion,ice%20quake,landslide,mine%20collapse,mine_collapse,mining%20explosion,mining_explosion,not%20reported,not_reported,nuclear%20explosion,nuclear_explosion,other%20event,other_event,quarry,quarry%20blast,quarry_blast,rock%20burst,rockslide,rock_burst,snow_avalanche,sonic%20boom,sonicboom,sonic_boom&minsig=600&orderby=time&limit=1000&offset=1";

    public static final String MAG_ALL_HOUR_URL =
            "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson";
    public static final String MAG_2_HALF_DAY_URL =
            "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson";
    public static final String MAG_4_HALF_WEEK_URL
            = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/4.5_week.geojson";
    public static final String MAG_SIGNIFICANT_MONTH_URL
            = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/significant_month.geojson";

    public static final String TABLE_NAME = "SIG_EQ";

    /* Defines tables contents
     */
    public static class FeedEntry implements BaseColumns {
        /* Specified in the geometry of geojson
         */
        public static final String ID_COLUMN = "id";
        public static final String LAT_COLUMN = "latitude";
        public static final String LONG_COLUMN = "longitude";

        /* Specified in Features geojson
         */
        public static final String TITLE_COLUMN = "title";
        public static final String MAG_COLUMN = "mag";
        public static final String PLACE_COLUMN = "place";
        public static final String TIME_COLUMN = "time";
        public static final String SIG_NUM_COLUMN = "sig";
        public static final String DURATION_MIN_COLUMN = "dmin";
        public static final String TYPE_EVENT_COLUMN = "type";
    }
}
