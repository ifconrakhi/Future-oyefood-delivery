package com.ifconit.oyedelivery.gcm;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.ifconit.oyedelivery.LoginActivity;
import com.ifconit.oyedelivery.NotificationReceiverActivity;
import com.ifconit.oyedelivery.NotifyService;
import com.ifconit.oyedelivery.OrderList;
import com.ifconit.oyedelivery.R;
import com.ifconit.oyedelivery.SplashActivity;

import java.util.Random;


public class GCMIntentService extends GcmListenerService {

	private static final String TAG = "GCMIntentService";
    public static Vibrator vibrator;
    public static Ringtone ringtoneSound;
    public static MediaPlayer mMediaPlayer;
     NotificationManager notificationManager;
    SharedPreferences prefsUid;
    SharedPreferences.Editor editor;
    public static final String PREFS_UID = "loginUserId";
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        String title=null,orderId=null;

        try {
             JSONObject jsonobj=new JSONObject(message);
             title = jsonobj.getString("alert");
             orderId = jsonobj.getString("order_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "title: " + title);
        Log.d(TAG, "orderId: " + orderId);


        sendNotification(title,orderId);


       /* if (from.startsWith("/topics/")) {

            // message received from some topic.
        } else {
            // normal downstream message.
        }*/


    }

    private void sendNotification(String title, String orderId) {
        try{
            final String name = getApplicationContext().getString(R.string.app_name);
            Intent intent = new Intent(this, OrderList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("order_id",orderId);
            intent.putExtra("title",title);
            intent.putExtra("from","GCM");
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

           // Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Uri defaultSoundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.siren_for);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(name)
                    .setContentText(title)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setContentIntent(pendingIntent);

            Random random = new Random();
            int m = random.nextInt(9999 - 1000) + 1000;

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(m, notificationBuilder.build());

            prefsUid = getSharedPreferences(PREFS_UID, 0);
            editor = prefsUid.edit();
            editor.putString("notifStatus", "1");
            editor.putString("order_id", orderId);
            editor.putString("title", title);
            editor.commit();


            long[] mVibratePattern = {0, 900, 100, 800, 200, 700, 300, 600, 400, 500, 500, 400, 600, 300, 700, 200, 800, 100, 900,};
            // play vibration
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(mVibratePattern,1);

            AudioManager mobilemode = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            mobilemode.setStreamVolume(AudioManager.STREAM_ALARM,mobilemode.getStreamMaxVolume(AudioManager.STREAM_ALARM),0);
            mobilemode.setStreamVolume(AudioManager.STREAM_NOTIFICATION,mobilemode.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),0);
            mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);

            try {
                //Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_RINGTONE);
                // Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.siren_for);  //Here is FILE_NAME is the name of file that you want to play
                //  mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren_for);
                mMediaPlayer=new MediaPlayer();
                mMediaPlayer.setDataSource(this, defaultSoundUri);
                //  final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (mobilemode.getStreamVolume(AudioManager.STREAM_RING) != 0) {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }




            // createNotification(message);
           // callActivityDirect(message);

        }catch (NumberFormatException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /*public void createNotification(String message){
        try{
            final String name = getApplicationContext().getString(R.string.app_name);


            JSONObject json = new JSONObject(message);
            final String title = json.getString("alert");
            // Log.e("GCMNotificationReceiver", ""+title);
            final String order_id = json.getString("order_id");
            // String order_refno = json.getString("order_refno");

            final int NOTIFY_ID = 0; // ID of notification
            String channelId = getApplicationContext().getString(R.string.default_notification_channel_id); // default_channel_id
            String channelName = getApplicationContext().getString(R.string.default_notification_channel_title); // Default Channel
            Intent intent;
            PendingIntent pendingIntent;
            NotificationCompat.Builder builder;

            if (notificationManager == null) {
                notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            }

            Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.siren_for);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                Log.d(TAG,"under_sendnotif if ");
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notificationManager.getNotificationChannel(channelId);

                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();

                if (mChannel == null) {
                    mChannel = new NotificationChannel(channelId, channelName, importance);
                    mChannel.enableVibration(true);
                    mChannel.setSound(soundUri,audioAttributes);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notificationManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

                intent = new Intent(getApplicationContext(), OrderList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("order_id",order_id);
                intent.putExtra("title",title);

                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                builder.setContentTitle("" + name)                            // required
                        .setSmallIcon(R.mipmap.ic_launcher)   // required
                        .setContentText(title) // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker("" + name)
                        .setSound(soundUri)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            }
            else {

                Log.d(TAG,"under_sendnotif else ");

                builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
                intent = new Intent(getApplicationContext(), OrderList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("order_id",order_id);
                intent.putExtra("title",title);

                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                builder.setContentTitle("" + name)                            // required
                        .setSmallIcon(R.mipmap.ic_launcher)   // required
                        .setContentText(title) // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker("" + name)
                        .setSound(soundUri)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH);
            }
            Notification notification = builder.build();
            notificationManager.notify(NOTIFY_ID, notification);




            long[] mVibratePattern = {0, 900, 100, 800, 200, 700, 300, 600, 400, 500, 500, 400, 600, 300, 700, 200, 800, 100, 900,};
            // play vibration
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(mVibratePattern, 0));
            }else{
                vibrator.vibrate(mVibratePattern,1);
            }


            Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_ALARM);
            ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);

            if (ringtoneSound != null) {
                ringtoneSound.play();
             }

            AudioManager mobilemode = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            mobilemode.setStreamVolume(AudioManager.STREAM_ALARM,mobilemode.getStreamMaxVolume(AudioManager.STREAM_ALARM),0);
            mobilemode.setStreamVolume(AudioManager.STREAM_NOTIFICATION,mobilemode.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),0);
            mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);

            try {
                //Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_RINGTONE);
                // Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.siren_for);  //Here is FILE_NAME is the name of file that you want to play
                //  mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren_for);
                mMediaPlayer=new MediaPlayer();
                mMediaPlayer.setDataSource(this, soundUri);
                //  final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (mobilemode.getStreamVolume(AudioManager.STREAM_RING) != 0) {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }


            //startVibration();


        } catch (JSONException e) {
            Log.d("NotificationReceiver", "JSONException: " + e.getMessage());
        }catch (NumberFormatException ex){
            ex.printStackTrace();
        }catch (NullPointerException exx){
            exx.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

    public void callActivityDirect(String message){
        try{

            JSONObject json = new JSONObject(message);
            final String title = json.getString("alert");
            // Log.e("GCMNotificationReceiver", ""+title);
            final String order_id = json.getString("order_id");

            Intent intent = new Intent(GCMIntentService.this, NotifyService.class);
            intent.putExtra("order_id",order_id);
            intent.putExtra("title",title);
            startService(intent);


        }catch (Exception e){
          e.printStackTrace();
        }
    }


   /* private void startVibration() {


        try{
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            if (vibrator != null && vibrator.hasVibrator()) {

               //  vibrateFor500ms();
               //  customVibratePatternNoRepeat();
                 customVibratePatternRepeatFromSpecificIndex();



               *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                   createOneShotVibrationUsingVibrationEffect();

                 //  createWaveFormVibrationUsingVibrationEffect();

                 //  createWaveFormVibrationUsingVibrationEffectAndAmplitude();

               }*//*



            } else {

                Toast.makeText(this, "Device does not support vibration", Toast.LENGTH_SHORT).show();

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void vibrateFor500ms() {

        vibrator.vibrate(1*60*1000); // for 1 minute

    }

    private void customVibratePatternNoRepeat() {



        // 0 : Start without a delay

        // 400 : Vibrate for 400 milliseconds

        // 200 : Pause for 200 milliseconds

        // 400 : Vibrate for 400 milliseconds

        long[] mVibratePattern = new long[]{0, 400, 200, 400};



        // -1 : Do not repeat this pattern

        // pass 0 if you want to repeat this pattern from 0th index

        vibrator.vibrate(mVibratePattern, -1);

    }
    private void customVibratePatternRepeatFromSpecificIndex() {

       // long[] mVibratePattern = new long[]{0, 400, 800, 600, 800, 800, 800, 1000};
        // 3 : Repeat this pattern from 3rd element of an array
        long[] mVibratePattern = {
                0,     // start vibrate immediately
                900,   // vibrate 1 900 ms
                100,   // silence 1 100 ms
                800,   // vibrate 2 800 ms
                200,   // silence 2 200 ms
                700,
                300,
                600,
                400,
                500,
                500,
                400,
                600,
                300,
                700,
                200,
                800,
                100,
                900,
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(2*60*1000, VibrationEffect.DEFAULT_AMPLITUDE));

            // vibrator.vibrate(mVibratePattern, 3);
        } else{
            vibrator.vibrate(mVibratePattern, 0);
        }

        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
         ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);

        if (ringtoneSound != null) {
            ringtoneSound.play();
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOneShotVibrationUsingVibrationEffect() {

        // 1000 : Vibrate for 1 sec

        // VibrationEffect.DEFAULT_AMPLITUDE - would perform vibration at full strength

        VibrationEffect effect = VibrationEffect.createOneShot(2*60*1000, VibrationEffect.DEFAULT_AMPLITUDE);

        vibrator.vibrate(effect);

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createWaveFormVibrationUsingVibrationEffect() {

        long[] mVibratePattern = new long[]{0, 400, 1000, 600, 1000, 800, 1000, 1000};

        // -1 : Play exactly once

        VibrationEffect effect = VibrationEffect.createWaveform(mVibratePattern, -1);

        vibrator.vibrate(effect);

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createWaveFormVibrationUsingVibrationEffectAndAmplitude() {



        long[] mVibratePattern = new long[]{0, 400, 800, 600, 800, 800, 800, 1000};

        int[] mAmplitudes = new int[]{0, 255, 0, 255, 0, 255, 0, 255};

        // -1 : Play exactly once



        if (vibrator.hasAmplitudeControl()) {

            VibrationEffect effect = VibrationEffect.createWaveform(mVibratePattern, mAmplitudes, -1);

            vibrator.vibrate(effect);

        }

    }*/

}
