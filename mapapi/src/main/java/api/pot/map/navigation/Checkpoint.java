package api.pot.map.navigation;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

public class Checkpoint {
    public LatLng location;

    public long reachedTime = -1;
    public float reachedDistance = -1;

    public int colorFill = Color.GRAY;
    public int colorStroke = Color.RED;

    public Checkpoint(LatLng location) {
        this.location = location;

    }
}
