package com.ifconit.oyedelivery;

/**
 * Created by user on 12/27/2016.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MyService extends Service {
    private static final String TAG = "MYSERVICEDATA";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000*2*60;
    private static final float LOCATION_DISTANCE = 0;
    SharedPreferences prefsUid,prefsOrderId;
    public static final String PREFS_UID="loginUserId";
    public static final String PREFS_OID="OrderId";
    SharedPreferences sharedpreferences;
    public static final String UserPref = "userPref" ;
    String res,code,token,userId,orderId,cws;
    double latitude, longitude;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            String providerVal = location.getProvider();
            //if(!providerVal.equals("network")) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                if (Utils.isConnected(getApplicationContext())) {
                    //Log.d("under_update_asyncClass", "data");
                    new UpdateLocation().execute();
                }
            //}
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        token= GlobalClass.getToken(getApplicationContext());

        prefsOrderId = getApplicationContext().getSharedPreferences(PREFS_OID, 0);
        orderId= prefsOrderId.getString("orderId", "");
        Log.d(TAG,""+orderId);

        prefsUid = getApplicationContext().getSharedPreferences(PREFS_UID, 0);
        userId= prefsUid.getString("uid", "");
        Log.d(TAG,""+userId);
        sharedpreferences = getApplicationContext().getSharedPreferences(UserPref, Context.MODE_PRIVATE);

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    class UpdateLocation extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            try {
                URL url = new URL(getApplicationContext().getString(R.string.base_url)+"add_delivery_boy_location&order_id="+orderId+"&delivery_boy_id="+userId+"&latitude="+latitude+"&longitude="+longitude+"&token="+token);
                Log.d(TAG, ""+url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line,json="";
                    JSONObject jObj=null;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    json = stringBuilder.toString();
                    try {
                        jObj = new JSONObject(json);
                    } catch (JSONException e) {
                        //  Log.e(TAG, "Error parsing data " + e.toString());
                    }
                    code=jObj.getString("code");
                    res="updated";
//					Log.v("res", "" + res);
//					Log.v("res", "" + stringBuilder.toString());
                    return res;
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                //Log.e("ERROR", e.getMessage(), e);
                return res;
            }
        }

        protected void onPostExecute(String file_url) {
            try{
                if(code.equals("200")){
                    Log.e("LocationReceiver", "Location "+res);
                }
                else if(code.equals("501")){
                    try {
                        String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                        cws = new CallWebService(getApplicationContext(),createTokenUrl).execute().get();
                        Log.d(TAG,"Token_URL:  "+cws);
                        JSONObject jo = new JSONObject(cws);
                        String c = jo.getString("code");
                        if (c.equals("200")) {
                            token = jo.getString("token");
                            GlobalClass.putToken(token,getApplicationContext());
                            new UpdateLocation().execute();
                        }
                    }catch (ExecutionException | InterruptedException ei){
                        ei.printStackTrace();
                        new UpdateLocation().execute();
                    }catch (JSONException je){
                        je.printStackTrace();
                        new UpdateLocation().execute();
                    }catch (NullPointerException ne){
                        new UpdateLocation().execute();
                    }

                }else{
                    //Log.e("LocationReceiver", "Location not Updated "+res);
                }
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }
        }
    }

}
