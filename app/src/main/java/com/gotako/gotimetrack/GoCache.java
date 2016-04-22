package com.gotako.gotimetrack;

import com.gotako.gotimetrack.database.DatabaseHelper;
import com.gotako.gotimetrack.model.DateTimeModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoCache {
    private static GoCache cache = null;

    private Date curentDate = null;
    private DatabaseHelper databaseHelper;
    private DateTimeModel currentDateTimeModel;

    private int lunchTimeStart = 12;
    private int lunchTimeEnd = 13;
    private List<DateTimeModel> dateTimeList = new ArrayList<DateTimeModel>();

    private GoCache() {

    }

    public static GoCache getInstance() {
        if (cache == null) {
            cache=new GoCache();
        }

        return cache;
    }

    public Date getCurentDate() {
        return curentDate;
    }

    public void setCurentDate(Date curentDate) {
        this.curentDate = curentDate;
    }

    public List<DateTimeModel> getDateTimeList() {
        return dateTimeList;
    }

    public void setDateTimeList(List<DateTimeModel> dateTimeList) {
        this.dateTimeList = dateTimeList;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public DateTimeModel getCurrentDateTimeModel() {
        return currentDateTimeModel;
    }

    public void setCurrentDateTimeModel(DateTimeModel currentDateTimeModel) {
        this.currentDateTimeModel = currentDateTimeModel;
    }

    public int getLunchTimeStart() {
        return lunchTimeStart;
    }

    public void setLunchTimeStart(int lunchTimeStart) {
        this.lunchTimeStart = lunchTimeStart;
    }

    public int getLunchTimeEnd() {
        return lunchTimeEnd;
    }

    public void setLunchTimeEnd(int lunchTimeEnd) {
        this.lunchTimeEnd = lunchTimeEnd;
    }
}
