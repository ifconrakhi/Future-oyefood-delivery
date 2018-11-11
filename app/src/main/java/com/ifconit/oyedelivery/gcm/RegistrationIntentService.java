package com.ifconit.oyedelivery.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.ifconit.oyedelivery.CallWebService;
import com.ifconit.oyedelivery.GlobalClass;
import com.ifconit.oyedelivery.R;
import com.ifconit.oyedelivery.gcm.QuickstartPreferences;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by comp11 on 1/21/2016.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    public static String token,tmSDK,versionName;
    String code="",message,cws,token1;
    int deviceId,userId;
    SharedPreferences prefs,detailPrefs;
    SharedPreferences.Editor editor;
    //private static final String symbols ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            prefs = getSharedPreferences("SaveDeviceId", 0);
            deviceId=prefs.getInt("device_id", 0);
            detailPrefs = getSharedPreferences("UserDetails", 0);
            userId=detailPrefs.getInt("userId", 0);

            token1= GlobalClass.getToken(this);

            InstanceID instanceID = InstanceID.getInstance(RegistrationIntentService.this);
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);


            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
           // Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

           // new AddDevice().execute();


    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

//    if (ContextCompat.checkSelfPermission(thisActivity,
//    Manifest.permission.RE)
//            != PackageManager.PERMISSION_GRANTED) {
//
//        // Should we show an explanation?
//        if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
//                Manifest.permission.READ_CONTACTS)) {
//
//            // Show an expanation to the user *asynchronously* -- don't block
//            // this thread waiting for the user's response! After the user
//            // sees the explanation, try again to request the permission.
//
//        } else {
//
//            // No explanation needed, we can request the permission.
//
//            ActivityCompat.requestPermissions(thisActivity,
//                    new String[]{Manifest.permission.READ_CONTACTS},
//                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//            // app-defined int constant. The callback method gets the
//            // result of the request.
//        }
//    }

    public class AddDevice extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(Void... urls) {

            if (Build.VERSION.SDK_INT >= 23) {
                // Marshmallow+
                //device_id =random(8);
                versionName="1.1.1";
                tmSDK="6.0";

            } else {
                // Pre-Marshmallow
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                tmSDK = android.os.Build.VERSION.SDK;
                Log.d("Device SDK version", tmSDK);
                //int versionCode = BuildConfig.VERSION_CODE;
               // versionName = BuildConfig.VERSION_NAME;
                //Log.d("App version", versionName);
            }


            try {
                URL url = new URL(getApplicationContext().getString(R.string.base_url)+"add_device_new&device_token="+token+"&app_version="+versionName+"&device_type=Android&os_version="+tmSDK+"&user_id="+String.valueOf(userId)+"&token="+token1);
                //Log.d("MainActivity", "URL is: " + url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    //return stringBuilder.toString();
                    JSONObject jo = new JSONObject(stringBuilder.toString());
                    code=jo.getString("code");
                    Log.d("MainActivity","code: "+code);
                    message=jo.getString("message");
                    Log.d("MainActivity","message: "+message);
                    if(code.equals("200") || code.equals("400")){
                    deviceId=Integer.parseInt(jo.getString("device_id"));
                    Log.d("MainActivity","deviceId: "+deviceId);
                    }
                    return code;
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
                return "";
            } catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return "";
            }
            //return res;
        }

        protected void onPostExecute(String response) {
            Log.v("JSON VALUE IS ",""+response);
            if(response.equals("200") || response.equals("400")){
                editor= prefs.edit();
                editor.putInt("device_id", deviceId);
                editor.putString("regId",token);
                editor.commit();
            }else if(code.equals("501")){
                Log.v("JSON VALUE IS ", "" + response);
                //Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                try {
                    cws=new CallWebService(RegistrationIntentService.this,"create_token").execute().get();
                  //  cws = new CallWebService(RegistrationIntentService.this, "create_token").execute().get();
                    Log.d("MainActivity","web service value is:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if(c.equals("200")){
                        token1=jo.getString("token");
                        GlobalClass.putToken(token1,RegistrationIntentService.this);
                        new AddDevice().execute();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                }catch (JSONException je){
                    je.printStackTrace();
                }
            }else{
                return;
            }
        }
    }

//    public static String random(int size) {
//
//        StringBuilder generatedToken = new StringBuilder();
//        char[] chars ={'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9'};
//        try {
//            SecureRandom number = SecureRandom.getInstance("SHA1PRNG");
//            // Generate 20 integers 0..20
//            for (int i = 0; i < size; i++) {
//                char selectedChar = chars[number.nextInt(chars.length)];
//                generatedToken.append(selectedChar);
//                //generatedToken.append(number.nextInt(symbols.length()));
//            }
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//
//        return generatedToken.toString();
//    }

}
