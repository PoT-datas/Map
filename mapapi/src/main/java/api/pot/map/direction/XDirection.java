package api.pot.map.direction;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import api.pot.map.map.XMap;
import api.pot.map.map.marker.XMarker;
import api.pot.map.navigation.Checkpoint;
import api.pot.map.tools.Gp;

public class XDirection {
    public List<Checkpoint> checkpoints = new ArrayList<>();

    Context context;
    GoogleMap mMap;
    XMap xMap;

    private int directionWidth = 15;
    private int directionColor = Color.BLUE;
    private PolylineOptions polylineOptions;

    private DirectionListener directionListener;

    private boolean enabled = false;

    private Handler handlerUi;

    public XDirection(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
    }

    public XDirection(XMap xMap, Context context, GoogleMap mMap) {
        this.xMap = xMap;
        this.context = context;
        this.mMap = mMap;
        handlerUi = new Handler();
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public boolean isEnabled() {
        return polylineOptions!=null && enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled && polylineOptions!=null;
    }

    private void initDistance() {
        distanceMiles = -1;
    }

    private double distanceMiles = -1;
    public double getDistance() {
        int iNextUnreached = getNextUnreachedPoint(0);
        //if(distanceMiles!=-1) return distanceMiles;
        LatLng[] points = new LatLng[checkpoints.size()];
        for(int i=iNextUnreached;i<checkpoints.size();i++)
            points[i] = checkpoints.get(i).location;
        distanceMiles = (position!=null?getDistance(position, checkpoints.get(iNextUnreached).location):0)
                +getDistance(points);
        return distanceMiles;
    }

    public double getDistance(LatLng... points) {
        double some = 0;
        if(points!=null){
            for(int i=0;i<points.length-1;i++) {
                if(points[i]!=null&&points[i]!=null)some += Gp.distance(points[i], points[i+1]);
            }
        }
        return some;
    }

    private int getNextUnreachedPoint(int i) {
        for (int k=i;k<checkpoints.size();k++){
            if(checkpoints.get(k).reachedTime==-1)
                return k;
        }
        return 0;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public LatLng getMyLocation(LatLng realPos) {
        moving(realPos);
        return realPos;
        ///
        /*if(!isEnabled() || startLine==null || stopLine==null) return realPos;
        ///
        return getProjection(realPos, startLine.location, stopLine.location);*/
    }

    private LatLng getProjection(LatLng realPos, LatLng startLine, LatLng stopLine) {
        final Coordo A;
        final Coordo B;
        final Coordo AB;
        final Coordo M;
        final LatLng H;//x, y
        ///
        M = new Coordo(realPos);
        A = new Coordo(startLine);
        B = new Coordo(stopLine);
        AB = new Coordo(A, B);
        double x = (cr(AB.x)*M.x+AB.x*AB.y*M.y + A.x*cr(AB.y)-A.y*AB.x*AB.y)/(cr(AB.x)+cr(AB.y));
        double y = (AB.y*AB.x*M.x+cr(AB.y)*M.y - AB.x*A.x*AB.y+A.y*cr(AB.x))/(cr(AB.x)+cr(AB.y));
        ///
        H = new LatLng(y, x);
        ///
        return H;
    }

    private LatLng position;
    private int iNext = -1;
    private boolean update = false;
    public void moving(final LatLng position){
        this.position = position;
        xMap.navigation.paramChange();
        ///
        if(!enabled || update) return;
        ///
        update = true;
        ///
        if (delay*10<System.currentTimeMillis()-updateTime)
            uiUpdating = false;
        if(handlerUi==null) handlerUi = new Handler();
        ///
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        iNext = getNextUnreachedPoint(iNext+1, position);
                        setReachedUntil(iNext);
                        setNextUnreachedPoint(iNext);
                        ///
                        post(new Runnable() {
                            @Override
                            public void run() {
                                drawPoints();
                            }
                        });
                        update = false;
                    }
                }).start();
            }
        }, delay);
    }

    private double maxDistFromDir = 0.00621371d;//mile == 10m
    private int getNextUnreachedPoint(int i, LatLng pos) {
        if(checkpoints==null || checkpoints.size()==0 || checkpoints.size()<=i) return -1;
        int iMin = 0;
        double min = -1;
        double distance;
        for (int k=0;k<checkpoints.size();k++){
            if(checkpoints.get(k).reachedTime==-1){
                distance = Gp.distance(pos, checkpoints.get(k).location);
                if(distance<min || min==-1){
                    if(distance<maxDistFromDir){
                        checkpoints.get(k).reachedTime = System.currentTimeMillis();
                        checkpoints.get(k).colorFill = Color.YELLOW;
                        checkpoints.get(k).colorStroke = Color.GRAY;
                        if(k==checkpoints.size()-1)
                            end();
                    }else {
                        min = distance;
                        iMin = k;
                    }
                }
            }
        }
        checkpoints.get(iMin).colorFill = Color.GREEN;
        checkpoints.get(iMin).colorStroke = Color.BLACK;
        return iMin;
    }

    public void drawPoints() {
        /*for(Checkpoint checkpoint : checkpoints){
            mMap.addCircle(new CircleOptions()
                    .center(checkpoint.location)
                    .radius(2)
                    .strokeColor(checkpoint.colorStroke)
                    .fillColor(checkpoint.colorFill));
        }*/

    }

    private long delay = 100;
    private boolean uiUpdating = false;
    private long updateTime;
    private void post(Runnable runnable) {
        if(uiUpdating) return;
        uiUpdating = true;
        updateTime = System.currentTimeMillis();
        if(handlerUi!=null){
            handlerUi.postDelayed(runnable, delay*10);
        }else {
        }
    }

    private void setReachedUntil(int i) {
        for(int k=0;k<i;k++){
            checkpoints.get(k).reachedTime = System.currentTimeMillis();
            checkpoints.get(k).colorFill = Color.YELLOW;
            checkpoints.get(k).colorStroke = Color.GRAY;
        }
        ///
        //drawPoints();
    }

    public double cr(double d){
        return Math.pow(d, 2);
    }

    long lastUnreached = -1;
    private Checkpoint startLine, stopLine;
    public void setNextUnreachedPoint(int target) {
        if(lastUnreached==target) return;
        lastUnreached = target;
        try{
            startLine = checkpoints.get(target-1);
        }catch (Exception e){
            startLine = null;
        }
        try{
            stopLine = checkpoints.get(target);
        }catch (Exception e){
            stopLine = null;
        }
        if(startLine==null&&stopLine!=null){
            startLine = checkpoints.get(target);
            stopLine = checkpoints.get(target+1);
        }
        if(startLine!=null && stopLine!=null){
            final PolylineOptions pl = new PolylineOptions();
            List<LatLng> pts = new ArrayList<>();
            pts.add(startLine.location);
            pts.add(stopLine.location);
            pl.addAll(pts);
            pl.width(directionWidth);
            pl.color(Color.GREEN);
            pl.geodesic(true);
            handlerUi.post(new Runnable() {
                @Override
                public void run() {
                    xMap.clearMap();
                    //mMap.addPolyline(pl);
                    xMap.camera.target(startLine.location)
                            .rotationAngle(getAngularOffset(startLine.location, stopLine.location));
                    xMap.camera.updateCamera();
                    //Toast.makeText(context, ""+getAngularOffset(startLine.location, stopLine.location), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void end() {
        handlerUi.post(new Runnable() {
            @Override
            public void run() {
                setEnabled(false);
                if(directionListener!=null) directionListener.onDirectionEnd();
            }
        });
    }

    private float getAngularOffset(LatLng org, LatLng target) {
        return (float) bearing(org.latitude, org.longitude, target.latitude, target.longitude);
    }

    protected double bearing(double startLat, double startLng, double endLat, double endLng){
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff= Math.toRadians(endLng - startLng);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    class Coordo{
        double x, y;

        public Coordo(LatLng latLng) {
            this.y = latLng.latitude;
            this.x = latLng.longitude;
        }

        public Coordo(Coordo a, Coordo b) {
            this.x = b.x-a.x;
            this.y = b.y-a.y;
        }
    }

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
        //
        xMap.navigation.drawPoints();
    }

    public void clearDirection() {
        polylineOptions = null;
    }

    public void getDirection(LatLng origin, LatLng dest, String google_map_key){
        getDirection(origin, dest, google_map_key, null);
    }

    private boolean parse = true;
    public void getDirection(LatLng origin, LatLng dest, String googleKeyApi, boolean parse, DirectionListener directionListener){
        this.parse = parse;
        getDirection(origin, dest, googleKeyApi, directionListener);
    }

    private LatLng origin, dest;
    private String googleKeyApi;
    public void getDirection(LatLng origin, LatLng dest, String googleKeyApi, DirectionListener directionListener){
        setEnabled(false);

        String url = getRequestedUrl(origin, dest, googleKeyApi);

        this.origin = origin;
        this.dest = dest;
        this.googleKeyApi = googleKeyApi;
        this.directionListener = directionListener;

        //adding our stringrequest to queue
        Volley.newRequestQueue(context).add(volleyRequest(url, directionListener));
    }

    public void reloadDirection(){
        getDirection(origin, dest, googleKeyApi, directionListener);
    }

    public void reloadDirection(LatLng from){
        getDirection(from, dest, googleKeyApi, directionListener);
    }

    private String dirMode = "driving";
    private String getRequestedUrl(LatLng origin, LatLng dest, String google_map_key) {
        //value of origine
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        //value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //set value enable the sensor
        String sensor = "sensor=false";
        //mode for find direction
        String mode = "mode="+dirMode;
        //output format
        String output = "json";
        //params
        String param = str_org + "&" + str_dest + "&" + mode;//try
        //url
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param +  "&key=" +google_map_key;//try

        return  url;
    }

    private StringRequest volleyRequest(String url, final DirectionListener directionListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(parse){
                    TaskParser taskParser = new TaskParser();
                    taskParser.execute(response);
                }else {
                    if(directionListener !=null) directionListener.onDirectionLoaded(response);
                    parse = true;
                }
            }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(directionListener !=null) directionListener.onDirectionLoaded(error.getMessage());
                        parse = true;
                    }
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
            //
            clearCheckpoints();
            //init polylineOptions
            polylineOptions = null;
            //polylineOptions = new PolylineOptions();
            if(lists!=null){
                for(List<HashMap<String, String>> path : lists){
                    //points = new ArrayList();//try
                    points = new ArrayList<>();
                    if(polylineOptions==null) polylineOptions = new PolylineOptions();
                    if(path!=null){
                        for(HashMap<String, String> point : path){
                            if(point!=null){
                                double lat = Double.parseDouble(point.get("lat"));
                                double lon = Double.parseDouble(point.get("lng"));
                                points.add(new LatLng(lat, lon));
                                addCheckpoint(new Checkpoint(new LatLng(lat, lon)));
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
                initDistance();
                ///
                mMap.addPolyline(polylineOptions);
                //
                xMap.navigation.drawPoints();
                //
                setEnabled(true);
                if(directionListener!=null) directionListener.onDirectionReady();
            }else{
                Toast.makeText(context, "Direction not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addCheckpoint(Checkpoint checkpoint) {
        checkpoints.add(checkpoint);
        xMap.navigation.checkpoints.add(checkpoint);
    }

    private void clearCheckpoints() {
        checkpoints = new ArrayList<>();
        xMap.navigation.checkpoints = new ArrayList<>();
    }
}
