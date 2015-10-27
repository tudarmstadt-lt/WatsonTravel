package model;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import org.mapsforge.core.model.LatLong;

import java.util.List;


public class LocationListener implements android.location.LocationListener {
    private LocationManager locationManager;
    private static Location location;

    public LocationListener(Activity activity) {
        this.locationManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean isGPSEnabled() {
        System.out.println(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void initSingleRequest() {
        //location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,null);
        //locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
    }

    public void initLoopRequest() {
        //location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
    }

    public Location getLocation() {
        return this.location;
    }

    public double getLongitude()
    {
        if(this.location == null)
            return Double.MAX_VALUE;
        else
            return this.location.getLongitude();
    }

    public double getLatitude()
    {
        if(this.location == null)
            return Double.MAX_VALUE;
        else
            return this.location.getLatitude();
    }

    public void stopUpdates() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("CHANGED");
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        initSingleRequest();
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}