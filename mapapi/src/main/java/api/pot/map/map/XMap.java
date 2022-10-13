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
import api.pot.map.direction.XDirectionsParser;

public class XMap {
    Context context;
    GoogleMap mMap;

    public static int ZOOM_LEVEL_WORLD = 1;
    public static int ZOOM_LEVEL_CONTINENT = 5;
    public static int ZOOM_LEVEL_CITY = 10;
    public static int ZOOM_LEVEL_STREETS = 15;
    public static int ZOOM_LEVEL_USER = 18;
    public static int ZOOM_LEVEL_NAVIGATION = 20;
    public static int ZOOM_LEVEL_BUILDINGS = 20;

    private int directionWidth = 15;
    private int directionColor = Color.BLUE;
    private PolylineOptions polylineOptions;

    private DirectionListener directionListener;

    public void setDirectionWidth(int directionWidth) {
        this.directionWidth = directionWidth;
        notifyDirection();
    }

    public void setDirectionColor(int directionColor) {
        this.directionColor = directionColor;
        notifyDirection();
    }

    public void notifyDirection() {
        if(polylineOptions==null) return;
        polylineOptions.width(directionWidth);
        polylineOptions.color(directionColor);
        mMap.addPolyline(polylineOptions);
    }

    public void clearDirection() {
        polylineOptions = null;
    }

    public XMap(Context context, GoogleMap mMap){
        this.context=context;
        this.mMap = mMap;
    }

    public void getDirection(LatLng origin, LatLng dest, String google_map_key){
        getDirection(origin, dest, google_map_key, null);
    }

    public void getDirection(LatLng origin, LatLng dest, String google_map_key, DirectionListener directionListener){
        String url = getRequestedUrl(origin, dest, google_map_key);

        this.directionListener = directionListener;

        //adding our stringrequest to queue
        Volley.newRequestQueue(context).add(volleyRequest(url));
    }

    private StringRequest volleyRequest(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                TaskParser taskParser = new TaskParser();
                taskParser.execute(response);
            }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });
        return stringRequest;
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String,String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                XDirectionsParser directionsParser = new XDirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //get list route and display it into the map
            //ArrayList points = null;//try
            ArrayList<LatLng> points;
            //init polylineOptions
            polylineOptions = null;
            //polylineOptions = new PolylineOptions();
            if(lists!=null){
                for(List<HashMap<String, String>> path : lists){
                    //points = new ArrayList();//try
                    points = new ArrayList<>();
                    polylineOptions = new PolylineOptions();
                    if(path!=null){
                        for(HashMap<String, String> point : path){
                            if(point!=null){
                                double lat = Double.parseDouble(point.get("lat"));
                                double lon = Double.parseDouble(point.get("lng"));
                                points.add(new LatLng(lat, lon));
                            }
                        }
                    }
                    polylineOptions.addAll(points);
                    polylineOptions.width(directionWidth);
                    polylineOptions.color(directionColor);
                    polylineOptions.geodesic(true);
                }
            }
            if(polylineOptions!=null){
                mMap.addPolyline(polylineOptions);
                if(directionListener!=null) directionListener.onDirectionReady();
            }else{
                Toast.makeText(context, "Direction not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getRequestedUrl(LatLng origin, LatLng dest, String google_map_key) {
        //value of origine
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        //value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //set value enable the sensor
        String sensor = "sensor=false";
        //mode for find direction
        String mode = "mode=driving";
        //output format
        String output = "json";
        //params
        String param = str_org + "&" + str_dest + "&" + mode;//try
        //url
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param +  "&key=" +google_map_key;//try

        return  url;
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


    //intent for navigation
    public void loadNavigationView(Context context, String lat, String lng){
        Uri navigation = Uri.parse("google.navigation:q="+lat+","+lng+"");
        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigation);
        navigationIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(navigationIntent);

        /*Intent intent = new Intent(Intent.ACTION_VIEW,

                Uri.parse("http://ditu.google.cn/maps?f=d&source=s_d" +
                        "&saddr="+source.getLatitude()+
                        ","+source.getLongitude()+"&daddr="+
                        destination.latitude+
                        ","+destination.longitude+
                        "&hl=zh&t=m&dirflg=d"));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
        startActivityForResult(intent, 1);*/
    }

}
