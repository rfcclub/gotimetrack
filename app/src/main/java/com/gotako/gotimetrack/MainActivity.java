package com.gotako.gotimetrack;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.gotako.gotimetrack.adapter.TabsPagerAdapter;
import com.gotako.gotimetrack.database.DatabaseHelper;
import com.gotako.gotimetrack.fragment.IFragment;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;

    private MenuItem refreshMenuItem = null;
    private MenuItem addMenuItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

        initCache();

        viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(mAdapter);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Tab tab = bar.newTab();
        tab.setText("TRACK");
        tab.setTabListener(this);
        bar.addTab(tab);

        tab = bar.newTab();
        tab.setText("REPORT");
        tab.setTabListener(this);
        bar.addTab(tab);

        tab = bar.newTab();
        tab.setText("SETTING");
        tab.setTabListener(this);
        bar.addTab(tab);

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                bar.setSelectedNavigationItem(position);

                TabsPagerAdapter adapter = (TabsPagerAdapter) viewPager.getAdapter();
                IFragment fragment = (IFragment) adapter.getItem(viewPager.getCurrentItem());
                fragment.fragmentSelected();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        refreshMenuItem = menu.findItem(R.id.action_refresh);
        addMenuItem = menu.findItem(R.id.action_add);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        doAction(item.getItemId());
        return true;
    }

    public void doAction(int actionId) {
        TabsPagerAdapter adapter = (TabsPagerAdapter) viewPager.getAdapter();
        IFragment fragment = (IFragment) adapter.getItem(viewPager.getCurrentItem());
        fragment.actionMenu(actionId);
    }

    private void initCache() {
        GoCache cache = GoCache.getInstance();
        if (cache.getDatabaseHelper() == null) {
            cache.setDatabaseHelper(new DatabaseHelper(this));
        }
        GoSetting.instance().load(this);
    }

    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction arg1) {
        viewPager.setCurrentItem(tab.getPosition(), true);
        TabsPagerAdapter adapter = (TabsPagerAdapter) viewPager.getAdapter();
        IFragment fragment = (IFragment) adapter.getItem(viewPager.getCurrentItem());
        fragment.actionMenu(R.id.action_refresh);
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    public void goToTab(int number) {
        viewPager.setCurrentItem(number, true);
    }

    public void showHideActionMenu(int position) {
        if (position == 2) {
            if (refreshMenuItem != null) {
                refreshMenuItem.setVisible(false);
            }
            if (addMenuItem != null) {
                addMenuItem.setVisible(false);
            }
        } else {
            if (refreshMenuItem != null) {
                refreshMenuItem.setVisible(true);
            }
            if (addMenuItem != null) {
                addMenuItem.setVisible(true);
            }
        }
    }
}
