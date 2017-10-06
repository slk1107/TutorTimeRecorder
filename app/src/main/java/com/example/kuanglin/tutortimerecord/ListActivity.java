package com.example.kuanglin.tutortimerecord;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.kuanglin.tutortimerecord.R.id.list_left_arrow;
import static com.example.kuanglin.tutortimerecord.R.id.list_right_arrow;

public class ListActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<ITEM> list = new ArrayList<>();
    DBHelper dbHelper;
    private ListView listView;
    private TextView mTotalHourTextView, mTotalPriceTextView, currentListIndexTextView;
    private AdView mAdView;
    int currentListIndex;
    private static final String ADMOB_APP_ID = "ca-app-pub-8896432770281749~3911574832";

    /**
     * --------------
     * <p>
     * Life Cycle
     * <p>
     * --------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        dbHelper = new DBHelper(this, "record.db", null, DBHelper.DB_VERSION);
        isStoragePermissionGranted();
        initView();


        MobileAds.initialize(this, ADMOB_APP_ID);

        mAdView = (AdView) findViewById(R.id.list_adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Calendar c = Calendar.getInstance();
        currentListIndex = c.get(Calendar.MONTH) + 1;
        updateList(currentListIndex);
        mAdView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdView.destroy();
    }

    /**x`
     * --------------
     * <p>
     * Private Methods
     * <p>
     * --------------
     */

    private void updateList(int index) {
        loadListFromDB(index);
        String listIndexText = index + "月";
        currentListIndexTextView.setText(listIndexText);
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        updateTotal();
    }


    private ArrayList<ITEM> loadListFromDB(int month) {
        String column =
                DBHelper.COLUMN_DATE + ", "
                        + DBHelper.COLUMN_START_TIME + ", "
                        + DBHelper.COLUMN_END_TIME + ", "
                        + DBHelper.COLUMN_SIGNATURE_PATH;

        String SQLCommand = String.format("select %s from %s where %s = '%s'", column, DBHelper.TABLE_NAME, DBHelper.COLUMN_MONTH, month);

        list.clear();
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQLCommand, null);
        try {
            while (cursor.moveToNext()) {
                ITEM item = new ITEM();
                item.date = cursor.getString(0);
                item.startTime = cursor.getString(1);
                item.endTime = cursor.getString(2);
                item.signaturePath = cursor.getString(3);
                list.add(item);
            }

        } catch (Exception e) {
            Log.e("QQ", "[Database Error] Error on loading DB");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

//    private void checkDB(){
//        for(ITEM item : list){
//            if(item.startTime.contains("：")){
//                item.startTime = item.startTime.replace("：",":");
//                item.endTime = item.endTime.replace("：",":");
//                updateClassRecordDB(item);
//            }
//        }
//    }

    private void deleteItemFromDB(String path) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.TABLE_NAME, DBHelper.COLUMN_SIGNATURE_PATH + " = ?", new String[]{path});
        updateList(currentListIndex);
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    /**
     *
     * Views
     *
     * */

    private void initView() {
        mTotalHourTextView = (TextView) findViewById(R.id.result_main_total_hour);
        mTotalPriceTextView = (TextView) findViewById(R.id.result_main_total_money);
        listView = (ListView) findViewById(R.id.main_list);

        currentListIndexTextView = (TextView) findViewById(R.id.current_list_index);
        ImageButton leftButton = (ImageButton) findViewById(list_left_arrow);
        ImageButton rightButton = (ImageButton) findViewById(R.id.list_right_arrow);

        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);


        MainListAdapter adapter = new MainListAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int itemIndex = i < 2 ? i : i - 1;
                AlertDialog dialog = new AlertDialog.Builder(ListActivity.this).create();
                dialog.setTitle(list.get(itemIndex).date);
                dialog.setMessage(list.get(itemIndex).startTime + "\n" + list.get(itemIndex).endTime);
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "刪除", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        deleteItemFromDB(list.get(itemIndex).signaturePath);
                    }
                });

                dialog.show();
            }
        });

        mTotalPriceTextView.setText("");
        mTotalHourTextView.setText("");
    }


    private void updateTotal() {
        int totalHours = 0;
        for (int i = 0; i < list.size(); i++) {
            try {
                totalHours += getClassDuration(list.get(i).startTime, list.get(i).endTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int salary = 650;
        int totalPrice = totalHours * salary;

        String totalHourText = totalHours + "小時";
        String totalPriceText = totalPrice + "元";
        mTotalHourTextView.setText(totalHourText);
        mTotalPriceTextView.setText(totalPriceText);

    }

    private int getClassDuration(String startTime, String endTime) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(GlobalConst.TimeFormat,getResources().getConfiguration().locale);
        Date startTimeDate = dateFormat.parse(startTime);
        Date endTimeDate = dateFormat.parse(endTime);
        int startHour = startTimeDate.getHours();
        int startMinute = startTimeDate.getMinutes();
        int endHour = endTimeDate.getHours();
        int endMinute = endTimeDate.getMinutes();

        int minuteGap;
        int hourGap;
        int totalMinute;

        if (endMinute < startMinute) {
            minuteGap = endMinute + 60 - startMinute;
            endHour = endHour - 1;
        } else {
            minuteGap = endMinute - startMinute;
        }
        hourGap = endHour < startHour ? endHour + 24 - startHour : endHour - startHour;
        totalMinute = hourGap * 60 + minuteGap;

        if (totalMinute > 12 * 60) {
            return 0;
        }

        return totalMinute / 60;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activty_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(ListActivity.this, AddActivity.class);
        startActivityForResult(intent, 0);
//        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case list_left_arrow:
                currentListIndex = currentListIndex - 1;
                if (currentListIndex < 1) {
                    currentListIndex = 12;
                }
                updateList(currentListIndex);
                break;
            case list_right_arrow:
                currentListIndex = currentListIndex + 1;
                if (currentListIndex > 12) {
                    currentListIndex = 1;
                }
                updateList(currentListIndex);
                break;
        }
    }
//    private void updateClassRecordDB(ITEM item) {
//        if (item.signaturePath == null) {
//            return;
//        }
//        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(DBHelper.COLUMN_START_TIME, item.startTime);
//        values.put(DBHelper.COLUMN_END_TIME, item.endTime);
//        try {
//            db.update(DBHelper.TABLE_NAME, values, DBHelper.COLUMN_SIGNATURE_PATH + " = ?", new String[]{item.signaturePath});
//        } catch (SQLiteConstraintException e) {
//            Log.e("SQL", e.getMessage());
//        }
//    }
}
