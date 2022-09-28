package api.pot.map.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
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

    private static FusedLocationProviderClient mFusedLocationClient;
    private static Activity activity;

    private static LocationCallback mLocationCallback;
    private static OnLocationsChangeListener mLocationsChangeListener;





    public static XLocation activity(Activity activityIn){
        activity = activityIn;
        return location;
    }

    public static XLocation updateInterval(int updateInterval){
        UPDATE_INTERVAL = updateInterval;
        return location;
    }

    public static XLocation fastestInterval(int fastestInterval){
        FATEST_INTERVAL = fastestInterval;
        return location;
    }

    public static XLocation displacement(int displacement){
        DISPLACEMENT = displacement;
        return location;
    }

    public static XLocation setLocationsChangeListener(OnLocationsChangeListener onLocationsChangeListener){
        if(activity==null) return location;
        //
        mLocationsChangeListener = onLocationsChangeListener;
        init();
        if(mLocationsChangeListener!=null) listener();
        requestLocations();
        return location;
    }





    public static void removeLocationUpdates(){
        if(mFusedLocationClient==null || mLocationCallback==null) return;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public static double kmph(double mps){
        return kmph(mps, 3);
    }

    public static double kmph(double mps, int precision){
        return round((mps*3.6),precision, BigDecimal.ROUND_HALF_UP);
    }

    public static double mps(double kmph){
        return kmph(kmph, 3);
    }

    public static double mps(double kmph, int precision){
        return round((kmph/3.6),precision, BigDecimal.ROUND_HALF_UP);
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }





    private static void init() {
        if(mFusedLocationClient!=null) return;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    private static void listener() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<android.location.Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    android.location.Location location = locationList.get(locationList.size() - 1);
                    android.location.Location location2 = locationResult.getLastLocation();
                    if(mLocationsChangeListener!=null){
                        mLocationsChangeListener.onLocationChange(new LatLng(location.getLatitude(), location.getLongitude()));
                        mLocationsChangeListener.onSpeedChange(location.getSpeed());
                    }
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private static void requestLocations() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }
}
