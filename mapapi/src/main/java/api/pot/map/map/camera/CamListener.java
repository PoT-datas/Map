package api.pot.map.map.camera;

import com.google.android.gms.maps.model.LatLng;

public interface CamListener {
    void horizonChange(float horizon);
    void zoomChange(float zoom);
    void targetChange(LatLng target);
    void myLocationChange(LatLng target);
}
