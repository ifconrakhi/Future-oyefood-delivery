package com.ifconit.oyedelivery.gcm;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.ifconit.oyedelivery.OrderList;
import com.ifconit.oyedelivery.R;


public class GCMIntentService extends GcmListenerService {

	private static final String TAG = "GCMIntentService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

//        if (from.startsWith("/topics/")) {
//            // message received from some topic.
//        } else {
//            // normal downstream message.
//        }

        sendNotification(message);
    }

    private void sendNotification(String message) {


        String name = getApplicationContext().getString(R.string.app_name);


        try{
            JSONObject json = new JSONObject(message);
            String title = json.getString("alert");
           // Log.e("GCMNotificationReceiver", ""+title);
            //String code = json.getString("code");

            //long when = System.currentTimeMillis();

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("" + name)
                    .setContentText(title)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri);



                Intent intent = new Intent(this, OrderList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(String.valueOf(System.currentTimeMillis()));
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
                notificationBuilder.setContentIntent(pendingIntent);
                notificationBuilder.setVibrate(new long[]{500, 500});
                notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

               Notification notification = notificationBuilder.build();
               notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
               notificationManager.notify(0, notification);
           // notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());


        } catch (JSONException e) {
            Log.d("NotificationReceiver", "JSONException: " + e.getMessage());
        }catch (NumberFormatException ex){
            ex.printStackTrace();
        }catch (NullPointerException exx){
            exx.printStackTrace();
        }

    }

}
