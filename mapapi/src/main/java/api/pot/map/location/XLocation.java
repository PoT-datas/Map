package api.pot.map.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.util.List;

public class XLocation {
    private static XLocation location;

    private static int UPDATE_INTERVAL = 1000;
    private static int FATEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 0;

    private FusedLocationProviderClient mFusedLocationClient;
    private Activity activity;

    private LocationCallback mLocationCallback;
    private OnLocationsChangeListener mLocationsChangeListener;

    public XLocation(Activity activityIn) {
        this.activity = activityIn;
    }


    public static XLocation with(Activity activity){
        if(location==null) location = new XLocation(activity);
        return location;
    }

    @SuppressLint("MissingPermission")
    public static Location getMyLocation(Context context) {
        try {
            if(context==null) return null;
            ///
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(!isGPSEnabled && !isNetEnabled) return null;
            ///
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            ///
            long locationGPStime = 0;
            long locationNetTime = 0;
            if(locationGPS!=null) locationGPStime=locationGPS.getTime();
            if(locationNet!=null) locationNetTime=locationNet.getTime();
            ///
            if(locationNetTime<locationGPStime){
                return locationGPS;
            } else {
                ///
                ////Toast.makeText(context, locationGPS+" | "+locationNet, Toast.LENGTH_SHORT).show();
                return locationNet;
            }
            ///
            ///return new LatLng(latitude, longitude);
        }catch (Exception e){
            return null;
        }
    }

    public static LatLng getMyLocationLatLon(Context context) {
        Location location = getMyLocation(context);
        return location!=null?new LatLng(location.getLatitude(), location.getLongitude()):null;
    }

    public XLocation updateInterval(int updateInterval){
        UPDATE_INTERVAL = updateInterval;
        return location;
    }

    public XLocation fastestInterval(int fastestInterval){
        FATEST_INTERVAL = fastestInterval;
        return location;
    }

    public XLocation displacement(int displacement){
        DISPLACEMENT = displacement;
        return location;
    }

    public XLocation setLocationsChangeListener(OnLocationsChangeListener onLocationsChangeListener){
        if(activity==null) return location;
        //
        mLocationsChangeListener = onLocationsChangeListener;
        init();
        if(mLocationsChangeListener!=null) listener();
        requestLocations();
        return location;
    }





    public void removeLocationUpdates(){
        if(mFusedLocationClient==null || mLocationCallback==null) return;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public static String smallTimeUnit(long sTime){
        long s = sTime%60;
        long m = sTime/60;
        long h = m/60;
        m = m%60;
        String ret = h!=0?h+"H":"";
        ret += m!=0?m+"m":"";
        ret += (s!=0||ret.length()==0)?s+"s":"";
        return ret;
    }

    public static String smallDistanceUnit(double mDistance){
        long m = (long) mDistance;
        long km = m/1000;
        m = m%1000;
        String ret = km!=0?km+"Km":"";
        ret += (m!=0||ret.length()==0)?m+"m":"";
        return ret+"";
    }

    public static double kmph(double mps){
        return kmph(mps, 3);
    }

    public static double kmph(double mps, int precision){
        return round((mps*3.6),precision, BigDecimal.ROUND_HALF_UP);
    }

    public static double mps(double kmph){
        return mps(kmph, 3);
    }

    public static double mps(double kmph, int precision){
        return round((kmph/3.6),precision, BigDecimal.ROUND_HALF_UP);
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        if(precision==0) return (int)rounded.doubleValue();
        return rounded.doubleValue();
    }





    private void init() {
        if(mFusedLocationClient!=null) return;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    private static double latitude = 0;
    private static double longitude = 0;
    private void listener() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<android.location.Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    android.location.Location location = locationList.get(locationList.size() - 1);
                    android.location.Location location2 = locationResult.getLastLocation();
                    if(mLocationsChangeListener!=null){
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        mLocationsChangeListener.onLocationChange(new LatLng(latitude, longitude));
                        mLocationsChangeListener.onSpeedChange(location.getSpeed());
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if(mLocationsChangeListener!=null) mLocationsChangeListener.onLocationAvailability(locationAvailability);
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void requestLocations() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }


    /*
    *

    LocationManager mLocationManager;
    private void loc() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                new MyLocationListener(context) {
                    @Override
                    public void onLocationChanged(Location location) {
                        notifyMyLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                        //xtv1.setText(location.getLatitude()+"/"+location.getLongitude());
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                        //xtv2.setText(s);
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                        //xtv3.setText(s);
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        //xtv3.setText(s);
                    }
                });
    }*/
}
