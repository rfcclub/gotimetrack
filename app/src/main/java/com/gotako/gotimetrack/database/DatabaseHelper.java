package com.gotako.gotimetrack.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TIMETRACK";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQLs

        String timeTrackTableSQL = "CREATE TABLE TIME_TRACK (TIME_TRACK_ID INTEGER PRIMARY KEY, TIME_TRACK_TYPE "
                + " TEXT, TRACK_TIME INTEGER)";

        // create tables
        db.execSQL(timeTrackTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        /*
		 * db.execSQL("DROP TABLE IF EXISTS USER");
		 * db.execSQL("DROP TABLE IF EXISTS FINALCIAL");
		 * db.execSQL("DROP TABLE IF EXISTS TAG");
		 * db.execSQL("DROP TABLE IF EXISTS TAG_SELECTION"); // Create tables
		 * again onCreate(db);
		 */
        // DO NOTHING, JUST KEEP OLD DATABASE
    }
}
