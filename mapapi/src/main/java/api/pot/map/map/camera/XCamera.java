package api.pot.map.map.camera;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import api.pot.map.location.XLocation;
import api.pot.map.map.XMap;
import api.pot.map.navigation.XNavigation;
import api.pot.map.tools.Gp;

public class XCamera {
    Context context;
    GoogleMap mMap;
    XMap xMap;

    private CamListener camListener;

    private LatLng target = new LatLng(3.865351, 11.521022);
    private float viewAngle = 90;//0-90
    private float zoomLevel = 1;//1-21
    private float rotationAngle = 0;

    public void setCamListener(CamListener camListener) {
        this.camListener = camListener;
    }

    public XCamera(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
    }

    public XCamera(XMap xMap, Context context, GoogleMap mMap) {
        this.xMap = xMap;
        this.context = context;
        this.mMap = mMap;
        ///
        updateHandler = new Handler();
        manualHandler = new Handler();
        locHandle = new Handler();
        myLocation(XLocation.getMyLocationLatLon(context));
        onUpdate();
    }


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public LatLng getTarget() {
        return target;
    }

    public float getViewAngle() {
        return viewAngle;
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private float horizon;
    public float getHorizon() {
        return horizon;
    }

    public void setDeviceHorizon(float horizon) {
        this.horizon = horizon;
        rotationAngle(this.horizon);
        updateCamera();
        if(camListener!=null) camListener.horizonChange(this.horizon);
    }

    public void setMoveHorizon(float horizon) {
        this.horizon = horizon;
        rotationAngle(this.horizon);
        updateCamera();
        if(camListener!=null) camListener.horizonChange(this.horizon);
    }

    LatLng lastCenter;
    double minHorizonCheckDistance = 5*0.000621d;
    public void checkMoveHorizon(LatLng center) {
        if(lastCenter==null) {
            lastCenter = center;
            return;
        }
        if(Gp.distance(lastCenter, center)>=minHorizonCheckDistance){
            setMoveHorizon(Gp.getAngularOffset(lastCenter, center));
            lastCenter = center;
        }
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private LatLng onTarget;
    public XCamera target(LatLng target){
        onTarget = target;
        if(!manualUpdate)
            this.target = target;
        if(camListener!=null&&this.target!=null) camListener.targetChange(this.target);
        return this;
    }

    private Float onViewAngle;
    public XCamera viewAngle(float viewAngle){
        onViewAngle = viewAngle;
        if(!manualUpdate)
            this.viewAngle = ((int)viewAngle)%91;
        return this;
    }

    private Float onZoomLevel;
    public XCamera zoomLevel(float zoomLevel){
        onZoomLevel = zoomLevel;
        if(!manualUpdate)
            this.zoomLevel = zoomLevel;
        if(camListener!=null) camListener.zoomChange(this.zoomLevel);
        return this;
    }

    private Float onRotationAngle;
    public XCamera rotationAngle(float rotationAngle){
        onRotationAngle = rotationAngle;
        if(!manualUpdate)
            this.rotationAngle = rotationAngle;
        return this;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public XCamera mMoveUp(double angle){
        mTarget(new LatLng(getTarget().latitude+angle, getTarget().longitude));
        return this;
    }

    public XCamera mMoveDown(double angle){
        mTarget(new LatLng(getTarget().latitude-angle, getTarget().longitude));
        return this;
    }

    public XCamera mMoveLeft(double angle){
        mTarget(new LatLng(getTarget().latitude, getTarget().longitude-angle));
        return this;
    }

    public XCamera mMoveRight(double angle){
        mTarget(new LatLng(getTarget().latitude, getTarget().longitude+angle));
        return this;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public XCamera mTarget(List<LatLng> polygon){
        if(polygon==null||polygon.isEmpty()) return this;
        LatLng center = polygon.get(0), left = polygon.get(0), top = polygon.get(0), right = polygon.get(0), bottom = polygon.get(0);
        double diagonal, width, height, minLat = polygon.get(0).latitude, maxLat = polygon.get(0).latitude, minLng = polygon.get(0).longitude, maxLng = polygon.get(0).longitude;
        for(LatLng p : polygon){
            if(p.latitude>maxLat) maxLat = p.latitude;
            if(p.latitude<minLat) minLat = p.latitude;
            if(p.longitude>maxLng) maxLng = p.longitude;
            if(p.longitude<minLng) minLng = p.longitude;
        }
        ///---
        left = new LatLng(maxLat, minLng);
        top = new LatLng(maxLat, maxLng);
        right = new LatLng(minLat, maxLng);
        bottom = new LatLng(minLat, minLng);
        ///---
        center = new LatLng((left.latitude+right.latitude)/2, (left.longitude+right.longitude)/2);
        ///---
        diagonal = Gp.distance(top, bottom);
        width = Gp.distance(left, top);
        ///---
        mTarget(center)
                .mRotationAngle(Gp.getAngularOffset(bottom, top))
                .mZoomLevel(Gp.getRightZoom(width))
                .mUpdateCamera();
        xMap.camera.manualUpdate(10000);
        ///---
        return this;
    }

    public XCamera mTarget(LatLng target){
        manualUpdate();
        this.target = target;
        if(camListener!=null&&this.target!=null) camListener.targetChange(this.target);
        return this;
    }

    public XCamera mViewAngle(float viewAngle){
        manualUpdate();
        this.viewAngle = ((int)viewAngle)%91;
        return this;
    }

    public XCamera mZoomLevel(float zoomLevel){
        manualUpdate();
        this.zoomLevel = zoomLevel;
        if(camListener!=null) camListener.zoomChange(this.zoomLevel);
        return this;
    }

    public XCamera mRotationAngle(float rotationAngle){
        manualUpdate();
        this.rotationAngle = rotationAngle;
        return this;
    }




    private long duringMu = 3000;
    public XCamera manualUpdate(long during) {
        duringMu = during;
        return manualUpdate(true);
    }

    public XCamera manualUpdate() {
        return manualUpdate(true);
    }

    public XCamera manualUpdate(boolean manualUpdate) {
        this.manualUpdate = manualUpdate;
        if(manualUpdate){
            targetMyLocationAfter(NO_TARGET_MY_LOC_DEFAULT_DURR);
            manualHandler.removeCallbacksAndMessages(null);
            manualHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    manualUpdate(false);
                }
            }, duringMu);
        }else {
            if(duringMu != 3000) duringMu = 3000;
            ///---
            if(onTarget!=null){
                target(onTarget);
                onTarget = null;
            }
            if(onViewAngle!=null){
                viewAngle(onViewAngle);
                onViewAngle = null;
            }
            if(onRotationAngle!=null){
                rotationAngle(onRotationAngle);
                onRotationAngle = null;
            }
            if(onZoomLevel!=null){
                zoomLevel(onZoomLevel);
                onZoomLevel = null;
            }
            updateCamera();
        }
        return this;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public XCamera addZoom(float adding){
        zoomLevel(this.zoomLevel + adding);
        return this;
    }

    public XCamera addViewAngle(float adding) {
        viewAngle(this.viewAngle + adding);
        return this;
    }

    public XCamera addRotationAngle(float adding) {
        //rotationAngle(this.rotationAngle + adding);
        return this;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    private void onUpdate() {
        updateHandler.removeCallbacksAndMessages(null);
        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updater();
                onUpdate();
            }
        }, updateDelais);
    }

    private void updater() {
        try{
            if(mMap==null || updating || !onUpdate) return;
            ///
            onUpdate = false;
            updating = true;
            ///
            final CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(target)      // Sets the center of the map to Mountain View
                    .tilt(viewAngle)
                    .zoom(zoomLevel)
                    .bearing(rotationAngle)                // Sets the orientation of the camera to east
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                    new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            updating = false;
                        }

                        @Override
                        public void onCancel() {
                            ///mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            ///
                            updating = false;
                        }
                    });
            ///
            ///onUpdate = false;
        }catch (Exception e){
            updating = false;
        }
    }

    public boolean manualUpdate = false;
    public boolean isManualUpdate() {
        return manualUpdate;
    }

    private long updateDelais = 50;
    private Handler updateHandler;
    private Handler manualHandler;
    private boolean onUpdate = false;
    private boolean updating = false;
    public void updateCamera(){
        if(!manualUpdate)
            updateCamera(false);
    }

    public void mUpdateCamera(){
        updateCamera(true);
    }

    public void updateCamera(boolean rightNow){
        if(rightNow){
            //manualUpdate();
            //updateHandler = new Handler();
            mMap.stopAnimation();
        }
        onUpdate = true;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public static long NO_TARGET_MY_LOC_DEFAULT_DURR = 3000;
    private long nextMyLocTargetDate = -1;
    public void targetMyLocationAfter(long duration){
        this.nextMyLocTargetDate = System.currentTimeMillis()+duration;
    }

    public void targetMyLocationNow(){
        targetMyLocationAfter(0);
    }

    private LatLng myLoc;
    private Handler locHandle;
    public void myLocation(final LatLng myLocation) {
        target(myLocation!=null?myLocation:XLocation.getMyLocationLatLon(context))
                .updateCamera();
        myLoc = myLocation;
        if(camListener!=null) camListener.myLocationChange(myLocation);
        /***locHandle.removeCallbacksAndMessages(null);
        locHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                myLocation(myLoc);
            }
        }, NO_TARGET_MY_LOC_DEFAULT_DURR/3);*/
    }


    /**public void myLocation(final LatLng myLocation) {
        if(System.currentTimeMillis()<nextMyLocTargetDate) return;
        ///---target(myLocation);
        this.target = myLocation;
        updateCamera();
        ///----
        myLoc = myLocation;
        locHandle.removeCallbacksAndMessages(null);
        locHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                myLocation(myLoc);
            }
        }, NO_TARGET_MY_LOC_DEFAULT_DURR/3);
    }*/
}
