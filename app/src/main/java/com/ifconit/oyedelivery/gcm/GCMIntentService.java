package com.ifconit.oyedelivery.gcm;

import org.json.JSONException;
import org.json.JSONObject;

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
            String channelName = "OyeFoodDelivery";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        channelId, channelName, importance);
                notificationManager.createNotificationChannel(mChannel);
            }



            startVibration();
            final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //fetch current Ringtone
          /*  final Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(this
                    .getApplicationContext(), RingtoneManager.TYPE_RINGTONE);*/

            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("" + name)
                    .setContentText(title)
                    .setAutoCancel(true)
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

        long[] mVibratePattern = new long[]{0, 400, 800, 600, 800, 800, 800, 1000};
        // 3 : Repeat this pattern from 3rd element of an array

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(2*60*1000, VibrationEffect.DEFAULT_AMPLITUDE));

            // vibrator.vibrate(mVibratePattern, 3);
        } else{
            vibrator.vibrate(mVibratePattern, 3);
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
