package com.gotako.gotimetrack;

import android.content.Context;
import android.content.SharedPreferences;


public class GoSetting {
    private static GoSetting goSetting;

    private boolean lunchTimeEnabled = false;
    private String lunchTimeStart;
    private String lunchTimeEnd;
    private int timeToKeepSelection;

    /**
     * Default constructor
     */
    private GoSetting() {
        // do nothing
    }

    public static GoSetting instance() {
        if (goSetting == null) {
            goSetting = new GoSetting();
        }
        return goSetting;
    }

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("GOTAKO_TIME_TRACK_SETTING", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("LunchTimeEnabled", lunchTimeEnabled);
        prefs.edit().putString("LunchTimeStart", lunchTimeStart);
        prefs.edit().putString("LunchTimeEnd", lunchTimeEnd);
        prefs.edit().putInt("TimeToKeepSelection", timeToKeepSelection);
        prefs.edit().commit();
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("VOZCONFIG", Context.MODE_PRIVATE);
        lunchTimeEnabled = prefs.getBoolean("LunchTimeEnabled", false);
        lunchTimeStart = prefs.getString("LunchTimeStart", null);
        lunchTimeEnd = prefs.getString("LunchTimeEnd", null);
        timeToKeepSelection = prefs.getInt("TimeToKeepSelection", 0);
    }

    public boolean isLunchTimeEnabled() {
        return lunchTimeEnabled;
    }

    public GoSetting setLunchTimeEnabled(boolean lunchTimeEnabled) {
        this.lunchTimeEnabled = lunchTimeEnabled;
        return this;
    }

    public String getLunchTimeStart() {
        return lunchTimeStart;
    }

    public GoSetting setLunchTimeStart(String lunchTimeStart) {
        this.lunchTimeStart = lunchTimeStart;
        return this;
    }

    public String getLunchTimeEnd() {
        return lunchTimeEnd;
    }

    public GoSetting setLunchTimeEnd(String lunchTimeEnd) {
        this.lunchTimeEnd = lunchTimeEnd;
        return this;
    }

    public int getTimeToKeepSelection() {
        return timeToKeepSelection;
    }

    public GoSetting setTimeToKeepSelection(int timeToKeepSelection) {
        this.timeToKeepSelection = timeToKeepSelection;
        return this;
    }
}
