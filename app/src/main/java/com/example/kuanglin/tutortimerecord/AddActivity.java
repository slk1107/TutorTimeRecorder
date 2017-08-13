package com.example.kuanglin.tutortimerecord;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.graphics.CanvasView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.example.kuanglin.tutortimerecord.R.id.add_date_input;
import static com.example.kuanglin.tutortimerecord.R.id.add_end_input;
import static com.example.kuanglin.tutortimerecord.R.id.add_redo;
import static com.example.kuanglin.tutortimerecord.R.id.add_start_input;

/**
 * Created by KuangLin on 2017/4/30.
 */

public class AddActivity extends AppCompatActivity implements View.OnClickListener {

    String startTime = "";
    String endTime = "";
    String date = "";
    TextView mDateTextView, mStartTimeTextView, mEndTimeTextView;
    DBHelper dbHelper;
    private CanvasView canvas;
//    public final static int BITMAP_WIDTH = 800;
//    public final static int BITMAP_HEIGHT = 360;
    public final static int BITMAP_WIDTH = 300;
    public final static int BITMAP_HEIGHT = 135;


    /**
     * -----------------------
     * Life Cycle
     * -----------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.add_activity);

        dbHelper = new DBHelper(this, "record.db", null, DBHelper.DB_VERSION);

        initView();
        if(getSupportActionBar()!=null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }else {
            Log.d("QQ","WTF");
        }
    }

    /**
     * -----------------------
     * View
     * -----------------------
     */

    private void initView() {
        setupDate();
        setupTimes();
        setupCanvas();
    }

    private void setupDate() {
        mDateTextView = (TextView) findViewById(add_date_input);
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        date = String.format("%s/%s/%s", year, month + 1, day);
        mDateTextView.setText(date);
        mDateTextView.setOnClickListener(this);
    }

    private void setupTimes() {
        mStartTimeTextView = (TextView) findViewById(add_start_input);
        mEndTimeTextView = (TextView) findViewById(add_end_input);

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        startTime = formatTime(hour, minute);
        endTime = formatTime(hour + 2, minute);

        mStartTimeTextView.setText(startTime);
        mEndTimeTextView.setText(endTime);

        mStartTimeTextView.setOnClickListener(this);
        mEndTimeTextView.setOnClickListener(this);
    }

    private void setupCanvas() {
        canvas = (CanvasView) this.findViewById(R.id.canvas);
        canvas.setPaintStrokeWidth(12F);
        this.canvas.setBaseColor(Color.parseColor("#fffafafa"));
        ImageButton redoButton = (ImageButton) findViewById(add_redo);
        redoButton.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_activty_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                ITEM result = getFrom();
                insertItem(result);
                finish();
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case add_date_input:
                showDatePicker();
                break;
            case add_end_input:
            case add_start_input:
                showTimePicker((TextView) view);
                break;
            case add_redo:
                canvas.undo();
                break;
        }

    }

    /**
     * -----------------------
     * Click Events
     * -----------------------
     */

    private void showDatePicker() {

        String[] dates = date.split("/");
        int currentYear = Integer.valueOf(dates[0]);
        int currentMonth = Integer.valueOf(dates[1]) - 1;
        int currentDay = Integer.valueOf(dates[2]);

        new DatePickerDialog(AddActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear,
                                  int dayOfMonth) {
                date = String.format("%s/%s/%s", year, monthOfYear + 1, dayOfMonth);
                mDateTextView.setText(date);
            }
        }, currentYear, currentMonth,
                currentDay).show();
    }

    private void showTimePicker(final TextView mTextView) {

        String[] times = mTextView.getText().toString().split("：");
        int hour = Integer.valueOf(times[0]);
        int minute = Integer.valueOf(times[1]);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddActivity.this, TimePickerDialog.THEME_DEVICE_DEFAULT_LIGHT, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                if (mTextView == mStartTimeTextView) {
                    startTime = formatTime(i, i1);
                    mTextView.setText(startTime);
                } else if (mTextView == mEndTimeTextView) {
                    endTime = formatTime(i, i1);
                    mTextView.setText(endTime);
                }
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private ITEM getFrom() {
        Calendar c = Calendar.getInstance();
        Locale.getDefault();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss",Locale.getDefault());
        String currentDateAndTime = sdf.format(c.getTime());
        String name = date.replace("/", "") + "T" + startTime.split("：")[0] + startTime.split("：")[1];
        String signaturePath = saveToInternalStorage(canvas.getScaleBitmap(BITMAP_WIDTH,BITMAP_HEIGHT), currentDateAndTime);
        Log.d("QQ",signaturePath);

        ITEM item = new ITEM();
        item.startTime = mStartTimeTextView.getText().toString();
        item.endTime = mEndTimeTextView.getText().toString();
        item.date = mDateTextView.getText().toString();
        item.signaturePath = signaturePath;
        return item;
    }

    /**
     * -----------------------
     * Util Function
     * -----------------------
     */

    private String formatTime(int hour, int minutes) {

        int hourLength = String.valueOf(hour).length();
        int minuteLength = String.valueOf(minutes).length();
        String hourString = hour > 23 ? String.valueOf(hour - 24) : String.valueOf(hour);
        hourString = hourLength < 2 ? "0" + hour : hourString;
        String minuteString = minuteLength < 2 ? "0" + minutes : String.valueOf(minutes);

        return String.format("%s：%s", hourString, minuteString);
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String name) {
        String path = Environment.getExternalStorageDirectory().toString() + "/Tutor signaturePath";
        File directory = new File(path);
        directory.mkdir();
        // Create imageDir
        File mypath = new File(path, name + ".jpg");
        Log.d("QQ", mypath.toString());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("QQ", mypath.getAbsolutePath());
        return mypath.getAbsolutePath();
    }

    private boolean insertItem(ITEM item){
        if(item == null){
            return false;
        }

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_DATE, item.date);
        values.put(DBHelper.COLUMN_START_TIME, item.startTime);
        values.put(DBHelper.COLUMN_END_TIME, item.endTime);
        values.put(DBHelper.COLUMN_MONTH, item.date.split("/")[1]);
        values.put(DBHelper.COLUMN_SIGNATURE_PATH, item.signaturePath);

        try {
            db.insertOrThrow(DBHelper.TABLE_NAME, null, values);
            return true;
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Log.d("QQ","insert Fail");
            return false;
        }
    }
}
