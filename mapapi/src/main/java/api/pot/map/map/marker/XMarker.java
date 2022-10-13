package api.pot.map.map.marker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class XMarker {

    public static void animateMarker(GoogleMap googleMap, final Marker marker, final LatLng toPosition,
                                     final MarkerAnimListener markerAnimListener, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        if(markerAnimListener!=null) markerAnimListener.onMarkerAnimStart();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                    if(markerAnimListener!=null) markerAnimListener.onMarkerAnimEnd();
                }
            }
        });
    }

    public static Bitmap getIconeFromPath(String path){
        return getIconeFromPath(path, Color.GRAY, 100, 150);
    }

    public static Bitmap getIconeFromPath(String path, int color){
        return getIconeFromPath(path, color, 70, 100);
    }

    public static Bitmap getIconeFromBitmap(Bitmap bmp, int color){
        return getIconeFromBitmap(bmp, color, 70, 100);
    }

    private static Paint paint;
    private static Paint paintBmp;
    private static RectF bound;
    private static RectF circle;
    private static Path path;
    private static int margin = 5;
    private static int strock = 5;
    public static Bitmap getIconeFromPath(String pathIcone, int color, int width, int height){
        if(width<=0 || height<=0) return null;
        //
        paint = new Paint();
        paint.setStrokeWidth(strock);
        paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
        //
        bound = new RectF(0+margin, 0+margin, width-margin, height-margin);
        path = new Path();
        path.moveTo(bound.left, bound.centerY());
        circle = new RectF(bound.left, bound.top, bound.right, bound.top+bound.width());
        path.addArc(circle, 150, 240);
        path.lineTo(bound.centerX(), bound.bottom);
        path.close();
        //
        Bitmap icone = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(icone);
        //
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        //
        cvs.drawPath(path, paint);
        //
        paintBmp = new Paint();
        paintBmp.setAntiAlias(true);
        paintBmp.setStyle(Paint.Style.FILL);
        paintBmp.setShader(new BitmapShader(decodeSampledBitmapFromPath(pathIcone,
                (int) circle.width()-2*strock, (int) circle.height()-2*strock),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        paintBmp.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
        //
        cvs.drawOval(circle, paintBmp);
        //
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        //
        cvs.drawPath(path, paint);
        //
        return icone;
    }
    public static Bitmap getIconeFromBitmap(Bitmap bmp, int color, int width, int height){
        if(width<=0 || height<=0 || bmp==null) return null;
        //
        paint = new Paint();
        paint.setStrokeWidth(strock);
        paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
        //
        bound = new RectF(0+margin, 0+margin, width-margin, height-margin);
        path = new Path();
        path.moveTo(bound.left, bound.centerY());
        circle = new RectF(bound.left, bound.top, bound.right, bound.top+bound.width());
        path.addArc(circle, 150, 240);
        path.lineTo(bound.centerX(), bound.bottom);
        path.close();
        //
        Bitmap icone = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(icone);
        //
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        //
        cvs.drawPath(path, paint);
        //
        paintBmp = new Paint();
        paintBmp.setAntiAlias(true);
        paintBmp.setStyle(Paint.Style.FILL);
        paintBmp.setShader(new BitmapShader(getResizedBitmap(bmp,
                (int) circle.width()-2*strock, (int) circle.height()-2*strock),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        paintBmp.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
        //
        cvs.drawOval(circle, paintBmp);
        //
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        //
        cvs.drawPath(path, paint);
        //
        return icone;
    }

    public static Bitmap getResizedBitmap(Bitmap originalBitmap, int newWidth, int newHeight){
        if(originalBitmap==null) return null;
        //
        if(newWidth<=0 && newHeight>0){
            newWidth = originalBitmap.getWidth() * newHeight / originalBitmap.getHeight();
        }else if(newWidth>0 && newHeight<=0){
            newHeight = originalBitmap.getHeight() * newWidth / originalBitmap.getWidth();
        } else if(newWidth<=0 && newHeight<=0) return null;
        //
        return Bitmap.createScaledBitmap(
                originalBitmap, newWidth, newHeight, false);
    }

    private static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            try{
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }catch (Exception e){}
        }

        return inSampleSize;
    }

    public static double distance(Marker point1, Marker point2) {
        return distance(point1.getPosition(), point2.getPosition());
    }

    public static double distance(LatLng point1, LatLng point2) {
        return distance(point1.latitude, point1.longitude, point2.latitude, point2.longitude);
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

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
