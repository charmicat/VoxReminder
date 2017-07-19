package com.vag.voxreminder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Luiza on 7/18/17.
 */

public class DAO extends SQLiteOpenHelper {

    public class ScheduleTable implements BaseColumns {
        public static final String TABLE_NAME = "schedule";
        public static final String COLUMN_NAME_DATE = "date"; //hhmmDDMMYYYY
        public static final String COLUMN_NAME_REPEAT = "repeat"; //0-none,1-daily,2-weekly,3-monthly,4-yearly
        public static final String COLUMN_NAME_TEXT = "text";
    }

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "VoxReminder.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ScheduleTable.TABLE_NAME + " (" +
                    ScheduleTable._ID + " INTEGER PRIMARY KEY," +
                    ScheduleTable.COLUMN_NAME_DATE + " TEXT," +
                    ScheduleTable.COLUMN_NAME_REPEAT + " INTEGER," +
                    ScheduleTable.COLUMN_NAME_TEXT + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ScheduleTable.TABLE_NAME;

    public DAO(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO: choose an upgrade strategy
        //db.execSQL(SQL_DELETE_ENTRIES);
        //onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void clearData(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }

}
