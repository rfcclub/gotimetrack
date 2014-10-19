package com.gotako.gotimetrack.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gotako.gotimetrack.GoCache;
import com.gotako.gotimetrack.GoSetting;
import com.gotako.gotimetrack.MainActivity;
import com.gotako.gotimetrack.R;
import com.gotako.gotimetrack.SelectTimeDialog;
import com.gotako.gotimetrack.Utils;
import com.gotako.gotimetrack.database.TimeTrackDAO;

public class SettingFragment extends Fragment implements IFragment {

    private TextView fromButton;
    private TextView toButton;
    private Switch typeSwitch;
    private LinearLayout selectTimeLayout;
    private Spinner spinnerType;
    private Button buttonPurge;

    private Context context;

    private String fromTime;
    private String toTime;

    private TimeTrackDAO dao = new TimeTrackDAO(GoCache.getInstance()
            .getDatabaseHelper());

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.setting_layout, null);

        fromButton = (TextView) view.findViewById(R.id.textViewFromButton);
        toButton = (TextView) view.findViewById(R.id.textViewToButton);
        typeSwitch = (Switch) view.findViewById(R.id.switchLunchEnable);
        selectTimeLayout = (LinearLayout) view.findViewById(R.id.layoutTimeSelection);
        buttonPurge = (Button) view.findViewById(R.id.buttonPurge);

        spinnerType = (Spinner) view.findViewById(R.id.spinnerDateKeep);
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter
                .createFromResource(context, R.array.time_to_keep_array,
                        R.layout.spinner_item_layout);
        // Specify the layout to use when the list of choices appears
        typeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerType.setAdapter(typeAdapter);

        GoSetting setting = GoSetting.instance();

        if (setting.isLunchTimeEnabled()) {
            typeSwitch.setChecked(true);
            selectTimeLayout.setVisibility(View.VISIBLE);
        } else {
            typeSwitch.setChecked(false);
            selectTimeLayout.setVisibility(View.GONE);
        }

        spinnerType.setSelection(setting.getTimeToKeepSelection());
        if (setting.getTimeToKeepSelection() == Utils.KEEP_FOREVER) {
            buttonPurge.setVisibility(View.GONE);
        } else {
            buttonPurge.setVisibility(View.VISIBLE);
        }

        if (Utils.isNotEmpty(setting.getLunchTimeStart())) {
            fromButton.setText(setting.getLunchTimeStart());
        }
        if (Utils.isNotEmpty(setting.getLunchTimeEnd())) {
            toButton.setText(setting.getLunchTimeEnd());
        }

        final Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        final Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);

        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                selectTimeLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });

        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                selectTimeLayout.setVisibility(View.GONE);
            }
        });

        typeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    slideDown.reset();
                    selectTimeLayout.clearAnimation();
                    selectTimeLayout.startAnimation(slideDown);
                    GoSetting.instance().setLunchTimeEnabled(false);
                } else {
                    slideDown.reset();
                    selectTimeLayout.clearAnimation();
                    selectTimeLayout.startAnimation(slideUp);
                    GoSetting.instance().setLunchTimeEnabled(false);
                }
                GoSetting.instance().save(context);
            }
        });

        fromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFromTimeDialog(v);
            }
        });

        toButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToTimeDialog(v);
            }
        });

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                GoSetting.instance().setTimeToKeepSelection(position).save(context);
                if (position == Utils.KEEP_FOREVER) {
                    buttonPurge.setVisibility(View.GONE);
                } else {
                    buttonPurge.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        buttonPurge.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // Yes button clicked
                                purgeData();
                                Toast.makeText(context, "Purged successfully!",
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

        return view;
    }

    public void showFromTimeDialog(View view) {
        FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
        SelectTimeDialog dialog = new SelectTimeDialog();
        //dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeWithCorners);
        //dialog.setTotalActivity(this);
        dialog.setSelectForFromTime(true);
        dialog.setFragment(this);
        dialog.setCurrentTime(fromButton.getText().toString());
        dialog.show(fm, "fragment_edit_name");
    }

    public void showToTimeDialog(View view) {
        FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
        SelectTimeDialog dialog = new SelectTimeDialog();
        //dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeWithCorners);
        //dialog.setTotalActivity(this);
        dialog.setSelectForFromTime(false);
        dialog.setFragment(this);
        dialog.setCurrentTime(toButton.getText().toString());
        dialog.show(fm, "fragment_edit_name");
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
    public void callback(Object obj) {
        Object[] objs = (Object[]) obj;
        if (objs[1] == null) {
            if ((Boolean) objs[0]) {
                fromTime = null;
                fromButton.setText("12 : 00");
            } else {
                toTime = null;
                toButton.setText("13 : 00");
            }
        } else {
            if ((Boolean) objs[0]) {
                fromTime = (String) objs[1];
                fromButton.setText(fromTime);
            } else {
                toTime = (String) objs[1];
                toButton.setText(toTime);
            }
        }
        GoSetting.instance().setLunchTimeStart(fromButton.getText().toString())
                .setLunchTimeEnd(toButton.getText().toString())
                .save(context);
    }

    public void purgeData() {
        long millis = System.currentTimeMillis();

        switch (spinnerType.getSelectedItemPosition()) {
            case Utils.KEEP_FOREVER:
                return;
            case Utils.KEEP_ONE_WEEK:
                millis -= 7 * 24 * 60 * 60 * 1000;
                break;
            case Utils.KEEP_ONE_MONTH:
                millis -= 30 * 24 * 60 * 60 * 1000;
                break;
            case Utils.KEEP_ONE_YEAR:
                millis -= 365 * 24 * 60 * 60 * 1000;
                break;
        }

        String condition = TimeTrackDAO.TRACK_TIME_COLUMN + " <= ? ";

        dao.deleteByCondition(condition, String.valueOf(millis));
    }

    @Override
    public void actionMenu(int actionId) {
        // TODO Auto-generated method stub
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
    }

    @Override
    public void fragmentSelected() {
        ((MainActivity) context).showHideActionMenu(2);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
