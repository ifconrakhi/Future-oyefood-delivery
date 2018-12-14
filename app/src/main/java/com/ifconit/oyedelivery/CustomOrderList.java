package com.ifconit.oyedelivery;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rakhit on 4/20/2016.
 */
public class CustomOrderList extends ArrayAdapter<String> {

    private Context mContext;
    ArrayList<String>alRefNo;
    ArrayList<String>alDate;
    ArrayList<String>alAmount;
    ArrayList<String>alRestName;
    ArrayList<String>alAddr;
    ArrayList<String>alStatus;
    ArrayList<String>alexpDelTime;
    ArrayList<String> alAckStatus;

    private Activity context;

    public CustomOrderList(Context context, ArrayList<String> alRefNo, ArrayList<String> alStatus, ArrayList<String> alDate, ArrayList<String> alAmount, ArrayList<String> alRestName, ArrayList<String> alAddr, ArrayList<String> alExpDelTime, ArrayList<String> alAckStatus) {
        super(context, R.layout.orderlist_item, alRefNo);
        this.context = (Activity) context;
        this.alRefNo=alRefNo;
        this.alStatus=alStatus;
        this.alDate=alDate;
        this.alAmount=alAmount;
        this.alRestName=alRestName;
        this.alAddr=alAddr;
        this.alexpDelTime=alExpDelTime;
        this.alAckStatus=alAckStatus;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.orderlist_item, null, true);
            holder.tvRefrence=(TextView)convertView.findViewById(R.id.tvOrderRef);
            holder.tvAmount=(TextView)convertView.findViewById(R.id.tvOrderAmount);
            holder.tvDate=(TextView)convertView.findViewById(R.id.tvOrderDate);
            holder.tvRestorentName=(TextView)convertView.findViewById(R.id.tvRestName);
            holder.tvCustomerAddr=(TextView)convertView.findViewById(R.id.tvCustomerLocality);
            holder.ivStatus=(ImageView)convertView.findViewById(R.id.ivStatus);
            holder.tvExpDelTime=(TextView)convertView.findViewById(R.id.tvExpDelTime);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            notifyDataSetChanged();
        }
          holder.tvRefrence.setText(alRefNo.get(position));
          holder.tvDate.setText(alDate.get(position));
          holder.tvAmount.setText(alAmount.get(position));
          holder.tvRestorentName.setText(alRestName.get(position));
          holder.tvCustomerAddr.setText(alAddr.get(position));
          holder.tvExpDelTime.setText(alexpDelTime.get(position));
        if(alStatus.get(position).equals("2")){
            holder.ivStatus.setImageResource(R.drawable.confirmed);
        }else if(alStatus.get(position).equals("6")){
            holder.ivStatus.setImageResource(R.drawable.inroute);
        }else if(alStatus.get(position).equals("3")){
            holder.ivStatus.setImageResource(R.drawable.delivered);
        }

        return convertView;
    }

    public static class ViewHolder {
       TextView tvRefrence,tvAmount,tvDate,tvRestorentName,tvCustomerAddr,tvExpDelTime;
        ImageView ivStatus;

    }

}
