package com.ifconit.oyedelivery;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{


	double lat,lng;
	String loc_update;

	SharedPreferences prefs;
	public static final String PREFS_Location="Loc_Update";
	GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	static String TAG = "GPSLocation";

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG,"OnBind");
		// TODO Auto-generated method stub
		return null;
	}

	public void  onCreate(){
		Log.d(TAG,"onCreate");
		super.onCreate();
		//Log.d("location_service", "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"onStartCommand");
	//	Log.d("service_inStart", "onStrartcommand");
//		prefs = getSharedPreferences(PREFS_Location, 0);
//		loc_update = prefs.getString("loc_updt", "30000");
//		Log.d("locUpdt_from_settings", loc_update);

		if(Build.VERSION.SDK_INT>16){
			//Log.d("under_if","apilevel>16");
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					while(true){
						startJob();
						try {
							/*if(OrderDetail.inRouteStatus==false)
							{
								Thread.sleep(60*1000);
							}
							else {*/
								Thread.sleep(60*1000);
//							}

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			});
			t.start();
		}else{
		//	Log.d("under_else","apilevel<16");
			mLocationRequest = LocationRequest.create();
			/*if(OrderDetail.inRouteStatus==false)
			{
				mLocationRequest.setInterval(60*1000);
			}
			else {*/
				mLocationRequest.setInterval(60*1000);
		//	}
			//mLocationRequest.setInterval(2*1000);
			mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			//mLocationRequest.setFastestInterval(1* 1000); // 1 second, in milliseconds
			//mLocationClient = new LocationClient(this, this, this);
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
			mGoogleApiClient.connect();
		}
		return START_STICKY;
	}

	public void startJob(){
		Log.d(TAG,"startJob");
		mLocationRequest = LocationRequest.create();
	/*	if(OrderDetail.inRouteStatus==false)
		{
			mLocationRequest.setInterval(60*1000);
		}
		else {*/
			mLocationRequest.setFastestInterval(60*1000);
		//}

		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		//mLocationRequest.setFastestInterval(15* 1000); // 1 second, in milliseconds
		//mLocationClient = new LocationClient(this, this, this);
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		mGoogleApiClient.connect();
	}

	public static void updateLocation(double latitude, double longitude)
	{
		Log.d(TAG,"OnBind");
	//	Log.d("under_updatelocation","data");
		Context appCtx = MyApplication.getAppContext();

		Intent filterRes = new Intent();
		filterRes.setAction("com.ifconit.oyedelivery.intent.action.LOCATION");
		filterRes.putExtra("latitude", latitude);
		filterRes.putExtra("longitude", longitude);
		appCtx.sendBroadcast(filterRes);
		//Log.e("Location Service", "update location");
	}

	@Override
	public void onConnected(Bundle bundle) {
		try {
			Log.d(TAG,"OnConnected");
			if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
				mGoogleApiClient.connect();
			}
			Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
			//Log.v("location_onconnected", "" + currentLocation);
			if (currentLocation != null) {
				try {
					//new GetFriends().execute();
					onLocationChanged(currentLocation);
				} catch (NullPointerException e) {
					//Log.d("Location_Service", "Connect to the Intenet to get the current Location and Map");
					return;
				}
			} else {
				try {
					LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
				}catch (IllegalStateException ex){
					ex.printStackTrace();
				}


//				if (!Utils.isGPSTurnOn(getApplicationContext())) {
//					//showGPSDialog();
//					return;
//				}
			}
		}catch (SecurityException se){
			se.printStackTrace();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG,"ConnectionSuspended");
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG,"onLocationChanged");
		if(location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();

			//Log.i("onlocation changed", "" + lat + "," + lng);
			LatLng coordinate = new LatLng(lat, lng);

			//Log.i("coordinate", "" + coordinate.latitude + "," + coordinate.longitude);

			updateLocation(lat, lng);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG,"onConnectionFailed");
//		if (connectionResult.hasResolution()) {
//			try {
//				// Start an Activity that tries to resolve the error
//				connectionResult.startResolutionForResult(this, 0);
//	                /*
//	                 * Thrown if Google Play services canceled the original
//	                 * PendingIntent
//	                 */
//			} catch (IntentSender.SendIntentException e) {
//				// Log the error
//				e.printStackTrace();
//			}
//		} else {
//	            /*
//	             * If no resolution is available, display a dialog to the
//	             * user with the error.
//	             */
//			Log.i("Map Activity", "Location services connection failed with code " + connectionResult.getErrorCode());
//		}
	}

}
