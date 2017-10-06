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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by KuangLin on 2017/4/29.
 */

public class MainListAdapter extends BaseAdapter {

    private ArrayList<ITEM> list = new ArrayList<>();
    private Context context;

    MainListAdapter(Context context, ArrayList<ITEM> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        // Ad need to be shown when there's two items.
        return list.size() >= 2 ? list.size() + 1 : list.size();
    }

    @Override
    public Object getItem(int i) {
        return i > 2 ? list.get(i) : list.get(i - 1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (position == 2) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_ad, viewGroup, false);
            view.setTag("Ad View");
            NativeExpressAdView adView = (NativeExpressAdView) (view.findViewById(R.id.adView));

            AdRequest request = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            adView.loadAd(request);
            return view;
        }

        if (view == null || "Ad View".equals(view.getTag())) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item, viewGroup, false);
        }
        TextView dateTextView = (TextView) view.findViewById(R.id.list_item_date);
        TextView startTimeTextView = (TextView) view.findViewById(R.id.list_item_start);
        TextView endTimeTextView = (TextView) view.findViewById(R.id.list_item_end);
        TextView timeIntervalTextView = (TextView) view.findViewById(R.id.list_item_interval);
        ImageView signImageView = (ImageView) view.findViewById(R.id.list_item_sign);

        if (list == null) {
            return view;
        }

        position = position > 2 ? position - 1 : position;


        dateTextView.setText(String.format(
                context.getResources().getString(R.string.class_date),
                list.get(position).date));
        startTimeTextView.setText(String.format(
                context.getResources().getString(R.string.start_time),
                list.get(position).startTime));
        endTimeTextView.setText(String.format(
                context.getResources().getString(R.string.end_time),
                list.get(position).endTime));
        try {
            String classDuration = getClassDuration(list.get(position).startTime, list.get(position).endTime);
            timeIntervalTextView.setText(String.format(
                    context.getResources().getString(R.string.class_duration),
                    classDuration));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Bitmap signature = getBitmap(list.get(position).signaturePath);
        signImageView.setImageBitmap(signature);
        // feature: add "IMG not found"
        return view;
    }

    private Bitmap getBitmap(String path) {
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
            bitmap = Bitmap.createBitmap(bitmap);
            return bitmap;
        } catch (Exception e) {
            Log.e("error", "signature has been deleted.");
            Log.e("error", e.getMessage());
            return null;
        }
    }

    private String getClassDuration(String startTimeString, String endTimeString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(GlobalConst.TimeFormat, context.getResources().getConfiguration().locale);
        Date startTime = dateFormat.parse(startTimeString);
        Date endTime = dateFormat.parse(endTimeString);
        int durationHours = endTime.getHours() - startTime.getHours();
        int durationMinutes = endTime.getMinutes() - startTime.getMinutes();
        double duration = durationHours + (durationMinutes > 30 ? 0.5 : 0);
        return String.valueOf(duration);
    }
}
