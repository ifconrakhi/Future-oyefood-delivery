package com.ifconit.oyedelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by pujak on 2/12/2016.
 */
public class CallWebService extends AsyncTask<Void, Void, String> {

    Context ctx;
    String serviceUrl,res;
    ProgressDialog pDialog;

    public CallWebService(Context context, String serviceUrl){
        this.ctx=context;
        this.serviceUrl=serviceUrl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    protected String doInBackground(Void... urls) {

        try {
            URL url = new URL(""+serviceUrl);
             Log.d("JSON_URL:  ",serviceUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
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
        //return res;
    }

    protected void onPostExecute(String response) {

        if(response == null) {
            response = "THERE WAS AN ERROR";
        }else{
            //Log.v("JSON VALUE IS ",""+response);
        }
        res=response;
    }
}