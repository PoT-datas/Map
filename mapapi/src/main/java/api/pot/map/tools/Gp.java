package api.pot.map.tools;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Geo Positionning*/
public class Gp {

    public static double distance(Marker point1, Marker point2) {
        return distance(point1.getPosition(), point2.getPosition());
    }

    public static double distance(LatLng point1, LatLng point2) {
        try {
            return distance(point1.latitude, point1.longitude, point2.latitude, point2.longitude);
        }catch (Exception e){
            return -1;
        }
    }

    /**
     * @return distance in miles*/
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    public static boolean isGeoPosition(String data){
        //return data!=null && data.matches("-?[1-9][0-9]*(\\.[0-9]+)?,\\s*-?[1-9][0-9]*(\\.[0-9]+)?\n");
        return data!=null && data.matches("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$");
    }

    private static Handler uiHandler;

    public static void getGeoPoints(final String script, final GpListener gpListener) {
        if(uiHandler==null) uiHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<LatLng> points = new ArrayList<>();
                try{
                    boolean formatLatLng = true;
                    if(script.contains("#T.format:lng,lat")) formatLatLng = false;
                    //
                    String dd = "[+-]?\\d{1,2}([.]\\d+)?(,)( )?[+-]?\\d{1,3}([.]\\d+)?",
                            ddm = "[+-]?\\d{1,2}( )[+-]?\\d{1,2}([.]\\d+)?(,)( )[+-]?\\d{1,3}( )[+-]?\\d{1,2}([.]\\d+)?",
                            dms = "\\d{1,2}(°)\\d{1,2}(')\\d{1,4}([.]\\d+)?(\")( )?[N,S]( )?\\d{1,3}(°)\\d{1,2}(')\\d{1,4}([.]\\d+)?(\")( )?[W,E]";
                    String regex_phoneNumber = "(?!(0-9))(?!(a-z))(?!(A-Z))(\\+[0-9]{1,3}( )?)?((([0-9])(( )?[0-9]){7,11})|(\\([0-9]{1,4}\\)( )[0-9]{1,4}-[0-9]{1,4}))(?!(0-9))(?!(a-z))(?!(A-Z))";
                    //
                    Pattern pattern_dd = Pattern.compile(dd), pattern_ddm = Pattern.compile(ddm), pattern_dms = Pattern.compile(dms);
                    Matcher matcher_dd = pattern_dd.matcher(script), matcher_ddm = pattern_ddm.matcher(script), matcher_dms = pattern_dms.matcher(script);
                    //
                    int fromIndex = 0;
                    int last_fromIndex = script.length();
                    //
                    int nbr=0;
                    while (true){
                        if(true){
                            if (matcher_dms.find(fromIndex)){
                                int startIndex = matcher_dms.start();
                                int endIndex = fromIndex = matcher_dms.end();
                                final String value = matcher_dms.group();
                                double[] geopos = GeoPos.getPosFromDMS(value);
                                ///---setGeoPos(startIndex, endIndex, geopos[0], geopos[1], ss, text);
                                ///----points.add(new LatLng(geopos[0], geopos[1]));
                                if(formatLatLng) points.add(new LatLng(geopos[0], geopos[1]));
                                else points.add(new LatLng(geopos[1], geopos[0]));
                                nbr++;
                                /**Log.s(context, text.substring(startIndex, endIndex));
                                 break;*/
                            }else if (matcher_ddm.find(fromIndex)){
                                int startIndex = matcher_ddm.start();
                                int endIndex = fromIndex = matcher_ddm.end();
                                final String value = matcher_ddm.group();
                                double[] geopos = GeoPos.getPosFromDDM(value);
                                ///---setGeoPos(startIndex, endIndex, geopos[0], geopos[1], ss, text);
                                ///----points.add(new LatLng(geopos[0], geopos[1]));
                                if(formatLatLng) points.add(new LatLng(geopos[0], geopos[1]));
                                else points.add(new LatLng(geopos[1], geopos[0]));
                                nbr++;
                                /**Log.s(context, text.substring(startIndex, endIndex));
                                 break;*/
                            }else if (matcher_dd.find(fromIndex)){
                                int startIndex = matcher_dd.start();
                                int endIndex = fromIndex = matcher_dd.end();
                                final String value = matcher_dd.group();
                                double[] geopos = GeoPos.getPosFromDD(value);
                                ///----points.add(new LatLng(geopos[0], geopos[1]));
                                ///---setGeoPos(startIndex, endIndex, geopos[0], geopos[1], ss, text);
                                if(formatLatLng) points.add(new LatLng(geopos[0], geopos[1]));
                                else points.add(new LatLng(geopos[1], geopos[0]));
                                nbr++;
                                /**Log.s(context, text.substring(startIndex, endIndex));
                                 break;*/
                            }
                        }
                        fromIndex++;
                        //
                        if(last_fromIndex==fromIndex) break;
                        ///---last_fromIndex = fromIndex;
                    }
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(gpListener!=null) gpListener.onParsingGeoPosEnd(points);
                        }
                    });
                }catch (Exception e){
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(gpListener!=null) gpListener.onParsingGeoPosEnd(points);
                        }
                    });
                }
            }
        }).start();
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static NearestSpot findNearestPoint(LatLng origin, List<LatLng> target) {
        double distance = -1;
        NearestSpot minimumDistancePoint = new NearestSpot();
        minimumDistancePoint.spot = target!=null&&target.size()==1?target.get(0):null;
        minimumDistancePoint.iOrigin = target!=null&&target.size()==1?0:-1;

        if (origin == null || target == null) {
            return minimumDistancePoint;
        }

        for (int i = 0; i < target.size(); i++) {
            LatLng point = target.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= target.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(origin, point, target.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                minimumDistancePoint.spot = findNearestPoint(origin, point, target.get(segmentPoint));
                minimumDistancePoint.iOrigin = i;
            }
        }

        if(minimumDistancePoint.spot==null)
            minimumDistancePoint = null;

        return minimumDistancePoint;
    }

    /**public static LatLng findNearestPoint(LatLng origin, List<LatLng> target) {
        double distance = -1;
        LatLng minimumDistancePoint = target!=null&&target.size()==1?target.get(0):null;

        if (origin == null || target == null) {
            return minimumDistancePoint;
        }

        for (int i = 0; i < target.size(); i++) {
            LatLng point = target.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= target.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(origin, point, target.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                minimumDistancePoint = findNearestPoint(origin, point, target.get(segmentPoint));
            }
        }

        return minimumDistancePoint;
    }*/

    /**public static LatLng findNearestPoint(LatLng test, List<LatLng> target) {
        double distance = -1;
        LatLng minimumDistancePoint = test;

        if (test == null || target == null) {
            return minimumDistancePoint;
        }

        for (int i = 0; i < target.size(); i++) {
            LatLng point = target.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= target.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(test, point, target.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                minimumDistancePoint = findNearestPoint(test, point, target.get(segmentPoint));
            }
        }

        return minimumDistancePoint;
    }*/

    public static LatLng findNearestPoint(final LatLng p, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return start;
        }

        final double s0lat = Math.toRadians(p.latitude);
        final double s0lng = Math.toRadians(p.longitude);
        final double s1lat = Math.toRadians(start.latitude);
        final double s1lng = Math.toRadians(start.longitude);
        final double s2lat = Math.toRadians(end.latitude);
        final double s2lng = Math.toRadians(end.longitude);

        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
        if (u <= 0) {
            return start;
        }
        if (u >= 1) {
            return end;
        }

        return new LatLng(start.latitude + (u * (end.latitude - start.latitude)),
                start.longitude + (u * (end.longitude - start.longitude)));
    }

    public static double calculateArea(LatLng... latLngs) {
        List<LatLng> latLngList = new ArrayList<>();
        if(latLngs!=null){
            for (LatLng latLng : latLngs)
                latLngList.add(latLng);
        }
        return calculateArea(latLngList);
    }

    public static double calculateArea(List<LatLng> latLngs) {
        return SphericalUtil.computeArea(latLngs);
    }

    public static boolean polygonContains(LatLng point, List<LatLng> polygon) {
        if(polygon==null || polygon.isEmpty() || polygon.size()<3) return false;
        return PolyUtil.containsLocation(point, polygon, true);
    }

    public static List<Interception> calculateInterceptions(GoogleMap mMap, List<LatLng> p1, List<LatLng> p2){
        List<Segment> segments = new ArrayList<>();
        List<Interception> interceptions = new ArrayList<>();

        int n = p1.size();
        int m = p2.size();
        for(int i=0;i<n;i++){
            if(polygonContains(p1.get(i), p2)!=polygonContains(p1.get((i+1)%n), p2)) {
                segments.add(new Segment(p1, i, (i+1)%n));

                mMap.addMarker(new MarkerOptions().position(p1.get(i))
                        .title("segment")
                        .snippet("Description")
                        .alpha(0.5f)
                        .zIndex(0.5f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.addMarker(new MarkerOptions().position(p1.get((i+1)%n))
                        .title("segment")
                        .snippet("Description")
                        .alpha(0.5f)
                        .zIndex(0.5f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }

        Segment segment;
        for(int i=0;i<m;i++){
            segment = new Segment(p2, i, (i+1)%m);
            for(int k=0;k<segments.size();k++){
                if(!segments.get(k).intercepted && segment.intercept(segments.get(k))) {
                    interceptions.add(new Interception(segments.get(k), segment));
                    segments.get(k).intercepted = true;

                    mMap.addMarker(new MarkerOptions().position(segment.polygon.get(segment.i))
                            .title("segment2")
                            .snippet("Description")
                            .alpha(0.5f)
                            .zIndex(0.5f)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.addMarker(new MarkerOptions().position(segment.polygon.get(segment.j))
                            .title("segment2")
                            .snippet("Description")
                            .alpha(0.5f)
                            .zIndex(0.5f)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
            }
        }

        return interceptions;
    }

    static int nexter = 0;
    public static List<LatLng> calculatePolygonBounds(GoogleMap mMap, List<LatLng>... polygons){
        try {
            if(polygons==null||polygons.length<2) {
                if(polygons!=null&&polygons.length==1) return polygons[0];
                else return null;
            }
            List<List<LatLng>> P = new ArrayList<>();
            for(int i=0;i<polygons.length;i++)
                P.add(polygons[i]);
            List<LatLng> b;
            List<LatLng> bounds = P.get(0);
            int n = P.size();
            nexter = 0;
            for(int i=1;i<P.size();i++) {
                b = getPolygonBounds(mMap, bounds, P.get(i));
                if(b==null){
                    P.add(P.get(i));
                    P.remove(i);
                    i--;
                    nexter++;
                }else {
                    bounds = b;
                    if(nexter!=0) nexter = 0;
                }
                if(nexter==(n-i+2)) break;
            }
            return bounds;
        }catch (Exception e){}
        return null;
    }

    public static List<LatLng> calculatePolygonBounds2(GoogleMap mMap, List<LatLng>... polygons){
        try {
            if(polygons==null||polygons.length<2) {
                if(polygons!=null&&polygons.length==1) return polygons[0];
                else return null;
            }
            List<LatLng> bounds = polygons[0];
            for(int i=1;i<polygons.length;i++)
                bounds = getPolygonBounds(mMap, bounds, polygons[i]);
            return bounds;
        }catch (Exception e){}
        return null;
    }

    public static List<LatLng> getPolygonBounds(GoogleMap mMap, List<LatLng> p1, List<LatLng> p2){
        return getPolygonBounds(mMap,
                calculateInterceptions(mMap, p1, p2));
    }

    public static List<LatLng> getPolygonBounds(GoogleMap mMap, List<Interception> interceptions){
        if(interceptions==null||interceptions.size()<2) return null;

        List<Interception> extremums = getExtremumInterceptions(interceptions);
        List<LatLng> p = new ArrayList<>();

        Toast.makeText(context, cmp+"::: \n"+interceptions.size()+" i|e "+extremums.size(), Toast.LENGTH_SHORT).show();

        ///---Toast.makeText(context, "|"+interceptions.size(), Toast.LENGTH_SHORT).show();

        if(extremums==null||extremums.size()<2) return null;

        ///---testing
        /**if(extremums.get(0).segment2.i==0||
                extremums.get(1).segment2.i==0||
                extremums.get(1).segment1.i==0||
                extremums.get(0).segment1.i==0) Collections.reverse(extremums);*/

        int i, j;
        List<LatLng> p1 = extremums.get(0).segment1.polygon, p2 = extremums.get(0).segment2.polygon;
        //
        p.add(extremums.get(0).location);
        i = extremums.get(0).segment2.i;
        j = extremums.get(1).segment2.i;
        p.addAll(subPolygon(p2, i, j));
        //
        p.add(extremums.get(1).location);
        i = extremums.get(1).segment1.i;
        j = extremums.get(0).segment1.i;
        p.addAll(subPolygon(p1, i, j));
        //
        cmp++;
        ///---
        //closing
        if(p.size()>0) p.add(p.get(0));
        //
        return p;
    }

    public static Context context;
    private static int cmp = 0;
    private static List<LatLng> subPolygon(List<LatLng> p1, int i, int j) {
        ///---Toast.makeText(context, p1.size()+" : "+i+"|"+j, Toast.LENGTH_SHORT).show();
        List<LatLng> p = new ArrayList<>();
        int k, n = p1.size();
        if(0<=i&&i<=j||i==0/**||j==0*/){
            if(i<j){
                for(k=i;k<j;k++)
                    p.add(p1.get(k%n));
            }else if(i>j){
                for(k=i;k>j;k--)
                    p.add(p1.get(k%n));
            }
            ///---Toast.makeText(context, "up", Toast.LENGTH_SHORT).show();
        }else {
            if(i<j){
                for(k=j;k<n+i;k++)
                    p.add(p1.get(k%n));
            }else if(i>j){
                for(k=i;k<n+j;k++)
                    p.add(p1.get(k%n));
            }
            ///---Toast.makeText(context, "down", Toast.LENGTH_SHORT).show();
        }
        return p;
    }

    public static List<Interception> getExtremumInterceptions(List<Interception> interceptions){
        List<Interception> extremums = new ArrayList<>();
        double d;
        double dMax=0;
        int n = interceptions.size();
        /**
         * une combinaison de la listes des inters sera pas utile car les extremes sont cotes à cote
         */
        for(int i=0;i<n;i++){
            d = minDistance(interceptions.get(i).segment1.i, interceptions.get((i+1)%n).segment1.i, interceptions.get(i).segment1.polygon);
            if(d>=dMax){
                dMax = d;
                extremums = new ArrayList<>();
                if((i+1)%n>i){
                    extremums.add(interceptions.get((i+1)%n));
                    extremums.add(interceptions.get(i));
                }else {
                    extremums.add(interceptions.get(i));
                    extremums.add(interceptions.get((i+1)%n));
                }
            }
        }
        ///---
        if(interceptions.size()>1 && extremums.size()==0){
            extremums.add(interceptions.get(0));
            extremums.add(interceptions.get(1));
        }
        ///---sense---calcul de la surface minimale
        if(extremums.size()>1){
            int i = extremums.get(0).segment1.i;
            int j = extremums.get(1).segment1.i;
            int i2 = extremums.get(0).segment2.i;
            int j2 = extremums.get(1).segment2.i;
            ///---
            /**if(iDistance(i2, j2, extremums.get(0).segment2.polygon)<
                    iDistance(j2, i2, extremums.get(0).segment2.polygon))///attention à cette condition.!!
                Collections.reverse(extremums);*/
            List<LatLng> p1 = extremums.get(0).segment1.polygon;
            List<LatLng> p2 = extremums.get(0).segment2.polygon;
            int n2 = p2.size();
            if(i2<=j2){
                /**if( polygonContains(p2.get(i2+(j2-i2)/2), p1) )
                    Collections.reverse(extremums);*/
                if( distance(p2.get(i2+(j2-i2)/2), p1)<distance(p2.get((j2+(n2+i2-j2)/2)%n2), p1) )
                    Collections.reverse(extremums);
            }else {
                /**if( polygonContains(p2.get((i2+(n2+j2-i2)/2)%n2), p1) )
                    Collections.reverse(extremums);*/
                if( distance(p2.get((i2+(n2+j2-i2)/2)%n2), p1)<distance(p2.get(j2+(i2-j2)/2), p1) )
                    Collections.reverse(extremums);
            }
        }
        ///---
        return extremums;
    }

    public static double distance(LatLng point, List<LatLng> polygon) {
        try {
            return distance(point, findNearestPoint(point, polygon).spot);
        }catch (Exception e){}
        return -1;
    }

    public static double distance(LatLng center, List<LatLng> p1, List<LatLng> p2) {
        try {
            return distance(findNearestPoint(center, p1).spot, findNearestPoint(center, p2).spot);
        }catch (Exception e){}
        return -1;
    }

    public static int iDistance(int i, int j, List<LatLng> p) {
        int n = p.size();
        if(i<=j){
            return j-i+1;
        }else {
            return (n-i)+j+1;
        }
    }

    private static double minDistance(int i, int j, List<LatLng> p) {
        return Math.min(distance(i, j, p), distance(j, i, p));
    }

    private static double distance(int i, int j, List<LatLng> p) {
        if(i>=p.size()||j>=p.size())
            return -1;
        int k;
        double some = 0;
        int n = p.size();
        if(i<j){
            for(k=i;k<j;k++)
                some+=distance(p.get(k), p.get(k+1));
        }else if(i>j){
            for(k=i;k<n+j;k++)
                some+=distance(p.get(k%n), p.get((k+1)%n));
        }
        return some;
    }

    /**private void createSurroundingPolygon(List<LatLng> polygonPath) {
        List<Coordinate> coordinates = new ArrayList<>();
        for (LatLng latLng : polygonPath) {
            coordinates.add(new Coordinate(latLng.longitude, latLng.latitude));
        }

        GeometryFactory factory = new GeometryFactory();
        Geometry lineString = factory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
        Polygon polygon = (Polygon) BufferOp.bufferOp(lineString, 0.0001);

        Coordinate[] coordinatesSurroundingPolygon = polygon.getExteriorRing().getCoordinates();
        List<LatLng> surroundingPolygon = new ArrayList<>();
        for (int i = 0; i < coordinatesSurroundingPolygon.length; i++) {
            surroundingPolygon.add(new LatLng(coordinatesSurroundingPolygon[i].y, coordinatesSurroundingPolygon[i].x));
        }
        ///drawPolygon(surroundingPolygon);
    }*/

    /**Location from = new Location(LocationManager.GPS_PROVIDER);
        Location to = new Location(LocationManager.GPS_PROVIDER);
        from.setLatitude(org.latitude);
        from.setLongitude(org.longitude);
        to.setLongitude(target.latitude);
        to.setLatitude(target.longitude);
        return from.bearingTo(to);*/

    public static float getAngularOffset(LatLng org, LatLng target) {
        return (float) bearing(org.latitude, org.longitude, target.latitude, target.longitude);
    }

    protected static double bearing(double startLat, double startLng, double endLat, double endLng){
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff= Math.toRadians(endLng - startLng);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    public static LatLng getSpotInDir(LatLng origin, LatLng target, double distance) {
        try {
            double d1 = distance(origin, target);
            if(d1<0.000621d) return origin;
            double d2 = distance;
            double ratio = d2/d1;
            double lat = origin.latitude+ratio*(target.latitude-origin.latitude);
            double lon = origin.longitude+ratio*(target.longitude-origin.longitude);
            return new LatLng(lat, lon);
        }catch (Exception e){}
        return origin;
    }

    public static float getRightZoom(double distance) {
        double max = 0.000621d*500;
        if(distance<=max){
            return 17;
        }
        max *= 5;
        if(distance<=max){
            return 15;
        }
        max *= 10;
        if(distance<=max){
            return 11;
        }
        max *= 20;
        if(distance<=max){
            return 8;
        }
        max *= 40;
        if(distance<=max){
            return 5;
        }
        max *= 80;
        if(distance<=max){
            return 2;
        }
        return 1;
        ////---return (float) (21-distance*4/meterToMile(500));
    }

    public static double meterFromMile(double miles) {
        return miles*1609.34f;
    }

    public static double meterToMile(double meters) {
        return meters*0.000621d;
    }

    public static LatLng findLocationMiddle(List<LatLng> p) {
        if(p.size()==0) return null;
        try {
            return findNearestPoint(getMiddle(p.get(0), p.get(p.size()-1)), p).spot;
        }catch (Exception e){}
        return p.get(0);
    }

    private static LatLng getMiddle(LatLng A, LatLng B) {
        return new LatLng((A.latitude+B.latitude)/2, (A.longitude+B.longitude)/2);
    }
}
