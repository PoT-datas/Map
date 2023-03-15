package api.pot.map.geocoding;

import api.pot.map.geocoding.Constants.AddressTypes;
import api.pot.map.geocoding.Constants.LocationTypes;
import api.pot.map.geocoding.Models.Coordinate;

public class XGeocoding {
    private String googleKeyApi;
    private GeocodingListener geocodingCallback;

    public XGeocoding(String googleKeyApi) {
        this.googleKeyApi = googleKeyApi;
    }

    public XGeocoding listener(GeocodingListener geocodingCallback){
        this.geocodingCallback = geocodingCallback;
        return this;
    }

    public void addressSearch(String address) {
        new Geocoding(address, googleKeyApi)
                .setLanguage("sv")
                .addComponent(AddressTypes.ADMINISTRATIVE_AREA_LEVEL_1, "Stockholm")
                .fetch(geocodingCallback);
    }

    public void coordinateSearch(Coordinate coordinate) {
        new ReverseGeocoding(coordinate, googleKeyApi)
                .setLocationTypes(LocationTypes.ROOFTOP)
                .setResultTypes(AddressTypes.STREET_ADDRESS)
                .isSensor(true)
                .fetch(geocodingCallback);
    }

    public void placeIdSearch(String placeId) {
        new ReverseGeocoding(placeId, googleKeyApi)
                .setLanguage("sv")
                .fetch(geocodingCallback);
    }





    /*private XGeocoding xGeocoding;
    private static final String TAG = "TGPS ::: MainActivity";
    private void geocoding() {
        loggingLevel = HttpLoggingInterceptor.Level.BASIC;

        xGeocoding = new XGeocoding(getString(R.string.google_map_key));
        xGeocoding.listener(new GeocodingListener() {
            @Override
            public void onResponse(Response response) {
                Log.d(TAG, "Status code: " + response.getStatus());
                Log.d(TAG, "Responses: " + response.getResults().length);

                for (Result result : response.getResults()) {
                    Log.d(TAG, "   Formatted address: " + result.getFormattedAddress());
                    Log.d(TAG, "   Place ID: " + result.getPlaceId());
                    Log.d(TAG, "   Location: " + result.getGeometry().getLocation());
                    Log.d(TAG, "       Location type: " + result.getGeometry().getLocationType());
                    Log.d(TAG, "       SouthWest: " + result.getGeometry().getViewport().getSouthWest());
                    Log.d(TAG, "       NorthEast: " + result.getGeometry().getViewport().getNorthEast());
                    Log.d(TAG, "   Types:");
                    for (int i = 0; i < result.getAddressTypes().length; i++)
                        Log.d(TAG, "       " + result.getAddressTypes()[i]);
                }

                search_area_submit.setEnabled(true);
                if(response.getResults().length!=0){
                    LatLng location = new LatLng(response.getResults()[0].getGeometry().getLocation().getLatitude(),
                            response.getResults()[0].getGeometry().getLocation().getLongitude());
                    xMap.camera.target(location);
                    if(location!=null){
                        markers.set(2,
                                mMap.addMarker(new MarkerOptions().position(location)
                                        .title(autoCompletePlace.getText().toString())
                                        .snippet("Description")
                                        .zIndex(1.0f)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        );


                        markerFrom = MY_LOCATION_MARKER;
                        markerTo = markers.get(markers.size()-1);
                        mapFragment.notifyMarkers(true, MY_LOCATION_MARKER);
                        mapFragment.notifyMarkers(fromSelected, markers.get(markers.size()-1));
                        setLocation(false);

                        setArcs(markers.size(), mapAddObj);
                    }
                /*CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(HERE)      // Sets the center of the map to Mountain View
                        .zoom(ZOOM_LEVEL)
                        .bearing(ROTATION_LEVEL)                // Sets the orientation of the camera to east
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
                    //
                    /*xMap.camera.updateCamera();
                    //
                    setLocation(false);
                }
            }

            @Override
            public void onFailed(Response response, IOException exception) {
                if (response != null) Log.e(TAG, (response.getErrorMessage() == null) ? response.getStatus() : response.getErrorMessage());
                else Log.e(TAG, exception.getLocalizedMessage());

                responsePlace.setText("Not Found!!!");
                search_area_submit.setEnabled(true);
            }
        });
    }*/


}
