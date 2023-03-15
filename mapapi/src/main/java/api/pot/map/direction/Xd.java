package api.pot.map.direction;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

public class Xd {

    public static void getDirection(Context context, LatLng origin, LatLng dest, String dirMode,
                                    String googleKeyApi, DirectionListener directionListener){
        String url = getRequestedUrl(origin, dest, dirMode, googleKeyApi);

        /**this.origin = origin;
        this.dest = dest;
        this.googleKeyApi = googleKeyApi;
        this.directionListener = directionListener;*/

        //adding our stringrequest to queue
        Volley.newRequestQueue(context).add(volleyRequest(url, directionListener));
    }

    private static String getRequestedUrl(LatLng origin, LatLng dest, String dirMode, String google_map_key) {
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

    private static StringRequest volleyRequest(String url, final DirectionListener directionListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(directionListener !=null) directionListener.onDirectionLoaded(response);
            }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(directionListener !=null) directionListener.onDirectionLoadingError(error.getMessage());
                    }
                });
        return stringRequest;
    }
}
