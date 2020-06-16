package com.example.homeapp;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
public class LocationTracker {
    Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    public LocationTracker(final Context context){
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
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
                    context.sendBroadcast(intent);
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
