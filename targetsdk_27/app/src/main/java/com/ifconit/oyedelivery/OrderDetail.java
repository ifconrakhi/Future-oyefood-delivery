package com.ifconit.oyedelivery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.mindorks.paracamera.Camera;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import im.delight.android.location.SimpleLocation;

import static com.ifconit.oyedelivery.OrderList.countDownTimer;


/**
 * Created by rakhit on 4/21/2016.
 */
public class OrderDetail extends AppCompatActivity {

    TextView custName, custPhone, custAddr, custAmountToRec, restName, restAddr, restAmountToPay,tvStatus;
    ListView lvItemDetail;
    LinearLayout llSignPic,llClubDelCode;
    ProgressDialog pDialog;
    String token, strBaseUrl, code, message, oId,orderId,OrderStatus="",signaturePath,comment,order_type,cws;
    String cNm, cPh, cAddr, cAmt, rNm, rAddr, rAmt, itemNm, itemQty,strRestAmt,cbStatus,strCustAmt,cbDepositeStatus,walletAmt;
    TextView tvAmountToPay2,tvAmountToPay3,tvAmountToPay5;
    private Menu menu;
    SharedPreferences prefsOrderId;
    SharedPreferences.Editor editor;
    public static final String PREFS_OID = "OrderId";
    public static boolean inRouteStatus=false;
    String res_payment1,res_payment2,res_payment3;
    RelativeLayout rviewPayment3,rviewPayment4,rviewPayment5;
    String in_route_time;
    TextView tvInRouteTime,tvDelType,tvOrderComment;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    String lastLat="",lastLng="";
    SharedPreferences prefsUid;
    public static final String PREFS_UID="loginUserId";
    String userId,userUltContact,contactNo;
    TextView tvCustUltNo;
    int version = Build.VERSION.SDK_INT;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private SimpleLocation location;
    String codeInRoute = "400",JsonURL,delMsg,captureBill,club_status,strClubDelCode;
    int totalSize = 0,sign=0;
    Button btTakePic;
    Dialog altDia;
    File sourceFile;
    int progress = 0;
    RelativeLayout barContent;
    TextView tvPercentage;
    public DonutProgress donut_progress;
    private final static int CAMERA_RQ = 6969;
    private final static int PERMISSION_RQ = 84;
    RequestQueue requestQueue;
    JSONArray dataJsonArr = null;
    RelativeLayout rlProgress;
    ArrayList<String> alItemName ;
    ArrayList<String> alItemQty ;
    ArrayList<String> alItemPrice ;
    private Camera camera;

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
        setContentView(R.layout.order_details);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        token = GlobalClass.getToken(this);
        strBaseUrl = getApplicationContext().getString(R.string.base_url);

        prefsOrderId = getSharedPreferences(PREFS_OID, 0);
        editor = prefsOrderId.edit();

        prefsUid = getSharedPreferences(PREFS_UID, 0);
        userId= prefsUid.getString("uid", "");

        orderId = getIntent().getStringExtra("orderId");//from intent

        tvStatus=(TextView)findViewById(R.id.tvStatus);
        custName = (TextView) findViewById(R.id.tvCustName);
        custPhone = (TextView) findViewById(R.id.tvCustPhone);
        custPhone.setPaintFlags(custPhone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        custAddr = (TextView) findViewById(R.id.tvCustAddr);
        custAmountToRec = (TextView) findViewById(R.id.tvAmountToTake);
        restName = (TextView) findViewById(R.id.tvRestName);
        restAddr = (TextView) findViewById(R.id.tvRestAddr);
        restAmountToPay = (TextView) findViewById(R.id.tvAmountToPay);
        lvItemDetail = (ListView) findViewById(R.id.itemList);
        tvAmountToPay2 = (TextView) findViewById(R.id.tvAmountToPay2);
        tvAmountToPay3 = (TextView) findViewById(R.id.tvAmountToPay3);
        tvAmountToPay5 = (TextView) findViewById(R.id.tvAmountToPay5);
        rviewPayment3 = (RelativeLayout) findViewById(R.id.rviewPayment3);
        rviewPayment4 = (RelativeLayout) findViewById(R.id.rviewPayment4);
        rviewPayment5 = (RelativeLayout) findViewById(R.id.rviewPayment5);
        tvInRouteTime = (TextView) findViewById(R.id.tvInRouteTime);
        tvCustUltNo = (TextView) findViewById(R.id.tvCustUltNo);
        tvDelType=(TextView)findViewById(R.id.tvDeliveryType);
        tvOrderComment=(TextView)findViewById(R.id.tvOrderComment);
        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        requestQueue.getCache().clear();



        tvCustUltNo.setPaintFlags(tvCustUltNo.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (Utils.isConnected(getApplicationContext()))
        {
           // new AsyncOrderDetail().execute();
            getOrderDetail();
        }
        else{
            Toast.makeText(getApplicationContext(),"No internet connection available!!",Toast.LENGTH_SHORT).show();
        }

        // construct a new instance of SimpleLocation
        location = new SimpleLocation(this);

        // if we can't access the location yet
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(this);
        }
        else
        {
            lastLat = String.valueOf(location.getLatitude());
            lastLng = String.valueOf(location.getLongitude());
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to save videos in external storage
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_RQ);
        }


        stopVibration();


        /*if (!Utils.isGPSTurnOn(getApplicationContext())) {
            showGPSDialog();
            return;
        }else{
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(OrderDetail.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(OrderDetail.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED) {
                    mLocationRequest = LocationRequest.create();
                    mLocationRequest.setInterval(1*1000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                    //mLocationClient = new LocationClient(this, this, this);
                    mGoogleApiClient = new GoogleApiClient.Builder(OrderDetail.this)
                            .addConnectionCallbacks(OrderDetail.this)
                            .addOnConnectionFailedListener(OrderDetail.this)
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
            }else{
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setInterval(1*1000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                //mLocationClient = new LocationClient(this, this, this);
                mGoogleApiClient = new GoogleApiClient.Builder(OrderDetail.this)
                        .addConnectionCallbacks(OrderDetail.this)
                        .addOnConnectionFailedListener(OrderDetail.this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
            }
        }*/

    }//close OnCreate


    private void stopVibration() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    /*@Override
    public void onConnected(Bundle bundle) {
        try {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
           *//* currentLat = String.valueOf(mLastLocation.getLatitude());
            currentLng = String.valueOf(mLastLocation.getLongitude());
            Log.d("PrecenceList","lat"+currentLat+"lang"+currentLng);*//*
           *//* if(currentLat.equals("")||currentLng.equals(""))
            {
                Toast.makeText(getApplicationContext(),"Your Current Location Not Found!",Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (Utils.isConnected(getApplicationContext())) {
                    new AsyncPresenceList().execute();
                }else{
                    Toast.makeText(getApplicationContext(),"No internet connection available!!",Toast.LENGTH_SHORT).show();
                }
            }*//*

            if (mLastLocation != null) {
                try {
                    //new GetFriends().execute();
                    onLocationChanged(mLastLocation);
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "Connect to the Intenet to get the current Location and Map", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            else{
                if (!Utils.isGPSTurnOn(getApplicationContext())) {
                    showGPSDialog();
                    return;
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLat=String.valueOf(location.getLatitude());
        lastLng=String.valueOf(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }*/

    public void getOrderDetail() {
        final String TAG = "getOrderDetail";
        alItemName = new ArrayList<>();
        alItemQty = new ArrayList<>();
        alItemPrice = new ArrayList<>();

        String yourJsonStringUrl = strBaseUrl + "delivery_order_detail&order_id=" + orderId + "&token=" + token;// set your json string url here

        rlProgress.setVisibility(View.VISIBLE);

        yourJsonStringUrl=yourJsonStringUrl.replaceAll(" ","%20");
         Log.d("JSONURL",""+yourJsonStringUrl);

        JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(yourJsonStringUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //loading.dismiss();
                requestQueue.cancelAll("tag1");
                showOrderDetail(response);
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

    public void showOrderDetail(JSONObject json){

        try {

            code=json.getString("code");
            // Log.d(TAG, "code: " + code);
            message=json.getString("message");
            // Log.d(TAG,"message: "+message);
            if (code.equals("200")) {

                rlProgress.setVisibility(View.GONE);
                oId = json.getString("order_id");
                order_type=json.getString("order_type");
                comment=json.getString("comment");
                cNm = json.getString("customer_name");
                cPh = json.getString("customer_mobile");
                cAddr = json.getString("customer_address");
                cAmt = json.getString("amount_to_be_received");
                rNm = json.getString("restaurant_name");
                rAddr = json.getString("restaurant_address");
                rAmt = json.getString("amounttobe_paid_torestaureant");
                OrderStatus = json.getString("cart_status");
                res_payment1 = json.getString("res_payment1");
                res_payment2 = json.getString("res_payment2");
                res_payment3 = json.getString("res_payment3");
                in_route_time = json.getString("in_route_time");
                userUltContact = json.getString("user_ult_contact");
                club_status=json.getString("club_status");
                dataJsonArr = json.getJSONArray("cart");
                for (int i = 0; i < dataJsonArr.length(); i++) {
                    JSONObject c = null;
                    try {
                        c = dataJsonArr.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        itemNm = c.getString("name");
                        itemQty = c.getString("quantity");
                        String itemPrice = c.getString("item_price");

                        alItemName.add(itemNm);
                        alItemQty.add(itemQty);
                        alItemPrice.add(itemPrice);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }//close main loop

                custName.setText(cNm);
                custPhone.setText(cPh);
                custAddr.setText(cAddr);
                custAmountToRec.setText(cAmt);
                restName.setText(rNm);
                restAddr.setText(rAddr);
                restAmountToPay.setText(rAmt);

                tvDelType.setText(order_type);
                tvOrderComment.setText(comment);

                if (userUltContact.equals("")) {
                    tvCustUltNo.setVisibility(View.GONE);
                } else {
                    tvCustUltNo.setVisibility(View.VISIBLE);
                }

                tvCustUltNo.setText(userUltContact);
                tvCustUltNo.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View v) {
                        contactNo = tvCustUltNo.getText().toString().trim();
                        if (version < 23) {
                            Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contactNo));
                            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(in);
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(getApplicationContext(), "Cannot make Call", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            CallUser();
                        }
                    }
                });
                if (res_payment2.equals("0.00") || res_payment2.equals("") || res_payment3.equals("0.00") || res_payment3.equals("") || res_payment3.equals("0.00") || res_payment3.equals("")) {
                    rviewPayment3.setVisibility(View.GONE);
                    rviewPayment4.setVisibility(View.GONE);
                    rviewPayment5.setVisibility(View.GONE);
                } else {
                    tvAmountToPay2.setText(res_payment1);
                    tvAmountToPay3.setText(res_payment2);
                    tvAmountToPay5.setText(res_payment3);
                }
                if (in_route_time != null && !in_route_time.isEmpty() && !in_route_time.equals("null")) {
                    tvInRouteTime.setText(GlobalClass.getStringFormatted(in_route_time));
                }


                if (OrderStatus.equals("2")) {
                    tvStatus.setText(R.string.confirmed);
                    updateMenuTitles();
                } else if (OrderStatus.equals("6")) {
                    editor.putString("orderId", oId);
                    tvStatus.setText(R.string.inRoute);
                    updateMenuTitles();
                } else if (OrderStatus.equals("3")) {
                    tvStatus.setText(R.string.delivered);
                    editor.putString("orderId", "0");
                    updateMenuTitles();
                }
                editor.commit();
                CustomOrderDetail clAdapter = new CustomOrderDetail(OrderDetail.this, alItemName, alItemQty, alItemPrice);
                lvItemDetail.setAdapter(clAdapter);
                setListViewHeightBasedOnChildren(lvItemDetail);
                //  GlobalClass.setListViewHeightBasedOnChildren(lvItemDetail);
            }else if (code.equals("501")) {

                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(OrderDetail.this,createTokenUrl).execute().get();
                    Log.d("Login","Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, OrderDetail.this);
                       // new AsyncOrderDetail().execute();
                        getOrderDetail();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                   // new AsyncOrderDetail().execute();
                    getOrderDetail();
                }catch (JSONException je){
                    je.printStackTrace();
                  //  new AsyncOrderDetail().execute();
                    getOrderDetail();
                }catch (NullPointerException ne){
                  //  new AsyncOrderDetail().execute();
                    getOrderDetail();
                }
            } else {
                rlProgress.setVisibility(View.GONE);
                Toast.makeText(OrderDetail.this, message, Toast.LENGTH_SHORT).show();
            }

        }catch (JSONException je){
            je.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    public  void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        //int totalHeight = 0;
        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);

            if (view instanceof ViewGroup) {
                view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
            }
            view.measure(0, 0);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);

    }

    public void onCall(View v) {
        String val = custPhone.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + val));
        startActivity(intent);
    }

    private void updateMenuTitles() {
        try {
            MenuItem menuItem = menu.findItem(R.id.inRoute);
            if (OrderStatus.equals("6")) {
                menuItem.setTitle(R.string.delivered);
            } else if (OrderStatus.equals("2")) {
                menuItem.setTitle(R.string.inRoute);
            } else if (OrderStatus.equals("3")) {
                menuItem.setVisible(false);
            }
        }catch (NullPointerException npe){
            npe.printStackTrace();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        MenuItem item = menu.findItem(R.id.inRoute);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent=new Intent(getApplicationContext(),OrderList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("cart_status", OrderStatus);
                startActivity(intent);
                finish();
                //  onBackPressed();
                return true;

            case R.id.inRoute:
                // invalidateOptionsMenu();
                if(item.getTitle().equals("In-Route")){
                    // if we can't access the location yet
                    if (!location.hasLocationEnabled()) {
                        // ask the user to enable location access
                        SimpleLocation.openSettings(this);
                    }
                    else
                    {
                        location.beginUpdates();
                        lastLat = String.valueOf(location.getLatitude());
                        lastLng = String.valueOf(location.getLongitude());
                        showInrouteDialog();
                        // new AsyncInRoute().execute();
                        // if(codeInRoute.equals("200")) {

                        // }
                    }
                }
                else if(item.getTitle().equals("Delivered")){
                    // if we can't access the location yet
                    if (!location.hasLocationEnabled()) {
                        // ask the user to enable location access
                        SimpleLocation.openSettings(this);
                    }
                    else
                    {
                        location.beginUpdates();
                        lastLat = String.valueOf(location.getLatitude());
                        lastLng = String.valueOf(location.getLongitude());
                        // new AsyncDelivered().execute();
                        showDeliveredDialog();

                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void showInrouteDialog(){
        altDia= new Dialog(OrderDetail.this);
        altDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
        altDia.setContentView(R.layout.custom_inroute_layout);
        altDia.setCancelable(false);
        final EditText input=(EditText)altDia.findViewById(R.id.input_amount);
        final CheckBox cbPaid=(CheckBox)altDia.findViewById(R.id.cbPaid);
        btTakePic=(Button)altDia.findViewById(R.id.btTakePic);
        Button btnYes = (Button) altDia.findViewById(R.id.btnYes);
        Button btnNo = (Button) altDia.findViewById(R.id.btnNo);
        barContent=(RelativeLayout)altDia.findViewById(R.id.barContent);
        donut_progress=(DonutProgress)altDia. findViewById(R.id.progressBarUpload);
        tvPercentage=(TextView)altDia.findViewById(R.id.txtPercentage) ;

        btTakePic.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
               /* File saveDir = null;
                if (ContextCompat.checkSelfPermission(OrderDetail.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    saveDir = new File(Environment.getExternalStorageDirectory(), "MaterialCamera");
                    saveDir.mkdirs();
                }

                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(OrderDetail.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_RQ);
                    Log.d("Under_Inroute"," if ");
                    new MaterialCamera(OrderDetail.this)
                            .stillShot() // launches the Camera in stillshot mode
                            .labelConfirm(R.string.mcam_use_stillshot)
                            .start(CAMERA_RQ);
                }else{
                    Log.d("Under_Inroute"," else ");
                    new MaterialCamera(OrderDetail.this)
                            .stillShot() // launches the Camera in stillshot mode
                            .labelConfirm(R.string.mcam_use_stillshot)
                            .start(CAMERA_RQ);

                }*/

                if( ContextCompat.checkSelfPermission(OrderDetail.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 5);
                }else{
                    cameraIntent();
                }
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strRestAmt=input.getText().toString().trim();
                if(!cbPaid.isChecked()){
                    cbStatus="0";
                }else{
                    cbStatus="1";
                }
                if(strRestAmt.equals("")||strRestAmt.isEmpty()){
                    strRestAmt="0";
                }else{
                    strRestAmt=input.getText().toString().trim();
                }

               // new UploadInRouteFileToServer().execute();

                if(captureBill!=null&&!captureBill.isEmpty()){
                    Log.d("Under_Inroute"," amt "+strRestAmt+" cbStatus "+cbStatus);
                     // new AsyncInRoute().execute();
                    // new SendInRouteData().execute();
                    new UploadInRouteFileToServer().execute();
                }else{
                    OrderDetail.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderDetail.this,"Please Take Photo of Bill to In route the Order !!",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                altDia.dismiss();
            }
        });

        altDia.show();
    }

    public void showDeliveredDialog(){
        try {
            altDia = new Dialog(OrderDetail.this);
            altDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //f.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            altDia.setContentView(R.layout.custom_dellivered_layout);
            altDia.setCancelable(false);
            // int dialog_height= getting_screen_height(f);
            //int device_height=getHeight(OrderDetail.this);

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            double wi = (double) width / (double) dm.xdpi;
            double hi = (double) height / (double) dm.ydpi;
            double x = Math.pow(wi, 2);
            double y = Math.pow(hi, 2);
            double screenInches = Math.sqrt(x + y);

            Log.d("Under_orderDetail", " screenInches " + screenInches);
            final EditText input = (EditText) altDia.findViewById(R.id.input_amount);
            final CheckBox cbPaid = (CheckBox) altDia.findViewById(R.id.cbPaid);
            Button btnYes = (Button) altDia.findViewById(R.id.btnYes);
            Button btnNo = (Button) altDia.findViewById(R.id.btnNo);
            llSignPic = (LinearLayout) altDia.findViewById(R.id.llSignPic);
            llClubDelCode = (LinearLayout) altDia.findViewById(R.id.clubDelCodeLayout);
            final EditText etClubDelCode = (EditText) altDia.findViewById(R.id.input_club_code);
            final SignaturePad mSignaturePad = (SignaturePad) altDia.findViewById(R.id.signature_pad);
            final ImageButton ibSave = (ImageButton) altDia.findViewById(R.id.ibSave);
            final ImageButton ibRefresh = (ImageButton) altDia.findViewById(R.id.ibRefresh);
            final TextView tvSign = (TextView) altDia.findViewById(R.id.tvSign);

            if (screenInches < 4.5) {
                ViewGroup.LayoutParams lp = llSignPic.getLayoutParams();
                lp.height = 180;
                llSignPic.setLayoutParams(lp);
            }
            //donut_progress=(DonutProgress)f. findViewById(R.id.progressBarUpload);
            //tvPercentage=(TextView)f.findViewById(R.id.txtPercentage) ;

            if (club_status.equalsIgnoreCase("0")) // order from restaurants
            {

                llSignPic.setVisibility(View.VISIBLE);
                llClubDelCode.setVisibility(View.GONE);
                ibRefresh.setVisibility(View.VISIBLE);
                ibSave.setVisibility(View.VISIBLE);
            } else // order from club
            {
                llSignPic.setVisibility(View.GONE);
                ibRefresh.setVisibility(View.GONE);
                ibSave.setVisibility(View.GONE);
                llClubDelCode.setVisibility(View.VISIBLE);
            }

            mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
                @Override
                public void onStartSigning() {
                    // Toast.makeText(OrderDetail.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
                    sign = 1;
                    tvSign.setVisibility(View.GONE);
                }

                @Override
                public void onSigned() {
                    ibSave.setEnabled(true);
                    ibRefresh.setEnabled(true);
                }

                @Override
                public void onClear() {
                    ibSave.setEnabled(false);
                    ibRefresh.setEnabled(false);
                }
            });

            ibRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSignaturePad.clear();
                }
            });
            ibSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                    if (addJpgSignatureToGallery(signatureBitmap)) {
                        Toast.makeText(OrderDetail.this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OrderDetail.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
                    }
//                if (addSvgSignatureToGallery(mSignaturePad.getSignatureSvg())) {
//                    Toast.makeText(MainActivity.this, "SVG Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "Unable to store the SVG signature", Toast.LENGTH_SHORT).show();
//                }
                }
            });


            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {

                        if (club_status.equalsIgnoreCase("0")) // order from restaurants
                        {
                            strCustAmt = input.getText().toString().trim();
                            Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();

                            if (sign == 1) {
                                Uri imgUri = getImageUri1(OrderDetail.this, signatureBitmap);
                                signaturePath = imgUri.getPath();
                                if (strCustAmt.equals("") || strCustAmt.isEmpty()) {
                                    strCustAmt = "0";
                                    cbDepositeStatus = "0";
                                    walletAmt = "0";
                                    Log.d("Under_Inroute if ", " amt " + strCustAmt + " cbDepositeStatus " + cbDepositeStatus + "walletAmt " + walletAmt);
                                } else {
                                    try {
                                        strCustAmt = input.getText().toString().trim();
                                        if (!cbPaid.isChecked()) {
                                            cbDepositeStatus = "0";
                                            walletAmt = "0";
                                        } else {
                                            cbDepositeStatus = "1";
                                            walletAmt = String.valueOf(Float.parseFloat(strCustAmt) - Float.parseFloat(cAmt));
                                        }
                                        Log.d("Under_Inroute else ", " amt " + strCustAmt + " cbDepositeStatus " + cbDepositeStatus + "walletAmt " + walletAmt);
                                    } catch (NumberFormatException nfe) {
                                        nfe.printStackTrace();
                                    }
                                }
                                Log.d("Under_Inroute else ", " signatureBitmap " + signatureBitmap + " signaturePath " + signaturePath);
                                if (signaturePath != null) {
                                    new SendDeliveryData().execute();
//                        JsonURL="http://ws.oyefood.in/WS/admin_service.php?mode=order_take_action_deliveryboy&order_id=" + orderId + "&status=3&user_add_lat="+lastLat+"&user_add_lng="+lastLng+"&del_boy_id="+userId+"&from_user_id=Del"+userId+"&cust_wallet_status="+cbDepositeStatus+"&cust_amount="+strCustAmt+"&wallet_amount="+walletAmt;
//                        pDialog = ProgressDialog.show(OrderDetail.this, "", "file uploading", true);
//                        new Thread(new Runnable() {
//                            public void run() {
//                                SendDeliveryData(signaturePath);
//                            }
//                        }).start();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Please take sign first!", Toast.LENGTH_SHORT).show();
                            }

                        } else // order from club
                        {
                            if (etClubDelCode.getText().toString().trim().equalsIgnoreCase("") || etClubDelCode.getText().toString().trim().isEmpty()) {
                                etClubDelCode.setError("Enter Club Delivery Code");
                            } else {
                                strClubDelCode = etClubDelCode.getText().toString().trim();
                                new SendClubDeliveryData().execute();
                            }


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });

            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    altDia.dismiss();
                }
            });

            altDia.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            Log.d("Under_Inroute ","imgFile "+photo.getAbsolutePath());
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }


    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        OrderDetail.this.sendBroadcast(mediaScanIntent);
    }

    public Uri getImageUri1(Context inContext, Bitmap inImage) {

        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(inImage, 200, 200,
                    true);
            File file = new File(OrderDetail.this.getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out =OrderDetail.this.openFileOutput(file.getName(),
                    Context.MODE_WORLD_READABLE);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //get absolute path
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio; } final float totalPixels = width * height; final float totalReqPixelsCap = reqWidth * reqHeight * 2; while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0,0);
    }

   /* @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_RQ) {

            if (resultCode == RESULT_OK) {
                // Toast.makeText(this, "Saved to: " + data.getDataString(), Toast.LENGTH_LONG).show();
                File file = new File(data.getData().getPath());
                // Uri imgUri = Uri.fromFile(file);
                captureBill = file.getAbsolutePath();

                String filename=captureBill.substring(captureBill.lastIndexOf("/")+1);
                btTakePic.setText(filename);
                btTakePic.setCompoundDrawablesWithIntrinsicBounds(null,null, null, null);
                Log.d("CAPTURE_PICTURE_PATH : ", "*** captureBill " + captureBill+" filename "+filename);

                *//*filePath = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    Bitmap pic=rotateImage(bitmap,90);
                    captureBill = getStringImage(pic);
                    String filename=filePath.getPath().substring(filePath.getPath().lastIndexOf("/")+1);
                    btTakePic.setText(filename);
                    btTakePic.setCompoundDrawablesWithIntrinsicBounds(null,null, null, null);
                    Log.d("CAPTURE_PICTURE_PATH : ", "*** captureBill " + captureBill+" filename "+filename);

                } catch (IOException e) {
                    e.printStackTrace();
                }*//*

               // Bitmap bitmap = GlobalClass.StringToBitMap(picturePath);


            } else if (data != null) {
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }else{
            startActivity(new Intent(OrderDetail.this,OrderDetail.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        //finish();

        //}
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_RQ && resultCode == Activity.RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            File file = new File(data.getData().getPath());
            captureBill = file.getAbsolutePath();
            String filename=captureBill.substring(captureBill.lastIndexOf("/")+1);
            btTakePic.setText("IMG_"+filename+".jpg");
            btTakePic.setCompoundDrawablesWithIntrinsicBounds(null,null, null, null);
            Log.d("CAPTURE_PICTURE_PATH : ", "*** captureBill " + captureBill+" file "+file.toString());
        }
        /*if (requestCode == Camera.REQUEST_TAKE_PHOTO) {
            Bitmap bitmap = camera.getCameraBitmap();
            String filePath=camera.getCameraBitmapPath();
            File file = new File(data.getData().getPath());
            if (bitmap != null) {
               // picFrame.setImageBitmap(bitmap);
                Log.d("CAPTURE_PICTURE_PATH : ", "*** filePath " + filePath+" file "+file.toString());
            } else {
                Toast.makeText(this.getApplicationContext(), "Picture not taken!", Toast.LENGTH_SHORT).show();
            }
        }*/
    }


    public void CallUser() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            // Log.d("Device_id", "in marshmallow");
            requestReadPhoneStatePermission();
        } else {
            // READ_PHONE_STATE permission is already been granted.
            //Log.d("Device_id", "below marshmallow");
            doPermissionGrantedStuffs();
        }
    }

    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CALL_PHONE)) {

            ActivityCompat.requestPermissions(OrderDetail.this,
                    new String[]{android.Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // Received permission result for READ_PHONE_STATE permission.est.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_STATE permission has been granted, proceed with displaying IMEI Number
                //alertAlert(getString(R.string.permision_available_read_phone_state));
                doPermissionGrantedStuffs();
            } else {
                Toast.makeText(OrderDetail.this, "Sorry! Can't Make Call", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if(requestCode==5){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                cameraIntent();
            }else{
                doPermissionGrantedStuffs();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void doPermissionGrantedStuffs() {
        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" +contactNo));
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            startActivity(in);
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        else
        {
            startActivity(in);
        }

    }

    private void cameraIntent()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_RQ);


//        camera = new Camera.Builder()
//                .setDirectory("OyeDeliveryPics")
//                .setName("IMG_" + System.currentTimeMillis())
//                .setImageFormat(Camera.IMAGE_JPEG)
//                .setCompression(75)
//                .setImageHeight(1000)
//                .build(this);
//        try {
//            camera.takePicture();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void onCaptureImageResult(Intent data) {
       try{

           if(data!=null){
               File file = new File(data.getData().getPath());
               // Uri imgUri = Uri.fromFile(file);
              // captureBill = file.getAbsolutePath();

               File destination = new File(Environment.getExternalStorageDirectory(),
                       System.currentTimeMillis() + ".jpg");

               captureBill=destination.toString();

               String filename=captureBill.substring(captureBill.lastIndexOf("/"));
               btTakePic.setText(filename);
               btTakePic.setCompoundDrawablesWithIntrinsicBounds(null,null, null, null);
               Log.d("CAPTURE_PICTURE_PATH : ", "*** captureBill " + captureBill+" filename "+filename);
           }else{
               Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
               e.printStackTrace();
               Log.d("CAPTURE_PICTURE_PATH : ", "*** Exception " + e.getMessage());
               Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
           }


       }catch (Exception e){
           e.printStackTrace();
           Log.d("CAPTURE_PICTURE_PATH : ", "*** captureBill Exception" + e.getMessage());
       }
    }

    class SendDeliveryData extends AsyncTask<String,Void,String> {
        //String yourJsonStringUrl = "http://ws.oyefood.in/WS/admin_service.php?mode=order_take_action_deliveryboy";
        String yourJsonStringUrl="http://ws.oyefood.in/WS/admin_service.php?mode=order_take_action_deliveryboy&order_id=" + orderId + "&status=3&user_add_lat="+lastLat+"&user_add_lng="+lastLng+"&del_boy_id="+userId+"&from_user_id=Del"+userId+"&cust_wallet_status="+cbDepositeStatus+"&cust_amount="+strCustAmt+"&wallet_amount="+walletAmt+"&club_del_code="+strClubDelCode;
        ProgressDialog loading;
        RequestHandler rh = new RequestHandler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(OrderDetail.this, "Uploading File", "Please wait...", true);
        }
        @Override
        protected String doInBackground(String... params) {

//            HashMap<String, String> data = new HashMap<>();
//            data.put("filename", captureBill);

            String result = rh.sendPostRequest(yourJsonStringUrl, signaturePath);
            Log.d("UnderUploadFile","jsonUrl "+yourJsonStringUrl+" result : "+result);
            JSONObject jo = null;
            try {
                jo = new JSONObject(result);

                code = jo.getString("code");
                Log.d("code", "" + code);
                delMsg = jo.getString("message");
                Log.d("message", "" + delMsg);

            } catch (JSONException e) {
                e.printStackTrace();
                loading.dismiss();
                altDia.dismiss();
            }catch (Exception e){
                loading.dismiss();
                altDia.dismiss();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loading.dismiss();
            altDia.dismiss();
            //Toast.makeText(getApplicationContext(),delMsg,Toast.LENGTH_SHORT).show();

            if (code.equals("200")) {
                inRouteStatus=false;
                menu.findItem(R.id.inRoute).setVisible(false);
                tvStatus.setText(R.string.delivered);
                Toast.makeText(getApplicationContext(), "Order Delivered Sucessfully!", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getApplicationContext(),OrderList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("cart_status", OrderStatus);
                startActivity(intent);
                finish();
            } else if (code.equals("501")) {
                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(OrderDetail.this,createTokenUrl).execute().get();
                   // Log.d("Upload_File","Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, OrderDetail.this);
                        new SendDeliveryData().execute();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    new SendDeliveryData().execute();
                }catch (JSONException je){
                    je.printStackTrace();
                    new SendDeliveryData().execute();
                }catch (NullPointerException ne){
                    new SendDeliveryData().execute();
                }

            }
            else
            {
                location.beginUpdates();
                lastLat = String.valueOf(location.getLatitude());
                lastLng = String.valueOf(location.getLongitude());
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }
        }
    }

    public class SendClubDeliveryData extends AsyncTask<String, String, String> {

        final String TAG = "AsyncFinanceList";

        String yourJsonStringUrl="http://ws.oyefood.in/WS/admin_service.php?mode=order_take_action_deliveryboy&order_id=" + orderId + "&status=3&user_add_lat="+lastLat+"&user_add_lng="+lastLng+"&del_boy_id="+userId+"&from_user_id=Del"+userId+"&cust_wallet_status="+cbDepositeStatus+"&cust_amount="+strCustAmt+"&wallet_amount="+walletAmt+"&club_del_code="+strClubDelCode;// set your json string url here
        // contacts JSONArray
        JSONArray dataJsonArr = null,expenceJsonArr=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // pDialog =  ProgressDialog.show(LoginActivity.this,"","Wait for login.....", true,false);
            // Log.d("FinanceList",yourJsonStringUrl);
            pDialog = new ProgressDialog(OrderDetail.this);
            pDialog.setMessage("Wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {

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
                    code = json.getString("code");
                    Log.d("code", "" + code);
                    delMsg = json.getString("message");
                    Log.d("message", "" + delMsg);

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
            altDia.dismiss();
            try {

                if (code.equals("200")) {
                    inRouteStatus=false;
                    menu.findItem(R.id.inRoute).setVisible(false);
                    tvStatus.setText(R.string.delivered);

                    Toast.makeText(getApplicationContext(), "Order Delivered Sucessfully!", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(),OrderList.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("cart_status", OrderStatus);
                    startActivity(intent);
                    finish();
                } else if (code.equals("501")) {
                    try {
                        String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                        cws = new CallWebService(OrderDetail.this,createTokenUrl).execute().get();
                        // Log.d("Upload_File","Token_URL:  "+cws);
                        JSONObject jo = new JSONObject(cws);
                        String c = jo.getString("code");
                        if (c.equals("200")) {
                            token = jo.getString("token");
                            GlobalClass.putToken(token, OrderDetail.this);
                            new SendDeliveryData().execute();
                        }
                    }catch (ExecutionException | InterruptedException ei){
                        ei.printStackTrace();
                        new SendDeliveryData().execute();
                    }catch (JSONException je){
                        je.printStackTrace();
                        new SendDeliveryData().execute();
                    }catch (NullPointerException ne){
                        new SendDeliveryData().execute();
                    }

                }
                else
                {
                    location.beginUpdates();
                    lastLat = String.valueOf(location.getLatitude());
                    lastLng = String.valueOf(location.getLongitude());
                    Toast.makeText(getApplicationContext(),delMsg,Toast.LENGTH_LONG).show();
                }

            }catch (NullPointerException ex) {
                ex.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class UploadInRouteFileToServer extends AsyncTask<String, String, String> {
        String yourJsonStringUrl = "http://ws.oyefood.in/WS/admin_service.php?mode=order_take_action_deliveryboy&order_id=" + orderId + "&status=6&del_boy_id="+userId+"&inroute_lat="+lastLat+"&inroute_lng="+lastLng+"&rest_payment="+strRestAmt+"&rest_paid_status="+cbStatus;

        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            donut_progress.setVisibility(View.VISIBLE);
            tvPercentage.setVisibility(View.VISIBLE);
            barContent.setVisibility(View.GONE);

            donut_progress.setProgress(0);
            sourceFile = new File(captureBill);
            totalSize = (int)sourceFile.length();

            Log.d("JSON_INROUTE_URL","InRoute : "+yourJsonStringUrl);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.d("PROG", progress[0]);
            donut_progress.setProgress(Integer.parseInt(progress[0])); //Updating progress
        }

        @Override
        protected String doInBackground(String... args) {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = null;
            String fileName = sourceFile.getName();

            try {
                connection = (HttpURLConnection) new URL(yourJsonStringUrl).openConnection();
                connection.setRequestMethod("POST");
                String boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                String metadataPart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                        + "" + "\r\n";

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"filename\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";

                long fileLength = sourceFile.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart + fileHeader;

                long requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();

               // int progress = 0;
                int bytesRead = 0;
                byte buf[] = new byte[1024];
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(sourceFile));
                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();
                    progress += bytesRead; // Here progress is total uploaded bytes

                    publishProgress(""+(int)((progress*100)/totalSize)); // sending progress percent to publishProgress
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();
                out.close();

                // Get server response
               /* BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder builder = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    builder.append(line);
                }*/

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    System.out.println(inputLine);
                    JSONObject jo = new JSONObject(inputLine);
                    codeInRoute = jo.getString("code");
                    // Log.d("code", code);
                    message = jo.getString("message");
                    Log.d("SERVER_RESPONCE message", "" + message);
                    if(codeInRoute.equals("200")){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvPercentage.setText("Data Uploaded Successfully!!");
                            }
                        });

                        //PostMeterRecord(imgId);
                    }
                }
                in.close();

            } catch (Exception e) {
                // Exception
            } finally {
                if (connection != null) connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("Response", "Response from server: " + result);
            if(progress==100){
                tvPercentage.setText("Data Uploaded Successfully!!");
                donut_progress.setVisibility(View.GONE);
                tvPercentage.setVisibility(View.GONE);
            }

            altDia.dismiss();

            try {
                if (code.equals("200")) {
                    menu.findItem(R.id.inRoute).setTitle(R.string.delivered);
                    tvStatus.setText(R.string.inRoute);
                    inRouteStatus=true;
                    Toast.makeText(getApplicationContext(), "Order Marked In-Route !", Toast.LENGTH_SHORT).show();
                }
                else if(code.equals("501")){
                   /* try {
                        String createTokenUrl=getApplicationContext().getString(R.string.create_token_url);

                        JsonParser jParser = new JsonParser();
                        JSONObject jo = jParser.getJSONFromUrl(createTokenUrl);
                        String c = jo.getString("code");
                        if(c.equals("200")){
                            token=jo.getString("token");
                            GlobalClass.putToken(token,AddExpence.this);
                            JsonURL=strBaseUrl+"add_delivery_boy_expense&del_boy_id="+userId+"&category="+strCat+"&amount="+strAmount+"&order_refno="+strRefNo+"&desc="+strDesc+"&token="+token+"&expense_date="+selectedDate;
                            new UploadFileToServer().execute();
                        }
                    }catch (JSONException je){
                        je.printStackTrace();
                    }*/

                    try {
                        String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                        cws = new CallWebService(OrderDetail.this,createTokenUrl).execute().get();
                        // Log.d(TAG,"Token_URL:  "+cws);
                        JSONObject jo = new JSONObject(cws);
                        String c = jo.getString("code");
                        if (c.equals("200")) {
                            token = jo.getString("token");
                            GlobalClass.putToken(token, OrderDetail.this);

                            new UploadInRouteFileToServer().execute();
                        }
                    }catch (ExecutionException | InterruptedException ei){
                        ei.printStackTrace();
                        new UploadInRouteFileToServer().execute();
                    }catch (JSONException je){
                        je.printStackTrace();
                        new UploadInRouteFileToServer().execute();
                    }catch (NullPointerException ne){
                        new UploadInRouteFileToServer().execute();
                    }

                }
                else
                {
                    location.beginUpdates();
                    lastLat = String.valueOf(location.getLatitude());
                    lastLng = String.valueOf(location.getLongitude());
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                }
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }
            super.onPostExecute(result);
        }

    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("Under_OrienChanged()","onConfigurationChanged  mode : "+newConfig.orientation);
        altDia.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // make the device update its location
        location.beginUpdates();
        lastLat = String.valueOf(location.getLatitude());
        lastLng = String.valueOf(location.getLongitude());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
