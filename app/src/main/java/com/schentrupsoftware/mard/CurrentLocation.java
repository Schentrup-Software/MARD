package com.schentrupsoftware.mard;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class CurrentLocation implements LocationListener {

    private double longitude;
    private double latitude;

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
