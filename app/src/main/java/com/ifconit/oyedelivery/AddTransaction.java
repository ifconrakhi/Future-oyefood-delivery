package com.ifconit.oyedelivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by user on 5/23/2016.
 */
public class AddTransaction extends BaseDrawer {
//************************************************************************************************************************************************************************************************

    SharedPreferences prefsUid;
    public static final String PREFS_UID="loginUserId";
    String userId,cws;
    String token,strBaseUrl;
    ProgressDialog pDialog;
    String code,message,result;
    EditText etAmountPaid,etTotalAmount,etKm;
    String amountPaid,totalAmount,km,orderId;
    Button btnSubmit;
    Spinner spinnerOrderRef;
    TextView tvUser;
    ArrayList<String> orderIdValue,refNoValue,fullName;
    //************************************************************************************************************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.add_transaction);
        getLayoutInflater().inflate(R.layout.add_transaction, frameLayout);
    //************************************************************************************************************************************************************************************************
        prefsUid = getSharedPreferences(PREFS_UID, 0);
        userId= prefsUid.getString("uid", "");

        etAmountPaid = (EditText) findViewById(R.id.etAmountPaid);
        etTotalAmount = (EditText) findViewById(R.id.etTotalAmount);
        etKm = (EditText) findViewById(R.id.etKm);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        spinnerOrderRef = (Spinner) findViewById(R.id.spinnerOrderRef);
        tvUser  = (TextView) findViewById(R.id.userOrder);

        tvStart.setVisibility(View.GONE);

        token=GlobalClass.getToken(this);
        strBaseUrl=getApplicationContext().getString(R.string.base_url);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        orderIdValue=new ArrayList<>();
        refNoValue=new ArrayList<>();
        fullName=new ArrayList<>();

        new OrderListRef().execute();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isConnected(getApplicationContext())) {
                    amountPaid = etAmountPaid.getText().toString().trim();
                    totalAmount = etTotalAmount.getText().toString().trim();
                    km = etKm.getText().toString().trim();
                    if(orderIdValue.size()==0)
                    {
                        Toast.makeText(getApplicationContext(), "You Don't Have any Order to add Transaction !!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (amountPaid.equals("")) {
                            Toast.makeText(getApplicationContext(), "Please Give Paid Amount to Restaurant!!", Toast.LENGTH_SHORT).show();
                        }
                        else if(totalAmount.equals(""))
                        {
                            Toast.makeText(getApplicationContext(), "Please Give Amount Received From Customer!!", Toast.LENGTH_SHORT).show();
                        }
                        else if (km.equals(""))
                        {
                            Toast.makeText(getApplicationContext(), "Please Give KM You Traveled!!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            new AsyncAddFinance().execute();
                        }
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "No internet connection available!!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    //************************************************************************************************************************************************************************************************
    }//Content Closed
    //************************************************************************************************************************************************************************************************
    public class AsyncAddFinance extends AsyncTask<String, String, String> {

            final String TAG = "AsyncOrderList.java";


            String yourJsonStringUrl = strBaseUrl+"add_new_finance&delivery_boy_id="+userId+"&order_id="+orderId+"&amount_paid="+totalAmount+"&amount_recd="+amountPaid+"&km_run="+km+"&token="+token;

            // contacts JSONArray
            JSONArray dataJsonArr = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // pDialog =  ProgressDialog.show(LoginActivity.this,"","Wait for login.....", true,false);
                pDialog = new ProgressDialog(AddTransaction.this);
                pDialog.setMessage(" data loading...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected String doInBackground(String... arg0) {

                try {
                    URL url = new URL(""+yourJsonStringUrl);
                    // Log.d("JSON_URL:  ",serviceUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        JSONObject json = new JSONObject(stringBuilder.toString());

                        code = json.getString("code");
                        //   Log.d("codefromurl", code);
                        message = json.getString("message");
                    }
                    finally{
                        urlConnection.disconnect();
                    }
                } catch (MalformedURLException e)
                {
                    e.printStackTrace();
                    return null;
                } catch(Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                    return null;
                }


               return code;

            }
            @Override
            protected void onPostExecute(String strFromDoInBg) {
                pDialog.dismiss();

                if (code.equals("200")) {
                    orderIdValue.remove(orderId);
                    Toast.makeText(getApplicationContext(), "Data Inserted Successfully!!!", Toast.LENGTH_SHORT).show();
                    new OrderListRef().execute();
                }
                else if(code.equals("501")){
                    //  Log.v("JSON VALUE IS ", "" + strFromDoInBg);
                    //Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                   /* try {
                        String createTokenUrl=getApplicationContext().getString(R.string.create_token_url);

                        JsonParser jParser = new JsonParser();
                        JSONObject jo = jParser.getJSONFromUrl(createTokenUrl);
                        String c = jo.getString("code");
                        if(c.equals("200")){
                            token=jo.getString("token");
                            GlobalClass.putToken(token,AddTransaction.this);
                            new AsyncAddFinance().execute();
                        }
                    }catch (JSONException je){
                        je.printStackTrace();
                    }*/

                    try {
                        String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                        cws = new CallWebService(AddTransaction.this,createTokenUrl).execute().get();
                      //  Log.d(TAG,"Token_URL:  "+cws);
                        JSONObject jo = new JSONObject(cws);
                        String c = jo.getString("code");
                        if (c.equals("200")) {
                            token = jo.getString("token");
                            GlobalClass.putToken(token, AddTransaction.this);
                            new AsyncAddFinance().execute();
                        }
                    }catch (ExecutionException | InterruptedException ei){
                        ei.printStackTrace();
                        new AsyncAddFinance().execute();
                    }catch (JSONException je){
                        je.printStackTrace();
                        new AsyncAddFinance().execute();
                    }catch (NullPointerException ne){
                        new AsyncAddFinance().execute();
                    }

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Data Not Inserted!!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    //******************************************************************************************************************************************
    public class OrderListRef extends AsyncTask<String, String, String> {

            final String TAG = "OrderListRef.java";
            String yourJsonStringUrl = strBaseUrl+"order_ref_list&user_id="+userId+"&token="+token;

            // contacts JSONArray
            JSONArray dataJsonArr = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("JSONURL", yourJsonStringUrl);
                // pDialog =  ProgressDialog.show(LoginActivity.this,"","Wait for login.....", true,false);
                pDialog = new ProgressDialog(AddTransaction.this);
                pDialog.setMessage("Loading Data...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected String doInBackground(String... arg0) {

                try {
                    URL url = new URL("" + yourJsonStringUrl);
                    // Log.d("JSON_URL:  ",serviceUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        JSONObject json = new JSONObject(stringBuilder.toString());
                        code = json.getString("code");
                        //   Log.d("codefromurl", code);
                        message = json.getString("message");
                        if (code.equals("200")) {
                            result = json.getString("Result");

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
                                    orderIdValue.add(c.getString("order_id"));
                                    refNoValue.add(c.getString("ref_no"));
                                    fullName.add(c.getString("full_name"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        return code;
                    }
                    finally {
                        urlConnection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                    return null;
                }

            }
            @Override
            protected void onPostExecute(String strFromDoInBg) {
                pDialog.dismiss();

                if (code.equals("200")) {
                    ArrayAdapter<String> refAdapter = new ArrayAdapter<String>(AddTransaction.this, android.R.layout.simple_spinner_item, refNoValue);
                    refAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerOrderRef.setAdapter(refAdapter);

                    spinnerOrderRef.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            orderId = orderIdValue.get(position);
                            tvUser.setText(fullName.get(position));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                }
                else if(code.equals("501")){
                   /* try {
                        String createTokenUrl=getApplicationContext().getString(R.string.create_token_url);

                        JsonParser jParser = new JsonParser();
                        JSONObject jo = jParser.getJSONFromUrl(createTokenUrl);
                        String c = jo.getString("code");
                        if(c.equals("200")){
                            token=jo.getString("token");
                            GlobalClass.putToken(token,AddTransaction.this);
                            new OrderListRef().execute();
                        }
                    }catch (JSONException je){
                        je.printStackTrace();
                    }*/

                    try {
                        String createTokenUrl = getApplicationContext().getString(R.string.create_token_url);
                        cws = new CallWebService(AddTransaction.this,createTokenUrl).execute().get();
                      //  Log.d(TAG,"Token_URL:  "+cws);
                        JSONObject jo = new JSONObject(cws);
                        String c = jo.getString("code");
                        if (c.equals("200")) {
                            token = jo.getString("token");
                            GlobalClass.putToken(token, AddTransaction.this);
                            new OrderListRef().execute();
                        }
                    }catch (ExecutionException | InterruptedException ei){
                        ei.printStackTrace();
                        new OrderListRef().execute();
                    }catch (JSONException je){
                        je.printStackTrace();
                        new OrderListRef().execute();
                    }catch (NullPointerException ne){
                        new OrderListRef().execute();
                    }

                }
                else
                {
                    spinnerOrderRef.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "No Order Found!!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    //******************************************************************************************************************************************

    @Override
    public void onBackPressed() {
        if(orderIdValue.size()!=0)
        {
            Toast.makeText(getApplicationContext(), "You have not inserted the transaction detail for all orders!!", Toast.LENGTH_SHORT).show();
        }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //******************************************************************************************************************************************

}
