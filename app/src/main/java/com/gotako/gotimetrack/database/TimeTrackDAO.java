package com.gotako.gotimetrack.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gotako.gotimetrack.model.DateTimeModel;

import java.util.ArrayList;
import java.util.List;

public class TimeTrackDAO {

    public static final String TIME_TRACK = "TIME_TRACK";
    public static final String TIME_TRACK_ID_COLUMN = "TIME_TRACK_ID";
    public static final String TIME_TRACK_TYPE_COLUMN = "TIME_TRACK_TYPE";
    public static final String TRACK_TIME_COLUMN = "TRACK_TIME";

    private static String[] COLUMNS = new String[]{"TIME_TRACK_ID", "TIME_TRACK_TYPE", "TRACK_TIME"};

    DatabaseHelper helper = null;

    public TimeTrackDAO(DatabaseHelper helper) {
        this.helper = helper;
    }

    public void insert(DateTimeModel model) {
        int nextId = countAllForKey() + 1;
        model.setId(nextId);

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TIME_TRACK_ID_COLUMN, model.getId());
        values.put(TIME_TRACK_TYPE_COLUMN, model.getStatus());
        values.put(TRACK_TIME_COLUMN, model.getTime());
        // Inserting Income
        db.insert(TIME_TRACK, null, values);

        db.close(); // Closing database connection
    }

    public void update(DateTimeModel model) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TIME_TRACK_TYPE_COLUMN, model.getStatus());
        values.put(TRACK_TIME_COLUMN, model.getTime());
        // Inserting Income
        String whereClause = "TIME_TRACK_ID = ?";
        String[] whereArgs = new String[]{String.valueOf(model.getId())};
        // Update Income
        db.update(TIME_TRACK, values, whereClause, whereArgs);

        db.close(); // Closing database connection
    }

    public void deleteById(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();

        String whereClause = "TIME_TRACK_ID = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        db.delete(TIME_TRACK, whereClause, whereArgs);

        db.close(); // Closing database connection

    }

    public void deleteByCondition(String condition, String... args) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(TIME_TRACK, condition, args);

        db.close(); // Closing database connection

    }

    public DateTimeModel select(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        // Select All Incomes
        String selection = " " + TIME_TRACK_ID_COLUMN + " = ?";
        Cursor cursor = db.query(TIME_TRACK, COLUMNS, selection,
                new String[]{String.valueOf(id)}, null, null, null);
        DateTimeModel model = null;
        if (cursor.moveToFirst()) {
            if (cursor.moveToNext()) {
                model = new DateTimeModel(cursor.getLong(0), cursor.getString(1),
                        cursor.getLong(2));

            }
        }
        db.close();
        return model;
    }

    public List<DateTimeModel> selectByCondition(String condition, String orderBy, String limit) {
        List<DateTimeModel> list = new ArrayList<DateTimeModel>();
        SQLiteDatabase db = helper.getReadableDatabase();
        // Select All Incomes
        String selection = " ";
        if (condition != null && condition.length() > 0) {
            selection += condition;
        }

        Cursor cursor = db.query(TIME_TRACK, COLUMNS, selection, null,
                null, null, orderBy, limit);
        DateTimeModel model = null;
        while (cursor.moveToNext()) {
            model = new DateTimeModel(cursor.getLong(0), cursor.getString(1),
                    cursor.getLong(2));
            list.add(model);
        }

        db.close();
        return list;
    }

    public int countAllForKey() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TIME_TRACK, COLUMNS, null, null,
                null, null, null);
        int count = 0;
        while (cursor.moveToNext()) {
            count += 1;
        }
        return count;
    }
}
