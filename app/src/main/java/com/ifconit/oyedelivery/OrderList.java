package com.ifconit.oyedelivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import im.delight.android.location.SimpleLocation;

/**
 * Created by rakhit on 4/20/2016.
 */
public class OrderList extends BaseDrawer   {
    ListView lvOrderList;
    ProgressDialog pDialog;
    String code,message,result,token,userId,strBaseUrl,orderId,refNo,finalAmount,statusFlag,orderDate,locality,restName,status;
    String expDelTime,cws;
    SharedPreferences prefsUid;
    public static final String PREFS_UID="loginUserId";
    public static ArrayList<String> alOrderId,alRefNo,alStatus,alDate,alAmount,alRestName,alAddr,alExpDelTime;
    CustomOrderList clAdapter;
    TextView tvNoOrder;
    String cartStatus="2";
    String total_distance;
    String currentLat="",currentLng="";
    private static final int REQUEST_CODE_PERMISSION = 2;
    private SimpleLocation location;
    String mPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
    RequestQueue requestQueue;
    JSONArray dataJsonArr = null,expenceJsonArr=null;
    RelativeLayout rlProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.order_list, frameLayout);

        /*Intent mServiceIntent = new Intent(this, LocationService.class);
        startService(mServiceIntent);
*/
        startService(new Intent(this, MyService.class));

        token=GlobalClass.getToken(this);
        strBaseUrl=getApplicationContext().getString(R.string.base_url);
        cartStatus=getIntent().getStringExtra("cart_status");

        prefsUid = getSharedPreferences(PREFS_UID, 0);
        userId= prefsUid.getString("uid", "");
        tvNoOrder=(TextView) findViewById(R.id.txtvwNoOrder);
        lvOrderList=(ListView)findViewById(R.id.lvOrderList);

        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        requestQueue.getCache().clear();

        tvNoOrder.setVisibility(View.GONE);
        imgvwAttendance.setVisibility(View.GONE);
        tvStart.setVisibility(View.VISIBLE);
        tvStop.setVisibility(View.VISIBLE);
        tvKm.setVisibility(View.VISIBLE);

        if((int)Build.VERSION.SDK_INT < 23)
        {
            getLocationValue();
        }
        else
        {
            try {
                if (ActivityCompat.checkSelfPermission(this, mPermission)
                        != MockPackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{mPermission}, REQUEST_CODE_PERMISSION);

                    // If any permission above not allowed by user, this condition will execute every time, else your else part will work
                }
                else
                {
                    getLocationValue();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

      /*  if (!Utils.isGPSTurnOn(getApplicationContext())) {
            showGPSDialog();
            return;
        }else {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(OrderList.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(OrderList.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED) {
                    mLocationRequest = LocationRequest.create();
                    mLocationRequest.setInterval(1 * 1000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                    //mLocationClient = new LocationClient(this, this, this);
                    mGoogleApiClient = new GoogleApiClient.Builder(OrderList.this)
                            .addConnectionCallbacks(OrderList.this)
                            .addOnConnectionFailedListener(OrderList.this)
                            .addApi(LocationServices.API)
                            .build();
                    mGoogleApiClient.connect();

                } else {
                    if (!shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) && !shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showMessageOKCancel("You need to allow access to GPS",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    @TargetApi(23)
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                });
                        return;
                    }
                    requestPermissions(
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
            } else {
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setInterval(1 * 1000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                //mLocationClient = new LocationClient(this, this, this);
                mGoogleApiClient = new GoogleApiClient.Builder(OrderList.this)
                        .addConnectionCallbacks(OrderList.this)
                        .addOnConnectionFailedListener(OrderList.this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
            }
        }*/


        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if we can't access the location yet
                if (!location.hasLocationEnabled()) {
                    // ask the user to enable location access
                    SimpleLocation.openSettings(OrderList.this);
                }
                else
                {
                    location.beginUpdates();
                    currentLat = String.valueOf(location.getLatitude());
                    currentLng = String.valueOf(location.getLongitude());
                    if(currentLat.equals("")||currentLng.equals(""))
                    {
                        Toast.makeText(getApplicationContext(),"Your Current Location Not Found!",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if (Utils.isConnected(getApplicationContext())) {
                            new StartServiceAsync().execute();
                        }else{
                            Toast.makeText(getApplicationContext(),"No internet connection available!!",Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });

        tvStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if we can't access the location yet
                if (!location.hasLocationEnabled()) {
                    // ask the user to enable location access
                    SimpleLocation.openSettings(OrderList.this);
                }
                else
                {
                    location.beginUpdates();
                    currentLat = String.valueOf(location.getLatitude());
                    currentLng = String.valueOf(location.getLongitude());
                    if(currentLat.equals("")||currentLng.equals(""))
                    {
                        Toast.makeText(getApplicationContext(),"Your Current Location Not Found!",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if (Utils.isConnected(getApplicationContext())) {
                            new StopService().execute();
                        }else{
                            Toast.makeText(getApplicationContext(),"No internet connection available!!",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        lvOrderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), OrderDetail.class);
                i.putExtra("orderId", alOrderId.get(position));
                startActivity(i);
               // finish();
            }
        });
        Log.d("StartService","OnCreate status "+cartStatus);
        if (Utils.isConnected(getApplicationContext())) {
           // new AsyncOrderList().execute();
            getOrderList();
        }else{
            Toast.makeText(getApplicationContext(),"No internet connection available!!",Toast.LENGTH_SHORT).show();
        }
    }//close onCreate

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length == 1 &&
                grantResults[0] == MockPackageManager.PERMISSION_GRANTED ) {
                //Request Granted Stuff
                getLocationValue();
            }
            else{
                Log.d("jjjjjjjj","requestNotGranted");
                try {
                    if (ActivityCompat.checkSelfPermission(this, mPermission)
                            != MockPackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{mPermission}, REQUEST_CODE_PERMISSION);

                        // If any permission above not allowed by user, this condition will execute every time, else your else part will work
                    }
                    else
                    {
                        getLocationValue();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void getOrderList(){
        alRefNo=new ArrayList<>();
        alDate=new ArrayList<>();
        alAmount=new ArrayList<>();
        alRestName=new ArrayList<>();
        alAddr=new ArrayList<>();
        alOrderId=new ArrayList<>();
        alStatus=new ArrayList<>();
        alExpDelTime = new ArrayList<>();

        String yourJsonStringUrl = strBaseUrl+"order_list_delivery&user_id="+userId+"&cart_status="+cartStatus+"&token="+token;// set your json string url here
        rlProgress.setVisibility(View.VISIBLE);

        yourJsonStringUrl=yourJsonStringUrl.replaceAll(" ","%20");
        Log.d("JSONURL",""+yourJsonStringUrl);

        JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(yourJsonStringUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //loading.dismiss();
                requestQueue.cancelAll("tag1");
                showOrderList(response);
            }
        },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.d("onError", " network error");
                        requestQueue.cancelAll("tag1");
                        rlProgress.setVisibility(View.GONE);

                    }
                }
        );
        jsonObjectRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest1.setShouldCache(false);
        jsonObjectRequest1.setTag("tag1");
        requestQueue.add(jsonObjectRequest1);
        requestQueue.getCache().remove(yourJsonStringUrl);
        requestQueue.getCache().clear();
    }

    public void showOrderList(JSONObject json){
        try {

            code=json.getString("code");
            // Log.d(TAG, "code: " + code);
            message=json.getString("message");
            // Log.d(TAG,"message: "+message);
            if (code.equals("200")) {

                rlProgress.setVisibility(View.GONE);
                result = json.getString("Result");
                total_distance = json.getString("total_distance");
                // get the hharray of users
                dataJsonArr = json.getJSONArray("Result");
                for (int i = 0; i < dataJsonArr.length(); i++) {
                    JSONObject c = null;
                    try {
                        c = dataJsonArr.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // Storing each json item in variable

                    try {
                        orderId = c.getString("order_id");
                        refNo = c.getString("ref_no");
                        finalAmount = c.getString("final_total");
                        orderDate = GlobalClass.toLocal(c.getString("order_date"));
                        locality = c.getString("locality");
                        restName = c.getString("restaurant_name");
                        status=c.getString("cart_status");
                        expDelTime = c.getString("expected_del_time");

                        alRefNo.add(refNo);
                        alDate.add(orderDate);
                        alAmount.add(finalAmount);
                        alRestName.add(restName);
                        alAddr.add(locality);
                        alOrderId.add(orderId);
                        alStatus.add(status);
                        alExpDelTime.add(expDelTime);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    tvKm.setText(total_distance + " Km");
                    Toast.makeText(getApplicationContext(), "Data loaded successfully..", Toast.LENGTH_SHORT).show();
                    clAdapter = new CustomOrderList(OrderList.this, alRefNo, alStatus, alDate, alAmount, alRestName, alAddr, alExpDelTime);
                    lvOrderList.setAdapter(clAdapter);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }else if (code.equals("501")) {

                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(OrderList.this,createTokenUrl).execute().get();
                    Log.d("Login","Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, OrderList.this);
                        // new AsyncOrderDetail().execute();
                        getOrderList();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    // new AsyncOrderDetail().execute();
                    getOrderList();
                }catch (JSONException je){
                    je.printStackTrace();
                    //  new AsyncOrderDetail().execute();
                    getOrderList();
                }catch (NullPointerException ne){
                    //  new AsyncOrderDetail().execute();
                    getOrderList();
                }
            } else {
                rlProgress.setVisibility(View.GONE);
                Toast.makeText(OrderList.this, message, Toast.LENGTH_SHORT).show();
                tvNoOrder.setVisibility(View.VISIBLE);
            }

        }catch (JSONException je){
            je.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

   /* public class AsyncOrderList extends AsyncTask<String, String, String> {

        final String TAG = "AsyncOrderList.java";
        String yourJsonStringUrl = strBaseUrl+"order_list_delivery&user_id="+userId+"&cart_status="+cartStatus+"&token="+token;// set your json string url here

        // contacts JSONArray
        JSONArray dataJsonArr = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            alRefNo=new ArrayList<>();
            alDate=new ArrayList<>();
            alAmount=new ArrayList<>();
            alRestName=new ArrayList<>();
            alAddr=new ArrayList<>();
            alOrderId=new ArrayList<>();
            alStatus=new ArrayList<>();
            alExpDelTime = new ArrayList<>();

            // pDialog =  ProgressDialog.show(LoginActivity.this,"","Wait for login.....", true,false);
            pDialog = new ProgressDialog(OrderList.this);
            pDialog.setMessage(" data loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL(yourJsonStringUrl);

               // Log.d("StartService", "URL_is: " + url);
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
                    if (code.equals("200")) {
                        result = json.getString("Result");
                        total_distance = json.getString("total_distance");
                        // get the hharray of users
                        dataJsonArr = json.getJSONArray("Result");
                        for (int i = 0; i < dataJsonArr.length(); i++) {
                            JSONObject c = null;
                            try {
                                c = dataJsonArr.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Storing each json item in variable

                            try {
                                orderId = c.getString("order_id");
                                refNo = c.getString("ref_no");
                                finalAmount = c.getString("final_total");
                                orderDate = GlobalClass.toLocal(c.getString("order_date"));
                                locality = c.getString("locality");
                                restName = c.getString("restaurant_name");
                                status=c.getString("cart_status");
                                expDelTime = c.getString("expected_del_time");

                                alRefNo.add(refNo);
                                alDate.add(orderDate);
                                alAmount.add(finalAmount);
                                alRestName.add(restName);
                                alAddr.add(locality);
                                alOrderId.add(orderId);
                                alStatus.add(status);
                                alExpDelTime.add(expDelTime);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
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
            try
            {
            pDialog.dismiss();
            if (code.equals("200")) {
                try {
                    tvKm.setText(total_distance + " Km");
                    Toast.makeText(getApplicationContext(), "Data loaded successfully..", Toast.LENGTH_SHORT).show();
                    clAdapter = new CustomOrderList(OrderList.this, alRefNo, alStatus, alDate, alAmount, alRestName, alAddr, alExpDelTime);
                    lvOrderList.setAdapter(clAdapter);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }

            } else if (code.equals("501")) {
                *//*try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);

                    JsonParser jParser = new JsonParser();
                    JSONObject jo = jParser.getJSONFromUrl(createTokenUrl);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, OrderList.this);
                        new AsyncOrderList().execute();
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }*//*

                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(OrderList.this,createTokenUrl).execute().get();
                   // Log.d(TAG,"Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, OrderList.this);
                        new AsyncOrderList().execute();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    new AsyncOrderList().execute();
                }catch (JSONException je){
                    je.printStackTrace();
                    new AsyncOrderList().execute();
                }catch (NullPointerException ne){
                    new AsyncOrderList().execute();
                }

            } else if (code.equals("400")) {
                tvNoOrder.setVisibility(View.VISIBLE);
            }
        } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    Log.d("StartService","OnActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        startActivity(new Intent(OrderList.this,OrderList.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
       // new AsyncOrderList().execute();
        getOrderList();
    }

    @Override
    public void onResume() {
        cartStatus=getIntent().getStringExtra("cart_status");
        Log.d("StartService","OnResume status "+cartStatus);
        super.onResume();
        if ((int)Build.VERSION.SDK_INT < 23) {
            // make the device update its location
                location.beginUpdates();
                currentLat = String.valueOf(location.getLatitude());
                currentLng = String.valueOf(location.getLongitude());
        }



    }
/*
    @Override
    public void onConnected(Bundle bundle) {
        try {
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.v("location_onconnected", "" + currentLocation);
            if (currentLocation != null) {
                try {
                    //new GetFriends().execute();
                    onLocationChanged(currentLocation);
                } catch (NullPointerException e) {
                    Log.d("Location_Service", "Connect to the Intenet to get the current Location");
                    return;
                } catch (IllegalStateException ie){
                    ie.printStackTrace();
                }
            } else {
                // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(OrderList.this,android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(OrderList.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED) {
                        mLocationRequest = LocationRequest.create();
                        mLocationRequest.setInterval(1*1000);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                        //mLocationClient = new LocationClient(this, this, this);
                        mGoogleApiClient = new GoogleApiClient.Builder(OrderList.this)
                                .addConnectionCallbacks(OrderList.this)
                                .addOnConnectionFailedListener(OrderList.this)
                                .addApi(LocationServices.API)
                                .build();
                        mGoogleApiClient.connect();

                    } else {
                        if (!shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) && !shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showMessageOKCancel("You need to allow access to GPS",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        @TargetApi(23)
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                                                    REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    });
                            return;
                        }
                        requestPermissions(
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                }else {
                    mLocationRequest = LocationRequest.create();
                    mLocationRequest.setInterval(1*1000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                    //mLocationClient = new LocationClient(this, this, this);
                    mGoogleApiClient = new GoogleApiClient.Builder(OrderList.this)
                            .addConnectionCallbacks(OrderList.this)
                            .addOnConnectionFailedListener(OrderList.this)
                            .addApi(LocationServices.API)
                            .build();
                    mGoogleApiClient.connect();
                }
                //}
            }
        }catch (SecurityException se){
            se.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationRequest = LocationRequest.create();
                    mLocationRequest.setInterval(1*1000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                    //mLocationClient = new LocationClient(this, this, this);
                    mGoogleApiClient = new GoogleApiClient.Builder(OrderList.this)
                            .addConnectionCallbacks(OrderList.this)
                            .addOnConnectionFailedListener(OrderList.this)
                            .addApi(LocationServices.API)
                            .build();
                    mGoogleApiClient.connect();

                } else {
                    // Permission Denied
                    // Toast.makeText(SelectAddress.this, "Location Acces Denied", Toast.LENGTH_SHORT)
                    //           .show();

                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            //if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // Received permission result for READ_PHONE_STATE permission.est.");
            // Check if the only required permission has been granted
            // }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }




    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            Log.i("onlocation changed", "" + latitude + "," + longitude);
            LatLng coordinate = new LatLng(latitude, longitude);

            Log.i("coordinate", "" + coordinate.latitude + ","
                    + coordinate.longitude);
            currentLat=String.valueOf(latitude);
            currentLng=String.valueOf(longitude);
        }
        else {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(OrderList.this,android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(OrderList.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED) {
                    mLocationRequest = LocationRequest.create();
                    mLocationRequest.setInterval(1*1000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                    //mLocationClient = new LocationClient(this, this, this);
                    mGoogleApiClient = new GoogleApiClient.Builder(OrderList.this)
                            .addConnectionCallbacks(OrderList.this)
                            .addOnConnectionFailedListener(OrderList.this)
                            .addApi(LocationServices.API)
                            .build();
                    mGoogleApiClient.connect();

                } else {
                    if (!shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) && !shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showMessageOKCancel("You need to allow access to GPS",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    @TargetApi(23)
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                });
                        return;
                    }
                    requestPermissions(
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
            }else {
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setInterval(1*1000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                mGoogleApiClient = new GoogleApiClient.Builder(OrderList.this)
                        .addConnectionCallbacks(OrderList.this)
                        .addOnConnectionFailedListener(OrderList.this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
            }

        }
    }*/

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(OrderList.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
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
                Toast.makeText(OrderList.this, " For Executing Tap On Yes", Toast.LENGTH_SHORT)
                        .show();
            }
        }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }
/*========================================================================================================================================================================================================================================*/
    public class StopService extends AsyncTask<String, String, String> {
    final String TAG = "StopService.java";
    // set your json string url here
    String autoAssignUrl = "http://wsnew.oyefood.in/WS/delivery_service.php?mode=stop_service&user_id="+userId+"&current_lat="+currentLat+"&current_lng="+currentLng;
    String code,message;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = ProgressDialog.show(OrderList.this, "", "Wait..", true, false);
        pDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    protected String doInBackground(String... arg0) {
        try {
            Log.d(TAG, autoAssignUrl);
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrl(autoAssignUrl);

            code = json.getString("code");
            //  Log.d("code", code);
            message = json.getString("message");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    @Override
    protected void onPostExecute(String strFromDoInBg) {

        try{
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (code.equals("200")) {
                  /* editor.putBoolean(startKey, false);
                   editor.commit();*/
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            } else if (code.equals("501")) {
                // Log.v("JSON VALUE IS ", "" + strFromDoInBg);
                //Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);

                    JsonParser jParser = new JsonParser();
                    JSONObject jo = jParser.getJSONFromUrl(createTokenUrl);

                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, OrderList.this);
                        new StopService().execute();
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            else
            {
                if(message.equals(""))
                {
                    message = "Please Try After Some Time!!";
                }
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
        catch (IllegalArgumentException ex)
        {
            ex.printStackTrace();
        }
    }
}//close AsyncAutoAssign
/*========================================================================================================================================================================================================================================*/
    public class StartServiceAsync extends AsyncTask<String, String, String> {
    final String TAG = "StartServiceAsync.java";
    // set your json string url here
    String autoAssignUrl = "http://wsnew.oyefood.in/WS/delivery_service.php?mode=start_del_track&user_id="+userId+"&current_lat="+currentLat+"&current_lng="+currentLng;
    String code,message;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = ProgressDialog.show(OrderList.this, "", "Wait..", true, false);
        pDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    protected String doInBackground(String... arg0) {
        try {
            Log.d(TAG, autoAssignUrl);
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrl(autoAssignUrl);

            code = json.getString("code");
            //  Log.d("code", code);
            message = json.getString("message");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    @Override
    protected void onPostExecute(String strFromDoInBg) {

        try{
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (code.equals("200")) {
                //  inRouteStatus = false;
                // btDelivered.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Your Tracking Started!", Toast.LENGTH_SHORT).show();
               /* editor.putBoolean(startKey, true);
                // Save the changes in SharedPreferences
                editor.commit(); // commit changes*/
                // tvStart.setText("Stop");

            } else if (code.equals("501")) {
                // Log.v("JSON VALUE IS ", "" + strFromDoInBg);
                //Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);

                    JsonParser jParser = new JsonParser();
                    JSONObject jo = jParser.getJSONFromUrl(createTokenUrl);

                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, OrderList.this);
                        new StartServiceAsync().execute();
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            else
            {
                if(message.equals(""))
                {
                    message = "Please Try After Some Time!!";
                }
                location.beginUpdates();
                currentLat = String.valueOf(location.getLatitude());
                currentLng = String.valueOf(location.getLongitude());
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
        catch (IllegalArgumentException ex)
        {
            ex.printStackTrace();
        }
    }
}//close AsyncAutoAssign
/*========================================================================================================================================================================================================================================*/
    public void getLocationValue()
    {
        // construct a new instance of SimpleLocation
        location = new SimpleLocation(this);

        // if we can't access the location yet
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(this);
        }
        else
        {
            currentLat = String.valueOf(location.getLatitude());
            currentLng = String.valueOf(location.getLongitude());
        }
    }

}