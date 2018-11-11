package com.ifconit.oyedelivery;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ifconit.oyedelivery.gcm.QuickstartPreferences;
import com.ifconit.oyedelivery.gcm.RegistrationIntentService;
import com.ifconit.oyedelivery.gcm.WakeLocker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import im.delight.android.location.SimpleLocation;

public class LoginActivity extends AppCompatActivity {
    EditText etMobile,etPass;
    TextView tvForgotPass;
    Button btSignIn;
    String strMobNo,strPass,code,message,uid,token,strBaseUrl,regId,cws;
    ProgressDialog pDialog;
    SharedPreferences prefsUid;
    SharedPreferences.Editor editor;
    public static final String PREFS_UID="loginUserId";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        token=GlobalClass.getToken(this);
        //Log.d("TOKENVALUE", "," + token);
        strBaseUrl=getApplicationContext().getString(R.string.base_url);


        prefsUid = getSharedPreferences(PREFS_UID, 0);
        editor= prefsUid.edit();

        etMobile=(EditText)findViewById(R.id.input_number);
        etPass=(EditText)findViewById(R.id.input_password);
       // tvForgotPass=(TextView)findViewById(R.id.tvForgetPass);
        btSignIn=(Button)findViewById(R.id.btSignIn);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
               // Log.v("Login", "Sent");
                if (sentToken) {
                   // Log.v("Login", "Sent");
                    regId = RegistrationIntentService.token;
                    Log.d("regId_value_is: ", "" + regId);
                    //new AddDevice().execute();
                } else {
                   // Log.v("Login", "error");
                }
            }
        };

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strMobNo=etMobile.getText().toString();
                strPass=etPass.getText().toString();
                Log.d("UnderSignIn", "data");
                if (Utils.isConnected(getApplicationContext())) {
                    if (checkPlayServices()) {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(LoginActivity.this, RegistrationIntentService.class);
                        startService(intent);
                    }
                    mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            //mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                            SharedPreferences sharedPreferences =
                                    PreferenceManager.getDefaultSharedPreferences(context);
                            boolean sentToken = sharedPreferences
                                    .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                            // Log.v("Login", "Sent");
                            if (sentToken) {
                                // Log.v("Login", "Sent");
                                regId = RegistrationIntentService.token;
                                Log.d("regId_value_is: ", "" + regId);
                                //new AddDevice().execute();
                            } else {
                                // Log.v("Login", "error");
                            }
                        }
                    };
                    new AsyncUserLogin().execute();
                }else{
                    Toast.makeText(getApplicationContext(),"No internet connection available!!",Toast.LENGTH_SHORT).show();
                }


            }
        });
    }//close OnCreate

    public class AsyncUserLogin extends AsyncTask<String, String, String> {
        final String TAG = "AsyncUserLogin";

        // set your json string url here
        String yourJsonStringUrl =strBaseUrl+"delivery_login&mobile="+strMobNo+"&password="+strPass+"&device_token="+RegistrationIntentService.token+"&token="+token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          //  Log.d("onPreExecute", "data");
            // pDialog =  ProgressDialog.show(LoginActivity.this,"","Wait for login.....", true,false);
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Wait for login..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }


        @Override
        protected String doInBackground(String... arg0) {
           /* try {
                Log.d("jsonUrl", yourJsonStringUrl);
                JsonParser jParser = new JsonParser();
                JSONObject json = jParser.getJSONFromUrl(yourJsonStringUrl);

                code = json.getString("code");
               // Log.d("code", code);
                message = json.getString("message");
               // Log.d("message", message);
                if(code.equals("200")){
                    uid=json.getString("user_id");
                  //  Log.d("user_id", uid);
                }


            } catch (JSONException e) {

                e.printStackTrace();
            }catch (NullPointerException ne){
                ne.printStackTrace();
            }
            return message;*/

            try {
                URL url = new URL(yourJsonStringUrl);
                 Log.d(TAG, "URL_is: " + url);
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
                    JSONObject json = new JSONObject(stringBuilder.toString());
                    code=json.getString("code");
                    // Log.d(TAG, "code: " + code);
                    message=json.getString("message");
                    // Log.d(TAG,"message: "+message);
                    if(code.equals("200")){
                        uid=json.getString("user_id");
                        //  Log.d("user_id", uid);
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


        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {

            pDialog.dismiss();
            try {
                //  Log.d("the_code_value_is", code);
                if (code.equals("200")) {
                    // Log.d("user_id_again", uid);
                    Toast.makeText(getApplicationContext(), "Login successful!!", Toast.LENGTH_SHORT).show();
                    editor.putString("uid", uid);
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(), OrderList.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                } else if (code.equals("501")) {
                    try {
                        String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                        cws = new CallWebService(getApplicationContext(),createTokenUrl).execute().get();
                        //Log.d(TAG,"Token_URL:  "+cws);
                        JSONObject jo = new JSONObject(cws);
                        String c = jo.getString("code");
                        if (c.equals("200")) {
                            token = jo.getString("token");
                            GlobalClass.putToken(token,getApplicationContext());
                            new AsyncUserLogin().execute();
                        }
                    }catch (ExecutionException | InterruptedException ei){
                        ei.printStackTrace();
                        new AsyncUserLogin().execute();
                    }catch (JSONException je){
                        je.printStackTrace();
                        new AsyncUserLogin().execute();
                    }catch (NullPointerException ne){
                        new AsyncUserLogin().execute();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }catch (NullPointerException npe){
                npe.printStackTrace();
                Toast.makeText(getApplicationContext(), npe.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }//close AsyncUserLogin

    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString("message");
            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());
            /**
             * Take appropriate action on this message
             * depending upon your app requirement
             * For now i am just displaying it on the screen
             * */
            // Showing received message
           // Log.e("push Message", "1"+newMessage);
            //Toast.makeText(getApplicationContext(), "New Message: " + newMessage, Toast.LENGTH_LONG).show();
            // Releasing wake lock
            WakeLocker.release();
        }
    };

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
              //  Log.i("Login", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /** Called before the activity is destroyed. */
    @Override
    public void onDestroy() {

            try {
                unregisterReceiver(mHandleMessageReceiver);

            } catch (Exception e) {
                e.printStackTrace();
            }

        super.onDestroy();
    }

    @Override
    public void onPause(){

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

    }
}//close Activity
