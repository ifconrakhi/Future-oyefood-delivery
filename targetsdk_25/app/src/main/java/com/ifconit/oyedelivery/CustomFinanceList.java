package com.ifconit.oyedelivery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by user on 5/25/2016.
 */
public class CustomFinanceList extends ArrayAdapter<String> {

    private Activity context;
    ArrayList<String> order_id;
    ArrayList<String> refno;
    ArrayList<String> bill_value;
    ArrayList<String> amt_paid;
    ArrayList<String> amt_received;

    public CustomFinanceList(Activity context, ArrayList<String> order_id, ArrayList<String> refno, ArrayList<String> bill_value, ArrayList<String> amt_paid, ArrayList<String> amt_received) {
        super(context, R.layout.custom_finance_list,order_id );
        try{
        this.context=context;
        this.order_id=order_id;
        this.refno=refno;
        this.bill_value=bill_value;
        this.amt_paid=amt_paid;
        this.amt_received=amt_received;

        }catch (NullPointerException ex){
            ex.printStackTrace();
        }catch (IndexOutOfBoundsException exx)
        {
            exx.printStackTrace();
        }
    }


    private class ViewHolder {
        TextView tvRefNo,tvAmountPaid,tvAmountReceived,tvBillVal;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder =null;
        LayoutInflater inflater = context.getLayoutInflater();


        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.custom_finance_list, null, true);
            holder.tvRefNo = (TextView) convertView.findViewById(R.id.tvOrderRef);
            holder.tvAmountPaid = (TextView) convertView.findViewById(R.id.tvAmtPaid);
            holder.tvAmountReceived = (TextView) convertView.findViewById(R.id.tvAmtRec);
            holder.tvBillVal = (TextView) convertView.findViewById(R.id.tvBillVal);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            if(position % 2 == 0)
                convertView.setBackgroundColor(context.getResources().getColor(R.color.caldroid_lighter_gray));
            else
                convertView.setBackgroundColor(Color.WHITE);

            //  Log.d("orderRef",orderRef.get(position));
            holder.tvRefNo.setText(refno.get(position));
            holder.tvAmountPaid.setText(amt_paid.get(position));
            holder.tvAmountReceived.setText(amt_received.get(position));
            holder.tvBillVal.setText(bill_value.get(position));

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
