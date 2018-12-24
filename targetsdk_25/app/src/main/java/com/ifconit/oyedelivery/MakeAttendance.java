package com.ifconit.oyedelivery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import im.delight.android.location.SimpleLocation;

/**
 * Created by user on 10/8/2016.
 */

    public class MakeAttendance extends BaseDrawer implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

        SharedPreferences prefsUid;
        public static final String PREFS_UID = "loginUserId";
        String token, strBaseUrl, userId;
       // LocationRequest mLocationRequest;
       // GoogleApiClient mGoogleApiClient;
        String currentLat = "", currentLng = "", status = "";
        ArrayList<String> attTime;
        ArrayList<String> attStatus;
        String selectedDate,cws;
        Dialog f;
        ActionBar ab;
        private boolean isConnected = false;
        CaldroidFragment caldroidFragment;
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Dialog dialogAtt;
        //private SimpleLocation location;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;

    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private LocationManager locationManager;

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
            getLayoutInflater().inflate(R.layout.attendance, frameLayout);

            imgvwAttendance.setVisibility(View.VISIBLE);
            tvStart.setVisibility(View.GONE);
            ab = getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);

            token = GlobalClass.getToken(this);
            strBaseUrl = getApplicationContext().getString(R.string.base_url);
            prefsUid = getSharedPreferences(PREFS_UID, 0);
            userId = prefsUid.getString("uid", "");

            attStatus = new ArrayList<>();
            attTime = new ArrayList<>();
            f = new Dialog(MakeAttendance.this);
            f.requestWindowFeature(Window.FEATURE_NO_TITLE);
            f.setContentView(R.layout.attendance_detail);

            dialogAtt = new Dialog(MakeAttendance.this);
            dialogAtt.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogAtt.setContentView(R.layout.mark_attendance);

            caldroidFragment = new CaldroidFragment();
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.SUNDAY); // Tuesday
            caldroidFragment.setArguments(args);

            // construct a new instance of SimpleLocation
            //location = new SimpleLocation(this);

            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            t.replace(R.id.calendar1, caldroidFragment);
            t.commit();

            if (Utils.isConnected(getApplicationContext())) {
                new AsyncPresenceList().execute();
            } else {
                Toast.makeText(getApplicationContext(), "No internet connection available!!", Toast.LENGTH_SHORT).show();
            }

           /* getMap();
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(1 * 1000 * 60);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
            //mLocationClient = new LocationClient(this, this, this);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();*/
            // if we can't access the location yet
           /* if (!location.hasLocationEnabled()) {
                // ask the user to enable location access
                SimpleLocation.openSettings(this);
            }
            else
            {
                currentLat = String.valueOf(location.getLatitude());
                currentLng = String.valueOf(location.getLongitude());

                Log.d("Under_MakeAttendance","curr lat "+currentLat+" curr Lng "+currentLng);
            }*/

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

            //checkLocation();

            if (!Utils.isGPSTurnOn(getApplicationContext())) {
                showGPSDialog();
                return;
            }


            imgvwAttendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMarkAttendance();
                }
            });
        }//Oncreate Closed

      /*  @Override
        public void onConnected(Bundle bundle) {
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
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (mLastLocation != null) {
                    try {
                        //new GetFriends().execute();
                        onLocationChanged(mLastLocation);
                    } catch (NullPointerException e) {
                        Toast.makeText(getApplicationContext(), "Connect to the Intenet to get the current Location and Map", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    if (!Utils.isGPSTurnOn(getApplicationContext())) {
                        showGPSDialog();
                        return;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onLocationChanged(Location location) {
            currentLat = String.valueOf(location.getLatitude());
            currentLng = String.valueOf(location.getLongitude());
            Log.d("PrecenceListLocation", "lat" + currentLat + "lang" + currentLng);
            if (currentLat.equals("") || currentLng.equals("")) {
                Toast.makeText(getApplicationContext(), "Your Current Location Not Found!", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            // TODO Auto-generated method stub
            if (connectionResult.hasResolution()) {
                try {
                    // Start an Activity that tries to resolve the error
                    connectionResult.startResolutionForResult(this, 0);
	                *//*
	                 * Thrown if Google Play services canceled the original
	                 * PendingIntent
	                 *//*
                } catch (IntentSender.SendIntentException e) {
                    // Log the error
                    e.printStackTrace();
                }
            } else {
	            *//*
	             * If no resolution is available, display a dialog to the
	             * user with the error.
	             *//*
                Log.i("Map Activity", "Location services connection failed with code " + connectionResult.getErrorCode());
            }
        }*/

    @Override
    public void onConnected(Bundle bundle) {
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

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
       // Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
      //  Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());
        Log.d("Under_MakeAttendance","curr lat "+Double.toString(location.getLatitude())+" curr Lng "+Double.toString(location.getLongitude()));
        currentLat = String.valueOf(location.getLatitude());
        currentLng = String.valueOf(location.getLongitude());

        Log.d("Under_MakeAttendance","curr lat "+currentLat+" curr Lng "+currentLng);
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    private boolean checkLocation() {
        if(!isLocationEnabled()){}
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showGPSDialog() {
            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme)) // Theme
                    .setTitle("Current Location") // setTitle
                    .setMessage("Select Current Location") // setMessage
                    .setCancelable(false) //
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                            dialog.dismiss();
                            //finish();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    dialog.cancel();
                    Toast.makeText(MakeAttendance.this, " For Executing Tap On Yes", Toast.LENGTH_SHORT)
                            .show();
                }
            }).setIcon(android.R.drawable.ic_dialog_alert).show();
        }

//        @Override
//        protected void onStart() {
//            super.onStart();
//            // Connect the client.
//           // mGoogleApiClient.connect();
//
//        }
//
//        @Override
//        protected void onStop() {
//            // Disconnect the client.
//          //  mGoogleApiClient.disconnect();
//            super.onStop();
//        }

        public void getMap() {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            if (!Utils.isConnected(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Internet not available. Cross check your internet connectivity and Restart App", Toast.LENGTH_LONG).show();
                return;
            }/*else if (!Utils.isGPSTurnOn(getApplicationContext())) {
				    showGPSDialog();
				    return;
				}*/ else {
                // Getting status
                int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getBaseContext());

                // Showing status
                if (status == ConnectionResult.SUCCESS) {
                    Log.i("GPService", "Google Play Services are available");
                    //if (Utils.isConnected(getApplicationContext())) {
                    // map = mf.getMap();

                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    criteria.setPowerRequirement(Criteria.POWER_LOW);
                    // criteria.setAltitudeRequired(false);
                    // criteria.setSpeedRequired(false);
                    criteria.setCostAllowed(true);

                } else {
                    Log.i("GPService", "Google Play Services are not available");
                    int requestCode = 10;
                    Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, requestCode, status);
                    dialog.show();
                }
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            // make the device update its location
//            location.beginUpdates();
//            currentLat = String.valueOf(location.getLatitude());
//            currentLng = String.valueOf(location.getLongitude());
        }

        @Override
        protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            //if (Utils.isGPSTurnOn(getApplicationContext())) {
            //onResume();
            startActivity(new Intent(MakeAttendance.this, MakeAttendance.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            new AsyncPresenceList().execute();
        }


        public class AsyncMakeAttendance extends AsyncTask<String, String, String> {
        final String TAG = "AsyncMakeAttendance";

        // set your json string url here
        String yourJsonStringUrl = "http://wsnew.oyefood.in/WS/delivery_service.php?mode=update_attendance&token="+token+"&user_id="+userId+"&current_lat="+currentLat+"&current_lng="+currentLng+"&status="+status;
        String code,message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(MakeAttendance.this, "", "Wait..", true, false);
            pDialog.setCanceledOnTouchOutside(true);
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                yourJsonStringUrl = yourJsonStringUrl.replaceAll(" ","%20");
                URL url = new URL(yourJsonStringUrl);
              //  Log.d(TAG, "URL_is: " + url);
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
                    code = json.getString("code");
                    message = json.getString("message");
                    return code;
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return "";
            }

        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if (code.equals("200")) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                dialogAtt.dismiss();
                new AsyncPresenceList().execute();
            } else if (code.equals("501")) {
                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(getApplicationContext(),createTokenUrl).execute().get();
                   // Log.d(TAG,"Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token,getApplicationContext());
                        new AsyncMakeAttendance().execute();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    new AsyncMakeAttendance().execute();
                }catch (JSONException je){
                    je.printStackTrace();
                    new AsyncMakeAttendance().execute();
                }catch (NullPointerException ne){
                    new AsyncMakeAttendance().execute();
                }
            }
            else {
//                location.beginUpdates();
//                currentLat = String.valueOf(location.getLatitude());
//                currentLng = String.valueOf(location.getLongitude());
                dialogAtt.dismiss();
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }

        }
    }//close AsyncInRoute

        public class AsyncAttendanceList extends AsyncTask<String, String, String> {
        final String TAG = "AsyncAttendanceList";

        // set your json string url here
        String yourJsonStringUrl = "http://wsnew.oyefood.in/WS/delivery_service.php?mode=list_attendance&token="+token+"&user_id="+userId+"&selected_date="+selectedDate;
        String code,message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            attTime = new ArrayList<>();
            attStatus = new ArrayList<>();
            pDialog = ProgressDialog.show(MakeAttendance.this, "", "Wait..", true, false);
            pDialog.setCanceledOnTouchOutside(true);
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                yourJsonStringUrl = yourJsonStringUrl.replaceAll(" ","%20");
                URL url = new URL(yourJsonStringUrl);
               // Log.d(TAG, "URL_is: " + url);
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
                    code = jo.getString("code");
                    message = jo.getString("message");
                    if (code.equals("200")) {
                        JSONArray  dataJsonArr = jo.getJSONArray("Result");
                        for (int i = 0; i < dataJsonArr.length(); i++) {
                            JSONObject c = null;
                            try {
                                c = dataJsonArr.getJSONObject(i);
                                attTime.add(c.getString("att_time"));
                                attStatus.add(c.getString("status"));
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    return code;
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return "";
            }

        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if (code.equals("200")) {
                showAttendanceDetail();
            } else if (code.equals("501")) {
                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(getApplicationContext(),createTokenUrl).execute().get();
                   // Log.d(TAG,"Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token,getApplicationContext());
                        new AsyncAttendanceList().execute();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    new AsyncAttendanceList().execute();
                }catch (JSONException je){
                    je.printStackTrace();
                    new AsyncAttendanceList().execute();
                }catch (NullPointerException ne){
                    new AsyncAttendanceList().execute();
                }

            }
            else {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }

        }
    }

        public class AsyncPresenceList extends AsyncTask<String, String, String> {
        final String TAG = "AsyncPresenceList";

        // set your json string url here
        String yourJsonStringUrl = "http://wsnew.oyefood.in/WS/delivery_service.php?mode=list_presnce_date_updated&user_id="+userId;
        String code,message;
        ArrayList<Date> precenseTime = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(MakeAttendance.this, "", "Wait..", true, false);
            pDialog.setCanceledOnTouchOutside(true);
        }

        @Override
        protected String doInBackground(String... arg0) {

            try {
                yourJsonStringUrl = yourJsonStringUrl.replaceAll(" ","%20");
                URL url = new URL(yourJsonStringUrl);
               // Log.d(TAG, "URL_is: " + url);
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
                    code = jo.getString("code");
                    message = jo.getString("message");
                    if (code.equals("200")) {
                        JSONArray  dataJsonArr = jo.getJSONArray("Result");
                        for (int i = 0; i < dataJsonArr.length(); i++) {
                            JSONObject c = null;
                            try {
                                c = dataJsonArr.getJSONObject(i);
                                String dateval= c.getString("att_time");
                                //precenseTime.add(Long.valueOf(c.getString("att_time")));
                                SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                                Date date = format.parse(dateval);
                                precenseTime.add(date);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    return code;
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (code.equals("200")) {
                for (int i=0;i<precenseTime.size();i++)
                {
                    caldroidFragment.setTextColorForDate(R.color.md_green_400,precenseTime.get(i));
                    //caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.drawable.txt_shape),precenseTime.get(i));
                }
                caldroidFragment.refreshView();
                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                t.replace(R.id.calendar1, caldroidFragment);
                t.commit();

                // Setup listener
                final CaldroidListener listener = new CaldroidListener() {

                    @Override
                    public void onSelectDate(Date date, View view) {
                       /* Toast.makeText(getApplicationContext(), formatter.format(date),
                                Toast.LENGTH_SHORT).show();*/
                        selectedDate = formatter.format(date);
                        new AsyncAttendanceList().execute();
                    }

                    @Override
                    public void onChangeMonth(int month, int year) {
                        String text = "month: " + month + " year: " + year;
                       /* Toast.makeText(getApplicationContext(), text,
                                Toast.LENGTH_SHORT).show();*/
                    }

                    @Override
                    public void onLongClickDate(Date date, View view) {
                       /* Toast.makeText(getApplicationContext(),
                                "Long click " + formatter.format(date),
                                Toast.LENGTH_SHORT).show();*/
                    }

                    @Override
                    public void onCaldroidViewCreated() {
                        if (caldroidFragment.getLeftArrowButton() != null) {
                            /*Toast.makeText(getApplicationContext(),
                                    "Caldroid view is created", Toast.LENGTH_SHORT)
                                    .show();*/
                        }
                    }

                };

                // Setup Caldroid
                caldroidFragment.setCaldroidListener(listener);

            }
            else if(code.equals("400"))
            {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                // define a listener to receive callbacks when certain events happen.
            }
            else if (code.equals("501")) {
                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(getApplicationContext(),createTokenUrl).execute().get();
                   // Log.d(TAG,"Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token,getApplicationContext());
                        new AsyncPresenceList().execute();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    new AsyncPresenceList().execute();
                }catch (JSONException je){
                    je.printStackTrace();
                    new AsyncPresenceList().execute();
                }catch (NullPointerException ne){
                    new AsyncPresenceList().execute();
                }

            }
            else {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }

        }
    }

        public void showMarkAttendance() {
            // f.setTitle("Add Review");
            final TextView tvMarkAttendance = (TextView) dialogAtt.findViewById(R.id.tvMarkAttendance);
            final TextView tvBreak = (TextView) dialogAtt.findViewById(R.id.tvBreak);
            final TextView tvBackWork = (TextView) dialogAtt.findViewById(R.id.tvBackWork);
            final TextView tvWorkOver = (TextView) dialogAtt.findViewById(R.id.tvWorkOver);
            final TextView tvCancel = (TextView) dialogAtt.findViewById(R.id.tvCancel);

            tvMarkAttendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if we can't access the location yet
                   /* if (!location.hasLocationEnabled()) {
                        // ask the user to enable location access
                        SimpleLocation.openSettings(MakeAttendance.this);
                    }
                    else
                    {
                        location.beginUpdates();
                        currentLat = String.valueOf(location.getLatitude());
                        currentLng = String.valueOf(location.getLongitude());
                        status = "Attendance Marked";
                        new AsyncMakeAttendance().execute();
                    }*/

                    if (!Utils.isGPSTurnOn(getApplicationContext())) {
                        showGPSDialog();
                        return;
                    }else{
                        status = "Attendance Marked";
                        new AsyncMakeAttendance().execute();
                    }


                }
            });

            tvBreak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // if we can't access the location yet
                   /* if (!location.hasLocationEnabled()) {
                        // ask the user to enable location access
                        SimpleLocation.openSettings(MakeAttendance.this);
                    }
                    else
                    {
                        location.beginUpdates();
                        currentLat = String.valueOf(location.getLatitude());
                        currentLng = String.valueOf(location.getLongitude());
                        status = "Break";
                        new AsyncMakeAttendance().execute();
                    }*/

                    if (!Utils.isGPSTurnOn(getApplicationContext())) {
                        showGPSDialog();
                        return;
                    }else{
                        status = "Break";
                        new AsyncMakeAttendance().execute();
                    }

                }
            });

            tvBackWork.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if we can't access the location yet
                    /*if (!location.hasLocationEnabled()) {
                        // ask the user to enable location access
                        SimpleLocation.openSettings(MakeAttendance.this);
                    }
                    else
                    {
                        location.beginUpdates();
                        currentLat = String.valueOf(location.getLatitude());
                        currentLng = String.valueOf(location.getLongitude());
                        status = "Return From Break";
                        new AsyncMakeAttendance().execute();
                    }*/

                    if (!Utils.isGPSTurnOn(getApplicationContext())) {
                        showGPSDialog();
                        return;
                    }else{
                        status = "Return From Break";
                        new AsyncMakeAttendance().execute();
                    }

                }
            });

            tvWorkOver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if we can't access the location yet
                    /*if (!location.hasLocationEnabled()) {
                        // ask the user to enable location access
                        SimpleLocation.openSettings(MakeAttendance.this);
                    }
                    else
                    {
                        location.beginUpdates();
                        currentLat = String.valueOf(location.getLatitude());
                        currentLng = String.valueOf(location.getLongitude());
                        status = "Work Over";
                        new AsyncMakeAttendance().execute();
                    }*/

                    if (!Utils.isGPSTurnOn(getApplicationContext())) {
                        showGPSDialog();
                        return;
                    }else{
                        status = "Work Over";
                        new AsyncMakeAttendance().execute();
                    }
                }
            });

            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAtt.dismiss();
                }
            });

            dialogAtt.show();

        }

        public void showAttendanceDetail() {
            // f.setTitle("Add Review");
            final ListView lvAttendance = (ListView) f.findViewById(R.id.lvAttendance);
            CustomAttendanceList attAdapter = new CustomAttendanceList(getApplicationContext(), attTime, attStatus);
            lvAttendance.setAdapter(attAdapter);
            f.show();

        }

        public class NetworkChangeReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(final Context context, final Intent intent) {

                //Log.v("Network Change", "Receieved notification about network status");
                isNetworkAvailable(context);

            }


            private boolean isNetworkAvailable(Context context) {
                ConnectivityManager connectivity = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity != null) {
                    NetworkInfo[] info = connectivity.getAllNetworkInfo();
                    if (info != null) {
                        for (int i = 0; i < info.length; i++) {
                            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                                if (!isConnected) {
                                    Log.v("Network Change", "Now you are connected to Internet!");
                                    isConnected = true;
                                    //do your processing here ---
	   	        /*Intent intent = new Intent(MapActivity.this, MapActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				finish();*/
                                    mGoogleApiClient.connect();
                                }
                                return true;
                            }
                        }
                    }
                }
                Log.v("Network Change", "You are not connected to Internet!");
                isConnected = false;
                return false;
            }
        }
    }

