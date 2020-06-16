package com.example.homeapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

public class TrackingWork extends Worker {

    private LocationInfo currentLocation;
    private LocationInfo homeLocationSaved;
    private SharedPreferences sharedPreferences;
    private LocationTracker locationTracker;
    private Gson gson;
    private BroadcastReceiver broadcastReceiver;
    private String phone_number;

    public TrackingWork(Context context, WorkerParameters workerParameters){
        super(context,workerParameters);
        sharedPreferences = getApplicationContext().getSharedPreferences("sp",Context.MODE_PRIVATE);
        gson = new Gson();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return Result.success();
        }
        String savedHome = sharedPreferences.getString("saved location",null);
        if(savedHome == null){
            return Result.success();
        }
        homeLocationSaved = gson.fromJson(savedHome,LocationInfo.class);
        if(homeLocationSaved.getAccuracy() == -1){
            return  Result.success();
        }
        String savedNumber = sharedPreferences.getString("phone Number",null);
        if(savedNumber == null){
            phone_number ="";
            return Result.success();
        }
        phone_number = gson.fromJson(savedNumber,String.class);
        if (phone_number.equals("")){
            return Result.success();
        }
        broadcastReceiver = new TrackingWork.MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter("Location");
        getApplicationContext().registerReceiver(broadcastReceiver, filter);
        locationTracker = new LocationTracker(getApplicationContext());
        locationTracker.stopTracking();
        return Result.success();
    }

    private double calculateDistance(LocationInfo current, LocationInfo previous){
        Location locationCurrent = new Location("current");
        locationCurrent.setLatitude(current.getLatitude());
        locationCurrent.setLongitude(current.getLongitude());
        Location locationPrevious = new Location("previous");
        locationPrevious.setLongitude(previous.getLongitude());
        locationPrevious.setLatitude(previous.getLatitude());
        return locationCurrent.distanceTo(locationPrevious);
    }

    private void sendMassage(){
        MyApp app = (MyApp) getApplicationContext();
        Intent intent = new Intent();
        intent.setAction("POST_PC.ACTION_SEND_SMS");
        intent.putExtra(app.localReceiver.PHONE_KEY, phone_number);
        intent.putExtra(app.localReceiver.CONTENT_KEY,
                "Honey I'm Home!");
        app.sendBroadcast(intent);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("Location")){
                float acc = intent.getFloatExtra("accuracy",50);
                if(acc < 50){
                    locationTracker.stopTracking();
                    getApplicationContext().unregisterReceiver(broadcastReceiver);
                    currentLocation = new LocationInfo(intent.getDoubleExtra("latitude",0),
                            intent.getDoubleExtra("longitude",0),acc);
                    String savedLocation = sharedPreferences.getString("PreviousLocation",null);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String toSave = gson.toJson(currentLocation);
                    editor.putString("PreviousLocation",toSave);
                    editor.apply();
                    if(savedLocation == null){
                        return;
                    }
                    LocationInfo lastLocationInfo = gson.fromJson(savedLocation, LocationInfo.class);
                    double distance1 = calculateDistance(currentLocation,lastLocationInfo);
                    if ((distance1 < 50)){
                        return;
                    }
                    double distance2 = calculateDistance(currentLocation,homeLocationSaved);
                    if(distance2 < 50){
                        sendMassage();
                    }


                }
            }
        }
    }

}
