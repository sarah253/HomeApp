package com.example.homeapp;

import android.content.Intent;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationTracker {
    MainActivity mainActivity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    public LocationTracker(final MainActivity mainActivity){
        this.mainActivity = mainActivity;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity);
        locationRequest = LocationRequest.create().setInterval(5000).setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult == null){
                    return;
                }
                for(Location location : locationResult.getLocations()){
                    Intent intent = new Intent().setAction("Location");
                    intent.putExtra("latitude",location.getLatitude());
                    intent.putExtra("longitude",location.getLongitude());
                    intent.putExtra("accuracy",location.getAccuracy());
                    mainActivity.sendBroadcast(intent);
                }
            }
        };

    }

    public void startTracking(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,Looper.getMainLooper());


    }

    public void stopTracking(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

}
