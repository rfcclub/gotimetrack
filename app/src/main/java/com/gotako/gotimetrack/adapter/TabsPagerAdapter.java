/**
 *
 */
package com.gotako.gotimetrack.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.gotako.gotimetrack.MainActivity;
import com.gotako.gotimetrack.fragment.ReportFragment;
import com.gotako.gotimetrack.fragment.SettingFragment;
import com.gotako.gotimetrack.fragment.TrackTimeFragment;

/**
 * @author lnguyen66
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    private MainActivity context;
    private TrackTimeFragment trackTimeFragment;
    private ReportFragment reportFragment;
    private SettingFragment settingFragment;

    public TabsPagerAdapter(FragmentManager fm, MainActivity context) {
        super(fm);
        this.context = context;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
     */
    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                if (trackTimeFragment == null) {
                    trackTimeFragment = new TrackTimeFragment();
                }
                trackTimeFragment.setContext(context);
                return trackTimeFragment;
            case 1:
                if (reportFragment == null) {
                    reportFragment = new ReportFragment();
                }
                reportFragment.setContext(context);
                return reportFragment;
            case 2:
                if (settingFragment == null) {
                    settingFragment = new SettingFragment();
                }
                settingFragment.setContext(context);
                return settingFragment;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        return 3;
    }

}
