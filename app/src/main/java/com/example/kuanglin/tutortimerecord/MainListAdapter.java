package com.example.kuanglin.tutortimerecord;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by KuangLin on 2017/4/29.
 */

public class MainListAdapter extends BaseAdapter {

    ArrayList<ITEM> list = new ArrayList<>();
    Context context;

    public MainListAdapter(Context context, ArrayList list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.list_item, viewGroup, false);
        }
        TextView dateTextView = (TextView) view.findViewById(R.id.list_item_date);
        TextView sartTimeTextView = (TextView) view.findViewById(R.id.list_item_start);
        TextView endTimeTextView = (TextView) view.findViewById(R.id.list_item_end);
        ImageView signImageView = (ImageView) view.findViewById(R.id.list_item_sign);

        if(list == null){
            return view;
        }

        dateTextView.setText(list.get(position).date);
        sartTimeTextView.setText(list.get(position).startTime);
        endTimeTextView.setText(list.get(position).endTime);
        Bitmap signature = getBitmap(list.get(position).signaturePath);
        signImageView.setImageBitmap(signature);
        return view;
    }

    private Bitmap getBitmap(String path) {
        try{
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
            bitmap = Bitmap.createBitmap(bitmap);
            return bitmap;
        }catch (Exception e){
            Log.e("error","signature has been deleted.");
            Log.e("error",e.getMessage());
            return null;
        }
    }
}
