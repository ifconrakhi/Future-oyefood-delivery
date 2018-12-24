package com.ifconit.oyedelivery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiverActivity extends BroadcastReceiver {
    Context context;
    String order_id;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();
        this.context=context;
        String action=intent.getStringExtra("action");
        order_id=intent.getStringExtra("order_id");
        if(action.equals("Accept")){
            performAccept();
        }
        else if(action.equals("Reject")){
            performReject();

        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void performAccept(){
        Toast.makeText(context,"Order Accepted for : "+order_id,Toast.LENGTH_LONG).show();
    }

    public void performReject(){
        Toast.makeText(context,"Order Rejected for : "+order_id,Toast.LENGTH_LONG).show();
    }
}