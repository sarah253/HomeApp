package com.example.homeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView home_header;
    TextView home_location;
    TextView current_location;
    TextView latitude_text;
    TextView longitude_text;
    TextView accuracy_text;
    Button clear_button;
    Button control_button;
    Button set_button;
    Button set_sms;
    Button test_sms;
    BroadcastReceiver broadcastReceiver;
    double latitude;
    double longitude;
    float accuracy;
    SharedPreferences sharedPreferences;
    LocationInfo locationInfo;
    LocationTracker locationTracker;
    MyApp myApp;
    String phone_number;
    boolean track = false;
    int MYLocation_PERMISSIONS_REQUEST = 123;
    int MYSMS_PERMISSIONS_REQUEST = 1234;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        home_header = findViewById(R.id.homeHeader);
        home_location = findViewById(R.id.homeLocation);
        current_location = findViewById(R.id.currentLocation);
        latitude_text = findViewById(R.id.textViewLatitude);
        longitude_text = findViewById(R.id.textViewLongitude);
        accuracy_text = findViewById(R.id.textViewAccuracy);
        clear_button  = findViewById(R.id.clearButton);
        control_button = findViewById(R.id.buttonStart);
        set_button = findViewById(R.id.buttonSet);
        set_sms = findViewById(R.id.buttonSetSMS);
        test_sms = findViewById(R.id.buttonTstSms);
        sharedPreferences = getSharedPreferences("sp",MODE_PRIVATE);
        getLocation();
        getNumber();
        myApp = (MyApp) getApplicationContext();
        locationTracker = new LocationTracker(this);
        broadcastReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter("Location");
        registerReceiver(broadcastReceiver, filter);
        control_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (control_button.getText().equals("start tracking location")){
                    getPermission();
                }
                else {
                    control_button.setText("start tracking location");
                    latitude_text.setText("");
                    longitude_text.setText("");
                    accuracy_text.setText("");
                    set_button.setVisibility(View.INVISIBLE);
                    locationTracker.stopTracking();
                    track = false;
                }
            }
        });
        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHomeLocation();
            }
        });
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLocation();
            }
        });
        set_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermissionSms();
            }
        });
        test_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMassage();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MYLocation_PERMISSIONS_REQUEST);
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MYLocation_PERMISSIONS_REQUEST);
            }
        } else {
            control_button.setText("stop tracking");
            locationTracker.startTracking();
            track = true;
        }
    }

    private void getPermissionSms(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MYSMS_PERMISSIONS_REQUEST);
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MYSMS_PERMISSIONS_REQUEST);
            }
        } else {
            getNumberFromUser();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode == MYLocation_PERMISSIONS_REQUEST){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                control_button.setText("stop tracking");
                locationTracker.startTracking();
            }
            else {
                 new AlertDialog.Builder(this).setTitle("Allow Location Permission")
                        .setMessage("we need to use Location or the App cannot operate.")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MYLocation_PERMISSIONS_REQUEST);
                            }
                        }).setNegativeButton("Don't allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                }).create().show();
            }
        }
        if(requestCode == MYSMS_PERMISSIONS_REQUEST){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getNumberFromUser();
            }
            else {
                new AlertDialog.Builder(this).setTitle("Allow SMS Permission")
                        .setMessage("we need to use SMS or the App cannot send sms to your phone")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.SEND_SMS},
                                        MYSMS_PERMISSIONS_REQUEST);
                            }
                        }).setNegativeButton("Don't allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                }).create().show();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void getLocation(){
        Gson gson = new Gson();
        String saved = sharedPreferences.getString("saved location",null);
        if (saved == null){
            return;
        }
        locationInfo = gson.fromJson(saved,LocationInfo.class);
        if(locationInfo.getAccuracy() == -1){
            return;
        }
        home_location.setText("your home location is defined as <" + locationInfo.getLatitude()+
                ","+locationInfo.getLongitude()+">");
        home_header.setVisibility(View.VISIBLE);
        clear_button.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void saveHomeLocation(){
        locationInfo = new LocationInfo(latitude,longitude,accuracy);
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String toSave = gson.toJson(locationInfo);
        editor.putString("saved location",toSave);
        editor.apply();
        home_location.setText("your home location is defined as <" + locationInfo.getLatitude()+
                ","+locationInfo.getLongitude()+">");
        home_header.setVisibility(View.VISIBLE);
        clear_button.setVisibility(View.VISIBLE);
    }

    private void deleteLocation(){
        locationInfo.setAccuracy(-1);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(locationInfo);
        editor.putString("saved location", json);
        editor.apply();
        home_location.setText("");
        home_header.setVisibility(View.INVISIBLE);
        clear_button.setVisibility(View.INVISIBLE);
    }

    private void getNumber(){
        Gson gson = new Gson();
        String savedNumber = sharedPreferences.getString("phone Number",null);
        if (savedNumber == null){
            phone_number = "";
            return;
        }
        phone_number = gson.fromJson(savedNumber,String.class);
        if (phone_number.equals("")){
            return;
        }
        test_sms.setVisibility(View.VISIBLE);
    }

    private void saveNumber(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String numberToSave = gson.toJson(phone_number);
        editor.putString("phone Number",numberToSave);
        editor.apply();
        if(phone_number.equals("")){
            test_sms.setVisibility(View.INVISIBLE);
            return;
        }
        test_sms.setVisibility(View.VISIBLE);
    }

    private void getNumberFromUser(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText input_number = new EditText(this);
        input_number.setText(phone_number);
        input_number.setInputType(InputType.TYPE_CLASS_NUMBER);
        dialog.setTitle("Phone Number").setView(input_number).setMessage("Enter Your number")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        phone_number = input_number.getText().toString();
                        saveNumber();
                    }
                }).setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        }).create().show();
    }

    private void sendMassage(){
        Intent intent = new Intent();
        intent.setAction("POST_PC.ACTION_SEND_SMS");
        intent.putExtra(myApp.localReceiver.PHONE_KEY, phone_number);
        intent.putExtra(myApp.localReceiver.CONTENT_KEY,
                "Honey I'm Home!");
        myApp.sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTracker.stopTracking();
        unregisterReceiver(broadcastReceiver);
    }

    private class MyReceiver extends BroadcastReceiver{

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.requireNonNull(intent.getAction()).equals("Location") && track){
                latitude = intent.getDoubleExtra("latitude",0);
                latitude_text.setText("Latitude: " + latitude);
                longitude = intent.getDoubleExtra("longitude",0);
                longitude_text.setText("Longitude: "+ longitude);
                accuracy = intent.getFloatExtra("accuracy",50);
                accuracy_text.setText( "Accuracy: "+ accuracy);
                if(accuracy < 50.0){
                    set_button.setVisibility(View.VISIBLE);
                }
                else {
                    set_button.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
