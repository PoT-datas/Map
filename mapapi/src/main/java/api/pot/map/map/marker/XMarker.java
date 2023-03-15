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
import android.graphics.Rect;
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
    private static long duration = 1000;
    public static void setDuration(long duration) {
        if(duration<=100) return;
        XMarker.duration = duration;
    }

    public static long getDuration() {
        return duration;
    }

    public static void animateMarker(GoogleMap googleMap, final Marker marker, final LatLng toPosition,
                                     final MarkerAnimListener markerAnimListener, final boolean hideMarker) {
        animateMarker(googleMap, marker, toPosition, markerAnimListener, hideMarker, duration);
    }

    public static void animateMarker(GoogleMap googleMap, final Marker marker, final LatLng toPosition,
                                     final MarkerAnimListener markerAnimListener, final boolean hideMarker, final long duration) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new LinearInterpolator();

        if(markerAnimListener!=null) markerAnimListener.onMarkerAnimStart();

        handler.post(new Runnable() {
            @Override
            public void run() {
                float t = 0;

                try{
                    long elapsed = SystemClock.uptimeMillis() - start;
                    t = interpolator.getInterpolation((float) elapsed
                            / duration);
                    double lng = t * toPosition.longitude + (1 - t)
                            * startLatLng.longitude;
                    double lat = t * toPosition.latitude + (1 - t)
                            * startLatLng.latitude;
                    marker.setPosition(new LatLng(lat, lng));
                }catch (Exception e){}

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
    }/**

    public static Bitmap getIconeFromBitmap(Bitmap bmp, int color, int strokeColor){
        //return getIconeFromBitmap(bmp, color, 70, 100);
        return getIconeFromBitmap(bmp, color, strokeColor, 100, 150);
    }*/

    private static Paint paint;
    private static Paint paintBmp;
    private static RectF bound;
    private static RectF circle;
    private static Path path;
    private static int margin = 5;
    private static int strock = 10;
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

    public static Bitmap getIconeFromBitmap(Bitmap bmp, int color, int strokeColor, int width, int height, Path form){
        if(width<=0 || height<=0 || bmp==null) return null;
        ///---
        ///---strock *= 2;
        ///---
        paint = new Paint();
        paint.setStrokeWidth(strock);
        paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
        ///
        bound = new RectF(0+margin, 0+margin, width-margin, height-margin);
        ///
        float offL = bound.height()/4;
        float offXl = bound.height()/3;
        path = new Path();
        path.moveTo(bound.left, bound.top);
        path.lineTo(bound.right-offL, bound.top);
        path.quadTo(bound.right, bound.top, bound.right, bound.top+offL);
        path.lineTo(bound.right, bound.bottom);
        path.quadTo(bound.right, bound.bottom-offXl, bound.right-offXl, bound.bottom-offXl);
        path.lineTo(bound.left+offXl, bound.bottom-offXl);
        path.quadTo(bound.left, bound.bottom-offXl, bound.left, bound.bottom-offXl-offL);
        path.close();
        ///
        circle = new RectF(bound.left+strock*3, bound.top+strock*3, bound.right, bound.bottom-offXl);
        ///
        Bitmap icone = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(icone);
        ///----
        int save = cvs.save();
        ///----
        cvs.clipPath(form);
        //
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        //
        cvs.drawPath(path, paint);
        //
        paintBmp = new Paint();
        paintBmp.setAntiAlias(true);
        paintBmp.setStyle(Paint.Style.FILL);
        /*paintBmp.setShader(new BitmapShader(getResizedBitmap(bmp,
                (int) circle.width()-2*strock, (int) circle.height()-2*strock),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));*/
        paintBmp.setMaskFilter(new BlurMaskFilter(strock*2, BlurMaskFilter.Blur.NORMAL));
        //
        ///---cvs.drawRect(circle, paintBmp);
        cvs.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()),
                new Rect( (0+strock), (0+strock), (cvs.getWidth()-strock), (int)(cvs.getHeight()-offXl-strock/2)), paintBmp);
        ///---
        cvs.restoreToCount(save);
        //
        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        //
        cvs.drawLine(bound.centerX(), bound.bottom, bound.centerX(), bound.bottom-bound.bottom/3, paint);
        //
        cvs.drawPath(form, paint);
        ///---
        return icone;
    }

    /**public static Bitmap getIconeFromBitmap(Bitmap bmp, int color, int strokeColor, int width, int height){
        if(width<=0 || height<=0 || bmp==null) return null;
        paint = new Paint();
        paint.setStrokeWidth(strock);
        paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
        ///
        bound = new RectF(0+margin, 0+margin, width-margin, height-margin);
        ///
        float offL = bound.height()/4;
        float offXl = bound.height()/3;
        path = new Path();
        path.moveTo(bound.left, bound.top);
        path.lineTo(bound.right-offL, bound.top);
        path.quadTo(bound.right, bound.top, bound.right, bound.top+offL);
        path.lineTo(bound.right, bound.bottom);
        path.quadTo(bound.right, bound.bottom-offXl, bound.right-offXl, bound.bottom-offXl);
        path.lineTo(bound.left+offXl, bound.bottom-offXl);
        path.quadTo(bound.left, bound.bottom-offXl, bound.left, bound.bottom-offXl-offL);
        path.close();
        ///
        circle = new RectF(bound.left+strock*3, bound.top+strock*3, bound.right, bound.bottom-offXl);
        ///
        Bitmap icone = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(icone);
        ///----
        int save = cvs.save();
        ///----
        cvs.clipPath(path);
        //
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        //
        cvs.drawPath(path, paint);
        //
        paintBmp = new Paint();
        paintBmp.setAntiAlias(true);
        paintBmp.setStyle(Paint.Style.FILL);
        paintBmp.setMaskFilter(new BlurMaskFilter(strock*2, BlurMaskFilter.Blur.NORMAL));
        //
        ///---cvs.drawRect(circle, paintBmp);
        cvs.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()),
                new Rect( (0+strock), (0+strock), (cvs.getWidth()-strock), (int)(cvs.getHeight()-offXl-strock)), paintBmp);
        //
        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        //
        cvs.drawPath(path, paint);
        ///---
        cvs.restoreToCount(save);
        ///---
        return icone;
    }*/

    /*public static Bitmap getIconeFromBitmap(Bitmap bmp, int color, int width, int height){
        if(width<=0 || height<=0 || bmp==null) return null;
        //
        paint = new Paint();
        paint.setStrokeWidth(strock);
        paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
        ///
        bound = new RectF(0+margin, 0+margin, width-margin, height-margin);
        ///
        path = new Path();
        path.moveTo(bound.left, bound.centerY());
        circle = new RectF(bound.left, bound.top, bound.right, bound.top+bound.width());
        path.addArc(circle, 150, 240);
        path.lineTo(bound.centerX(), bound.bottom);
        path.close();
        ///
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
    }*/

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

    public static Bitmap getPlotBitmap(int width, int height, int color) {
        Bitmap icone = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(icone);
        ///
        float soft = width/20;
        ///
        RectF bound = new RectF(0+soft, 0+soft, width-soft, height-soft);
        Paint paint = new Paint();
        paint.setColor(color);
        ///
        paint.setMaskFilter(new BlurMaskFilter(soft/5, BlurMaskFilter.Blur.NORMAL));
        paint.setShadowLayer(soft,soft/3, soft, Color.BLACK);
        ///
        cvs.drawCircle(bound.centerX(), bound.top+bound.height()/3, bound.top+bound.height()/4, paint);
        ///
        Path path = new Path();
        path.moveTo(bound.centerX(), bound.bottom);
        path.rLineTo(-bound.width()/4, -bound.width()/3);
        path.rLineTo(bound.width()/4, bound.width()/5);
        path.rLineTo(bound.width()/4, -bound.width()/5);
        path.close();
        ///
        cvs.drawPath(path, paint);
        ///
        return icone;
    }
}
