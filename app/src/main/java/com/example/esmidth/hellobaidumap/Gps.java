package com.example.esmidth.hellobaidumap;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.location.Criteria;


/**
 * Created by Great Esmidth on 2016/1/23.
 * Stay Foolish
 */
public class Gps {
    private Location location = null;
    private LocationManager locationManager = null;
    private Context context = null;

    public Location getLocation() {
        return location;
    }

    public Gps(Context ctx) {
        context = ctx;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(getProvider());
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
    }

    public String getProvider() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return locationManager.getBestProvider(criteria, true);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location locatioln) {
            if (locatioln != null)
                locatioln = locatioln;

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l != null)
                location = l;
        }

        @Override
        public void onProviderDisabled(String provider) {
            location = null;
        }
    };

    public void closeLocation() {
        if (locationManager != null) {
            if (locationListener != null) {
                locationManager.removeUpdates(locationListener);
                locationListener = null;
            }
            locationManager = null;
        }
    }
}
