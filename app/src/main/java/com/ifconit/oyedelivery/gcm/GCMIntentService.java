package com.ifconit.oyedelivery.gcm;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.ifconit.oyedelivery.LoginActivity;
import com.ifconit.oyedelivery.NotificationReceiverActivity;
import com.ifconit.oyedelivery.OrderList;
import com.ifconit.oyedelivery.R;
import com.ifconit.oyedelivery.SplashActivity;


public class GCMIntentService extends GcmListenerService {

	private static final String TAG = "GCMIntentService";
    public static Vibrator vibrator;
    public static Ringtone ringtoneSound;
    public static MediaPlayer mMediaPlayer;
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


        final String name = getApplicationContext().getString(R.string.app_name);


        try{
            JSONObject json = new JSONObject(message);
            final String title = json.getString("alert");
           // Log.e("GCMNotificationReceiver", ""+title);
            final String order_id = json.getString("order_id");
           // String order_refno = json.getString("order_refno");

            //long when = System.currentTimeMillis();

            /*Intent intent = new Intent(this, NotificationReceiverActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(String.valueOf(System.currentTimeMillis()));
            intent.putExtra("action","Accept");
            intent.putExtra("action","Reject");
            intent.putExtra("order_id",order_id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
*/
           // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int notificationId = 1;
            final String channelId = "channel-01";
            String channelName = getApplicationContext().getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;


            Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.siren_for);  //Here is FILE_NAME is the name of file that you want to play
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
                mChannel.enableLights(true);
                mChannel.enableVibration(true);
                mChannel.setSound(sound, attributes); // This is IMPORTANT

                notificationManager.createNotificationChannel(mChannel);
            }

            long[] mVibratePattern = {0, 900, 100, 800, 200, 700, 300, 600, 400, 500, 500, 400, 600, 300, 700, 200, 800, 100, 900,};
            // play vibration
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(mVibratePattern, 0));
            }else{
                vibrator.vibrate(mVibratePattern,1);
            }


           /* Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_ALARM);
            ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);

            if (ringtoneSound != null) {
                ringtoneSound.play();
             }*/

            AudioManager mobilemode = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            mobilemode.setStreamVolume(AudioManager.STREAM_ALARM,mobilemode.getStreamMaxVolume(AudioManager.STREAM_ALARM),0);
            mobilemode.setStreamVolume(AudioManager.STREAM_NOTIFICATION,mobilemode.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),0);
            mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);

            try {
                //Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_RINGTONE);
               // Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.siren_for);  //Here is FILE_NAME is the name of file that you want to play
                mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren_for);

                // mMediaPlayer.setDataSource(this, sound);
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
            //  final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //fetch current Ringtone
            final Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);

            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("" + name)
                    .setContentText(title)
                    .setAutoCancel(true)
                    .setLights(Color.RED, 3000, 3000)
                    .setSound(defaultSoundUri);


            Intent intent1 = new Intent(getApplicationContext(), OrderList.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("order_id",order_id);
            intent1.putExtra("title",title);
            intent1.setAction(String.valueOf(System.currentTimeMillis()));
            PendingIntent pendingIntent1 = PendingIntent.getActivity(getApplicationContext(), 0 /* Request code */, intent1, PendingIntent.FLAG_ONE_SHOT);
            notificationBuilder.setContentIntent(pendingIntent1);
            // notificationBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
            notificationBuilder.setSound(defaultSoundUri);
            // NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = notificationBuilder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
            notificationManager.notify(0, notification);




           /* try{

                Thread timerThread = new Thread(){
                    public void run(){
                        try{
                            sleep(100);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }finally{



                        }
                    }
                };
                timerThread.start();

            }catch (Exception e){
                e.printStackTrace();
            }*/





           // notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());


        } catch (JSONException e) {
            Log.d("NotificationReceiver", "JSONException: " + e.getMessage());
        }catch (NumberFormatException ex){
            ex.printStackTrace();
        }catch (NullPointerException exx){
            exx.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void startVibration() {


        try{
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            if (vibrator != null && vibrator.hasVibrator()) {

               //  vibrateFor500ms();
               //  customVibratePatternNoRepeat();
                 customVibratePatternRepeatFromSpecificIndex();



               /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                   createOneShotVibrationUsingVibrationEffect();

                 //  createWaveFormVibrationUsingVibrationEffect();

                 //  createWaveFormVibrationUsingVibrationEffectAndAmplitude();

               }*/



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

    }

}
