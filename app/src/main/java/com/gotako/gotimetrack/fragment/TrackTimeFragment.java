package com.gotako.gotimetrack.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gotako.gotimetrack.AddDateTimeDialog;
import com.gotako.gotimetrack.GoCache;
import com.gotako.gotimetrack.MainActivity;
import com.gotako.gotimetrack.R;
import com.gotako.gotimetrack.TimeTrackCenter;
import com.gotako.gotimetrack.Utils;
import com.gotako.gotimetrack.adapter.RecentListAdapter;
import com.gotako.gotimetrack.database.TimeTrackDAO;
import com.gotako.gotimetrack.model.DateTimeModel;

import java.util.Date;
import java.util.List;


public class TrackTimeFragment extends Fragment implements IFragment {

    private final int REFRESH_RATE = 1000;
    MainActivity context;
    TextView workingTimeTextView, textViewWeekWorkingTime, textViewGoOutTime;
    ImageView inBtn;
    ImageView outBtn;
    //ImageView imageViewCurrentStatus;
    private long secs, mins, hrs, msecs, elapsedTime;
    private long weekSecs, weekMins, weekHrs, weekElapsedTime;
    private Handler mHandler = new Handler();
    private Handler mFreeHandler = new Handler();
    private long startTime = 0;
    private long weekStartTime = 0;
    private long workingTime = 0;
    private long weekWorkingTime = 0;
    private Runnable startTimer = new Runnable() {
        public void run() {
            if (startTime > 0) {
                elapsedTime = System.currentTimeMillis() - startTime + workingTime;
            } else {
                elapsedTime = workingTime;
            }

            if (weekStartTime > 0) {
                weekElapsedTime = System.currentTimeMillis() - weekStartTime + weekWorkingTime;
            } else {
                weekElapsedTime = weekWorkingTime;
            }

            updateTimer(elapsedTime);
            updateWeekTimer(weekElapsedTime);
            TimeTrackCenter.instance().updateTime();
            mHandler.postDelayed(this, REFRESH_RATE);
        }
    };
    private long startFreeTime = 0;
    private String currentStatus = "";
    private ListView recentActivityListView;
    private RecentListAdapter adapter = null;
    private TimeTrackDAO dao = new TimeTrackDAO(GoCache.getInstance().getDatabaseHelper());

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, null);

        workingTimeTextView = (TextView) view.findViewById(R.id.textViewWorkingTime);
        textViewWeekWorkingTime = (TextView) view.findViewById(R.id.textViewWeekWorkingTime);
        textViewGoOutTime = (TextView) view.findViewById(R.id.textViewGoOutTime);
        //lastStatusTextView = (TextView) view.findViewById(R.id.textViewLastStatus);
        inBtn = (ImageView) view.findViewById(R.id.imageViewIn);
        outBtn = (ImageView) view.findViewById(R.id.imageViewOut);
        //imageViewCurrentStatus = (ImageView) view.findViewById(R.id.imageViewStatus);
        recentActivityListView = (ListView) view.findViewById(R.id.listViewRecentActivity);

        inBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inOutBtnClick(Utils.IN_STATUS);
            }
        });

        outBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inOutBtnClick(Utils.OUT_STATUS);
            }
        });
        adapter = new RecentListAdapter(context, R.layout.recent_activity_list_item, getRecentList(), this);
        recentActivityListView.setAdapter(adapter);

        weekWorkingTime = Utils.calculateWorkingTimeWithLunchTime(getWeekTimeList(), true);
        workingTime = Utils.calculateWorkingTimeWithLunchTime(getTodayTimeList(" ASC"), true);
        updateWeekTimer(weekWorkingTime);
        updateTimer(workingTime);

        return view;
    }

    private void inOutBtnClick(String status) {
        /*DateTimeModel model = new DateTimeModel(-1, status, System.currentTimeMillis());
		GoCache.getInstance().getDateTimeList().add(model);
		dao.insert(model);*/
        TimeTrackCenter.instance().update(status);
        currentStatus = TimeTrackCenter.currentStatus();
        workingTime = Utils.calculateWorkingTimeWithLunchTime(getTodayTimeList(" ASC"), true);
        weekWorkingTime = Utils.calculateWorkingTimeWithLunchTime(getWeekTimeList(), true);
        if (Utils.IN_STATUS.equals(status)) {
            //imageViewCurrentStatus.setImageResource(R.drawable.in);
            startTime = System.currentTimeMillis();
            weekStartTime = startTime;
            if (!Utils.isInLunchTime(Utils.getHourFromTime(startTime), Utils.getMinuteFromTime(startTime), GoCache.getInstance().getLunchTimeStart(), GoCache.getInstance().getLunchTimeEnd())) {
                mHandler.removeCallbacks(startTimer);
                mHandler.postDelayed(startTimer, 0);
            }
            startFreeTime = 0;
            mFreeHandler.removeCallbacks(startFreeTimer);
        } else {
            //imageViewCurrentStatus.setImageResource(R.drawable.out);
            mHandler.removeCallbacks(startTimer);
            startTime = 0;
            weekStartTime = 0;
            startFreeTime = System.currentTimeMillis();
            mFreeHandler.postDelayed(startFreeTimer, 0);
        }

        //lastStatusTextView.setText("Last Status: " + Utils.formatTime(model.getTime()));

        List<DateTimeModel> recentList = getRecentList();
        adapter = new RecentListAdapter(context, R.layout.recent_activity_list_item, recentList, this);
        recentActivityListView.setAdapter(adapter);
    }

    private void clearData() {
        GoCache.getInstance().getDateTimeList().clear();
        GoCache.getInstance().setCurentDate(new Date());
        currentStatus = "";
        workingTime = 0;
    }    private Runnable startFreeTimer = new Runnable() {

        @Override
        public void run() {
            elapsedTime = System.currentTimeMillis() - startFreeTime;
            updateFreeTimer(elapsedTime);
            TimeTrackCenter.instance().updateTime();
            mFreeHandler.postDelayed(startFreeTimer, REFRESH_RATE);
        }

    };

    private void updateFreeTimer(long time) {
        secs = (long) (time / 1000) % 60;
        mins = (long) ((time / 1000) / 60) % 60;
        hrs = (long) (((time / 1000) / 60) / 60);
        textViewGoOutTime.setText(String.format("%02d:%02d:%02d", hrs, mins, secs));
    }

    private void updateTimer(long time) {
        secs = (long) (time / 1000);
        mins = (long) ((time / 1000) / 60);
        hrs = (long) (((time / 1000) / 60) / 60); /*
												 * Convert the seconds to String
												 * * and format to ensure it has
												 * * a leading zero when
												 * required
												 */
        secs = secs % 60;
        mins = mins % 60;

        workingTimeTextView.setText(String.format("%02d:%02d:%02d", hrs, mins, secs));
    }

    private void updateWeekTimer(long time) {
        weekSecs = (long) (time / 1000);
        weekMins = (long) ((time / 1000) / 60);
        weekHrs = (long) (((time / 1000) / 60) / 60); /*
												 * Convert the seconds to String
												 * * and format to ensure it has
												 * * a leading zero when
												 * required
												 */
        weekSecs = weekSecs % 60;
        weekMins = weekMins % 60;

        textViewWeekWorkingTime.setText(String.format("%02d:%02d:%02d", weekHrs, weekMins, weekSecs));
    }

    private List<DateTimeModel> getTodayTimeList(String orderDirection) {

        long[] weeks = Utils.getStartEndToday();
        StringBuffer buff = new StringBuffer();
        buff.append(TimeTrackDAO.TRACK_TIME_COLUMN).append(" >= ").append(weeks[0])
                .append(" AND ").append(TimeTrackDAO.TRACK_TIME_COLUMN).append(" <= ").append(weeks[1]);

        List<DateTimeModel> resultList = dao.selectByCondition(buff.toString(), TimeTrackDAO.TRACK_TIME_COLUMN + orderDirection, null);
        return resultList;
    }

    private List<DateTimeModel> getWeekTimeList() {

        long[] weeks = Utils.getStartEndWeekDays();
        StringBuffer buff = new StringBuffer();
        buff.append(TimeTrackDAO.TRACK_TIME_COLUMN).append(" >= ").append(weeks[0])
                .append(" AND ").append(TimeTrackDAO.TRACK_TIME_COLUMN).append(" <= ").append(weeks[1]);

        List<DateTimeModel> resultList = dao.selectByCondition(buff.toString(), TimeTrackDAO.TRACK_TIME_COLUMN + " ASC", null);
        return resultList;
    }

    private List<DateTimeModel> getRecentList() {
        return getTodayTimeList(" DESC");
    }

    public MainActivity getContext() {
        return context;
    }

    public void setContext(MainActivity context) {
        this.context = context;
    }

    @Override
    public void callback(Object obj) {
        // TODO Auto-generated method stub

    }

    @Override
    public void actionMenu(int actionId) {
        switch (actionId) {
            case R.id.action_refresh:
                refresh();
                break;
            case R.id.action_add:
                add();
                break;
//        case R.id.action_graph:
//            handleBarGraph();
//            break;
            default:
                break;
        }
    }

    private void add() {
        FragmentManager fm = ((FragmentActivity) context)
                .getSupportFragmentManager();
        AddDateTimeDialog dialog = new AddDateTimeDialog();
        dialog.setContext(context);
        dialog.setFragment(this);
        dialog.show(fm, "fragment_edit_name");
    }

    @Override
    public void refresh() {
        if (recentActivityListView == null) {
            return;
        }
        List<DateTimeModel> recentList = getRecentList();
        adapter = new RecentListAdapter(context, R.layout.recent_activity_list_item, recentList, this);
        recentActivityListView.setAdapter(adapter);

        GoCache.getInstance().setDateTimeList(recentList);
        if (recentList.size() > 0) {
            DateTimeModel model = recentList.get(0);
            currentStatus = model.getStatus();
            workingTime = Utils.calculateWorkingTimeWithLunchTime(getTodayTimeList(" ASC "), true);
            weekWorkingTime = Utils.calculateWorkingTimeWithLunchTime(getWeekTimeList(), true);
            if (Utils.IN_STATUS.equals(currentStatus)) {
                //imageViewCurrentStatus.setImageResource(R.drawable.in);
                startTime = System.currentTimeMillis();
                weekStartTime = startTime;
                mHandler.removeCallbacks(startTimer);
                mHandler.postDelayed(startTimer, 0);
            } else {
                //imageViewCurrentStatus.setImageResource(R.drawable.out);
                mHandler.removeCallbacks(startTimer);
                startTime = 0;
            }
            //lastStatusTextView.setText("Last Status: " + Utils.formatTime(model.getTime()));
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        refresh();
    }

    @Override
    public void fragmentSelected() {
        ((MainActivity) context).showHideActionMenu(0);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming invisible, then...
            if (isVisibleToUser) {
                fragmentSelected();
            }
        }
    }


}
