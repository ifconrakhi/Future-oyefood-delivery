package com.ifconit.oyedelivery;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by rakhit on 4/21/2016.
 */
public class CustomOrderDetail extends ArrayAdapter<String> {

    private Context mContext;
    ArrayList<String> alItemName;
    ArrayList<String>alItemQty;
    ArrayList<String> alItemPrice;
    private Activity context;
    public CustomOrderDetail(Context context,ArrayList<String>alItemName,ArrayList<String>alItemQty,ArrayList<String> alItemPrice) {
        super(context, R.layout.order_detail_item, alItemName);
        this.context = (Activity) context;
        this.alItemName=alItemName;
        this.alItemQty=alItemQty;
        this.alItemPrice=alItemPrice;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.order_detail_item, null, true);

            holder.tvItemName=(TextView)convertView.findViewById(R.id.itemName);
            holder.tvItemQty=(TextView)convertView.findViewById(R.id.itemQty);
            holder.tvItemPrice=(TextView)convertView.findViewById(R.id.itemPrice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            notifyDataSetChanged();
        }
        holder.tvItemName.setText(alItemName.get(position));
        holder.tvItemQty.setText(alItemQty.get(position));
        holder.tvItemPrice.setText(alItemPrice.get(position));

        return convertView;
    }

    public static class ViewHolder {
        TextView tvItemName,tvItemQty,tvItemPrice;
    }

}
