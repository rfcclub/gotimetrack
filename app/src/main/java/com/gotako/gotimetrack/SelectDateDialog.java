/**
 *
 */
package com.gotako.gotimetrack;

import android.app.Dialog;
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

import com.gotako.gotimetrack.fragment.IFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * @author lnguyen66
 */
public class SelectDateDialog extends DialogFragment {

    IFragment fragment;

    boolean selectForFromDate;

    String currentDate;

    DatePicker datePicker = null;

    private Button buttonCancel;
    private Button buttonClear;
    private Button buttonSelect;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.select_date_layout, container);

        datePicker = (DatePicker) view.findViewById(R.id.datePickerSelection);
        buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        buttonClear = (Button) view.findViewById(R.id.buttonClear);
        buttonSelect = (Button) view.findViewById(R.id.buttonSelect);

        if (currentDate != null) {
            Date date = Utils.toDate(currentDate);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelClick(v);
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearClick(v);
            }
        });

        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectClick(v);
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

    public void clearClick(View view) {
        getDialog().dismiss();
        Object[] objs = new Object[2];
        if (selectForFromDate) {
            objs[0] = true;
        } else {
            objs[0] = false;
        }
        fragment.callback(objs);
    }

    public void selectClick(View view) {
        getDialog().dismiss();
        Object[] objs = new Object[2];
        if (selectForFromDate) {
            objs[0] = true;
        } else {
            objs[0] = false;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, datePicker.getYear());
        cal.set(Calendar.MONTH, datePicker.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
        objs[1] = Utils.formatDate(cal.getTimeInMillis());
        fragment.callback(objs);
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

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public IFragment getFragment() {
        return fragment;
    }

    public void setFragment(IFragment fragment) {
        this.fragment = fragment;
    }

    public boolean isSelectForFromDate() {
        return selectForFromDate;
    }

    public void setSelectForFromDate(boolean selectForFromDate) {
        this.selectForFromDate = selectForFromDate;
    }
}
