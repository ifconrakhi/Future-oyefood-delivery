package com.ifconit.oyedelivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by rakhit on 4/20/2016.
 */
public class SplashActivity extends AppCompatActivity {
    SharedPreferences prefsUid;
    public static final String PREFS_UID="loginUserId";
    String userId;

    String name_activity="SplashActivity",TAG="SplashActivity";
//    private Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* try{
            //android O fix bug orientation
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        }catch (RuntimeException re){
            re.printStackTrace();
        }*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

       // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.splash_screen);

        prefsUid = getSharedPreferences(PREFS_UID, 0);
        userId= prefsUid.getString("uid", "");
        //Log.d("USERID_fromLogin", "" + userId);

//        MyApplication application = (MyApplication) getApplication();
//        mTracker = application.getDefaultTracker();

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(500);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    if(userId.equalsIgnoreCase("")){
                        Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        finish();
                    }else{
                        Intent intent = new Intent(SplashActivity.this,OrderList.class);
                        intent.putExtra("cart_status","2");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        finish();
                    }

                }
            }
        };
        timerThread.start();
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.i(TAG, name_activity);
//        mTracker.setScreenName("Image~" + name_activity);
//        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
//        mTracker.send(new HitBuilders.EventBuilder()
//                .setCategory("Action")
//                .setAction("Share")
//                .build());
//    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }

}



