package api.pot.map.tools;

import com.google.android.gms.maps.model.LatLng;

public class Interception {
    public LatLng location;
    public Segment segment1;
    public Segment segment2;

    public Interception(Segment s1, Segment s2) {
        this.segment1 = s1;
        this.segment2 = s2;
        ///---
        double x = (s2.b-s1.b)/(s1.a-s2.a);
        double y = (s1.a*s2.b-s2.a*s1.b)/(s1.a-s2.a);
        location = new LatLng(y, x);
    }
}
