package api.pot.map.location;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.maps.model.LatLng;

public interface OnLocationsChangeInterface {
    void onLocationChange(LatLng latLng);
    void onSpeedChange(double speed);
    void onLocationAvailability(LocationAvailability locationAvailability);
}
