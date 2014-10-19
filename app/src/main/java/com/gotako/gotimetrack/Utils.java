package com.gotako.gotimetrack;

import android.content.Context;

import com.gotako.gotimetrack.model.DateTimeModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {

    public static final String IN_STATUS = "I";
    public static final String OUT_STATUS = "O";

    public static final int KEEP_FOREVER = 0;
    public static final int KEEP_ONE_WEEK = 1;
    public static final int KEEP_ONE_MONTH = 2;
    public static final int KEEP_ONE_YEAR = 3;

    public static final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat timeSimpleFmt = new SimpleDateFormat("HH : mm");
    public static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd");

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static long calculateWorkingTime(List<DateTimeModel> dateTimeList) {
        if (dateTimeList.size() == 0) {
            return 0;
        }

        DateTimeModel currentDateTime = null;
        long workingTime = 0;
        for (int i = 0; i < dateTimeList.size(); i++) {
            DateTimeModel model = dateTimeList.get(i);

            if (currentDateTime == null) {
                if (IN_STATUS.equals(model.getStatus())) {
                    currentDateTime = model;
                } else {
                    // wrong input time, ignore till we get in status
                    continue;
                }
            } else {
                if (IN_STATUS.equals(model.getStatus())) {
                    if (IN_STATUS.equals(currentDateTime.getStatus())) {
                        // wrong input time, current tracking will be ignored
                        currentDateTime = model;
                        continue;
                    } else {
                        // normal case
                        currentDateTime = model;
                        continue;
                    }
                } else { // OUT_STATUS.equals(model.getStatus())
                    if (OUT_STATUS.equals(currentDateTime.getStatus())) {
                        // wrong input time, current tracking will be ignored
                        continue;
                    } else {
                        // normal case, calculate the time
                        long timeInMillis = model.getTime() - currentDateTime.getTime();
                        workingTime += timeInMillis;
                        currentDateTime = model;
                    }
                }
            }
        }
        return workingTime;
    }

    public static long calculateWorkingTimeWithLunchTime(List<DateTimeModel> dateTimeList, boolean forDisplay) {
        if (dateTimeList.size() == 0) {
            return 0;
        }

        DateTimeModel currentDateTime = null;
        DateTimeModel lunchDateTime = null;
        long workingTime = 0;
        List<DateTimeModel> calculateDTList = new ArrayList<DateTimeModel>();

        int lunchTimeStart = GoCache.getInstance().getLunchTimeStart();
        int lunchTimeEnd = GoCache.getInstance().getLunchTimeEnd();

        if (!GoSetting.instance().isLunchTimeEnabled()) {
            lunchTimeStart = lunchTimeEnd = 0;
        }

        for (int i = 0; i < dateTimeList.size(); i++) {
            DateTimeModel model = dateTimeList.get(i);

            int currHour = getHourFromTime(model.getTime());
            int currMin = getMinuteFromTime(model.getTime());

            if (currentDateTime == null) {
                if (IN_STATUS.equals(model.getStatus())) {
                    if (isInLunchTime(currHour, currMin, lunchTimeStart, lunchTimeEnd)) {
                        lunchDateTime = model;
                    } else {
                        calculateDTList.add(model);
                        currentDateTime = model;
                    }
                } else {
                    // wrong input time, ignore till we get in status
                    continue;
                }
            } else {
                if (IN_STATUS.equals(model.getStatus())) {
                    if (IN_STATUS.equals(currentDateTime.getStatus())) {
                        // wrong input time, current tracking will be ignored
                        if (isInLunchTime(currHour, currMin, lunchTimeStart, lunchTimeEnd)) {
                            lunchDateTime = model;
                        } else {
                            calculateDTList.remove(calculateDTList.size() - 1);
                            calculateDTList.add(model);
                            currentDateTime = model;
                            lunchDateTime = null;
                        }
                        continue;
                    } else {
                        // normal case
                        if (isInLunchTime(currHour, currMin, lunchTimeStart, lunchTimeEnd)) {
                            lunchDateTime = model;
                        } else {
                            calculateDTList.add(model);
                            currentDateTime = model;
                            lunchDateTime = null;
                        }
                        continue;
                    }
                } else { // OUT_STATUS.equals(model.getStatus())
                    if (OUT_STATUS.equals(currentDateTime.getStatus())) {
                        // wrong input time, current tracking will be ignored
                        continue;
                    } else {
                        // normal case, calculate the time
                        if (isInLunchTime(currHour, currMin, lunchTimeStart, lunchTimeEnd)) {
                            if (lunchDateTime != null) {
                                // ignore
                            } else {
                                model.setTime(shiftTimeToStartLunchTime(model.getTime(), lunchTimeStart));
                                calculateDTList.add(model);
                            }
                        } else {
                            if (lunchDateTime != null) {
                                DateTimeModel modelTemp = new DateTimeModel(-1, Utils.IN_STATUS, shiftTimeToStartLunchTime(model.getTime(), lunchTimeEnd));
                                calculateDTList.add(modelTemp);

                                calculateDTList.add(model);
                                lunchDateTime = null;
                                currentDateTime = model;
                            } else {
                                if (currHour <= lunchTimeStart) {
                                    // normal
                                    calculateDTList.add(model);
                                } else {
                                    if (getHourFromTime(currentDateTime.getTime()) <= lunchTimeStart) {
                                        DateTimeModel modelTemp = new DateTimeModel(-1, Utils.OUT_STATUS, shiftTimeToStartLunchTime(model.getTime(), lunchTimeStart));
                                        calculateDTList.add(modelTemp);

                                        modelTemp = new DateTimeModel(-1, Utils.IN_STATUS, shiftTimeToStartLunchTime(model.getTime(), lunchTimeEnd));
                                        calculateDTList.add(modelTemp);

                                        calculateDTList.add(model);
                                        lunchDateTime = null;
                                    } else {
                                        calculateDTList.add(model);
                                        lunchDateTime = null;
                                    }
                                }
                                currentDateTime = model;
                            }
                        }
                    }
                }
            }
        }

        if (dateTimeList.size() > 0) {
            DateTimeModel model = dateTimeList.get(dateTimeList.size() - 1);
            if (IN_STATUS.equals(model.getStatus()) && forDisplay) {
                calculateDTList.add(new DateTimeModel(-1, OUT_STATUS, System.currentTimeMillis()));
            }
        }

        long timeTemp = 0;
        for (int i = 0; i < calculateDTList.size(); i++) {
            DateTimeModel model = calculateDTList.get(i);
            if (i % 2 == 0) {
                timeTemp = model.getTime();
            } else {
                workingTime += (model.getTime() - timeTemp);
            }
        }

        return workingTime;
    }

    public static Date toDate(String str) {
        try {
            return new Date(dateFmt.parse(str).getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toTimeSimple(String str) {
        try {
            return new Date(timeSimpleFmt.parse(str).getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static long shiftTimeToStartLunchTime(long millis, int lunchTimeStart) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, lunchTimeStart);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }

    public static boolean isInLunchTime(int currHour, int currMin, int lunchTimeStart, int lunchTimeEnd) {
        if (currHour == lunchTimeStart) {
            return true;
        }
        if (currHour == lunchTimeEnd && currMin == 0) {
            return true;
        }
        return false;
    }

    public static int getHourFromTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinuteFromTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        return cal.get(Calendar.MINUTE);
    }

    public static String formatTime(long time) {
        return timeFmt.format(new Date(time));
    }

    public static String formatTimeSimple(long time) {
        return timeSimpleFmt.format(new Date(time));
    }

    public static String formatDate(long time) {
        return dateFmt.format(new Date(time));
    }

    public static String formatDate(long time, Context context) {
        Calendar cal = Calendar.getInstance();

        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        cal.setTimeInMillis(time);

        if (month == cal.get(Calendar.MONTH) && year == cal.get(Calendar.YEAR)) {
            if (day == cal.get(Calendar.DAY_OF_MONTH)) {
                return context.getString(R.string.today_string);
            }
        }

        return dateFmt.format(new Date(time));
    }

    public static String getWeekFromDate(long millis, Context context) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        StringBuffer result = new StringBuffer();

        int numberOfDayToSunday = Calendar.SUNDAY - cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_MONTH, numberOfDayToSunday);

        long startWeek = cal.getTimeInMillis();

        result.append(formatDate(cal.getTimeInMillis()));

        cal.add(Calendar.DAY_OF_MONTH, 6);
        result.append(" - ").append(formatDate(cal.getTimeInMillis()));

        long endWeek = cal.getTimeInMillis();
        long currentTime = System.currentTimeMillis();

        if (currentTime >= startWeek && currentTime <= endWeek) {
            return context.getString(R.string.this_week_string);
        } else {
            return result.toString();
        }
    }

    public static String getMonthFromDate(long millis, Context context) {
        Calendar cal = Calendar.getInstance();

        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        cal.setTimeInMillis(millis);

        if (month == cal.get(Calendar.MONTH) && year == cal.get(Calendar.YEAR)) {
            return context.getString(R.string.this_month_string);
        } else {
            return (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
        }
    }

    public static String getYearFromDate(long millis, Context context) {
        Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);

        cal.setTimeInMillis(millis);

        if (year == cal.get(Calendar.YEAR)) {
            return context.getString(R.string.this_year_string);
        } else {
            return cal.get(Calendar.YEAR) + "";
        }
    }

    public static String formatWorkingTime(long workingTime) {
        long secs = (long) (workingTime / 1000);
        long mins = (long) ((workingTime / 1000) / 60);
        long hrs = (long) (((workingTime / 1000) / 60) / 60); /*
                                                 * Convert the seconds to String
												 * * and format to ensure it has
												 * * a leading zero when
												 * required
												 */
        secs = secs % 60;
        mins = mins % 60;

        return String.format("%02d : %02d : %02d", hrs, mins, secs);
    }

    public static long[] getStartEndWeekDays() {
        long[] result = new long[2];
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        result[0] = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        result[1] = cal.getTimeInMillis();

        return result;
    }

    public static long[] getStartEndToday() {
        long[] result = new long[2];
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        result[0] = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        result[1] = cal.getTimeInMillis();

        return result;
    }
}
