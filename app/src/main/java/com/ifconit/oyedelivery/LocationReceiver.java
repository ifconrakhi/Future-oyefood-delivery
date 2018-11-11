package com.ifconit.oyedelivery;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;


public class LocationReceiver extends BroadcastReceiver {


	double latitude, longitude;
	Context context;
    String res,code,token,userId,orderId,uid,address,gmtTime,strUrl,locAddr;
	String cws;
	SharedPreferences prefsUid,prefsOrderId;
	public static final String PREFS_UID="loginUserId";
	public static final String PREFS_OID="OrderId";
	static String TAG = "GPSLocation";
	SharedPreferences sharedpreferences;
	public static final String UserPref = "userPref" ;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
    public void onReceive(Context context, Intent calledIntent)
    {
		Log.d(TAG,"OnReceive");
		this.context=context;
      //  Log.d("LOC_RECEIVER", "Location RECEIVED!");
		latitude = calledIntent.getDoubleExtra("latitude", -1);
        longitude = calledIntent.getDoubleExtra("longitude", -1);

		token= GlobalClass.getToken(context);

		prefsOrderId = context.getSharedPreferences(PREFS_OID, 0);
		orderId= prefsOrderId.getString("orderId", "");
		Log.d("prefsOrderId",""+orderId);

		prefsUid = context.getSharedPreferences(PREFS_UID, 0);
		userId= prefsUid.getString("uid", "");
		Log.d("prefsUid_fromLogin",""+userId);
		sharedpreferences = context.getSharedPreferences(UserPref, Context.MODE_PRIVATE);

		if(Utils.isConnected(context)) {
			//Log.d("under_update_asyncClass", "data");
			new UpdateLocation().execute();
		}
    }//close onReceive

	class UpdateLocation extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected String doInBackground(String... args) {
			try {
				URL url = new URL(context.getString(R.string.base_url)+"add_delivery_boy_location&order_id="+orderId+"&delivery_boy_id="+userId+"&latitude="+latitude+"&longitude="+longitude+"&token="+token);
				//Log.d("Under_loc_Update", ""+url);
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
						String createTokenUrl = context.getString(R.string.create_token_url);
						cws = new CallWebService(context,createTokenUrl).execute().get();
						//Log.d(TAG,"Token_URL:  "+cws);
						JSONObject jo = new JSONObject(cws);
						String c = jo.getString("code");
						if (c.equals("200")) {
							token = jo.getString("token");
							GlobalClass.putToken(token,context);
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
