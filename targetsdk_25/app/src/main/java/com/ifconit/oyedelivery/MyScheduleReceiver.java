package com.ifconit.oyedelivery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by tejp on 3/29/2016.
 */
public class MyScheduleReceiver extends BroadcastReceiver {

    // restart service every 5 seconds
    private static long REPEAT_TIME=1000*60*2;
    String TAG = "GPSLocation";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"OnReceive");
        AlarmManager service = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MyStartServiceReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent.FLAG_CANCEL_CURRENT
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        // cal.add(Calendar.SECOND, 30);

        cal.setTimeInMillis(System.currentTimeMillis()+1000*5);
        // fetch every 30 seconds
        // InexactRepeating allows Android to optimize the energy consumption

        //Log.d("REPEATTIME", String.valueOf(REPEAT_TIME));
        service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);

        // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
        // REPEAT_TIME, pending);

    }
}
