package com.ifconit.oyedelivery;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NotifyService extends Service {

   String orderid,title;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try{

            orderid=intent.getStringExtra("order_id");
            title=intent.getStringExtra("title");

            Log.d("UnderNotifyService","onStartCommand orderId : "+orderid+" title: "+title);

            intent = new Intent(getApplicationContext(), OrderList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("order_id",orderid);
            intent.putExtra("title",title);
            startActivity(intent);

        }catch (Exception e){
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

}