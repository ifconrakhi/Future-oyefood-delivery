package com.ifconit.oyedelivery;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by rakhit on 4/20/2016.
 */
public class GlobalClass {
    public static void putToken(String token, Context context){
        SharedPreferences tokenPrefs = context.getSharedPreferences("TokenDetails", 0);
        SharedPreferences.Editor editor = tokenPrefs.edit();
        editor.putString("token",token);
        editor.commit();
    }

    public static String getToken(Context context){
        SharedPreferences tokenPrefs = context.getSharedPreferences("TokenDetails", 0);
        String token = tokenPrefs.getString("token", "");
        return token;
    }

    public static Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte=Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static boolean isGPSTurnOn(final Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String toLocal(String ts) throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateval = null;
        Date value = null;
        try {
            value = formatter.parse(ts);
            dateval= String.valueOf(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM,yyyy HH:mm:ss");
        //dateFormatter.setTimeZone(TimeZone.getDefault());
        String dt = dateFormatter.format(value);

        return dt;

    }

    public static String getStringFormatted(String d1) {
//        String format = "dd MMM yyyy HH:mm:ss";
//        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
//        return sdf.format(new Date(datestring.replaceAll("-", "/")));
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
        Date d = null;
        try {
            d = originalFormat.parse(d1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dateString = targetFormat.format(d);
        return dateString;
    }

}
