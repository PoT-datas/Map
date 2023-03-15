package api.pot.map.map;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import api.pot.map.direction.DirectionListener;
import api.pot.map.direction.XDirection;
import api.pot.map.direction.XDirectionsParser;
import api.pot.map.map.camera.XCamera;
import api.pot.map.navigation.XNavigation;

public class XMap {
    public XDirection direction;
    public XNavigation navigation;
    public XCamera camera;

    private MapListener mapListener;

    public Context context;
    public GoogleMap mMap;
    public String googleKeyApi;

    public static int ZOOM_LEVEL_WORLD = 1;
    public static int ZOOM_LEVEL_CONTINENT = 5;
    public static int ZOOM_LEVEL_CITY = 10;
    public static int ZOOM_LEVEL_STREETS = 15;
    public static int ZOOM_LEVEL_USER = 17;
    public static int ZOOM_LEVEL_NAVIGATION = 19;
    public static int ZOOM_LEVEL_BUILDINGS = 20;

    public XMap(Context context, GoogleMap mMap, String googleKeyApi){
        this.context=context;
        this.mMap = mMap;
        this.googleKeyApi = googleKeyApi;
        camera = new XCamera(this, this.context, this.mMap);
        direction = new XDirection(this, this.context, this.mMap);
        navigation = new XNavigation(this, this.context, this.mMap);
    }

    public Boolean setMapStyle(int json_style){
        boolean success = false;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, json_style));
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
        return success;
    }


    public void clearMap() {
        //mMap.clear();
        //direction.notifyDirection();
        ///navigation.
        if(this.mapListener!=null) this.mapListener.onMapClear();
    }

    public void setMapListener(MapListener mapListener) {
        this.mapListener = mapListener;
    }
}
