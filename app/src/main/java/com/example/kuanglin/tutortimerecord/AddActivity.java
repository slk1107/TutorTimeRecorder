package com.example.kuanglin.tutortimerecord;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
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
import java.util.Date;
import java.util.Locale;

import static com.example.kuanglin.tutortimerecord.R.id.add_date_input;
import static com.example.kuanglin.tutortimerecord.R.id.add_end_input;
import static com.example.kuanglin.tutortimerecord.R.id.add_redo;
import static com.example.kuanglin.tutortimerecord.R.id.add_start_input;

/**
 * Created by KuangLin on 2017/4/30.
 */

public class AddActivity extends AppCompatActivity implements View.OnClickListener {

    private String startTime = "";
    private String endTime = "";
    private String date = "";
    private TextView mDateTextView, mStartTimeTextView, mEndTimeTextView;
    DBHelper dbHelper;
    private SimpleDateFormat mDateFormat;
    private CanvasView canvas;
    public final static int BITMAP_WIDTH = 300;

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

        initDateFormat();
        initView();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            Log.d("QQ", "WTF");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        ITEM intentItem = (ITEM) (extras.getSerializable(GlobalConst.ITEM));
        if (intentItem == null) {
            return;
        }
        mDateTextView.setText(intentItem.date);
        mStartTimeTextView.setText(intentItem.startTime);
        mEndTimeTextView.setText(intentItem.endTime);
        //The key argument here must match that used in the other activity

    }

    private void initDateFormat() {
        mDateFormat = new SimpleDateFormat(GlobalConst.TimeFormat, getResources().getConfiguration().locale);
    }

    /**
     * -----------------------
     * View
     * -----------------------
     */

    private void initView() {
        initDate();
        initTimes();
        setupCanvas();
    }

    private void initDate() {
        mDateTextView = (TextView) findViewById(add_date_input);
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        date = String.format("%s/%s/%s", year, month + 1, day);
        mDateTextView.setText(date);
        mDateTextView.setOnClickListener(this);
    }

    private void initTimes() {
        mStartTimeTextView = (TextView) findViewById(add_start_input);
        mEndTimeTextView = (TextView) findViewById(add_end_input);

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        startTime = mDateFormat.format(c.getTime());
        endTime = mDateFormat.format(new Date(0, 0, 0, hour + 2, min));


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

    private void showTimePicker(final TextView selectedTextView) {

        try {
            Date time = mDateFormat.parse(selectedTextView.getText().toString());
            int hour = time.getHours();
            int minute = time.getMinutes();
            TimePickerDialog timePickerDialog = new TimePickerDialog(AddActivity.this, TimePickerDialog.THEME_DEVICE_DEFAULT_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    if (selectedTextView == mStartTimeTextView) {
                        startTime = formatTime(i, i1);
                        endTime = formatTime(i+2, i1);
                        selectedTextView.setText(startTime);
                        mEndTimeTextView.setText(endTime);
                    } else if (selectedTextView == mEndTimeTextView) {
                        endTime = formatTime(i, i1);
                        selectedTextView.setText(endTime);
                    }
                }
            }, hour, minute, true);
            timePickerDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ITEM getFrom() {

        ITEM item = new ITEM();
        item.startTime = mStartTimeTextView.getText().toString();
        item.endTime = mEndTimeTextView.getText().toString();
        item.date = mDateTextView.getText().toString();

        Calendar c = Calendar.getInstance();
        Locale.getDefault();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        String currentDateAndTime = sdf.format(c.getTime());
        int bitmapHeight = (int)(BITMAP_WIDTH * ((float)canvas.getHeight() / (float) canvas.getWidth()));
        String signaturePath = saveToInternalStorage(canvas.getScaleBitmap(BITMAP_WIDTH, bitmapHeight), currentDateAndTime);
        Log.d("QQ", signaturePath);

        item.signaturePath = signaturePath;
        return item;
    }

    /**
     * -----------------------
     * Util Function
     * -----------------------
     */

    private String formatTime(int hour, int minutes) {
        return mDateFormat.format(new Date(0, 0, 0, hour, minutes));
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String name) {
        String path = Environment.getExternalStorageDirectory().toString() + "/Tutor signaturePath";
        File directory = new File(path);
        directory.mkdir();
        // Create imageDir
        File myPath = new File(path, name + ".jpg");
        Log.d("QQ", myPath.toString());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
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
        Log.d("QQ", myPath.getAbsolutePath());
        return myPath.getAbsolutePath();
    }

    private boolean insertItem(ITEM item) {
        if (item == null) {
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
            Log.d("QQ", "insert Fail");
            return false;
        }
    }

}
