package com.ifconit.oyedelivery;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by tejp on 3/29/2016.
 */
public class MyStartServiceReceiver extends BroadcastReceiver {

    Context context;
    String TAG = "GPSLocation";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"OnReceiveReceive");
        this.context=context;

     //   Intent mServiceIntent = new Intent(context, LocationService.class);

        if (check()==true) {
            Log.d("theValueUnder","if");
            Intent mServiceIntent = new Intent(context, LocationService.class);
            context.startService(mServiceIntent);
        }

    }

    public boolean check(){
        Log.d("theValueUnder","check");
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.ifconit.oyedelivery.LocationService"
                    .equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return true;
    }
   /* public boolean check(){

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.ifconit.oyedelivery.LocationService".equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }*/

//    public boolean check1(){
//        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
//        {
//            if ("com.ksinfotech.kidsafety.ContactsService"
//                    .equals(service.service.getClassName()))
//            {
//                return true;
//            }
//        }
//        return false;
//    }
}
