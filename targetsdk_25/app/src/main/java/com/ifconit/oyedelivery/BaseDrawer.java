package com.ifconit.oyedelivery;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by pujak on 3/10/2016.
 */
public class BaseDrawer extends AppCompatActivity  {
/*========================================================================================================================================================================================================================================*/
 //save our header or result
    public AccountHeader headerResult = null;
    public Drawer result = null;
    public Toolbar toolbar;
    protected FrameLayout frameLayout;
    int screenWidthInDp;
    public static int drawerSelectedPosition=1;
    ProgressDialog pDialog;
    TextView tvStart;
    SharedPreferences prefsUid;
    SharedPreferences.Editor editor;
    public static final String PREFS_UID="loginUserId";
    String userId,token,cws,confirm_count,deliver_count,all_pending_count;
    String currentLat="",currentLng="";
    ImageView imgvwAttendance;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Bundle activityInstance;
    TextView tvStop,tvKm;
    ImageButton ibCalendar,ibExpence;


/*========================================================================================================================================================================================================================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_drawer_layout);

        activityInstance = savedInstanceState;
//********************************************************************************************************************
        //Initalize Variable Here
        frameLayout=(FrameLayout) findViewById(R.id.frame_container);

       // pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
       // editor = pref.edit();
        final DisplayMetrics displayMetrics=getResources().getDisplayMetrics();
        screenWidthInDp=Math.round(displayMetrics.widthPixels / displayMetrics.density);
        prefsUid = getSharedPreferences(PREFS_UID, 0);
        userId= prefsUid.getString("uid", "");
        token=GlobalClass.getToken(this);
       // started =  pref.getBoolean(startKey, false);//If Flase then show start else Stop

//********************************************************************************************************************
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvStart = (TextView) findViewById(R.id.tvStart);
        imgvwAttendance = (ImageView) findViewById(R.id.imgvwAttendance);
        tvStop = (TextView) findViewById(R.id.tvStop);
        tvKm = (TextView) findViewById(R.id.tvKm);

        imgvwAttendance.setVisibility(View.GONE);
        ibCalendar=(ImageButton)findViewById(R.id.ibCalendar);
        ibExpence=(ImageButton)findViewById(R.id.ibExpence);
       // buttonVal = tvStart.getText().toString();

        if(Utils.isConnected(this)){
            new AsyncDelOrderCount().execute();
        }else{
            Toast.makeText(getApplicationContext(),"No Internet Connection !! ",Toast.LENGTH_LONG).show();
        }

        final IProfile profile = new ProfileDrawerItem().withName("Mike Penz").withEmail("mikepenz@gmail.com");
    try {
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withDrawer(result)
                .withHeaderBackground(R.drawable.header)
                .withSavedInstance(savedInstanceState)
                .build();
    }
    catch(Exception e)
    {
        e.printStackTrace();
    }

        // new PrimaryDrawerItem().withName(R.string.inRouteOrders).withIdentifier(3),
        /*else if(drawerItem != null && drawerItem.getIdentifier() == 3){
                                    Intent gotoCategoryList = new Intent(BaseDrawer.this, OrderList.class);
                                    gotoCategoryList.putExtra("cart_status","7");
                                    startActivity(gotoCategoryList);

                                }*/



        /*getMap();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1 * 1000 * 60);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1 * 1000); // 1 second, in milliseconds
        //mLocationClient = new LocationClient(this, this, this);
        mGoogleApiClient = new GoogleApiClient.Builder(BaseDrawer.this)
                .addConnectionCallbacks(BaseDrawer.this)
                .addOnConnectionFailedListener(BaseDrawer.this)
                .addApi(LocationServices.API)
                .build();*/

        // mGoogleApiClient.connect();

       // }

/*========================================================================================================================================================================================================================================*/
    }//Content Closed

    public class AsyncDelOrderCount extends AsyncTask<String, String, String> {

        final String TAG = "AsyncDelTypeList.java";
        String deliveryTypeUrl = getApplicationContext().getString(R.string.base_url) +"delboy_orders_count&user_id="+userId+"&token="+token;
        JSONArray Results;
        String code="400",message="Sorry Error Occured!";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                deliveryTypeUrl = deliveryTypeUrl.replaceAll(" ", "%20");
               // Log.d(TAG, "ServiceUrl" + deliveryTypeUrl);
                URL url = new URL(deliveryTypeUrl);
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
                    JSONObject json = new JSONObject(stringBuilder.toString());
                    code=json.getString("code");
                    // Log.d(TAG, "code: " + code);
                    message=json.getString("message");
                    // Log.d(TAG,"message: "+message);
                    if (code.equals("200")) {
                        confirm_count=json.getString("confirm_count");
                        deliver_count=json.getString("deliver_count");
                        all_pending_count=json.getString("all_pending_count");
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
            //pDialog.dismiss();
            if (code.equals("200")) {

                //Create the drawer
                result = new DrawerBuilder()
                        .withActivity(BaseDrawer.this)
                        .withToolbar(toolbar)
                        .withHasStableIds(true)
                        .withSelectedItemByPosition(drawerSelectedPosition)
                        .withDrawerWidthDp(screenWidthInDp - 35)
                        .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                        .addDrawerItems(
                                new PrimaryDrawerItem().withName(R.string.confirmedOrders).withIdentifier(1).withBadge(confirm_count),
                               // new PrimaryDrawerItem().withName(R.string.deliveredOrders).withIdentifier(2).withBadge(deliver_count),
                               // new PrimaryDrawerItem().withName(R.string.pending_count).withIdentifier(3).withBadge(all_pending_count).withEnabled(false),
                               // new PrimaryDrawerItem().withName(R.string.addTransaction).withIdentifier(4),
                                new PrimaryDrawerItem().withName(R.string.listFinances).withIdentifier(5),
                               // new PrimaryDrawerItem().withName(R.string.attendance).withIdentifier(6)
                                new PrimaryDrawerItem().withName(R.string.logout).withIdentifier(7)

                        ) // add the items we want to use with our Drawer
                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                if (drawerItem != null) {
                                    drawerSelectedPosition = position;
                                    if (drawerItem instanceof Nameable) {
                                        if (drawerItem != null && drawerItem.getIdentifier() == 1) {
                                            Intent gotoCategoryList = new Intent(BaseDrawer.this, OrderList.class);
                                            gotoCategoryList.putExtra("cart_status","2");
                                            gotoCategoryList.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(gotoCategoryList);
                                            overridePendingTransition(0,0);
                                            //finish();
                                            //finish();
                                        }
                                        /*else if (drawerItem != null && drawerItem.getIdentifier() == 2) {
                                            Intent gotoCategoryList = new Intent(BaseDrawer.this, OrderList.class);
                                            gotoCategoryList.putExtra("cart_status","3");
                                            gotoCategoryList.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(gotoCategoryList);
                                            overridePendingTransition(0,0);
                                            //finish();
                                            //finish();
                                        }
                                        else if(drawerItem != null && drawerItem.getIdentifier() == 4){

                                            Intent gotoCategoryList = new Intent(BaseDrawer.this, AddTransaction.class);
                                            gotoCategoryList.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(gotoCategoryList);
                                            overridePendingTransition(0,0);
                                           // finish();
                                        }*/
                                        else if(drawerItem != null && drawerItem.getIdentifier() == 5){
                                            Intent gotoCategoryList = new Intent(BaseDrawer.this, FinanceList.class);
                                            gotoCategoryList.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(gotoCategoryList);
                                            overridePendingTransition(0,0);
                                           // finish();
                                        }
                                       /* else if(drawerItem != null && drawerItem.getIdentifier() == 6){
                                            Intent gotoCategoryList = new Intent(BaseDrawer.this, MakeAttendance.class);
                                            gotoCategoryList.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(gotoCategoryList);
                                            overridePendingTransition(0,0);
                                           // finish();
                                        }*/
                                       /* else if(drawerItem != null && drawerItem.getIdentifier() == 3){
                                           Intent intent = new Intent(BaseDrawer.this, null);
                                           startActivity(intent);
                                           // result.openDrawer();
                                        }*/
                                        else if(drawerItem != null && drawerItem.getIdentifier() == 7){
                                          showLogoutDialog();
                                            // result.openDrawer();
                                        }
                                      }
                                    if (drawerItem instanceof Badgeable) {
                                        try {
                                            Badgeable badgeable = (Badgeable) drawerItem;
                                            if (badgeable.getBadge() != null) {
                                                int badge = Integer.valueOf(badgeable.getBadge().toString());
                                                if (badge > 0) {
                                                    badgeable.withBadge(String.valueOf(badge - 1));
                                                    result.updateItem(drawerItem);
                                                }
                                            }
                                        }catch (NumberFormatException nfe){
                                            nfe.printStackTrace();
                                        }
                                    }

                                }
                                return false;
                            }
                        })
                        .withSelectedItem(drawerSelectedPosition)
                        .withOnDrawerListener(new Drawer.OnDrawerListener() {
                            @Override
                            public void onDrawerOpened(View drawerView) {
                            }

                            @Override
                            public void onDrawerClosed(View drawerView) {

                            }

                            @Override
                            public void onDrawerSlide(View drawerView, float slideOffset) {

                            }
                        })
                        .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {
                                if (drawerItem instanceof SecondaryDrawerItem) {
                                    Toast.makeText(BaseDrawer.this, ((SecondaryDrawerItem) drawerItem).getName().getText(BaseDrawer.this), Toast.LENGTH_SHORT).show();
                                }
                                return false;
                            }
                        })
                        .withSavedInstance(activityInstance)
                        .withShowDrawerOnFirstLaunch(true)
                        .build();
                }
            else if(code.equals("501")){
                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(BaseDrawer.this,createTokenUrl).execute().get();
                    //Log.d(TAG,"Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, BaseDrawer.this);
                        new AsyncDelOrderCount().execute();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    new AsyncDelOrderCount().execute();
                }catch (JSONException je){
                    je.printStackTrace();
                    new AsyncDelOrderCount().execute();
                }catch (NullPointerException ne){
                    new AsyncDelOrderCount().execute();
                }

            } else{
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }
          }

    }//close async
//*****************************************************************************************************************

//**************************************************************************************************************

    public void showLogoutDialog(){
        try {
            final Dialog f = new Dialog(BaseDrawer.this);
            f.requestWindowFeature(Window.FEATURE_NO_TITLE);
            f.setContentView(R.layout.custome_change_cart);
            f.setCancelable(false);
            TextView barTitle=(TextView)f.findViewById(R.id.barTitle);
            TextView textView = (TextView) f.findViewById(R.id.textChange);
            Button btnYes = (Button) f.findViewById(R.id.btnYes);
            Button btnNo = (Button) f.findViewById(R.id.btnNo);

            barTitle.setText("Log-Out");
            textView.setText("  Do you want to Log Out?");
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    f.dismiss();
                    editor = prefsUid.edit();
                    editor.putString("uid", "");
                    editor.commit();
                    Intent i = new Intent(BaseDrawer.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                    finish();


                }
            });
           btnNo.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   f.dismiss();
               }
           });


            f.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
//******************************************************************************************************************


    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

}
