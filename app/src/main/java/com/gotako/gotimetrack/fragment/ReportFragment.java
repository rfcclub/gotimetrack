package com.gotako.gotimetrack.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gotako.gotimetrack.AddDateTimeDialog;
import com.gotako.gotimetrack.GoCache;
import com.gotako.gotimetrack.MainActivity;
import com.gotako.gotimetrack.R;
import com.gotako.gotimetrack.SelectDateDialog;
import com.gotako.gotimetrack.Utils;
import com.gotako.gotimetrack.adapter.ReportExpendableListAdapter;
import com.gotako.gotimetrack.database.TimeTrackDAO;
import com.gotako.gotimetrack.model.DateTimeModel;
import com.gotako.gotimetrack.model.ReportHeader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.hoang8f.android.segmented.SegmentedGroup;

public class ReportFragment extends Fragment implements IFragment {
    List<DateTimeModel> models = null;
    private ExpandableListView listMoneyView = null;
    private TextView fromButton;
    private TextView toButton;
    private Context context;
    private String fromDate;
    private String toDate;
    private SegmentedGroup radioGroupBy;
    private Map<String, List<DateTimeModel>> modelGroup = new HashMap<String, List<DateTimeModel>>();
    private List<ReportHeader> modelHeaderList = new ArrayList<ReportHeader>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.report_fragment, null);

        fromButton = (TextView) view.findViewById(R.id.textViewFromButton);
        toButton = (TextView) view.findViewById(R.id.textViewToButton);

        fromButton.setText(getString(R.string.beginning_string));
        toButton.setText(getString(R.string.today_string));

        listMoneyView = (ExpandableListView) view.findViewById(R.id.listTimeTrack);

        radioGroupBy = (SegmentedGroup) view.findViewById(R.id.segmentedGroupBy);

        radioGroupBy.setSelected(true);

        radioGroupBy.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (models == null) {
                    models = loadDateTimeModel();
                }
                parseDateTimeModelList(models);

                ReportExpendableListAdapter adapter = new ReportExpendableListAdapter(context, modelHeaderList, modelGroup, ReportFragment.this);

                listMoneyView.setAdapter(adapter);

                for (int i = 0; i < adapter.getGroupCount(); i++) {
                    listMoneyView.expandGroup(i);
                }
            }
        });

        listMoneyView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        listMoneyView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });

        fromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFromDateDialog(v);
            }
        });

        toButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToDateDialog(v);
            }
        });

        return view;
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


    @Override
    public void onResume() {
        super.onResume();
    }

    public void report() {
        parseDateTimeModelList(loadDateTimeModel());

        ReportExpendableListAdapter adapter = new ReportExpendableListAdapter(context, modelHeaderList, modelGroup, this);

        listMoneyView.setAdapter(adapter);

        for (int i = 0; i < adapter.getGroupCount(); i++) {
            listMoneyView.expandGroup(i);
        }
    }

    private List<DateTimeModel> loadDateTimeModel() {
        TimeTrackDAO dao = new TimeTrackDAO(GoCache.getInstance()
                .getDatabaseHelper());

        StringBuffer condition = new StringBuffer();

//		if (fromDate != null) {
//			condition.append(FINANCIAL_DATETIME).append(" >= '").append(Utility.buildDateStrForSqlite(fromDate)).append("'");
//		}
//		
//		if (toDate != null) {
//			if (condition.length() > 0) {
//				condition.append(" AND ");
//			}
//			condition.append(FINANCIAL_DATETIME).append(" <= '").append(Utility.buildDateStrForSqlite(toDate)).append("'");
//		}

        if (condition.length() == 0) {
            condition.append(" 1 = 1 ");
        }

        List<DateTimeModel> models = dao.selectByCondition(condition.toString(), TimeTrackDAO.TRACK_TIME_COLUMN + " DESC ", null);

        this.models = models;
        return models;
    }

    private void parseDateTimeModelList(List<DateTimeModel> modelList) {
        modelGroup.clear();
        modelHeaderList.clear();
        //int groupByPos = groupBySpinner.getSelectedItemPosition();
        int groupByPos = radioGroupBy.getCheckedRadioButtonId();

        for (DateTimeModel model : modelList) {
            String key = "";
            switch (groupByPos) {
                case /*Utility.GROUP_BY_WEEK*/ R.id.buttonWeek:
                    key = /*getString(R.string.week_string) + " " +*/ Utils.getWeekFromDate(model.getTime(), context);
                    break;
                case /*Utility.GROUP_BY_MONTH*/ R.id.buttonMonth:
                    key = /*getString(R.string.month_string) + " " + */Utils.getMonthFromDate(model.getTime(), context);
                    break;
                case /*Utility.GROUP_BY_YEAR*/ R.id.buttonYear:
                    key = /*getString(R.string.year_string) + " " + */Utils.getYearFromDate(model.getTime(), context);
                    break;
                default:
                    key = Utils.formatDate(model.getTime(), context);
                    break;
            }

            List<DateTimeModel> moneys = modelGroup.get(key);
            if (moneys == null) {
                moneys = new ArrayList<DateTimeModel>();
                modelGroup.put(key, moneys);
            }
            moneys.add(model);

            boolean found = false;
            for (ReportHeader header : modelHeaderList) {
                if (header.getGroupBy().equals(key)) {
                    double sum = header.getSummary();
                    //double amount = model.getAmount();
                    double amount = 0;
                    sum += amount;
                    header.setSummary(sum);
                    if (amount >= 0) {
                        header.setIncome(header.getIncome() + amount);
                    } else {
                        header.setOutcome(header.getOutcome() + amount);
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                ReportHeader header = new ReportHeader();
                header.setGroupBy(key);
                double amount = 0;// model.getAmount();
                header.setSummary(amount);

                if (amount >= 0) {
                    header.setIncome(amount);
                } else {
                    header.setOutcome(amount);
                }

                modelHeaderList.add(header);
            }
        }
    }

    public void showFromDateDialog(View view) {
        FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
        SelectDateDialog dialog = new SelectDateDialog();
        //dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeWithCorners);
        //dialog.setTotalActivity(this);
        dialog.setSelectForFromDate(true);
        dialog.setFragment(this);
        dialog.setCurrentDate(fromDate);
        dialog.show(fm, "fragment_edit_name");
    }

    public void showToDateDialog(View view) {
        FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
        SelectDateDialog dialog = new SelectDateDialog();
        //dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeWithCorners);
        //dialog.setTotalActivity(this);
        dialog.setSelectForFromDate(false);
        dialog.setFragment(this);
        dialog.setCurrentDate(toDate);
        dialog.show(fm, "fragment_edit_name");
    }

    @Override
    public void callback(Object obj) {
        Object[] objs = (Object[]) obj;
        if (objs[1] == null) {
            if ((Boolean) objs[0]) {
                fromDate = null;
                fromButton.setText(getString(R.string.beginning_string));
            } else {
                toDate = null;
                toButton.setText(getString(R.string.today_string));
            }
        } else {
            if ((Boolean) objs[0]) {
                fromDate = (String) objs[1];
                fromButton.setText(fromDate);
            } else {
                toDate = (String) objs[1];

                Calendar cal = Calendar.getInstance();
                cal.setTime(Utils.toDate(toDate));
                Date today = new Date();
                if (today.getDay() == cal.get(Calendar.DAY_OF_MONTH)
                        && today.getMonth() == cal.get(Calendar.MONTH) + 1
                        && today.getYear() == cal.get(Calendar.YEAR)) {
                    toButton.setText(getString(R.string.today_string));
                } else {
                    toButton.setText(toDate);
                }
            }
        }

        report();
    }

    @Override
    public void actionMenu(int actionId) {
        switch (actionId) {
            case R.id.action_refresh:
                report();
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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void refresh() {
        report();
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
    public void fragmentSelected() {
        ((MainActivity) context).showHideActionMenu(1);
    }
}
