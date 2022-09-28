package com.pot.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.util.List;

import api.pot.map.location.OnLocationsChangeListener;
import api.pot.map.location.XLocation;

public class SpeedActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);

        textView = findViewById(R.id.textView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!runtime_permissions()) {
            XLocation.activity(this)
                    .setLocationsChangeListener(new OnLocationsChangeListener(){
                @Override
                public void onLocationChange(LatLng latLng) {
                    textView.setText("\nlng=== "+latLng.longitude+"\nlat=== "+latLng.latitude);
                }

                @Override
                public void onSpeedChange(double speed) {
                    textView.setText(textView.getText()+"\nspeed=== "+speed+"\nkmph speed === "+XLocation.kmph(speed));
                }
            });

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        XLocation.removeLocationUpdates();
    }




    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onResume();
            } else {
                runtime_permissions();
            }
        }
    }
}
