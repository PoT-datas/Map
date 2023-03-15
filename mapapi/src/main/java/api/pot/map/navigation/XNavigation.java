package api.pot.map.navigation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.renderscript.RenderScript;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import api.pot.map.direction.XDirection;
import api.pot.map.location.XLocation;
import api.pot.map.map.XMap;
import api.pot.map.map.marker.XMarker;
import api.pot.map.tools.Gp;

public class XNavigation {
    public List<Checkpoint> checkpoints = new ArrayList<>();

    private Handler handlerUi;

    XMap xMap;
    Context context;
    GoogleMap mMap;

    private NavigationListener navigationListener;
    private boolean insideRadius = false;
    private boolean onWay = false;

    public XNavigation(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
        handlerUi = new Handler();
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if(enabled) xMap.camera.mViewAngle(90);
        this.enabled = enabled;
        xMap.camera.mUpdateCamera();
        if(navigationListener!=null) navigationListener.onEnabledChange(this.enabled);
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setNavigationListener(NavigationListener navigationListener) {
        this.navigationListener = navigationListener;
    }

    public NavigationListener getNavigationListener() {
        return navigationListener;
    }

    private double speedMs=-1;
    public void speedChange(double speedMs) {
        this.speedMs = speedMs;
        //
        if(!xMap.direction.isEnabled()) return;
        double distM = meterFromMile((float) xMap.direction.getDistance());
        if(speedMs<=1) speedMs = XLocation.mps(60);
        long durationS = (long) (distM/speedMs);
        if(navigationListener!=null) navigationListener.onParamsChange(durationS, distM, speedMs);
    }
    public void paramChange() {
        if(!xMap.direction.isEnabled() || speedMs==-1) return;
        double distM = meterFromMile((float) xMap.direction.getDistance());
        if(speedMs<=1) speedMs = XLocation.mps(60);
        long durationS = (long) (distM/speedMs);
        if(navigationListener!=null) navigationListener.onParamsChange(durationS, distM, speedMs);
    }

    public static float meterFromMile(float miles) {
        return miles*1609.34f;
    }

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public XNavigation(XMap xMap, Context context, GoogleMap mMap) {
        this.xMap = xMap;
        this.context = context;
        this.mMap = mMap;
    }

    public void drawPoints() {/*
        for(Checkpoint checkpoint : checkpoints){
            mMap.addCircle(new CircleOptions()
                    .center(checkpoint.location)
                    .radius(2)
                    .strokeColor(checkpoint.colorStroke)
                    .fillColor(checkpoint.colorFill));
        }*/

    }

    private boolean uiUpdating = false;
    private long updateTime;
    private void post(Runnable runnable) {
        if(uiUpdating) return;
        uiUpdating = true;
        updateTime = System.currentTimeMillis();
        if(handlerUi!=null){
            handlerUi.postDelayed(runnable, delay);
        }else {
        }
    }

    private boolean enabled = false;
    private long delay = 100;
    ///
    private long negativeCount = 0;
    private boolean hasUpdate = false;
    private double lastDistFromDir = -1;//
    private double maxDistFromDir = 0.00621371d;//mile == 10m
    //private double maxDistFromDir = 0.0124274d;//mile == 20m
    private LatLng nextPos;
    private boolean updating = false;
    private boolean onUpdate = false;
    public void moving(final LatLng position) {
        if(!/*xMap.direction.*/isEnabled()) return;
        nextPos = position;
        if(updating) {
            onUpdate = true;
            return;
        }
        onUpdate = false;

        if (delay<System.currentTimeMillis()-updateTime)
            uiUpdating = false;
        if(handlerUi==null) handlerUi = new Handler();

        /**
         * *******************************/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        LatLng pos = position;
                        ///
                        int iNext = 0;
                        Checkpoint next = null;
                        int minIndex = -1;
                        double minDist = -1;
                        double distance = -1;
                        ///
                        int recent = iNext;
                        ///
                        while ( (iNext=getNextUnreachedPoint(iNext, pos))!=-1 ){
                            if(next!=null && next.colorFill==Color.GREEN){
                                next.colorFill = Color.GRAY;
                                next.colorStroke = Color.RED;
                            }
                            next = checkpoints.get(iNext);
                            if(next!=null && next.colorFill!=Color.GREEN){
                                hasUpdate = true;
                                next.colorFill = Color.GREEN;
                                next.colorStroke = Color.BLACK;
                            }
                            distance = Gp.distance(pos, next.location);
                            if(distance<maxDistFromDir){
                                if(enabled && navigationListener!=null && !insideRadius) {
                                    insideRadius = true;
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            navigationListener.movingOnRadius(insideRadius);
                                        }
                                    });
                                }
                                if( (distance<minDist || minDist==-1) ){
                                    minDist = distance;
                                    minIndex = iNext;
                                }else {
                                    if(minIndex!=-1) {
                                        setReachedUntil(minIndex);
                                    }
                                    //if(navigationListener!=null) navigationListener.onWay();
                                    if(enabled && navigationListener!=null && !onWay) {
                                        onWay = true;
                                        post(new Runnable() {
                                            @Override
                                            public void run() {
                                                navigationListener.movingOnWay(onWay);
                                            }
                                        });
                                    }
                                    break;
                                }
                                lastDistFromDir = distance;
                            }else {
                                if( distance>lastDistFromDir && lastDistFromDir!=-1 ){
                                    if(enabled && navigationListener!=null && onWay) {
                                        onWay = false;
                                        post(new Runnable() {
                                            @Override
                                            public void run() {
                                                navigationListener.movingOnWay(onWay);
                                            }
                                        });
                                    }
                                }else if(distance<lastDistFromDir){
                                    //if(navigationListener!=null) navigationListener.onWay();
                                    if(enabled && navigationListener!=null && !onWay) {
                                        onWay = true;
                                        post(new Runnable() {
                                            @Override
                                            public void run() {
                                                navigationListener.movingOnWay(onWay);
                                            }
                                        });
                                    }
                                }else {
                                    //not moving
                                }
                                lastDistFromDir = distance;
                                if(enabled && navigationListener!=null && insideRadius) {
                                    insideRadius = false;
                                    //navigationListener.movingOnRadius(insideRadius);
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            navigationListener.movingOnRadius(insideRadius);
                                        }
                                    });
                                }
                                break;
                            }
                        }
                        ///
                        if(hasUpdate){
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    notifyMap();
                                }
                            });
                            hasUpdate = false;
                        }
                        ///
                        if(navigationListener!=null && iNext!=-1 && iNext<checkpoints.size()-1) {
                            ///
                            xMap.direction.setNextUnreachedPoint(iNext);
                            ///
                            if(enabled){
                                //navigationListener.onMoving(0f, getAngularOffset(pos, next.location));
                                offset = getAngularOffset(checkpoints.get(iNext).location,
                                        checkpoints.get(iNext+1).location);
                                if(offset!=lastOffset || lastOffset==-1) {
                                    lastOffset = offset;
                                    //navigationListener.onMoving(0f, offset);
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            navigationListener.onMoving(0f, offset);
                                        }
                                    });
                                }
                            }
                        }
                        ///
                        updating = false;
                        if(onUpdate) moving(nextPos);
                    }
                }).start();
            }
        }, delay);

        /**
         * *******************************/

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ///
                LatLng pos = position;
                ///
                int iNext = 0;
                Checkpoint next = null;
                int minIndex = -1;
                double minDist = -1;
                double distance = -1;
                ///
                int recent = iNext;
                ///
                while ( (iNext=getNextUnreachedPoint(iNext, pos))!=-1 ){
                    if(next!=null && next.colorFill==Color.GREEN){
                        next.colorFill = Color.GRAY;
                        next.colorStroke = Color.RED;
                    }
                    next = checkpoints.get(iNext);
                    if(next!=null && next.colorFill!=Color.GREEN){
                        hasUpdate = true;
                        next.colorFill = Color.GREEN;
                        next.colorStroke = Color.BLACK;
                    }
                    distance = XMarker.distance(pos, next.location);
                    if(distance<maxDistFromDir){
                        if(enabled && navigationListener!=null && !insideRadius) {
                            insideRadius = true;
                            navigationListener.movingOnRadius(insideRadius);
                        }
                        if( (distance<minDist || minDist==-1) ){
                            minDist = distance;
                            minIndex = iNext;
                        }else {
                            if(minIndex!=-1) setReachedUntil(minIndex);
                            //if(navigationListener!=null) navigationListener.onWay();
                            if(enabled && navigationListener!=null && !onWay) {
                                onWay = true;
                                navigationListener.movingOnWay(onWay);
                            }
                            break;
                        }
                        lastDistFromDir = distance;
                    }else {
                        if( distance>lastDistFromDir && lastDistFromDir!=-1 ){
                            if(enabled && navigationListener!=null && onWay) {
                                onWay = false;
                                navigationListener.movingOnWay(onWay);
                            }
                        }else if(distance<lastDistFromDir){
                            //if(navigationListener!=null) navigationListener.onWay();
                            if(enabled && navigationListener!=null && !onWay) {
                                onWay = true;
                                navigationListener.movingOnWay(onWay);
                            }
                        }else {
                            //not moving
                        }
                        lastDistFromDir = distance;
                        if(enabled && navigationListener!=null && insideRadius) {
                            insideRadius = false;
                            navigationListener.movingOnRadius(insideRadius);
                        }
                        break;
                    }
                }
                ///
                if(hasUpdate){
                    notifyMap();
                    hasUpdate = false;
                }
                ///
                if(navigationListener!=null && iNext!=-1 && iNext<checkpoints.size()-1) {
                    ///
                    xMap.direction.setNextUnreachedPoint(iNext);
                    ///
                    if(enabled){
                        //navigationListener.onMoving(0f, getAngularOffset(pos, next.location));
                        offset = getAngularOffset(checkpoints.get(iNext).location,
                                checkpoints.get(iNext+1).location);
                        if(offset!=lastOffset || lastOffset==-1) {
                            lastOffset = offset;
                            navigationListener.onMoving(0f, offset);
                        }
                    }
                }
                ///
                updating = false;
                if(onUpdate) moving(nextPos);
            }
        }, delay);*/


    }

    /*public void moving(final LatLng position) {
        if(!isEnabled()) return;
        nextPos = position;
        if(updating) {
            onUpdate = true;
            return;
        }
        onUpdate = false;
        ///
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ///
                LatLng pos = position;
                ///
                int iNext = 0;
                Checkpoint next = null;
                int minIndex = -1;
                double minDist = -1;
                double distance = -1;
                ///
                int recent = iNext;
                ///
                while ( (iNext=getNextUnreachedPoint(iNext))!=-1 ){
                    if(next!=null && next.colorFill==Color.GREEN){
                        next.colorFill = Color.GRAY;
                        next.colorStroke = Color.RED;
                    }
                    next = checkpoints.get(iNext);
                    if(next!=null && next.colorFill!=Color.GREEN){
                        hasUpdate = true;
                        next.colorFill = Color.GREEN;
                        next.colorStroke = Color.BLACK;
                    }
                    distance = XMarker.distance(pos, next.location);
                    if(distance<maxDistFromDir){
                        if(navigationListener!=null && !insideRadius) {
                            insideRadius = true;
                            navigationListener.movingOnRadius(insideRadius);
                        }
                        if( (distance<minDist || minDist==-1) ){
                            minDist = distance;
                            minIndex = iNext;
                        }else {
                            if(minIndex!=-1) setReachedUntil(minIndex);
                            //if(navigationListener!=null) navigationListener.onWay();
                            if(navigationListener!=null && !onWay) {
                                onWay = true;
                                navigationListener.movingOnWay(onWay);
                            }
                            break;
                        }
                        lastDistFromDir = distance;
                    }else {
                        if( distance>lastDistFromDir && lastDistFromDir!=-1 ){
                            if(navigationListener!=null && onWay) {
                                onWay = false;
                                navigationListener.movingOnWay(onWay);
                            }
                        }else if(distance<lastDistFromDir){
                            //if(navigationListener!=null) navigationListener.onWay();
                            if(navigationListener!=null && !onWay) {
                                onWay = true;
                                navigationListener.movingOnWay(onWay);
                            }
                        }else {
                            //not moving
                        }
                        lastDistFromDir = distance;
                        if(navigationListener!=null && insideRadius) {
                            insideRadius = false;
                            navigationListener.movingOnRadius(insideRadius);
                        }
                        break;
                    }
                }
                ///
                if(hasUpdate){
                    notifyMap();
                    hasUpdate = false;
                }
                ///
                if(navigationListener!=null && iNext!=-1 && iNext<checkpoints.size()-1) {
                    //navigationListener.onMoving(0f, getAngularOffset(pos, next.location));
                    offset = getAngularOffset(checkpoints.get(iNext).location,
                            checkpoints.get(iNext+1).location);
                    if(offset!=lastOffset || lastOffset==-1) {
                        lastOffset = offset;
                        navigationListener.onMoving(0f, offset);
                    }
                }
                ///
                updating = false;
                if(onUpdate) moving(nextPos);
            }
        }, delay);
    }*/

    private float offset, lastOffset=-1;

    private float getAngularOffset(LatLng org, LatLng target) {
        return (float) bearing(org.latitude, org.longitude, target.latitude, target.longitude);

        /*Location from = new Location(LocationManager.GPS_PROVIDER);
        Location to = new Location(LocationManager.GPS_PROVIDER);
        from.setLatitude(org.latitude);
        from.setLongitude(org.longitude);
        to.setLongitude(target.latitude);
        to.setLatitude(target.longitude);
        return from.bearingTo(to);*/
    }

    protected double bearing(double startLat, double startLng, double endLat, double endLng){
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff= Math.toRadians(endLng - startLng);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    private void setReachedUntil(int i) {
        for(int k=0;k<i+1;k++){
            checkpoints.get(k).reachedTime = System.currentTimeMillis();
            checkpoints.get(k).colorFill = Color.YELLOW;
            checkpoints.get(k).colorStroke = Color.GRAY;
        }
        ///
        post(new Runnable() {
            @Override
            public void run() {
                notifyMap();
            }
        });
    }

    private void notifyMap() {
        //xMap.direction.notifyDirection();
        drawPoints();
    }

    /*private int getNextUnreachedPoint(int i) {
        if(checkpoints==null || checkpoints.size()==0 || checkpoints.size()<=i) return -1;
        for (int k=i;k<checkpoints.size();k++){
            if(checkpoints.get(k).reachedTime==-1)
                return k;
        }
        return -1;
    }*/

    private int getNextUnreachedPoint(int i, LatLng pos) {
        if(checkpoints==null || checkpoints.size()==0 || checkpoints.size()<=i) return -1;
        int iMin = 0;
        double min = -1;
        double distance = 0;
        for (int k=i;k<checkpoints.size();k++){
            if(checkpoints.get(k).reachedTime==-1){
                distance = Gp.distance(pos, checkpoints.get(k).location);
                if(distance<min || min==-1){
                    min = distance;
                    iMin = k;
                }
            }
        }
        return iMin;
    }


    //intent for navigation
    public void loadGmailNavigation(Context context, String lat, String lng){
        Uri navigation = Uri.parse("google.navigation:q="+lat+","+lng+"");
        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigation);
        navigationIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(navigationIntent);
    }
}
