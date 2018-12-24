package com.ifconit.oyedelivery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Rakhi on 4/1/2017.
 */
public class CustomExpenceList extends ArrayAdapter<String> {

    private Activity context;
    ArrayList<String> alExpRefno;
    ArrayList<String> alExpCategory;
    ArrayList<String> alExpAmount;
    ArrayList<String> alExpDesc;



    public CustomExpenceList(Activity context, ArrayList<String> alExpRefno, ArrayList<String> alExpCategory, ArrayList<String> alExpAmount, ArrayList<String> alExpDesc) {
        super(context, R.layout.custom_expence_list,alExpRefno );
        try {
            this.context = context;
            this.alExpRefno = alExpRefno;
            this.alExpCategory = alExpCategory;
            this.alExpAmount = alExpAmount;
            this.alExpDesc = alExpDesc;

        }catch (NullPointerException ex){
            ex.printStackTrace();
        }catch (IndexOutOfBoundsException exx)
        {
            exx.printStackTrace();
        }
    }

    private class ViewHolder {
        TextView tvRefNo,tvCategory,tvAmount,tvDesc;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.custom_expence_list, null, true);
            holder.tvRefNo = (TextView) convertView.findViewById(R.id.tvOrderRefNo);
            holder.tvCategory = (TextView) convertView.findViewById(R.id.tvCategory);
            holder.tvAmount = (TextView) convertView.findViewById(R.id.tvAmount);
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            holder.tvRefNo.setText(alExpRefno.get(position));
            holder.tvCategory.setText(alExpCategory.get(position));
            holder.tvAmount.setText(alExpAmount.get(position));
            holder.tvDesc.setText(alExpDesc.get(position));

        }
        catch (NullPointerException ex)
        {
            ex.printStackTrace();
        }
        catch (RuntimeException ex)
        {
            ex.printStackTrace();
        }

        return convertView;
    }



}

