package api.pot.map.direction;

import com.google.android.gms.maps.model.LatLng;

public interface DirectionListener {
    void onDirectionLoaded(String response);
    void onDirectionLoadingError(String response);
    void onDirectionReady();
    void onDirectionEnd();
    //void onDirectionParsing(LatLng point);
}
