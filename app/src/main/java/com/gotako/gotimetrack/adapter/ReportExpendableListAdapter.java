/**
 *
 */
package com.gotako.gotimetrack.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gotako.gotimetrack.AddDateTimeDialog;
import com.gotako.gotimetrack.GoCache;
import com.gotako.gotimetrack.R;
import com.gotako.gotimetrack.Utils;
import com.gotako.gotimetrack.database.TimeTrackDAO;
import com.gotako.gotimetrack.fragment.IFragment;
import com.gotako.gotimetrack.model.DateTimeModel;
import com.gotako.gotimetrack.model.ReportHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lnguyen66
 */
public class ReportExpendableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<ReportHeader> _listDataHeader; // header titles
    // child data in format of header title, child title
    private Map<String, List<DateTimeModel>> _listDataChild;
    private IFragment fragment;

    public ReportExpendableListAdapter(Context context,
                                       List<ReportHeader> listDataHeader,
                                       Map<String, List<DateTimeModel>> listChildData, IFragment fragment) {
        this.context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.fragment = fragment;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(
                this._listDataHeader.get(groupPosition).getGroupBy()).get(
                childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final DateTimeModel model = (DateTimeModel) getChild(groupPosition,
                childPosition);

        //if (convertView == null) {
        LayoutInflater infalInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.recent_item_layout,
                null);
        //}

        if (model != null) {
            LinearLayout layout = (LinearLayout) convertView
                    .findViewById(R.id.linearLayoutReportItem);
            ImageView imageViewStatus = (ImageView) convertView
                    .findViewById(R.id.imageViewStatus);
            TextView textViewTime = (TextView) convertView
                    .findViewById(R.id.textViewTime);
            TextView textViewDate = (TextView) convertView
                    .findViewById(R.id.textViewDate);

            textViewTime.setText(Utils.formatTime(model.getTime()));
            textViewDate.setText(Utils.formatDate(model.getTime()));

            if (Utils.IN_STATUS.equals(model.getStatus())) {
                imageViewStatus.setImageResource(R.drawable.in);

                if (childPosition != 0) {
                    DateTimeModel previousModel = (DateTimeModel) getChild(
                            groupPosition, childPosition - 1);
                    if (previousModel != null) {
                        if (Utils.IN_STATUS.equals(previousModel.getStatus())) {
                            layout.setBackgroundColor(context.getResources()
                                    .getColor(R.color.alarm_background));
                        }
                    }
                }
            } else {
                imageViewStatus.setImageResource(R.drawable.out);
                if (childPosition != 0) {
                    DateTimeModel previousModel = (DateTimeModel) getChild(
                            groupPosition, childPosition - 1);
                    if (previousModel != null) {
                        if (Utils.OUT_STATUS.equals(previousModel.getStatus())) {
                            layout.setBackgroundColor(context.getResources()
                                    .getColor(R.color.alarm_background));
                        }
                    }
                }
            }

            ImageView edit = (ImageView) convertView
                    .findViewById(R.id.imageViewEdit);
            ImageView delete = (ImageView) convertView
                    .findViewById(R.id.imageViewDelete);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = ((FragmentActivity) context)
                            .getSupportFragmentManager();
                    AddDateTimeDialog dialog = new AddDateTimeDialog();
                    dialog.setContext(context);
                    dialog.setModel(model);
                    dialog.setFragment(fragment);
                    dialog.show(fm, "fragment_edit_name");
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    // Yes button clicked
                                    TimeTrackDAO dao = new TimeTrackDAO(GoCache
                                            .getInstance().getDatabaseHelper());
                                    dao.deleteById(model.getId());
                                    GoCache.getInstance().setCurrentDateTimeModel(
                                            null);
                                    fragment.refresh();
                                    Toast.makeText(context, "Delete successfully!",
                                            Toast.LENGTH_LONG).show();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    // No button clicked
                                    // do nothing
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage("Are you sure to delete?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .show();
                }
            });
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(
                this._listDataHeader.get(groupPosition).getGroupBy()).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ReportHeader header = (ReportHeader) getGroup(groupPosition);
        List<DateTimeModel> modelList = _listDataChild.get(this._listDataHeader
                .get(groupPosition).getGroupBy());
        String headerTitle = header.getGroupBy();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.report_header_layout,
                    null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.textViewReportHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        TextView summaryTextView = (TextView) convertView
                .findViewById(R.id.textViewSum);

        List<DateTimeModel> list = new ArrayList<DateTimeModel>(
                modelList.size());
        for (int i = modelList.size() - 1; i >= 0; i--) {
            list.add(modelList.get(i));
        }

        long workingTime = Utils.calculateWorkingTimeWithLunchTime(list, false);
        summaryTextView.setText(Utils.formatWorkingTime(workingTime));
        if (workingTime < 1000 * 60 * 60 * 8) {
            summaryTextView.setTextColor(context.getResources().getColor(
                    R.color.expense));
        } else {
            summaryTextView.setTextColor(context.getResources().getColor(
                    R.color.income));
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
