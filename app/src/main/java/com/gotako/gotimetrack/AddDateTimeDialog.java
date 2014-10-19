package com.gotako.gotimetrack;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gotako.gotimetrack.database.TimeTrackDAO;
import com.gotako.gotimetrack.fragment.IFragment;
import com.gotako.gotimetrack.model.DateTimeModel;

import java.util.Calendar;

public class AddDateTimeDialog extends DialogFragment {

    private Context context;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button buttonCancel;
    private Button buttonSave;
    private Switch typeSwitch;
    private DateTimeModel model;
    private TimeTrackDAO dao;
    private IFragment fragment;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dao = new TimeTrackDAO(GoCache.getInstance().getDatabaseHelper());
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.select_date_time_layout, container);

        datePicker = (DatePicker) view.findViewById(R.id.datePickerSelection);
        timePicker = (TimePicker) view.findViewById(R.id.timePickerSelection);
        buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        buttonSave = (Button) view.findViewById(R.id.buttonSave);
        typeSwitch = (Switch) view.findViewById(R.id.switchType);

        timePicker.setIs24HourView(true);

        if (model != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(model.getTime());
            datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));

            if (Utils.IN_STATUS.equals(model.getStatus())) {
                typeSwitch.setChecked(true);
            } else {
                typeSwitch.setChecked(false);
            }
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelClick(v);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, datePicker.getYear());
                cal.set(Calendar.MONTH, datePicker.getMonth());
                cal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                cal.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());

                long millis = cal.getTimeInMillis();

                if (model == null) {
                    model = new DateTimeModel(-1, typeSwitch.isChecked() ? Utils.IN_STATUS : Utils.OUT_STATUS, millis);
                    dao.insert(model);
                } else {
                    model.setStatus(typeSwitch.isChecked() ? Utils.IN_STATUS : Utils.OUT_STATUS);
                    model.setTime(millis);
                    dao.update(model);
                }
                fragment.refresh();
                Toast.makeText(context, R.string.save_success_string, Toast.LENGTH_LONG).show();

                getDialog().dismiss();
            }
        });

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;

        getDialog().getWindow().setLayout((int) (width * 0.85), 440);

        return view;
    }

    public void cancelClick(View view) {
        getDialog().dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getDialog().getWindow().getAttributes());

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;

        lp.width = (int) (width * 0.85);
        getDialog().getWindow().setAttributes(lp);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public DateTimeModel getModel() {
        return model;
    }

    public void setModel(DateTimeModel model) {
        this.model = model;
    }

    public IFragment getFragment() {
        return fragment;
    }

    public void setFragment(IFragment fragment) {
        this.fragment = fragment;
    }
}
