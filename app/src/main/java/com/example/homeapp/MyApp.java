package com.example.homeapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MyApp extends Application {

    public LocalSendSmsBroadcastReceiver localReceiver;
    PeriodicWorkRequest worker;

    @Override
    public void onCreate() {
        super.onCreate();
        localReceiver = new LocalSendSmsBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("POST_PC.ACTION_SEND_SMS");
        registerReceiver(localReceiver,intentFilter);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Channel","homeApp",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Home App");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        worker = new PeriodicWorkRequest.Builder(TrackingWork.class,15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("track",
                ExistingPeriodicWorkPolicy.REPLACE,worker);
    }
}
