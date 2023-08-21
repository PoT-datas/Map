package api.pot.map.tools;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Segment {
    public List<LatLng> polygon;
    public int i;
    public int j;
    public double a;
    public double b;

    public boolean intercepted = false;

    public Segment(List<LatLng> polygon, int i, int j) {
        this.polygon = polygon;
        this.i = i;
        this.j = j;
        ///---
        a = (polygon.get(j).latitude-polygon.get(i).latitude)/(polygon.get(j).longitude-polygon.get(i).longitude);
        /**b = (polygon.get(i).longitude-polygon.get(j).longitude)/
                (polygon.get(i).longitude*polygon.get(j).latitude-polygon.get(j).longitude*polygon.get(i).latitude);*/
        b = (polygon.get(i).longitude*polygon.get(j).latitude-polygon.get(j).longitude*polygon.get(i).latitude)/
                (polygon.get(i).longitude-polygon.get(j).longitude);
    }

    public boolean intercept(Segment segment) {
        if( Math.min(polygon.get(i).latitude, polygon.get(j).latitude)>
                Math.max(segment.polygon.get(segment.i).latitude, segment.polygon.get(segment.j).latitude) ||
                Math.max(polygon.get(i).latitude, polygon.get(j).latitude)<
                Math.min(segment.polygon.get(segment.i).latitude, segment.polygon.get(segment.j).latitude) )
            return false;
        if( Math.min(polygon.get(i).longitude, polygon.get(j).longitude)>
                Math.max(segment.polygon.get(segment.i).longitude, segment.polygon.get(segment.j).longitude) ||
                Math.max(polygon.get(i).longitude, polygon.get(j).longitude)<
                Math.min(segment.polygon.get(segment.i).longitude, segment.polygon.get(segment.j).longitude) )
            return false;
        return true;
    }
}
