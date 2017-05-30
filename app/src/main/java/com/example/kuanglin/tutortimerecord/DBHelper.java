package com.example.kuanglin.tutortimerecord;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by KuangLin on 2017/5/6.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "CLASS_RECORD";
    public static final String COLUMN_MONTH = "MONTH";
    public static final String COLUMN_DATE = "DATE";
    public static final String COLUMN_START_TIME = "START_TIME";
    public static final String COLUMN_END_TIME = "END_TIME";
    public static final String COLUMN_SIGNATURE_PATH = "SIGNATURE_PATH";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        final String sqCommand = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME + " ("
                + "id" + " INTEGER PRIMARY KEY,"
                + COLUMN_MONTH  + " TEXT, "
                + COLUMN_DATE  + " TEXT, "
                + COLUMN_START_TIME  + " TEXT, "
                + COLUMN_END_TIME  + " TEXT, "
                + COLUMN_SIGNATURE_PATH + " TEXT"
                + ")";

        sqLiteDatabase.execSQL(sqCommand);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
