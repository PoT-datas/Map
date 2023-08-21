package com.pot.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import api.pot.map.map.XMap;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int LOCATION_REQUEST = 500, ID_OF_REQUEST_PERMITION = 123;

    private static final LatLng SYDNEY = new LatLng(-33.88,151.21);
    private static final LatLng MOUNTAIN_VIEW = new LatLng(37.4, -122.1);
    private static final LatLng PERTH = new LatLng(-31.90, 115.86);

    private GoogleMap mMap;
    private XMap xMap;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = this;

        findViewById(R.id.toSpeed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, SpeedActivity.class));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        xMap = new XMap(this, mMap, getResources().getString(R.string.google_map_key));

        //Map Listener
        mapListener();

        //Marker Listener
        markerListener();
        
        //configuration de la map
        mapConfig();
        
        //my location
        locationEnabled();

        //direction
        second();
    }

    private void second() {
        final ArrayList<LatLng> listPoints = new ArrayList<>();
        //set onLongClic
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                //reset map and marker
                if( listPoints.size()==2 ){
                    listPoints.clear();
                    mMap.clear();
                }
                //save point
                listPoints.add(point);
                //create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                if( listPoints.size()==1 ){
                    //add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }else {
                    //add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);
                //TODO: request get direction code bellow
                if( listPoints.size()==2 ){
                    //get direction
                    xMap.direction.getDirection(listPoints.get(0), listPoints.get(1), getResources().getString(R.string.google_map_key));
                }
            }
        });
    }

    private void mapListener() {
        //map listener
        final ArrayList<LatLng> listPoints = new ArrayList<>();
        //set onLongClic
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                //reset map and marker
                if( listPoints.size()==2 ){
                    listPoints.clear();
                    mMap.clear();
                }
                //save point
                listPoints.add(point);
                //create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                if( listPoints.size()==1 ){
                    //add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }else {
                    //add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);
                //TODO: request get direction code bellow
                if( listPoints.size()==2 ){
                    //get direction
                    //mapMng.getDirection(listPoints.get(0), listPoints.get(1));
                }
            }
        });
    }

    private void markerListener() {
        //map marker listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Retrieve the data from the marker.
                Integer clickCount = (Integer) marker.getTag();
                // Check if a click count was set, then display the click count.
                if (clickCount != null) {
                    clickCount = clickCount + 1;
                    marker.setTag(clickCount);
                    Toast.makeText(MapsActivity.this,
                            marker.getTitle() +
                                    " has been clicked " + clickCount + " times.",
                            Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    private void mapConfig() {
        ///-------------Free Map---------------
        //Map Clear
        mMap.clear();
        //Zoom control
        mMap.getUiSettings().setZoomControlsEnabled(false);
        //Zoom level
        mMap.setMaxZoomPreference(mMap.getMaxZoomLevel());
        mMap.setMinZoomPreference(mMap.getMinZoomLevel());
        //Location control
        ///mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //Compass Control
        mMap.getUiSettings().setCompassEnabled(false);
        //GMap Link
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //Map style
        if(xMap.setMapStyle(R.raw.aubergine_map_style))
            Toast.makeText(context, "Style done", Toast.LENGTH_LONG);//.show();
        else
            Toast.makeText(context, "Style parsing failed.", Toast.LENGTH_LONG);//.show();

        //Map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.addMarker(new MarkerOptions().position(SYDNEY).title("Marker in Sydney")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(MOUNTAIN_VIEW).title("Mountain")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(PERTH).title("Australie...draggable")
                .snippet("Ce marker peut etre deplacable").draggable(true)
                .alpha(0.5f)
                .zIndex(1.0f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        // Move the camera instantly to Sydney with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 5000, null);
        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(PERTH)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void locationEnabled() {
        // My location
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, ID_OF_REQUEST_PERMITION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MapsActivity.this, "Need PERMISSION", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
