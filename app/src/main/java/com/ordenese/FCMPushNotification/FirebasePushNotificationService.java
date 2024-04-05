/*
package com.ordenese.FCMPushNotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ordenese.Activities.AppHome;
import com.ordenese.R;

import java.util.Random;

public class FirebasePushNotificationService extends FirebaseMessagingService {

    NotificationManager mNotificationManager;
    int random = new Random().nextInt(61) + 20; // [0, 60] + 20 => [20, 80]

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e("Token ","Token "+token);
    }
    @Override
    public boolean handleIntentOnMainThread(Intent intent) {
        return super.handleIntentOnMainThread(intent);
    }

    @Override
    public void handleIntent(Intent intent) {
       // super.handleIntent(intent);
        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Object value = intent.getExtras().get(key);
                Log.d( "Key: " + key , " Value: " + value);
            }
            Log.e("",intent.getExtras().toString());
            Log.e("gcm.notification.color",intent.getExtras().getString("gcm.notification.color"));
           // AppFunctions.toastShort(MainContext.getAppContext(),intent.getExtras().toString());

            Uri customSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_new);
           // if(remoteMessage.getData()!=null) {
             //   String sound = remoteMessage.getData().get("sound");
            //}

            //if(remoteMessage.getNotification()!=null) {
                String title = intent.getExtras().getString("gcm.notification.title");
                String body = intent.getExtras().getString("gcm.notification.body");
               // String sound=remoteMessage.getNotification().getSound();
              //  String icon=remoteMessage.getNotification().getIcon();
               // boolean defaultSound=remoteMessage.getNotification().getDefaultSound();

               // Map<String, String> data = remoteMessage.getData();

               // Log.e("Title and Body",title+" "+body+" "+sound+" "+icon+" "+defaultSound);
            //}

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setSound(customSoundUri)
                    .build();
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            manager.notify(123, notification);

        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Uri customSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_new);
        if(CheckDB.getInstance(MainContext.getAppContext()).getSizeOfList() > 0){
            CheckDB.getInstance(MainContext.getAppContext()).delete();
        }
        CheckDBDataSet checkDBDataSet = new CheckDBDataSet();
        checkDBDataSet.setTitle(remoteMessage.getNotification().getTitle());
        checkDBDataSet.setBody(remoteMessage.getNotification().getBody());
        CheckDB.getInstance(MainContext.getAppContext()).add(checkDBDataSet);
        if(remoteMessage.getNotification()!=null) {
            String title=remoteMessage.getNotification().getTitle();
            String body=remoteMessage.getNotification().getBody();
            String sound=remoteMessage.getNotification().getSound();
            String icon=remoteMessage.getNotification().getIcon();
            boolean defaultSound=remoteMessage.getNotification().getDefaultSound();
            String priority = ""+remoteMessage.getNotification().getNotificationPriority();
            Log.e("priority",""+priority);
            // Map<String, String> data = remoteMessage.getData();
            //Log.e("Title and Body",title+" "+body+" "+sound+" "+icon+" "+defaultSound);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setSound(customSoundUri)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .build();
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            manager.notify(123, notification);
        }
    }




    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        if(remoteMessage.getNotification()!=null){

            // playing audio and vibration when user se request
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                r.setLooping(false);
            }

            // vibration
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {100, 300, 300, 300};
            v.vibrate(pattern, -1);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID");

            Intent resultIntent = new Intent(this, AppHome.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,0 , resultIntent, PendingIntent.FLAG_IMMUTABLE);

            builder.setContentTitle(remoteMessage.getNotification().getTitle());
            builder.setContentText(remoteMessage.getNotification().getBody());
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentIntent(pendingIntent);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
            builder.setAutoCancel(true);
            builder.setPriority(Notification.PRIORITY_MAX);

            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                String channelId = String.valueOf(random);
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }

// notificationId is a unique int for each notification that you must define
            mNotificationManager.notify(100, builder.build());

        }

    }



}
*/
