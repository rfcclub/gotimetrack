package com.gotako.gotimetrack;

import android.os.Handler;

import com.gotako.gotimetrack.database.TimeTrackDAO;
import com.gotako.gotimetrack.model.DateTimeModel;

import java.util.List;

public class TimeTrackCenter {
    private static final int REFRESH_RATE = 1000;
    private static TimeTrackCenter ttCenter = null;
    protected long elapsedTime;
    private String weekTimeString = "00:00:00";
    private String workingTimeString = "00:00:00";
    private String goOutTimeString = "00:00:00";
    private TimeTrackDAO dao;
    private String currentStatus;
    private long workingTime;
    private long weekWorkingTime;
    private long startTime;
    private long startFreeTime;
    private long weekStartTime;
    private Handler mHandler, mFreeHandler;

    private TimeTrackCenter() {
        dao = new TimeTrackDAO(GoCache.getInstance().getDatabaseHelper());
        mHandler = new Handler();
        mFreeHandler = new Handler();
    }

    public static TimeTrackCenter instance() {
        if (ttCenter == null) {
            ttCenter = new TimeTrackCenter();
        }

        return ttCenter;
    }

    public static String weekTime() {
        return instance().weekTimeString;
    }

    public static String workingTime() {
        // TODO Auto-generated method stub
        return instance().workingTimeString;
    }

    public static String goOutTime() {
        // TODO Auto-generated method stub
        return instance().goOutTimeString;
    }

    public static String currentStatus() {
        return instance().currentStatus;
    }

    private void updateFreeTimer(long time) {
        long secs = (long) (time / 1000) % 60;
        long mins = (long) ((time / 1000) / 60) % 60;
        long hrs = (long) (((time / 1000) / 60) / 60);
        goOutTimeString = String.format("%02d:%02d:%02d", hrs, mins, secs);
    }

    private void updateTimer(long time) {
        long secs = (long) (time / 1000) % 60;
        long mins = (long) ((time / 1000) / 60) % 60;
        long hrs = (long) (((time / 1000) / 60) / 60);

        workingTimeString = String.format("%02d:%02d:%02d", hrs, mins, secs);
    }

    private void updateWeekTimer(long time) {
        long weekSecs = (long) (time / 1000);
        long weekMins = (long) ((time / 1000) / 60);
        long weekHrs = (long) (((time / 1000) / 60) / 60);
        weekSecs = weekSecs % 60;
        weekMins = weekMins % 60;

        weekTimeString = String.format("%02d:%02d:%02d", weekHrs, weekMins, weekSecs);
    }

    public void update(String status) {
        DateTimeModel model = new DateTimeModel(-1, status,
                System.currentTimeMillis());
        GoCache.getInstance().getDateTimeList().add(model);

        dao.insert(model);

        currentStatus = status;
        workingTime = Utils.calculateWorkingTimeWithLunchTime(
                getTodayTimeList(" ASC"), true);
        weekWorkingTime = Utils.calculateWorkingTimeWithLunchTime(
                getWeekTimeList(), true);
        updateTime();
    }

    public void updateTime() {
        if (Utils.IN_STATUS.equals(currentStatus)) {
            // imageViewCurrentStatus.setImageResource(R.drawable.in);
            startTime = System.currentTimeMillis();
            startFreeTime = 0;
            weekStartTime = startTime;
            if (!Utils.isInLunchTime(Utils.getHourFromTime(startTime), Utils
                    .getMinuteFromTime(startTime), GoCache.getInstance()
                    .getLunchTimeStart(), GoCache.getInstance()
                    .getLunchTimeEnd())) {
                runStartTimer();
            }
        } else {
            // imageViewCurrentStatus.setImageResource(R.drawable.out);
            startTime = 0;
            startFreeTime = System.currentTimeMillis();
            weekStartTime = 0;
            runStartFreeTimer();
        }
    }

    private List<DateTimeModel> getTodayTimeList(String orderDirection) {

        long[] weeks = Utils.getStartEndToday();
        StringBuffer buff = new StringBuffer();
        buff.append(TimeTrackDAO.TRACK_TIME_COLUMN).append(" >= ")
                .append(weeks[0]).append(" AND ")
                .append(TimeTrackDAO.TRACK_TIME_COLUMN).append(" <= ")
                .append(weeks[1]);

        List<DateTimeModel> resultList = dao.selectByCondition(buff.toString(),
                TimeTrackDAO.TRACK_TIME_COLUMN + orderDirection, null);
        return resultList;
    }

    private List<DateTimeModel> getWeekTimeList() {

        long[] weeks = Utils.getStartEndWeekDays();
        StringBuffer buff = new StringBuffer();
        buff.append(TimeTrackDAO.TRACK_TIME_COLUMN).append(" >= ")
                .append(weeks[0]).append(" AND ")
                .append(TimeTrackDAO.TRACK_TIME_COLUMN).append(" <= ")
                .append(weeks[1]);

        List<DateTimeModel> resultList = dao.selectByCondition(buff.toString(),
                TimeTrackDAO.TRACK_TIME_COLUMN + " ASC", null);
        return resultList;
    }

    private List<DateTimeModel> getRecentList() {
        return getTodayTimeList(" DESC");
    }

    private void runStartTimer() {
        long elapsedTime;
        if (startTime > 0) {
            elapsedTime = System.currentTimeMillis() - startTime + workingTime;
        } else {
            elapsedTime = workingTime;
        }

        long weekElapsedTime;
        if (weekStartTime > 0) {
            weekElapsedTime = System.currentTimeMillis() - weekStartTime + weekWorkingTime;
        } else {
            weekElapsedTime = weekWorkingTime;
        }

        updateTimer(elapsedTime);
        updateWeekTimer(weekElapsedTime);
    }

    private void runStartFreeTimer() {
        elapsedTime = System.currentTimeMillis() - startFreeTime;
        updateFreeTimer(elapsedTime);
    }
}
