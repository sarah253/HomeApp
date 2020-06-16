package com.example.homeapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;



public class LocalSendSmsBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "Channel";
    public final String PHONE_KEY = "PHONE";
    public final String CONTENT_KEY = "CONTENT";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("POST_PC.ACTION_SEND_SMS")){
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) !=
                    PackageManager.PERMISSION_GRANTED){
                Log.e("ERROR","there isn't a permission to send sms");
                return;
            }
        String number = intent.getStringExtra(PHONE_KEY);
        String content = intent.getStringExtra(CONTENT_KEY);
        if(number == null || number.equals("") || content == null || content.equals("") ){
            Log.e("ERROR","number or massage is empty");
            return;
        }
        SmsManager.getDefault().sendTextMessage(number,null,content,
                    null,null);

        String message = "Sending sms to " + number + ":\n" + content;
            NotificationCompat.Builder builder = new
                    NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.home_alert)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentTitle("SMS sending")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            int notificationId = 123;
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        }

    }
}
