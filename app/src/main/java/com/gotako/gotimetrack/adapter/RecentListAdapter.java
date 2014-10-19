package com.gotako.gotimetrack.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gotako.gotimetrack.GoCache;
import com.gotako.gotimetrack.R;
import com.gotako.gotimetrack.Utils;
import com.gotako.gotimetrack.database.TimeTrackDAO;
import com.gotako.gotimetrack.fragment.IFragment;
import com.gotako.gotimetrack.model.DateTimeModel;

import java.util.List;

public class RecentListAdapter extends ArrayAdapter<DateTimeModel> {

    private Context context;
    private List<DateTimeModel> dataList;
    private IFragment fragment;

    public RecentListAdapter(Context context, int resoure, List<DateTimeModel> objects, IFragment fragment) {
        super(context, resoure, objects);
        this.context = context;
        this.dataList = objects;
        this.fragment = fragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final DateTimeModel model = dataList.get(position);

        //if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.recent_activity_list_item, null);
        //}

        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.linearLayoutRecentItem);
        ImageView imageViewStatus = (ImageView) convertView.findViewById(R.id.imageViewStatus);
        TextView textViewTime = (TextView) convertView.findViewById(R.id.textViewTime);
        TextView textViewDate = (TextView) convertView.findViewById(R.id.textViewDate);

        textViewTime.setText(Utils.formatTime(model.getTime()));
        textViewDate.setText(Utils.formatDate(model.getTime()));

        if (Utils.IN_STATUS.equals(model.getStatus())) {
            imageViewStatus.setImageResource(R.drawable.in);

            if (position != 0) {
                DateTimeModel previousModel = dataList.get(position - 1);
                if (Utils.IN_STATUS.equals(previousModel.getStatus())) {
                    layout.setBackgroundColor(context.getResources().getColor(R.color.alarm_background));
                }
            }
        } else {
            imageViewStatus.setImageResource(R.drawable.out);
            if (position != dataList.size() - 1) {
                DateTimeModel previousModel = dataList.get(position + 1);
                if (Utils.OUT_STATUS.equals(previousModel.getStatus())) {
                    layout.setBackgroundColor(context.getResources().getColor(R.color.alarm_background));
                }
            }
        }

        ImageView delete = (ImageView) convertView.findViewById(R.id.imageViewDelete);

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

        return convertView;
    }


}
