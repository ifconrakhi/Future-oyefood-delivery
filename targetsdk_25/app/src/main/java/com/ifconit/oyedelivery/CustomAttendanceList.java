package com.ifconit.oyedelivery;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by user on 10/25/2016.
 */

public class CustomAttendanceList extends ArrayAdapter<String> {

    ArrayList<String> alAttTime = new ArrayList<>();
    ArrayList<String> alAttStatus = new ArrayList<>();
    private  Context context;
    TextView tvAttTime,tvAttStatus;

    public CustomAttendanceList(Context context, ArrayList<String> alAttTime, ArrayList<String> alAttStatus) {
        super(context, R.layout.custom_att_list, alAttTime);
        this.alAttStatus=alAttStatus;
        this.alAttTime=alAttTime;
        this.context=context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.custom_att_list, null, true);
        tvAttTime = (TextView) convertView.findViewById(R.id.tvAttTime);
        tvAttStatus = (TextView) convertView.findViewById(R.id.tvAttStatus);

        tvAttTime.setText(alAttTime.get(position));
        tvAttStatus.setText(alAttStatus.get(position));

        return convertView;
    }



}
