package com.ifconit.oyedelivery;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CalendarView;
import android.widget.LinearLayout;
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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by user on 5/25/2016.
 */
public class FinanceList  extends BaseDrawer  {

    ListView lvFinanceList,lvExpence;
    ProgressDialog pDialog;
    String code,message,result,token,userId,strBaseUrl,cws;
    SharedPreferences prefsUid;
    public static final String PREFS_UID="loginUserId";
    ArrayList<String> order_id,refno,bill_value,amt_paid,amt_received,alExpRefno,alExpCategory,alExpAmount,alExpDesc;
    CustomFinanceList clAdapter;
    TextView tvNoOrder;
    String financeDate,selected_calDate,date,total_bill_value,total_amount_todeposit,total_expense,paid_value,received_value,orders_array,expense_array;
    TextView tvDate,tvAmtRec,tvAmtPaid,tvBill,tvAmtDeposite,tvTotalExpense;
    LinearLayout llCalendar,llContant,finaceHeader;
    CalendarView calendar;
    Animation animation;
    boolean isOpen=true;
    //Dialog dialogAdd;
    private static final java.text.DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
    SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy");

    RequestQueue requestQueue;
    JSONArray dataJsonArr = null,expenceJsonArr=null;
    RelativeLayout rlProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.finance_list);
        getLayoutInflater().inflate(R.layout.finance_list, frameLayout);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Finance List");
        ab.setHomeButtonEnabled(true);

        ibCalendar.setVisibility(View.VISIBLE);
        ibExpence.setVisibility(View.VISIBLE);

        token=GlobalClass.getToken(this);
        strBaseUrl=getApplicationContext().getString(R.string.base_url);

        prefsUid = getSharedPreferences(PREFS_UID, 0);
        userId= prefsUid.getString("uid", "");

        // Log.d("USERID_fromLogin",""+userId);
        tvNoOrder=(TextView) findViewById(R.id.txtvwNoOrder);
        lvFinanceList=(ListView)findViewById(R.id.lvFinance);
        lvExpence=(ListView)findViewById(R.id.lvExpence);
        llCalendar = (LinearLayout) findViewById(R.id.popup_window);
        llContant = (LinearLayout) findViewById(R.id.contantLayout);
        finaceHeader = (LinearLayout) findViewById(R.id.finaceHeader);
        tvDate=(TextView) findViewById(R.id.tvDateVal);
        tvAmtRec=(TextView) findViewById(R.id.tvTotalAmtRec);
        tvAmtPaid=(TextView) findViewById(R.id.tvTotalAmtPaid);
        tvBill=(TextView) findViewById(R.id.tvTotalBillVal);
        tvAmtDeposite=(TextView) findViewById(R.id.tvTotalAmtDeposite);
        tvTotalExpense=(TextView)findViewById(R.id.tvTotalExpense);
        tvNoOrder.setVisibility(View.GONE);
        calendar = (CalendarView) findViewById(R.id.calendarView1);

        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        requestQueue.getCache().clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        selected_calDate = sdf.format(new Date(calendar.getDate()));
       // Log.d("Under_Finance","Current date "+selected_calDate);

        if (Utils.isConnected(getApplicationContext())) {
            // new AsyncFinanceList().execute();
            getFinanceList();
        }else{
            Toast.makeText(getApplicationContext(), "No internet connection available!!", Toast.LENGTH_SHORT).show();
        }

        ibCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOpen){
                    if(Build.VERSION.SDK_INT <=17){
                        llCalendar.setVisibility(View.VISIBLE);
                        initializeCalendar();

                        isOpen=false;
                        llContant.setVisibility(View.GONE);
                        tvNoOrder.setVisibility(View.GONE);
                    }else{
                        llCalendar.setVisibility(View.VISIBLE);
                        animation = AnimationUtils.loadAnimation(FinanceList.this,
                                R.anim.push_up_in);
                        llCalendar.startAnimation(animation);

                        initializeCalendar();

                        isOpen=false;
                        llContant.setVisibility(View.GONE);
                        tvNoOrder.setVisibility(View.GONE);
                    }


                }else{

                    if(Build.VERSION.SDK_INT <=17){
                        llCalendar.setVisibility(View.GONE);
                        isOpen=true;
                        llContant.setVisibility(View.VISIBLE);
                        tvNoOrder.setVisibility(View.GONE);
                    }else{
                        animation = AnimationUtils.loadAnimation(FinanceList.this, R.anim.push_up_out);
                        llCalendar.startAnimation(animation);
                        llCalendar.setVisibility(View.GONE);
                        isOpen=true;
                        llContant.setVisibility(View.VISIBLE);
                        tvNoOrder.setVisibility(View.GONE);
                    }

                }
            }
        });

        ibExpence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),AddExpence.class);
                startActivity(intent);
                finish();

            }
        });

    }

    public void initializeCalendar() {

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                month=month+1;
                selected_calDate = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(dayOfMonth);
               // Log.d("Under_Finance","calendar date "+selected_calDate);
                Toast.makeText(getApplicationContext(),selected_calDate, Toast.LENGTH_LONG).show();
                if(Build.VERSION.SDK_INT <=17){
                    llCalendar.setVisibility(View.GONE);
                    isOpen=true;
                    llContant.setVisibility(View.VISIBLE);

                    if (Utils.isConnected(getApplicationContext())) {
                      //  new AsyncFinanceList().execute();
                        getFinanceList();
                    }else{
                        Toast.makeText(getApplicationContext(), "No internet connection available!!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    animation = AnimationUtils.loadAnimation(FinanceList.this, R.anim.push_up_out);
                    llCalendar.startAnimation(animation);
                    llCalendar.setVisibility(View.GONE);
                    isOpen=true;
                    llContant.setVisibility(View.VISIBLE);

                    if (Utils.isConnected(getApplicationContext())) {
                       // new AsyncFinanceList().execute();
                        getFinanceList();
                    }else{
                        Toast.makeText(getApplicationContext(), "No internet connection available!!", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });
    }

    public void getFinanceList() {

        order_id=new ArrayList<>();
        refno=new ArrayList<>();
        bill_value =new ArrayList<>();
        amt_paid=new ArrayList<>();
        amt_received=new ArrayList<>();
        alExpRefno=new ArrayList<>();
        alExpCategory=new ArrayList<>();
        alExpAmount=new ArrayList<>();
        alExpDesc=new ArrayList<>();

        String yourJsonStringUrl = strBaseUrl+"list_delivery_boy_finance_bydate&desired_date="+selected_calDate+"&del_boy_id="+userId+"&token="+token;// set your json string url here
        rlProgress.setVisibility(View.VISIBLE);

        yourJsonStringUrl=yourJsonStringUrl.replaceAll(" ","%20");
        Log.d("JSONURL",""+yourJsonStringUrl);

        JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(yourJsonStringUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //loading.dismiss();
                requestQueue.cancelAll("tag1");
                showFinanceList(response);
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

    public void showFinanceList(JSONObject json){
        try {

            code=json.getString("code");
            // Log.d(TAG, "code: " + code);
            message=json.getString("message");
            // Log.d(TAG,"message: "+message);
            if (code.equals("200")) {

                rlProgress.setVisibility(View.GONE);
                date=json.getString("date");
                total_bill_value=json.getString("total_bill_value");
                paid_value=json.getString("paid_value");
                received_value=json.getString("received_value");
                total_expense=json.getString("expense_value");
                total_amount_todeposit =json.getString("total_amount_todeposit ");
                orders_array = json.getString("orders_array");
                if (!orders_array.isEmpty()&&!orders_array.equals("")){
                    dataJsonArr = json.getJSONArray("orders_array");
                    for (int i = 0; i < dataJsonArr.length(); i++) {
                        JSONObject c = null;
                        try {
                            c = dataJsonArr.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            order_id.add(c.getString("order_id"));
                            refno.add(c.getString("refno"));
                            bill_value.add(c.getString("bill_value"));
                            amt_paid.add(c.getString("paid_value"));
                            amt_received.add(c.getString("amt_received"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                expense_array=json.getString("expense_array");
                if (!expense_array.isEmpty()&&!expense_array.equals("")){
                    expenceJsonArr = json.getJSONArray("expense_array");
                    for (int i = 0; i < expenceJsonArr.length(); i++) {
                        JSONObject c = null;
                        try {
                            c = expenceJsonArr.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            alExpRefno.add(c.getString("refno"));
                            alExpCategory.add(c.getString("category"));
                            alExpAmount.add(c.getString("amount"));
                            alExpDesc.add(c.getString("desc"));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    // Toast.makeText(getApplicationContext(), "Data loaded successfully..", Toast.LENGTH_SHORT).show();
                    tvDate.setText(date);
                    tvAmtPaid.setText("Rs. " + paid_value);
                    tvBill.setText("Rs. " + total_bill_value);
                    tvAmtRec.setText("Rs. " + received_value);
                    tvTotalExpense.setText("Rs. " + total_expense);
                    tvAmtDeposite.setText("Rs. " + total_amount_todeposit);

                    if (order_id.size() > 0) {
                        finaceHeader.setVisibility(View.VISIBLE);
                        lvFinanceList.setVisibility(View.VISIBLE);

                        clAdapter = new CustomFinanceList(FinanceList.this, order_id, refno, bill_value, amt_paid, amt_received);
                        lvFinanceList.setAdapter(clAdapter);
                        GlobalClass.setListViewHeightBasedOnChildren(lvFinanceList);
                    } else {
                        finaceHeader.setVisibility(View.GONE);
                        lvFinanceList.setVisibility(View.GONE);
                    }

                    if (alExpCategory.size() > 0) {
                        lvExpence.setVisibility(View.VISIBLE);
                        CustomExpenceList cexp = new CustomExpenceList(FinanceList.this, alExpRefno, alExpCategory, alExpAmount, alExpDesc);
                        lvExpence.setAdapter(cexp);
                        GlobalClass.setListViewHeightBasedOnChildren(lvExpence);
                    } else {
                        lvExpence.setVisibility(View.GONE);
                    }

                    if (expenceJsonArr != null && dataJsonArr != null) {
                        tvNoOrder.setVisibility(View.GONE);
                    } else {
                        tvNoOrder.setVisibility(View.VISIBLE);
                    }

                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }else if (code.equals("501")) {

                try {
                    String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                    cws = new CallWebService(FinanceList.this,createTokenUrl).execute().get();
                    Log.d("Login","Token_URL:  "+cws);
                    JSONObject jo = new JSONObject(cws);
                    String c = jo.getString("code");
                    if (c.equals("200")) {
                        token = jo.getString("token");
                        GlobalClass.putToken(token, FinanceList.this);
                        // new AsyncOrderDetail().execute();
                        getFinanceList();
                    }
                }catch (ExecutionException | InterruptedException ei){
                    ei.printStackTrace();
                    // new AsyncOrderDetail().execute();
                    getFinanceList();
                }catch (JSONException je){
                    je.printStackTrace();
                    //  new AsyncOrderDetail().execute();
                    getFinanceList();
                }catch (NullPointerException ne){
                    //  new AsyncOrderDetail().execute();
                    getFinanceList();
                }
            } else {
                rlProgress.setVisibility(View.GONE);
                Toast.makeText(FinanceList.this, message, Toast.LENGTH_SHORT).show();
                tvNoOrder.setVisibility(View.VISIBLE);
            }

        }catch (JSONException je){
            je.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

   /* public class AsyncFinanceList extends AsyncTask<String, String, String> {

        final String TAG = "AsyncFinanceList";

        String yourJsonStringUrl = strBaseUrl+"list_delivery_boy_finance_bydate&desired_date="+selected_calDate+"&del_boy_id="+userId+"&token="+token;// set your json string url here
        // contacts JSONArray
        JSONArray dataJsonArr = null,expenceJsonArr=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // pDialog =  ProgressDialog.show(LoginActivity.this,"","Wait for login.....", true,false);
           // Log.d("FinanceList",yourJsonStringUrl);
            order_id=new ArrayList<>();
            refno=new ArrayList<>();
            bill_value =new ArrayList<>();
            amt_paid=new ArrayList<>();
            amt_received=new ArrayList<>();
            alExpRefno=new ArrayList<>();
            alExpCategory=new ArrayList<>();
            alExpAmount=new ArrayList<>();
            alExpDesc=new ArrayList<>();

            pDialog = new ProgressDialog(FinanceList.this);
            pDialog.setMessage(" data loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {

            try {
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
                    JSONObject json = new JSONObject(stringBuilder.toString());
                    code=json.getString("code");
                    // Log.d(TAG, "code: " + code);
                    message=json.getString("message");
                    // Log.d(TAG,"message: "+message);
                    if(code.equals("200")) {
                            date=json.getString("date");
                            total_bill_value=json.getString("total_bill_value");
                            paid_value=json.getString("paid_value");
                            received_value=json.getString("received_value");
                            total_expense=json.getString("expense_value");
                            total_amount_todeposit =json.getString("total_amount_todeposit ");
                            orders_array = json.getString("orders_array");
                            if (!orders_array.isEmpty()&&!orders_array.equals("")){
                                dataJsonArr = json.getJSONArray("orders_array");
                                for (int i = 0; i < dataJsonArr.length(); i++) {
                                    JSONObject c = null;
                                    try {
                                        c = dataJsonArr.getJSONObject(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        order_id.add(c.getString("order_id"));
                                        refno.add(c.getString("refno"));
                                        bill_value.add(c.getString("bill_value"));
                                        amt_paid.add(c.getString("paid_value"));
                                        amt_received.add(c.getString("amt_received"));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        expense_array=json.getString("expense_array");
                        if (!expense_array.isEmpty()&&!expense_array.equals("")){
                            expenceJsonArr = json.getJSONArray("expense_array");
                            for (int i = 0; i < expenceJsonArr.length(); i++) {
                                JSONObject c = null;
                                try {
                                    c = expenceJsonArr.getJSONObject(i);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    alExpRefno.add(c.getString("refno"));
                                    alExpCategory.add(c.getString("category"));
                                    alExpAmount.add(c.getString("amount"));
                                    alExpDesc.add(c.getString("desc"));


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
            pDialog.dismiss();
            try {
                if (code.equals("200")) {
                    try {
                        // Toast.makeText(getApplicationContext(), "Data loaded successfully..", Toast.LENGTH_SHORT).show();
                        tvDate.setText(date);
                        tvAmtPaid.setText("Rs. " + paid_value);
                        tvBill.setText("Rs. " + total_bill_value);
                        tvAmtRec.setText("Rs. " + received_value);
                        tvTotalExpense.setText("Rs. " + total_expense);
                        tvAmtDeposite.setText("Rs. " + total_amount_todeposit);

                        if (order_id.size() > 0) {
                            finaceHeader.setVisibility(View.VISIBLE);
                            lvFinanceList.setVisibility(View.VISIBLE);

                            clAdapter = new CustomFinanceList(FinanceList.this, order_id, refno, bill_value, amt_paid, amt_received);
                            lvFinanceList.setAdapter(clAdapter);
                            GlobalClass.setListViewHeightBasedOnChildren(lvFinanceList);
                        } else {
                            finaceHeader.setVisibility(View.GONE);
                            lvFinanceList.setVisibility(View.GONE);
                        }

                        if (alExpCategory.size() > 0) {
                            lvExpence.setVisibility(View.VISIBLE);
                            CustomExpenceList cexp = new CustomExpenceList(FinanceList.this, alExpRefno, alExpCategory, alExpAmount, alExpDesc);
                            lvExpence.setAdapter(cexp);
                            GlobalClass.setListViewHeightBasedOnChildren(lvExpence);
                        } else {
                            lvExpence.setVisibility(View.GONE);
                        }

                        if (expenceJsonArr != null && dataJsonArr != null) {
                            tvNoOrder.setVisibility(View.GONE);
                        } else {
                            tvNoOrder.setVisibility(View.VISIBLE);
                        }

                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }

                } else if (code.equals("501")) {
                    //  Log.v("JSON VALUE IS ", "" + strFromDoInBg);
                    //Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                    try {
                        String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                        cws = new CallWebService(FinanceList.this,createTokenUrl).execute().get();
                       // Log.d(TAG,"Token_URL:  "+cws);
                        JSONObject jo = new JSONObject(cws);
                        String c = jo.getString("code");
                        if (c.equals("200")) {
                            token = jo.getString("token");
                            GlobalClass.putToken(token, FinanceList.this);
                            new AsyncFinanceList().execute();
                        }
                    }catch (ExecutionException | InterruptedException ei){
                        ei.printStackTrace();
                        new AsyncFinanceList().execute();
                    }catch (JSONException je){
                        je.printStackTrace();
                        new AsyncFinanceList().execute();
                    }catch (NullPointerException ne){
                        new AsyncFinanceList().execute();
                    }

                } else if (code.equals("400")) {
                    tvNoOrder.setVisibility(View.VISIBLE);
                }
            }catch (NullPointerException ex) {
                ex.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }*/

    //******************************************************************************************************************************
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
       // getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
               onBackPressed();
                return true;

           // case R.id.action_cal:
          //      return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
