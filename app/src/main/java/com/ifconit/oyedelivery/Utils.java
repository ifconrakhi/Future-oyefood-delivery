package com.ifconit.oyedelivery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Utils
{
    /**
     * @Method used to checks if device having network connection or not.
     * @param context the context
     * @return <code>true</code> if the phone is connected
     */
	public static boolean isConnected(final Context context) {
		try {
			final ConnectivityManager connMngr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			final NetworkInfo wifiNetwork = connMngr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifiNetwork != null && wifiNetwork.isConnectedOrConnecting()) { return true; }

			final NetworkInfo mobileNetwork = connMngr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mobileNetwork != null && mobileNetwork.isConnectedOrConnecting()) { return true; }

			final NetworkInfo activeNetwork = connMngr.getActiveNetworkInfo();
			if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) { return true; }
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isGPSTurnOn(final Context context) {
		final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
				manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static boolean checkPermission(final Context context)
	{
		int currentAPIVersion = Build.VERSION.SDK_INT;
		if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
		{
			if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
					AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
					alertBuilder.setCancelable(true);
					alertBuilder.setTitle("Permission necessary");
					alertBuilder.setMessage("External storage permission is necessary");
					alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
						}
					});
					AlertDialog alert = alertBuilder.create();
					alert.show();

				} else {
					ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
				}
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

}
