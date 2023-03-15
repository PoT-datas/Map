package api.pot.map.tools;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface GpListener {
    void onParsingGeoPosEnd(List<LatLng> points);
}
